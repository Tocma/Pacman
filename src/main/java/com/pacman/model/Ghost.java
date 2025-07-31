package com.pacman.model;

import com.pacman.game.Direction;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ゴーストの基底クラス
 * 全てのゴーストに共通する機能と状態を管理
 */
public abstract class Ghost {
    // ゴーストの状態
    public enum GhostState {
        SCATTER, // 散開モード（各ゴーストが特定のコーナーを目指す）
        CHASE, // 追跡モード（パックマンを追いかける）
        FRIGHTENED, // 怯えモード（パワーペレット効果中）
        EATEN, // 食べられた状態（目玉だけでゴーストハウスに戻る）
        IN_HOUSE, // ゴーストハウス内
        EXITING_HOUSE // ゴーストハウスから出る途中
    }

    // 位置情報
    public double x;
    public double y;
    protected Point homePosition; // ゴーストハウス内の初期位置

    // 移動関連
    public Direction currentDirection;
    public double speed;
    protected Point targetTile; // 目標とするタイル座標

    // 状態管理
    public GhostState state;
    public int stateTimer;
    protected int frightenedTimer;
    protected int dotCounter; // ゴーストハウスから出るタイミング制御用

    // 外観
    protected Color normalColor;
    protected String n;

    // 定数
    protected static final double NORMAL_SPEED = 0.1;
    protected static final double FRIGHTENED_SPEED = 0.05;
    protected static final double EATEN_SPEED = 0.2;
    protected static final int FRIGHTENED_DURATION = 400; // フレーム数

    // ランダム要素用
    protected Random random = new Random();

    /**
     * コンストラクタ
     */
    public Ghost(String n, Color color, int startX, int startY) {
        this.n = n;
        this.normalColor = color;
        this.x = startX;
        this.y = startY;
        this.homePosition = new Point(startX, startY);
        this.currentDirection = Direction.UP;
        this.speed = NORMAL_SPEED;
        this.state = GhostState.IN_HOUSE;
        this.stateTimer = 0;
        this.frightenedTimer = 0;
        this.dotCounter = 0;
    }

    /**
     * ゴーストの更新処理
     */
    public void update(Maze maze, Pacman pacman, List<Ghost> otherGhosts) {
        // 状態タイマーの更新
        updateTimers();

        // 状態に応じた処理
        switch (state) {
            case IN_HOUSE:
                updateInHouse();
                break;
            case EXITING_HOUSE:
                updateExitingHouse(maze);
                break;
            case SCATTER:
            case CHASE:
            case FRIGHTENED:
            case EATEN:
                updateMovement(maze, pacman);
                break;
        }
    }

    /**
     * タイマーの更新
     */
    private void updateTimers() {
        stateTimer++;

        if (state == GhostState.FRIGHTENED) {
            frightenedTimer--;
            if (frightenedTimer <= 0) {
                state = GhostState.CHASE;
                speed = NORMAL_SPEED;
            }
        }

        // チェイス/スキャッターモードの切り替え（オリジナルのタイミングを模倣）
        if (state == GhostState.CHASE || state == GhostState.SCATTER) {
            // 簡略化されたモード切り替えパターン
            int cycle = stateTimer % 2000;
            if (cycle < 420) { // 7秒間 SCATTER
                state = GhostState.SCATTER;
            } else if (cycle < 1620) { // 20秒間 CHASE
                state = GhostState.CHASE;
            } else { // 残り SCATTER
                state = GhostState.SCATTER;
            }
        }
    }

    /**
     * ゴーストハウス内での動作
     */
    private void updateInHouse() {
        // 上下に軽く揺れる動作
        y += Math.sin(stateTimer * 0.1) * 0.02;

        // 出るタイミングをチェック
        if (shouldExitHouse()) {
            state = GhostState.EXITING_HOUSE;
        }
    }

    /**
     * ゴーストハウスから出る条件（サブクラスでオーバーライド可能）
     */
    protected boolean shouldExitHouse() {
        // デフォルトは一定時間経過後
        return stateTimer > 60; // 約1秒後
    }

