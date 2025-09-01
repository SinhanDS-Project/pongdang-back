package com.wepong.pongdang.socket;

import com.wepong.pongdang.dto.response.GameRoomResponseDTO;
import com.wepong.pongdang.dto.response.TurtlePlayerDTO;
import com.wepong.pongdang.entity.GameEntity;
import com.wepong.pongdang.entity.GameHistoryEntity;
import com.wepong.pongdang.entity.PongHistoryEntity;
import com.wepong.pongdang.entity.UserEntity;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.entity.enums.RankType;
import com.wepong.pongdang.entity.enums.GameRoomStatus;
import com.wepong.pongdang.entity.enums.Level;
import com.wepong.pongdang.model.multi.turtle.PlayerDAO;
import com.wepong.pongdang.model.multi.turtle.SessionService;
import com.wepong.pongdang.model.multi.turtle.TurtleGameService;
import com.wepong.pongdang.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

//웹소켓 메시지 처리
@Component
public class TurtleRunWebsocketHandler extends TextWebSocketHandler {

	// 스프링 빈 사용
	@Autowired
	private PlayerDAO playerDAO;
	@Autowired
	private SessionService sessionService;
	@Autowired
	private GameRoomService gameRoomService;
	@Autowired
	private AuthService authService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private GameRoomListWebSocket gameRoomListWebSocket;
	@Autowired
	private GameService gameService;
	@Autowired
	private TurtleGameService turtleGameService;

	private final Map<String, List<Double>> latestPositions = new ConcurrentHashMap<>();
	private final Map<String, ScheduledFuture<?>> broadcastTasks = new ConcurrentHashMap<>();
	private final Map<Long, List<TurtlePlayerDTO>> gameStartPlayersMap = new ConcurrentHashMap<>();
	private final Map<Long, Boolean> gameFinishMap = new ConcurrentHashMap<>();
	@Autowired
	private WalletService walletService;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		Long roomId = (Long) session.getAttributes().get("roomId");
		Long userId = (Long) session.getAttributes().get("userId");

