package com.wepong.pongdang.model.multi.turtle;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TurtleGameState {
    private final int turtleCount;
    private final double[] positions;       // 각 거북이 위치(0~100)
    private final double[] baseSpeeds;      // 기본 속도
    private final double[] burstChances;    // 버스트 확률
    private final List<Integer> finishOrder = new CopyOnWriteArrayList<>();   // 추가: 완주 순서(거북이 인덱스)를 저장  // 완주 시간 기록용
    private final List<String> podiumUserIds = new CopyOnWriteArrayList<>();  // 순위 기록용
    private boolean finished = false;       // 레이스 종료 여부
    private int winner = -1;                // 우승자(0~n-1)
    private String status = "WAITING";      // WAITING, RUNNING, FINISHED

    public TurtleGameState(int turtleCount) {
        this.turtleCount = turtleCount;
        this.positions = new double[turtleCount];
        this.baseSpeeds = new double[turtleCount];
        this.burstChances = new double[turtleCount];
        for (int i = 0; i < turtleCount; i++) {
            baseSpeeds[i] = 0.07 + Math.random() * 0.03;
            burstChances[i] = Math.random() * 0.3;
        }
    }
    // 레이스 한 프레임 진행
    public void updateRace() {
        if (finished) return;
        for (int i = 0; i < turtleCount; i++) {
            if (positions[i] < 100) {
                double burst = Math.random() < burstChances[i] ? 0.03 + Math.random() * 0.05 : 0;
                double variation = (Math.random() - 0.15) * 0.1;
                double move = baseSpeeds[i] + variation + burst;
                positions[i] += move;
                if (positions[i] >= 100) {
                    positions[i] = 100;

                    // ✅ 완주 순서에 한 번만 추가
                    if (!finishOrder.contains(i)) {
                        finishOrder.add(i);
                    }

                    // ✅ 1등은 winner로 기록(한 번만)
                    if (winner == -1) {
                        winner = i;
                    }

                    // 보기 좋게 결승선 살짝 넘긴 위치
                    positions[i] = 101;

                    // ✅ 3마리 완주하면 즉시 종료
                    if (finishOrder.size() >= 3) {
                        finished = true;
                        status = "FINISHED";
                        return;
                    }
                }
            }
        }
    }

    // 추가: Top3 조회(없으면 -1)
    public int[] getTop3TurtleIds() {
        int first  = finishOrder.size() > 0 ? finishOrder.get(0) : -1;
        int second = finishOrder.size() > 1 ? finishOrder.get(1) : -1;
        int third  = finishOrder.size() > 2 ? finishOrder.get(2) : -1;
        return new int[]{ first, second, third };
    }

    // getter
    public double[] getPositions() { return positions; }
    public boolean isFinished() { return finished; }
    public int getWinner() { return winner; }
    public String getStatus() { return status; }
    public int getTurtleCount() { return turtleCount; }
}
