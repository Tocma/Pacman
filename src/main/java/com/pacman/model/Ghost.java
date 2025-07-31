package com.pacman.model;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pacman.game.Direction;

/**
 * ゴーストの基底クラス（滑らかな移動制御付き）
 * 位置補正の競合を防止し、安定した移動を実現
 */
public abstract class Ghost {
    // ゴーストの状態
    public enum GhostState {
        SCATTER, CHASE, FRIGHTENED, EATEN, IN_HOUSE, EXITING_HOUSE
    }

    // 位置情報
    public double x;
    public double y;
    protected Point homePosition;

    // 移動関連
    public Direction currentDirection;
    public double speed;
    protected Point targetTile;

    // 状態管理
    public GhostState state;
    public int stateTimer;
    protected int frightenedTimer;
    protected int dotCounter;

    // 外観
    protected Color normalColor;
    protected String name;

    // 定数
    protected static final double NORMAL_SPEED = 0.125;
    protected static final double FRIGHTENED_SPEED = 0.0625;
    protected static final double EATEN_SPEED = 0.25;
    protected static final int FRIGHTENED_DURATION = 400;

    // 改善された位置補正用定数
    private static final double GRID_ALIGNMENT_THRESHOLD = 0.125; // グリッド整列判定の閾値
    private static final double INTERSECTION_THRESHOLD = 0.1; // 交差点判定の閾値
    private static final double CORRECTION_STRENGTH = 0.05; // 補正強度を弱める

    // ランダム要素用
    protected Random random = new Random();

    // 移動制御用（改善）
    private boolean needsDirectionChange = false;
    private int stuckCounter = 0;
    private static final int MAX_STUCK_FRAMES = 60;

    // 位置補正制御用
    private boolean isChangingDirection = false;
    private int directionChangeTimer = 0;
    private static final int DIRECTION_CHANGE_FREEZE_TIME = 5;

    /**
     * コンストラクタ
     */
    public Ghost(String name, Color color, int startX, int startY) {
        this.name = name;
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
        updateTimers();
        checkIfStuck(maze);

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

        // 方向変更タイマーの更新
        if (directionChangeTimer > 0) {
            directionChangeTimer--;
            if (directionChangeTimer == 0) {
                isChangingDirection = false;
            }
        }
    }

    /**
     * 穏やかな位置調整（競合を防ぐため）
     */
    private void performGentleAlignment() {
        // 水平移動時の垂直位置調整
        if (currentDirection == Direction.LEFT || currentDirection == Direction.RIGHT) {
            double targetY = Math.round(y);
            double diff = Math.abs(y - targetY);

            if (diff > GRID_ALIGNMENT_THRESHOLD) {
                double correction = Math.signum(targetY - y) * CORRECTION_STRENGTH;
                y += correction;
            } else if (diff < 0.01) {
                y = targetY; // 十分近い場合は正確に設定
            }
        }

        // 垂直移動時の水平位置調整
        if (currentDirection == Direction.UP || currentDirection == Direction.DOWN) {
            double targetX = Math.round(x);
            double diff = Math.abs(x - targetX);

            if (diff > GRID_ALIGNMENT_THRESHOLD) {
                double correction = Math.signum(targetX - x) * CORRECTION_STRENGTH;
                x += correction;
            } else if (diff < 0.01) {
                x = targetX; // 十分近い場合は正確に設定
            }
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

        // チェイス/スキャッターモードの切り替え
        if (state == GhostState.CHASE || state == GhostState.SCATTER) {
            int cycle = stateTimer % 2000;
            if (cycle < 420) {
                if (state != GhostState.SCATTER) {
                    state = GhostState.SCATTER;
                    needsDirectionChange = true;
                }
            } else if (cycle < 1620) {
                if (state != GhostState.CHASE) {
                    state = GhostState.CHASE;
                    needsDirectionChange = true;
                }
            } else {
                if (state != GhostState.SCATTER) {
                    state = GhostState.SCATTER;
                    needsDirectionChange = true;
                }
            }
        }
    }

    /**
     * 動作停止状態のチェック
     */
    private void checkIfStuck(Maze maze) {
        double nextX = x + currentDirection.getDx() * speed;
        double nextY = y + currentDirection.getDy() * speed;

        if (!canMoveTo(maze, nextX, nextY)) {
            stuckCounter++;
            if (stuckCounter > MAX_STUCK_FRAMES) {
                forceDirectionChange(maze);
                stuckCounter = 0;
            }
        } else {
            stuckCounter = 0;
        }
    }

    /**
     * 強制的な方向変更
     */
    private void forceDirectionChange(Maze maze) {
        List<Direction> validDirections = getValidDirections(maze);
        if (!validDirections.isEmpty()) {
            currentDirection = validDirections.get(random.nextInt(validDirections.size()));
            needsDirectionChange = false;
            executeDirectionChange();
        }
    }

    /**
     * 方向変更の実行
     */
    private void executeDirectionChange() {
        // 方向変更時に位置をグリッドに調整
        x = Math.round(x);
        y = Math.round(y);

        // 方向変更中フラグを設定
        isChangingDirection = true;
        directionChangeTimer = DIRECTION_CHANGE_FREEZE_TIME;
    }

    /**
     * 移動可能な方向のリストを取得
     */
    private List<Direction> getValidDirections(Maze maze) {
        List<Direction> validDirections = new ArrayList<>();
        int gridX = (int) Math.round(x);
        int gridY = (int) Math.round(y);

        for (Direction dir : Direction.values()) {
            if (dir != Direction.NONE) {
                if (maze.isGhostWalkable(gridX + dir.getDx(), gridY + dir.getDy())) {
                    validDirections.add(dir);
                }
            }
        }
        return validDirections;
    }

    /**
     * ゴーストハウス内での動作
     */
    private void updateInHouse() {
        y += Math.sin(stateTimer * 0.1) * 0.02;

        if (shouldExitHouse()) {
            state = GhostState.EXITING_HOUSE;
            currentDirection = Direction.UP;
            x = Math.round(x);
            y = Math.round(y);
        }
    }

    /**
     * ゴーストハウスから出る条件
     */
    protected boolean shouldExitHouse() {
        return stateTimer > 60;
    }

    /**
     * ゴーストハウスから出る処理
     */
    private void updateExitingHouse(Maze maze) {
        double targetX = 14;
        double targetY = 11;

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 0.5) {
            x = targetX;
            y = targetY;
            state = GhostState.SCATTER;
            currentDirection = Direction.LEFT;
        } else {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }
    }

