package com.pacman.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * パックマンの迷路を管理するクラス
 * オリジナルのアーケード版に準拠した28×31のグリッドサイズを使用
 */
public class Maze {
    // 迷路のサイズ定数
    public static final int WIDTH = 28;
    public static final int HEIGHT = 31;

    // タイルタイプの定数
    public static final int WALL = 0;
    public static final int PELLET = 1;
    public static final int POWER_PELLET = 2;
    public static final int EMPTY = 3;
    public static final int GHOST_HOUSE = 4;
    public static final int GHOST_HOUSE_DOOR = 5;

    // 迷路データ（0=壁, 1=通常ペレット, 2=パワーペレット, 3=空, 4=ゴーストハウス内部, 5=ゴーストハウスドア）
    private int[][] maze;

    // ペレットの総数（クリア判定用）
    private int totalPellets;
    private int remainingPellets;

    public Maze() {
        initializeMaze();
        countPellets();
    }

    /**
     * 迷路レイアウトの初期化
     * オリジナルのパックマンの迷路に近いレイアウトを定義
     */
    private void initializeMaze() {
        // 基本的な迷路レイアウト（0=壁, 1=通常ペレット, 2=パワーペレット, 3=空, 4=ゴーストハウス）
        maze = new int[][] {
                // 28列 × 31行のレイアウト
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 },
                { 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0 },
                { 0, 2, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 2, 0 },
                { 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 },
                { 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0 },
                { 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, 0, 0, 3, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 3, 0, 0, 3, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 0, 0, 0, 5, 5, 0, 0, 0, 3, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 0, 4, 4, 4, 4, 4, 4, 0, 3, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
                { 3, 3, 3, 3, 3, 3, 1, 3, 3, 3, 0, 4, 4, 4, 4, 4, 4, 0, 3, 3, 3, 1, 3, 3, 3, 3, 3, 3 }, // トンネル
                { 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 0, 4, 4, 4, 4, 4, 4, 0, 3, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 1, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 1, 0, 0, 0, 0, 0, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 },
                { 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0 },
                { 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0 },
                { 0, 2, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 3, 3, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 2, 0 },
                { 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0 },
                { 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0 },
                { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },
                { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0 },
                { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
        };
    }

    /**
     * ペレットの総数をカウント
     */
    private void countPellets() {
        totalPellets = 0;
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                if (maze[y][x] == PELLET || maze[y][x] == POWER_PELLET) {
                    totalPellets++;
                }
            }
        }
        remainingPellets = totalPellets;
    }

    /**
     * 指定座標のタイルタイプを取得
     */
    public int getTile(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return WALL;
        }
        return maze[y][x];
    }

    /**
     * 指定座標にタイルを設定
     */
    public void setTile(int x, int y, int tileType) {
        if (x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT) {
            maze[y][x] = tileType;
        }
    }

    /**
     * 指定座標が壁かどうか判定
     */
    public boolean isWall(int x, int y) {
        return getTile(x, y) == WALL;
    }

    /**
     * 指定座標が移動可能かどうか判定
     */
    public boolean isWalkable(int x, int y) {
        int tile = getTile(x, y);
        return tile != WALL && tile != GHOST_HOUSE_DOOR;
    }

    /**
     * ゴーストが通過可能かどうか判定（ゴーストハウスのドアも通過可能）
     */
    public boolean isGhostWalkable(int x, int y) {
        return getTile(x, y) != WALL;
    }

    /**
     * ペレットを消費
     */
    public int consumePellet(int x, int y) {
        int tile = getTile(x, y);
        if (tile == PELLET || tile == POWER_PELLET) {
            setTile(x, y, EMPTY);
            remainingPellets--;
            return tile;
        }
        return EMPTY;
    }

    /**
     * 全てのペレットを消費したかチェック
     */
    public boolean isAllPelletsConsumed() {
        return remainingPellets == 0;
    }

    /**
     * パックマンの初期位置を取得
     */
    public Point getPacmanStartPosition() {
        return new Point(14, 23); // オリジナルの開始位置
    }

    /**
     * ゴーストの初期位置を取得
     */
    public List<Point> getGhostStartPositions() {
        List<Point> positions = new ArrayList<>();
        positions.add(new Point(14, 14)); // Blinky (赤) - ゴーストハウスの外
        positions.add(new Point(14, 17)); // Pinky (ピンク) - ゴーストハウス内
        positions.add(new Point(12, 17)); // Inky (青) - ゴーストハウス内
        positions.add(new Point(16, 17)); // Clyde (オレンジ) - ゴーストハウス内
        return positions;
    }

    /**
     * トンネルの判定（左右の端）
     */
    public boolean isTunnel(int x, int y) {
        return y == 14 && (x == 0 || x == WIDTH - 1);
    }

    // ゲッターメソッド
    public int getTotalPellets() {
        return totalPellets;
    }

    public int getRemainingPellets() {
        return remainingPellets;
    }

    /**
     * 迷路をリセット（新しいゲーム開始時）
     */
    public void reset() {
        initializeMaze();
        countPellets();
    }
}