    /**
     * ゴーストハウスから出る処理
     */
    private void updateExitingHouse(Maze maze) {
        // ゴーストハウスの出口（中央上部）へ移動
        double targetX = 14;
        double targetY = 14;

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.1) {
            // 出口に到達
            x = targetX;
            y = targetY;
            state = GhostState.SCATTER;
            currentDirection = Direction.LEFT;
        } else {
            // 出口に向かって移動
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }
    }

    /**
     * 通常の移動処理
     */
    private void updateMovement(Maze maze, Pacman pacman) {
        // 目標タイルの設定
        updateTargetTile(maze, pacman);

        // 交差点での方向決定
        if (isAtIntersection(maze)) {
            Direction newDirection = chooseDirection(maze);
            if (newDirection != Direction.NONE) {
                currentDirection = newDirection;
            }
        }

        // 実際の移動
        move(maze);

        // トンネル処理
        handleTunnel(maze);
    }

    /**
     * 目標タイルの更新（状態に応じて）
     */
    private void updateTargetTile(Maze maze, Pacman pacman) {
        switch (state) {
            case SCATTER:
                targetTile = getScatterTarget();
                break;
            case CHASE:
                targetTile = getChaseTarget(pacman);
                break;
            case FRIGHTENED:
                // ランダムな方向
                targetTile = null;
                break;
            case EATEN:
                // ゴーストハウスの入口を目指す
                targetTile = new Point(14, 14);
                break;
        }
    }

    /**
     * 散開モードの目標位置（サブクラスで実装）
     */
    protected abstract Point getScatterTarget();

    /**
     * 追跡モードの目標位置（サブクラスで実装）
     */
    protected abstract Point getChaseTarget(Pacman pacman);

    /**
     * 交差点かどうかの判定
     */
    private boolean isAtIntersection(Maze maze) {
        // グリッドの中心にいる場合のみ判定
        if (Math.abs(x - Math.round(x)) < 0.05 && Math.abs(y - Math.round(y)) < 0.05) {
            int gridX = (int) Math.round(x);
            int gridY = (int) Math.round(y);

            // 可能な方向の数をカウント
            int possibleDirections = 0;
            for (Direction dir : Direction.values()) {
                if (dir != Direction.NONE && dir != currentDirection.opposite()) {
                    if (maze.isGhostWalkable(gridX + dir.getDx(), gridY + dir.getDy())) {
                        possibleDirections++;
                    }
                }
            }
            return possibleDirections > 1;
        }
        return false;
    }

    /**
     * 次の方向を選択
     */
    private Direction chooseDirection(Maze maze) {
        int gridX = (int) Math.round(x);
        int gridY = (int) Math.round(y);

        List<Direction> possibleDirections = new ArrayList<>();

        // 可能な方向をリストアップ（後退は禁止）
        for (Direction dir : Direction.values()) {
            if (dir != Direction.NONE && dir != currentDirection.opposite()) {
                if (maze.isGhostWalkable(gridX + dir.getDx(), gridY + dir.getDy())) {
                    possibleDirections.add(dir);
                }
            }
        }

        if (possibleDirections.isEmpty()) {
            return currentDirection.opposite(); // 行き止まりの場合は反転
        }

        // 怯えモードの場合はランダム
        if (state == GhostState.FRIGHTENED) {
            return possibleDirections.get(random.nextInt(possibleDirections.size()));
        }

        // 目標に最も近い方向を選択
        if (targetTile != null) {
            Direction bestDirection = possibleDirections.get(0);
            double minDistance = Double.MAX_VALUE;

            for (Direction dir : possibleDirections) {
                double nextX = gridX + dir.getDx();
                double nextY = gridY + dir.getDy();
                double distance = Math.sqrt(Math.pow(nextX - targetTile.x, 2) +
                        Math.pow(nextY - targetTile.y, 2));

                if (distance < minDistance) {
                    minDistance = distance;
                    bestDirection = dir;
                }
            }
            return bestDirection;
        }

        return possibleDirections.get(0);
    }

    /**
     * 実際の移動処理
     */
    private void move(Maze maze) {
        double nextX = x + currentDirection.getDx() * speed;
        double nextY = y + currentDirection.getDy() * speed;

        // 移動可能かチェック
        int gridX = (int) Math.round(nextX);
        int gridY = (int) Math.round(nextY);

        if (maze.isGhostWalkable(gridX, gridY)) {
            x = nextX;
            y = nextY;
        }
    }

    /**
     * トンネル処理
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
     * パワーペレット効果の開始
     */
    public void startFrightened() {
        if (state != GhostState.EATEN) {
            state = GhostState.FRIGHTENED;
            frightenedTimer = FRIGHTENED_DURATION;
            speed = FRIGHTENED_SPEED;
            // 方向反転
            currentDirection = currentDirection.opposite();
        }
    }

    /**
     * ゴーストが食べられた時の処理
     */
    public void setEaten() {
        state = GhostState.EATEN;
        speed = EATEN_SPEED;
    }

    /**
     * 現在の色を取得
     */
    public Color getCurrentColor() {
        switch (state) {
            case FRIGHTENED:
                // 青と白の点滅（残り時間が少ない時）
                if (frightenedTimer < 100 && (frightenedTimer / 10) % 2 == 0) {
                    return Color.WHITE;
                }
                return Color.BLUE;
            case EATEN:
                return Color.GRAY; // 目玉だけの状態
            default:
                return normalColor;
        }
    }

    // ゲッターメソッド
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public GhostState getState() {
        return state;
    }

    public String getName() {
        return n;
    }

    /**
     * グリッド座標を取得
     */
    public Point getGridPosition() {
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    /**
     * ペレットカウンターを増加（ゴーストハウスからの出現制御用）
     */
    public void incrementDotCounter() {
        dotCounter++;
    }
}