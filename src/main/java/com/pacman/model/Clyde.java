package main.java.com.pacman.model;

import java.awt.Color;
import java.awt.Point;

/**
 * Clyde（オレンジゴースト）- "Pokey"
 * 独特な行動パターンを持つゴースト
 * パックマンとの距離が8タイル以上の時は追跡、8タイル未満の時は散開モードの目標へ向かう
 */
public class Clyde extends Ghost {

    private static final double FLEE_DISTANCE = 8.0; // 逃走を開始する距離

    public Clyde(int startX, int startY) {
        super("Clyde", Color.ORANGE, startX, startY);
    }

    /**
     * 散開モードでの目標位置
     * Clydeは左下コーナーを目指す
     */
    @Override
    protected Point getScatterTarget() {
        return new Point(0, Maze.HEIGHT - 1);
    }

    /**
     * 追跡モードでの目標位置
     * パックマンとの距離に応じて行動を変える
     */
    @Override
    protected Point getChaseTarget(Pacman pacman) {
        Point pacmanPos = pacman.getGridPosition();
        Point myPos = getGridPosition();

        // パックマンとの距離を計算
        double distance = Math.sqrt(
                Math.pow(pacmanPos.x - myPos.x, 2) +
                        Math.pow(pacmanPos.y - myPos.y, 2));

        if (distance < FLEE_DISTANCE) {
            // 8タイル未満：散開モードの目標（左下コーナー）へ逃げる
            return getScatterTarget();
        } else {
            // 8タイル以上：パックマンを直接追跡
            return pacmanPos;
        }
    }

    /**
     * Clydeは60個のペレットが食べられた後に出る（最後に出るゴースト）
     */
    @Override
    protected boolean shouldExitHouse() {
        return dotCounter >= 60;
    }
}
