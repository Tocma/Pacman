package com.pacman.model;

import java.awt.Color;
import java.awt.Point;

import com.pacman.game.Direction;

/**
 * Blinky（赤ゴースト）- "Shadow"
 * 最もシンプルなAIで、常にパックマンを直接追跡する
 * スピードアップ機能：残りペレット数が少なくなると高速化
 */
public class Blinky extends Ghost {

    private int elroyDotsLeft1 = 20; // 第1段階高速化のペレット残数
    private int elroyDotsLeft2 = 10; // 第2段階高速化のペレット残数

    public Blinky(int startX, int startY) {
        super("Blinky", Color.RED, startX, startY);
        // Blinkyは最初からゴーストハウスの外にいる（修正：適切な位置に配置）
        this.state = GhostState.SCATTER;
        this.currentDirection = Direction.LEFT; // 初期方向を左に設定

        // 安全な初期位置に配置（ゴーストハウスの真上の通路）
        if (startY == 14) {
            // 既に適切な位置にいる場合はそのまま
            this.y = startY;
        } else {
            // ゴーストハウス入口の上に配置
            this.y = 11; // 迷路の通路部分
        }
        this.x = 14; // 中央の通路
    }

    /**
     * 散開モードでの目標位置
     * Blinkyは右上コーナーを目指す
     */
    @Override
    protected Point getScatterTarget() {
        return new Point(Maze.WIDTH - 2, 0);
    }

    /**
     * 追跡モードでの目標位置
     * Blinkyは常にパックマンの現在位置を直接狙う
     */
    @Override
    protected Point getChaseTarget(Pacman pacman) {
        // パックマンの現在位置をそのまま目標とする
        return pacman.getGridPosition();
    }

    /**
     * Blinkyは即座にゴーストハウスから出る
     */
    @Override
    protected boolean shouldExitHouse() {
        return true; // 常にtrue（既に外にいるため）
    }

    /**
     * "Cruise Elroy"モードのチェック
     * 残りペレット数に応じてBlinkyが高速化する
     * 
     * @param remainingPellets 残りペレット数
     */
    public void checkElroyMode(int remainingPellets) {
        if (state == GhostState.FRIGHTENED || state == GhostState.EATEN) {
            return; // 特殊状態では速度変更なし
        }

        if (remainingPellets <= elroyDotsLeft2) {
            // 第2段階：さらに高速
            speed = NORMAL_SPEED * 1.05;
        } else if (remainingPellets <= elroyDotsLeft1) {
            // 第1段階：少し高速
            speed = NORMAL_SPEED * 1.025;
        } else {
            // 通常速度
            speed = NORMAL_SPEED;
        }
    }
}