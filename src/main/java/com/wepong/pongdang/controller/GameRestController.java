package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.GameLevelResponseDTO;
import com.wepong.pongdang.dto.response.GameResponseDTO;
import com.wepong.pongdang.entity.*;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.entity.enums.RankType;
import com.wepong.pongdang.entity.enums.GameType;
import com.wepong.pongdang.service.AuthService;
import com.wepong.pongdang.service.GameLevelService;
import com.wepong.pongdang.service.GameService;
import com.wepong.pongdang.service.HistoryService;
import com.wepong.pongdang.service.WalletService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/game")
public class GameRestController {

	@Autowired
	private GameService gameService;

	@Autowired
	private GameLevelService gameLevelService;

	@Autowired
	private AuthService authService;
	
	@Autowired
	private HistoryService historyService;
	@Autowired
	private WalletService walletService;

	// 게임 리스트 조회
	@GetMapping("/list")
	public GameResponseDTO.GameListDTO selectAll() {
		List<GameEntity> gameEntityList = gameService.selectAll();
		List<GameResponseDTO.GameDetailDTO> details = gameEntityList.stream()
				.map(GameResponseDTO.GameDetailDTO::from).collect(Collectors.toList());
		return GameResponseDTO.GameListDTO.from(details);
	}

	// 게임 상세 조회
	@GetMapping("/detail/{gameId}")
	public GameResponseDTO.GameDetailDTO selectById(@PathVariable Long gameId) {
		GameEntity gameEntity = gameService.selectById(gameId);
		return GameResponseDTO.GameDetailDTO.from(gameEntity);
	}

	// 타입별 게임 조회
	@GetMapping("/list/type")
	public GameResponseDTO.GameListDTO selectByType(@RequestParam GameType type) {
		List<GameEntity> gameEntityList = gameService.selectByType(type);
		List<GameResponseDTO.GameDetailDTO> details = gameEntityList.stream()
				.map(GameResponseDTO.GameDetailDTO::from).collect(Collectors.toList());
		return GameResponseDTO.GameListDTO.from(details);
	}

	// 이름으로 게임 조회
	@GetMapping("/by-name/{name}")
	public GameResponseDTO.GameListDTO selectByName(@PathVariable String name) {
		List<GameEntity> gameEntityList = gameService.selectByName(name);
		List<GameResponseDTO.GameDetailDTO> details = gameEntityList.stream()
				.map(GameResponseDTO.GameDetailDTO::from).collect(Collectors.toList());
		return GameResponseDTO.GameListDTO.from(details);
	}
	
	@GetMapping("/levels/by-game/{uid}")
	public GameLevelResponseDTO.LevelListDTO selectLevelsByGame(@PathVariable Long uid) {
	    List<GameLevelEntity> levelList = gameLevelService.selectByGameUid(uid);
		List<GameLevelResponseDTO.LevelDetailDTO> details = levelList.stream()
				.map(GameLevelResponseDTO.LevelDetailDTO::from).collect(Collectors.toList());
		return GameLevelResponseDTO.LevelListDTO.from(details);
	}
	
	@PostMapping("/start")
	public ResponseEntity<?> startGame(@RequestHeader("Authorization") String authHeader,
	                                   @RequestBody Map<String, Object> requestBody) {

	    // 토큰에서 uid 꺼냄
		Long uid = authService.validateAndGetUserId(authHeader);

	    // uid로 유저 조회
	    UserEntity userEntity = authService.findById(uid);
	    if (userEntity == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                             .body(Map.of("message", "유저를 찾을 수 없습니다."));
	    }

	    // JSON에서 betAmount 추출
	    int betAmount = Integer.parseInt(requestBody.get("betAmount").toString());

	    // 포인트 차감
	    walletService.losePong(betAmount, userEntity.getId());

	    return ResponseEntity.ok().build();
	}
		
	//win
	@PostMapping("/stop")
	public ResponseEntity<?> stopGame(@RequestHeader("Authorization") String authHeader,
	                                  @RequestBody Map<String, Object> requestBody) {

		Long uid = authService.validateAndGetUserId(authHeader);
	    UserEntity userEntity = authService.findById(uid);
	    if (userEntity == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                             .body(Map.of("message", "유저를 찾을 수 없습니다."));
	    }

	    int winAmount = Integer.parseInt(requestBody.get("winAmount").toString());
	    int betAmount = Integer.parseInt(requestBody.get("betAmount").toString());
	    RankType gameResult = RankType.valueOf(requestBody.get("gameResult").toString());
	    String gameName = requestBody.get("gameName").toString();

	    // 포인트 적립
	    walletService.addPong(winAmount, userEntity.getId());

	    // 게임 UID 조회
		Long gameUid = gameService.selectByName(gameName)
	        .stream()
	        .findFirst()
	        .orElseThrow(() -> new IllegalStateException("'" + gameName + "' 게임을 찾을 수 없습니다."))
	        .getId();

		GameEntity gameEntity = gameService.selectById(gameUid);

	    // 게임 히스토리 저장 (gameName도 같이)
	    GameHistoryEntity gameHistoryEntity = GameHistoryEntity.builder()
				.game(gameEntity)
				.entryFee(betAmount)
				.pongValue(winAmount - betAmount)
				.rank(gameResult)
				.build();

		historyService.insertGameHistory(gameHistoryEntity, uid);

	    // 포인트 히스토리 저장
	    PongHistoryEntity pongHistoryEntity = PongHistoryEntity.builder()
				.type(PongHistoryType.GAME)
				.amount(winAmount - betAmount)
				.build();

	    historyService.insertPointHistory(pongHistoryEntity, uid);

	    return ResponseEntity.ok().build();
	}
	
	//lose
	@PostMapping("/lose")
	public ResponseEntity<?> loseGame(@RequestHeader("Authorization") String authHeader,
	                                  @RequestBody Map<String, Object> requestBody) {

		Long uid = authService.validateAndGetUserId(authHeader);
	    UserEntity userEntity = authService.findById(uid);
	    if (userEntity == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                             .body(Map.of("message", "유저를 찾을 수 없습니다."));
	    }

	    int betAmount = Integer.parseInt(requestBody.get("betAmount").toString());
	    RankType gameResult = RankType.valueOf(requestBody.get("gameResult").toString());
	    String gameName = requestBody.get("gameName").toString(); 
	    
	    // 게임 UID 조회
		Long gameUid = gameService.selectByName(gameName)
	        .stream()
	        .findFirst()
	        .orElseThrow(() -> new IllegalStateException("'" + gameName + "' 게임을 찾을 수 없습니다."))
	        .getId();

		GameEntity gameEntity = gameService.selectById(gameUid);

		// 게임 히스토리 저장 (gameName도 같이)
		GameHistoryEntity gameHistoryEntity = GameHistoryEntity.builder()
				.game(gameEntity)
				.entryFee(betAmount)
				.pongValue(betAmount)
				.rank(gameResult)
				.build();

		historyService.insertGameHistory(gameHistoryEntity, uid);

		// 포인트 히스토리 저장
		PongHistoryEntity pongHistoryEntity = PongHistoryEntity.builder()
				.type(PongHistoryType.GAME)
				.amount(betAmount)
				.build();

		historyService.insertPointHistory(pongHistoryEntity, uid);

	    return ResponseEntity.ok().build();
	}
	
	// 게임 난이도 상세 조회
	@GetMapping("/level/{levelId}")
	public GameLevelEntity selectLevelByRoom(@PathVariable Long levelId) {
		return gameLevelService.selectByLevelUid(levelId);
	}
}