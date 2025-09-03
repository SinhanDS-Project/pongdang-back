package com.wepong.pongdang.controller;

import com.wepong.pongdang.dto.response.GameLevelResponseDTO;
import com.wepong.pongdang.dto.response.GameResponseDTO;
import com.wepong.pongdang.entity.*;
import com.wepong.pongdang.entity.enums.PongHistoryType;
import com.wepong.pongdang.entity.enums.RankType;
import com.wepong.pongdang.entity.enums.GameType;
import com.wepong.pongdang.entity.enums.WalletType;
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

    // 단일게임 성공 (동전던지기 앞면 3번 연속 or 보물찾기 5번 연속 성공)
    @PostMapping("/success")
    public ResponseEntity<?> successGame(@RequestHeader("Authorization") String authHeader) {

        Long id = authService.validateAndGetUserId(authHeader);
        UserEntity userEntity = authService.findById(id);

        if (userEntity == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "유저를 찾을 수 없습니다."));
        }

        // 퐁 히스토리 저장 (ADD 타입)
        PongHistoryEntity pongHistoryEntity = PongHistoryEntity.builder()
                .type(PongHistoryType.ADD)   // ADD 타입 기록
                .amount(1)                   // 1퐁 지급
                .build();

        historyService.insertPointHistory(pongHistoryEntity, id);

        //  실제 포인트 지급
        walletService.add(1, id, WalletType.PONG);

        return ResponseEntity.ok(Map.of(
                "message", "게임 성공! 1퐁 지급 완료",
                "reward", 1
        ));
    }



    // 게임 난이도 상세 조회
	@GetMapping("/level/{levelId}")
	public GameLevelEntity selectLevelByRoom(@PathVariable Long levelId) {
		return gameLevelService.selectByLevelUid(levelId);
	}

    // 게임 삭제
    @DeleteMapping("/{gameId}")
    public ResponseEntity<?> deleteGame(@PathVariable Long gameId) {
        gameService.deleteGame(gameId);
        return ResponseEntity.ok("게임이 삭제되었습니다.");
    }
}