    /**
     * 通常の移動処理
     */
    private void updateMovement(Maze maze, Pacman pacman) {
        updateTargetTile(maze, pacman);

        // 交差点での方向決定（改善された判定）
        if (isAtGridCenter() || needsDirectionChange) {
            Direction newDirection = chooseDirection(maze);
            if (newDirection != Direction.NONE && newDirection != currentDirection) {
                // グリッドの中心で方向転換を実行
                x = Math.round(x);
                y = Math.round(y);
                currentDirection = newDirection;
                needsDirectionChange = false;
            }
        }

        // 移動実行
        move(maze);
        handleTunnel(maze);
    }

    /**
     * 目標タイルの更新
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
                targetTile = null;
                break;
            case EATEN:
                targetTile = new Point(14, 14);
                break;
        }
    }

    /**
     * 散開モードの目標位置
     */
    protected abstract Point getScatterTarget();

    /**
     * 追跡モードの目標位置
     */
    protected abstract Point getChaseTarget(Pacman pacman);

    /**
     * ゴーストがグリッドの中心にいるかを判定する
     */
    private boolean isAtGridCenter() {
        // 座標が整数（グリッドの中心）に十分近いかをチェック
        return Math.abs(x - Math.round(x)) < 0.01 &&
                Math.abs(y - Math.round(y)) < 0.01;
    }

    /**
     * 滑らかな交差点判定
     */
    private boolean isAtSmoothIntersection(Maze maze) {
        // より厳格な交差点判定
        if (Math.abs(x - Math.round(x)) < INTERSECTION_THRESHOLD &&
                Math.abs(y - Math.round(y)) < INTERSECTION_THRESHOLD) {

            int gridX = (int) Math.round(x);
            int gridY = (int) Math.round(y);

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
     * 方向選択
     */
    private Direction chooseDirection(Maze maze) {
        int gridX = (int) Math.round(x);
        int gridY = (int) Math.round(y);

        List<Direction> possibleDirections = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            if (dir != Direction.NONE) {
                if (maze.isGhostWalkable(gridX + dir.getDx(), gridY + dir.getDy())) {
                    if (dir != currentDirection.opposite() || possibleDirections.isEmpty()) {
                        possibleDirections.add(dir);
                    }
                }
            }
        }

        if (possibleDirections.isEmpty()) {
            return currentDirection.opposite();
        }

        if (possibleDirections.size() > 1) {
            possibleDirections.remove(currentDirection.opposite());
        }

        if (state == GhostState.FRIGHTENED) {
            return possibleDirections.get(random.nextInt(possibleDirections.size()));
        }

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
     * 移動可能性チェック
     */
    private boolean canMoveTo(Maze maze, double nextX, double nextY) {
        int gridX = (int) Math.round(nextX);
        int gridY = (int) Math.round(nextY);
        return maze.isGhostWalkable(gridX, gridY);
    }

    /**
     * 移動処理
     */
    private void move(Maze maze) {
        double nextX = x + currentDirection.getDx() * speed;
        double nextY = y + currentDirection.getDy() * speed;

        if (canMoveTo(maze, nextX, nextY)) {
            x = nextX;
            y = nextY;

            // パックマンと同様の位置補正
            if (currentDirection == Direction.LEFT || currentDirection == Direction.RIGHT) {
                y = Math.round(y);
            } else if (currentDirection == Direction.UP || currentDirection == Direction.DOWN) {
                x = Math.round(x);
            }

        } else {
            needsDirectionChange = true;
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
            currentDirection = currentDirection.opposite();
            needsDirectionChange = true;
            executeDirectionChange();
        }
    }

    /**
     * ゴーストが食べられた時の処理
     */
    public void setEaten() {
        state = GhostState.EATEN;
        speed = EATEN_SPEED;
        executeDirectionChange();
    }

    /**
     * 現在の色を取得
     */
    public Color getCurrentColor() {
        switch (state) {
            case FRIGHTENED:
                if (frightenedTimer < 100 && (frightenedTimer / 10) % 2 == 0) {
                    return Color.WHITE;
                }
                return Color.BLUE;
            case EATEN:
                return Color.GRAY;
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
        return name;
    }

    public Point getGridPosition() {
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    public void incrementDotCounter() {
        dotCounter++;
    }
}