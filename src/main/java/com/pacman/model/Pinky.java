package com.pacman.model;

import com.pacman.game.Direction;
import java.awt.Color;
import java.awt.Point;

/**
 * Pinky（ピンクゴースト）- "Speedy"
 * パックマンの進行方向の4タイル先を目標とする先回り型AI
 * オリジナルの上方向バグも再現
 */
public class Pinky extends Ghost {

    private static final int AHEAD_TILES = 4; // 先読みタイル数

    public Pinky(int startX, int startY) {
        super("Pinky", Color.PINK, startX, startY);
    }

    /**
     * 散開モードでの目標位置
     * Pinkyは左上コーナーを目指す
     */
    @Override
    protected Point getScatterTarget() {
        return new Point(2, 0);
    }

    /**
     * 追跡モードでの目標位置
     * パックマンの向いている方向の4タイル先を狙う
     */
    @Override
    protected Point getChaseTarget(Pacman pacman) {
        Point pacmanPos = pacman.getGridPosition();
        Direction pacmanDir = pacman.getCurrentDirection();

        int targetX = pacmanPos.x;
        int targetY = pacmanPos.y;

        // パックマンの向きに応じて4タイル先を計算
        switch (pacmanDir) {
            case UP:
                targetY -= AHEAD_TILES;
                // オリジナルのバグを再現：上方向の時は左にも4タイルずれる
                targetX -= AHEAD_TILES;
                break;
            case DOWN:
                targetY += AHEAD_TILES;
                break;
            case LEFT:
                targetX -= AHEAD_TILES;
                break;
            case RIGHT:
                targetX += AHEAD_TILES;
                break;
            case NONE:
                // 停止中はパックマンの位置をそのまま狙う
                break;
        }

        return new Point(targetX, targetY);
    }

    /**
     * Pinkyは即座にゴーストハウスから出る
     */
    @Override
    protected boolean shouldExitHouse() {
        return stateTimer > 0; // 即座に出る
    }
}