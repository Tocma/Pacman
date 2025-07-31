package com.pacman.model;

import com.pacman.game.Direction;
import java.awt.Point;

/**
 * パックマンエンティティクラス
 * プレイヤーが操作するパックマンの状態と動作を管理
 */
public class Pacman {
    // 位置情報（グリッド座標）
    private double x;
    private double y;

    // 移動関連
    private Direction currentDirection;
    private Direction requestedDirection;
    private double speed;

    // アニメーション関連
    private int animationFrame;
    private int animationCounter;
    private static final int ANIMATION_SPEED = 5; // フレーム更新速度

    // ゲーム状態
    private boolean alive;
    private int lives;

    // 定数
    private static final double BASE_SPEED = 0.125; // 基本移動速度（1フレームあたりのグリッド移動量）

    /**
     * コンストラクタ
     * 
     * @param startX 開始X座標
     * @param startY 開始Y座標
     */
    public Pacman(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.currentDirection = Direction.LEFT; // 初期方向は左
        this.requestedDirection = Direction.NONE;
        this.speed = BASE_SPEED;
        this.alive = true;
        this.lives = 3;
        this.animationFrame = 0;
        this.animationCounter = 0;
    }

    /**
     * パックマンの更新処理
     * 
     * @param maze 迷路オブジェクト
     */
    public void update(Maze maze) {
        if (!alive) {
            return;
        }

        // 方向転換の試行
        if (requestedDirection != Direction.NONE && requestedDirection != currentDirection) {
            if (canMove(maze, requestedDirection)) {
                currentDirection = requestedDirection;
                requestedDirection = Direction.NONE;
            }
        }

        // 現在の方向への移動
        if (currentDirection != Direction.NONE && canMove(maze, currentDirection)) {
            move(currentDirection);

            // トンネル処理
            handleTunnel(maze);

            // アニメーション更新
            updateAnimation();
        }
    }

    /**
     * 指定方向への移動が可能かチェック
     */
    private boolean canMove(Maze maze, Direction direction) {
        double nextX = x + direction.getDx() * speed;
        double nextY = y + direction.getDy() * speed;

        // グリッド境界を越える場合の座標計算
        int gridX1 = (int) Math.floor(nextX);
        int gridY1 = (int) Math.floor(nextY);
        int gridX2 = (int) Math.ceil(nextX);
        int gridY2 = (int) Math.ceil(nextY);

        // 四隅すべてが移動可能かチェック
        return maze.isWalkable(gridX1, gridY1) &&
                maze.isWalkable(gridX2, gridY1) &&
                maze.isWalkable(gridX1, gridY2) &&
                maze.isWalkable(gridX2, gridY2);
    }

    /**
     * 実際の移動処理
     */
    private void move(Direction direction) {
        x += direction.getDx() * speed;
        y += direction.getDy() * speed;

        // グリッドの中心に合わせる（滑らかな移動のため）
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            y = Math.round(y);
        } else if (direction == Direction.UP || direction == Direction.DOWN) {
            x = Math.round(x);
        }
    }

    /**
     * トンネル処理（画面端のワープ）
     */
    private void handleTunnel(Maze maze) {
        if (maze.isTunnel((int) x, (int) y)) {
            if (x < 0) {
                x = Maze.WIDTH - 1;
            } else if (x >= Maze.WIDTH) {
                x = 0;
            }
        }
    }

    /**
     * アニメーションフレームの更新
     */
    private void updateAnimation() {
        animationCounter++;
        if (animationCounter >= ANIMATION_SPEED) {
            animationCounter = 0;
            animationFrame = (animationFrame + 1) % 4; // 4フレームのアニメーション
        }
    }

    /**
     * 方向転換のリクエスト
     */
    public void setRequestedDirection(Direction direction) {
        this.requestedDirection = direction;
    }

    /**
     * パックマンがペレットを食べる処理
     * 
     * @return 食べたペレットの種類（PELLET, POWER_PELLET, またはEMPTY）
     */
    public int eatPellet(Maze maze) {
        int gridX = (int) Math.round(x);
        int gridY = (int) Math.round(y);
        return maze.consumePellet(gridX, gridY);
    }

    /**
     * パックマンが死亡した時の処理
     */
    public void die() {
        alive = false;
        lives--;
    }

    /**
     * パックマンをリスポーン
     */
    public void respawn(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.currentDirection = Direction.LEFT;
        this.requestedDirection = Direction.NONE;
        this.alive = true;
        this.animationFrame = 0;
    }

    /**
     * 現在のグリッド座標を取得
     */
    public Point getGridPosition() {
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    // ゲッターメソッド
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Direction getCurrentDirection() {
        return currentDirection;
    }

    public int getAnimationFrame() {
        return animationFrame;
    }

    public boolean isAlive() {
        return alive;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    /**
     * デバッグ用の文字列表現
     */
    @Override
    public String toString() {
        return String.format("Pacman[x=%.2f, y=%.2f, dir=%s, lives=%d]",
                x, y, currentDirection, lives);
    }
}