		sessionService.addSession(roomId, session);
	    // (1) 게임 시작 전이면 검증 건너뛰고, 그냥 세션 등록
	    List<TurtlePlayerDTO> startPlayers = gameStartPlayersMap.get(roomId);
	    if (startPlayers == null) {
	        return;
	    }
	    // (2) 게임 시작 후에는 userId가 freeze 목록에 없으면 강제퇴장
	    boolean inGame = false;
	    for (TurtlePlayerDTO player : startPlayers) {
	        if (player.getUserId().equals(userId)) {
	            inGame = true;
	            break;
	        }
	    }
	    if (!inGame) {
	        Map<String, Object> msg = new HashMap<>();
	        msg.put("type", "force_exit");
	        msg.put("reason", "no_player_info");
	        msg.put("targetUrl", "/gameroom");
	        ObjectMapper mapper = new ObjectMapper();
	        String jsonMsg = mapper.writeValueAsString(msg);
	        session.sendMessage(new TextMessage(jsonMsg));
	        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
	        return;
	    }
	}

	private void broadcastMessage(String type, Long roomId, Map<String, Object> data) throws IOException {
		// 웹소켓 메시지 전송
		List<WebSocketSession> sessions = sessionService.getSessions(roomId);
		if (sessions == null || sessions.isEmpty()) {
			// 더 이상 메시지 보낼 대상이 없음
			return;
		}

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> messageMap = new HashMap<>();
		messageMap.put("type", type);

		if (data != null) {
			messageMap.putAll(data);
		}

		String jsonMessage = mapper.writeValueAsString(messageMap);
		
		// 비정상 세션 감지 시 패배처리 하고 게임룸 리스트로 보냄
		List<WebSocketSession> sessionsCopy = new ArrayList<>(sessions);
		for (WebSocketSession session : sessionsCopy) {
		    try {
		    	synchronized(session) {
			        if (session.isOpen()) {
			            session.sendMessage(new TextMessage(jsonMessage));
			        }
		    	}
		    } catch (Exception e) {
		    	// 강제퇴장 메시지 전송 시도
		        try {
		            Map<String, Object> msg = new HashMap<>();
		            msg.put("type", "force_exit");
		            msg.put("reason", "connection_error");
		            msg.put("targetUrl", "/gameroom");
		            String jsonMsg = mapper.writeValueAsString(msg);
		            session.sendMessage(new TextMessage(jsonMsg));
		        } catch (Exception ignored) {}
		        // 세션 목록에서 제거
		        sessionService.removeSession(roomId, session);
		        if(!Boolean.TRUE.equals(gameFinishMap.get(roomId))) {
			        // 유저 아이디가 있으면 패배 처리도 같이!
					Long userId = (Long) session.getAttributes().get("userId");
			        if (userId != null) {
			            processUserLose(roomId, userId);
			        }
		        }
		    }
		}
	}

	public void onGameStart(Long roomId) throws IOException {
		// 게임 시작 시점의 참가자 전체 정보 저장
		List<TurtlePlayerDTO> startPlayers = playerDAO.getAll(roomId);
		// null 방지
		if (startPlayers != null) {
			gameStartPlayersMap.put(roomId, new ArrayList<>(startPlayers));
		}
		int totalBet = startPlayers.stream().mapToInt(TurtlePlayerDTO :: getEntryFee).sum();
		Map<String, Object> data = new HashMap<>();
		data.put("totalBet", totalBet);
		broadcastMessage("game_start", roomId, data);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		// 클라이언트가 보낸 메시지를 받아서 처리 (예: 채팅, 입장, 베팅 등)
		// 메시지 파싱
		String payload = message.getPayload();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode json = mapper.readTree(payload);

		String type = json.get("type").asText();

		Long roomId = (Long) session.getAttributes().get("roomId");
		Long userId = (Long) session.getAttributes().get("userId");
		GameRoomResponseDTO.GameRoomDetailDTO gameroom = gameRoomService.selectById(roomId);
	    Level difficulty = gameroom.getLevel();
		// 메시지 타입에 따라 분기
		switch (type) {
		case "game_start":
			int turtleCount = 6;
			switch (difficulty) {
				case EASY: turtleCount = 4; break;
				case NORMAL: turtleCount = 6; break;
				case HARD: turtleCount = 8; break;
		}
			turtleGameService.startGame(roomId, turtleCount, new TurtleGameService.RaceUpdateCallback() {
				@Override
				public void onRaceUpdate(Long roomId, double[] positions) {
					try {
						broadcastRaceUpdate(roomId, positions);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				@Override
				public void onRaceFinish(Long roomId, int winner, List<Map<String, Object>> results) {
					try {
						broadcastRaceFinish(roomId, winner, results);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			broadcastMessage("game_start", roomId, null);
            // 방의 host_uid 조회 (DB 또는 room info에서)
			Long roomHostUid = gameroom.getHostId();
            if (userId.equals(roomHostUid)) {
                onGameStart(roomId); // 참가자 freeze
            }
            break;
		case "end":
			List<TurtlePlayerDTO> players = playerDAO.getAll(roomId);
			for (TurtlePlayerDTO player : players) {
				player.setTurtleId(null);
				player.setReady(false);
				player.setEntryFee(gameRoomService.selectById(roomId).getEntryFee());
			}
			gameStartPlayersMap.remove(roomId);
			Map<String, Object> data = new HashMap<>();
			data.put("target", "/gameroom/detail/" + roomId);
			broadcastMessage("end", roomId, data);
			break;
		}
	}

	// 방에 위치 정보를 스케쥴러로 보내주는 함수
	private void broadcastRaceUpdate(Long roomId, double[] positions) throws IOException {
		List<WebSocketSession> sessions = sessionService.getSessions(roomId);
		if (sessions == null)
			return;

		Map<String, Object> msg = new HashMap<>();
		msg.put("type", "race_update");
		List<Double> posList = new ArrayList<>();
		for(double p : positions) posList.add(p);
		msg.put("positions", posList);
		broadcastMessage("race_update", roomId, msg);
	}

	private void broadcastRaceFinish(Long roomId, int winner, List<Map<String, Object>> results) throws IOException {
		List<WebSocketSession> sessions = sessionService.getSessions(roomId);
		if(sessions == null) 
			return;
		
		Map<String, Object> msg = new HashMap<>();
		msg.put("type",  "race_finish");
		msg.put("winner", winner);
		msg.put("results",  results);
		broadcastMessage("race_finish", roomId, msg);
		
		// 게임 종료 상태
		gameFinishMap.put(roomId, true);
	}
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		Long roomId = (Long) session.getAttributes().get("roomId");
		Long userId = (Long) session.getAttributes().get("userId");
		sessionService.removeSession(roomId, session);

		GameRoomResponseDTO.GameRoomDetailDTO gameroom = gameRoomService.selectById(roomId);
		GameRoomStatus gameStatus = gameroom.getStatus();
		if (!gameStatus.equals(GameRoomStatus.WAITING)) {
			processUserLose(roomId, userId);

			playerDAO.removePlayer(roomId, userId);

			if (userId.equals(gameroom.getHostId())) {
				// 방장 퇴장 시 host 위임 또는 방 삭제
				List<TurtlePlayerDTO> players = playerDAO.getAll(roomId); // 방장 제외하고 재조회
			
				if (players != null && !players.isEmpty()) {
					Long newHostId = players.get(0).getUserId();
					gameRoomService.updateHost(roomId, newHostId);
				
					// host_changed 메시지 브로드캐스트
					Map<String, Object> msg = new HashMap<>();
					msg.put("type", "host_changed");
					msg.put("newHostUid", newHostId);
					msg.put("positions", latestPositions.get(roomId));
					broadcastMessage("host_changed", roomId, msg);
				} else {
					// 플레이어가 0명일 때 방 삭제
					gameStartPlayersMap.remove(roomId);
					gameFinishMap.remove(roomId);
					gameRoomService.deleteRoom(roomId);
					gameRoomListWebSocket.broadcastMessage("delete");
					ScheduledFuture<?> task = broadcastTasks.remove(roomId);
					
					if (task != null)
						task.cancel(true);
					latestPositions.remove(roomId);
				}
			}
			gameRoomListWebSocket.broadcastMessage("exit");
		}
	}

	public void processUserLose(Long roomId, Long userId) {
		// 나간사람  패배처리
		List<TurtlePlayerDTO> startPlayers = gameStartPlayersMap.get(roomId);
		if (startPlayers != null) {
			for (TurtlePlayerDTO player : startPlayers) {
				if (player.getUserId().equals(userId)) {
					int betAmount = player.getEntryFee();
					String gameName = "Turtle Run";
					RankType gameResult = RankType.LOSE;
					int winAmount = 0;
					// 1) 유저 정보 조회
					UserEntity userEntity = authService.findById(userId);
					if (userEntity != null) {
						walletService.losePong(betAmount, userEntity.getId());
						Long gameId = gameService.selectByName(gameName).stream().findFirst()
								.orElseThrow(() -> new IllegalStateException("'" + gameName + "' 게임을 찾을 수 없습니다."))
								.getId();

						GameEntity gameEntity = gameService.selectById(gameId);

						// 게임 히스토리 저장 (gameName도 같이)
						GameHistoryEntity gameHistoryEntity = GameHistoryEntity.builder()
								.game(gameEntity)
								.entryFee(betAmount)
								.pongValue(Math.abs(winAmount - betAmount))
								.rank(gameResult)
								.build();

						historyService.insertGameHistory(gameHistoryEntity, userId);

						// 포인트 히스토리 저장
						PongHistoryEntity pongHistoryEntity = PongHistoryEntity.builder()
								.type(PongHistoryType.GAME_P)
								.amount(Math.abs(winAmount - betAmount))
								.build();

						historyService.insertPointHistory(pongHistoryEntity, userId);
					}
					break;
				}
			}
			// 2. startPlayers에서 userId 제거
	        startPlayers.removeIf(p -> p.getUserId().equals(userId));
		}
	}
	
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		// 예외 로깅 또는 복구 처리
	}
}