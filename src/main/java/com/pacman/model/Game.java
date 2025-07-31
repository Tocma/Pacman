package main.java.com.pacman.model;

import main.java.com.pacman.game.Direction;
import main.java.com.pacman.game.GameState;
import javax.swing.Timer;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * ゲーム全体のロジックを管理するクラス
 * ゲームループ、衝突判定、スコア管理、状態遷移などを担当
 */
public class Game {
    // ゲームエンティティ
    private Maze maze;
    private Pacman pacman;
    private List<Ghost> ghosts;

    // ゲーム状態
    private GameState state;
    private int score;
    private int highScore;
    private int level;

    // タイマーとカウンター
    private Timer gameTimer;
    private int stateTimer;
    private int powerPelletTimer;
    private int ghostEatenMultiplier;

    // 定数
    private static final int GAME_SPEED = 16; // 約60FPS
    private static final int PELLET_SCORE = 10;
    private static final int POWER_PELLET_SCORE = 50;
    private static final int GHOST_EATEN_BASE_SCORE = 200;
    private static final int READY_STATE_DURATION = 180; // 3秒
    private static final int DEATH_ANIMATION_DURATION = 120; // 2秒

    // ゲーム更新リスナー（UIへの通知用）
    private GameUpdateListener updateListener;

    /**
     * ゲーム更新リスナーインターフェース
     */
    public interface GameUpdateListener {
        void onGameUpdate();

        void onGameOver();

        void onLevelComplete();
    }

    /**
     * コンストラクタ
     */
    public Game() {
        initializeGame();
        setupGameTimer();
    }

    /**
     * ゲームの初期化
     */
    private void initializeGame() {
        // 迷路の作成
        maze = new Maze();

        // パックマンの作成
        Point pacmanStart = maze.getPacmanStartPosition();
        pacman = new Pacman(pacmanStart.x, pacmanStart.y);

        // ゴーストの作成
        ghosts = new ArrayList<>();
        List<Point> ghostPositions = maze.getGhostStartPositions();
        ghosts.add(new Blinky(ghostPositions.get(0).x, ghostPositions.get(0).y));
        ghosts.add(new Pinky(ghostPositions.get(1).x, ghostPositions.get(1).y));
        ghosts.add(new Inky(ghostPositions.get(2).x, ghostPositions.get(2).y));
        ghosts.add(new Clyde(ghostPositions.get(3).x, ghostPositions.get(3).y));

        // ゲーム状態の初期化
        state = GameState.READY;
        score = 0;
        level = 1;
        stateTimer = 0;
        powerPelletTimer = 0;
        ghostEatenMultiplier = 1;
    }

    /**
     * ゲームタイマーの設定
     */
    private void setupGameTimer() {
        gameTimer = new Timer(GAME_SPEED, (ActionEvent e) -> {
            updateGame();
            if (updateListener != null) {
                updateListener.onGameUpdate();
            }
        });
    }

    /**
     * ゲームの更新処理（メインゲームループ）
     */
    private void updateGame() {
        stateTimer++;

        switch (state) {
            case READY:
                handleReadyState();
                break;
            case PLAYING:
                handlePlayingState();
                break;
            case PACMAN_DIED:
                handlePacmanDiedState();
                break;
            case LEVEL_CLEAR:
                handleLevelClearState();
                break;
            case GAME_OVER:
                // ゲームオーバー時は更新停止
                break;
            case PAUSED:
                // 一時停止中は更新しない
                break;
        }
    }

    /**
     * READY状態の処理
     */
    private void handleReadyState() {
        if (stateTimer >= READY_STATE_DURATION) {
            state = GameState.PLAYING;
            stateTimer = 0;
        }
    }

    /**
     * PLAYING状態の処理
     */
    private void handlePlayingState() {
        // パックマンの更新
        pacman.update(maze);

        // ペレット消費のチェック
        int consumedTile = pacman.eatPellet(maze);
        if (consumedTile == Maze.PELLET) {
            score += PELLET_SCORE;
            incrementGhostDotCounters();
        } else if (consumedTile == Maze.POWER_PELLET) {
            score += POWER_PELLET_SCORE;
            startPowerPelletMode();
            incrementGhostDotCounters();
        }

        // ゴーストの更新
        updateGhosts();

        // Blinkyの速度調整（Elroyモード）
        if (ghosts.get(0) instanceof Blinky) {
            ((Blinky) ghosts.get(0)).checkElroyMode(maze.getRemainingPellets());
        }

        // 衝突判定
        checkCollisions();

        // パワーペレット効果のタイマー更新
        if (powerPelletTimer > 0) {
            powerPelletTimer--;
            if (powerPelletTimer == 0) {
                ghostEatenMultiplier = 1;
            }
        }

        // レベルクリアチェック
        if (maze.isAllPelletsConsumed()) {
            state = GameState.LEVEL_CLEAR;
            stateTimer = 0;
        }
    }

    /**
     * ゴーストの更新処理
     */
    private void updateGhosts() {
        // Inkyの特殊処理（Blinkyの位置を使用）
        for (int i = 0; i < ghosts.size(); i++) {
            Ghost ghost = ghosts.get(i);

            if (ghost instanceof Inky && ghost.getState() == Ghost.GhostState.CHASE) {
                // InkyはBlinkyの位置を使って目標を計算
                Point target = ((Inky) ghost).getChaseTargetWithBlinky(pacman, ghosts.get(0));
                // 本来はtargetTileを直接設定すべきだが、アクセス制限のため
                // Ghost基底クラスのupdate内で処理される
            }

            ghost.update(maze, pacman, ghosts);

            // 食べられたゴーストがゴーストハウスに到達したかチェック
            if (ghost.getState() == Ghost.GhostState.EATEN) {
                Point ghostPos = ghost.getGridPosition();
                if (ghostPos.x == 14 && ghostPos.y == 14) {
                    // ゴーストハウスに到達したらリスポーン
                    ghost.state = Ghost.GhostState.EXITING_HOUSE;
                    ghost.speed = Ghost.NORMAL_SPEED;
                }
            }
        }
    }

    /**
     * ゴーストのドットカウンターを増加
     */
    private void incrementGhostDotCounters() {
        for (Ghost ghost : ghosts) {
            ghost.incrementDotCounter();
        }
    }

    /**
     * パワーペレットモードの開始
     */
    private void startPowerPelletMode() {
        powerPelletTimer = 400; // 約6.7秒
        ghostEatenMultiplier = 1;

        for (Ghost ghost : ghosts) {
            ghost.startFrightened();
        }
    }

    /**
     * 衝突判定
     */
    private void checkCollisions() {
        Point pacmanPos = pacman.getGridPosition();

        for (Ghost ghost : ghosts) {
            Point ghostPos = ghost.getGridPosition();

            // 同じグリッドにいるかチェック
            if (pacmanPos.equals(ghostPos)) {
                if (ghost.getState() == Ghost.GhostState.FRIGHTENED) {
                    // ゴーストを食べる
                    ghost.setEaten();
                    score += GHOST_EATEN_BASE_SCORE * ghostEatenMultiplier;
                    ghostEatenMultiplier *= 2; // 200, 400, 800, 1600
                } else if (ghost.getState() != Ghost.GhostState.EATEN) {
                    // パックマンが捕まった
                    pacman.die();
                    state = GameState.PACMAN_DIED;
                    stateTimer = 0;
                }
            }
        }
    }

    /**
     * PACMAN_DIED状態の処理
     */
    private void handlePacmanDiedState() {
        if (stateTimer >= DEATH_ANIMATION_DURATION) {
            if (pacman.getLives() > 0) {
                // リスポーン
                resetPositions();
                state = GameState.READY;
                stateTimer = 0;
            } else {
                // ゲームオーバー
                state = GameState.GAME_OVER;
                gameTimer.stop();
                if (updateListener != null) {
                    updateListener.onGameOver();
                }
            }
        }
    }

    /**
     * LEVEL_CLEAR状態の処理
     */
    private void handleLevelClearState() {
        if (stateTimer >= 180) { // 3秒待機
            nextLevel();
        }
    }

    /**
     * 次のレベルへ進む
     */
    private void nextLevel() {
        level++;
        maze.reset();
        resetPositions();
        state = GameState.READY;
        stateTimer = 0;

        if (updateListener != null) {
            updateListener.onLevelComplete();
        }
    }

    /**
     * エンティティの位置をリセット
     */
    private void resetPositions() {
        // パックマンのリスポーン
        Point pacmanStart = maze.getPacmanStartPosition();
        pacman.respawn(pacmanStart.x, pacmanStart.y);

        // ゴーストのリセット
        List<Point> ghostPositions = maze.getGhostStartPositions();
        for (int i = 0; i < ghosts.size(); i++) {
            Ghost ghost = ghosts.get(i);
            ghost.x = ghostPositions.get(i).x;
            ghost.y = ghostPositions.get(i).y;
            ghost.state = (i == 0) ? Ghost.GhostState.SCATTER : Ghost.GhostState.IN_HOUSE;
            ghost.currentDirection = Direction.UP;
            ghost.stateTimer = 0;
            ghost.dotCounter = 0;
        }
    }

    /**
     * キー入力の処理
     */
    public void handleKeyPress(Direction direction) {
        if (state == GameState.PLAYING) {
            pacman.setRequestedDirection(direction);
        }
    }

    /**
     * ゲームの開始
     */
    public void start() {
        if (!gameTimer.isRunning()) {
            gameTimer.start();
        }
    }

    /**
     * ゲームの一時停止/再開
     */
    public void togglePause() {
        if (state == GameState.PLAYING) {
            state = GameState.PAUSED;
        } else if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
        }
    }

    /**
     * 新しいゲームの開始
     */
    public void newGame() {
        gameTimer.stop();
        initializeGame();
        gameTimer.start();
    }

    // ゲッターメソッド
    public GameState getState() {
        return state;
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return Math.max(score, highScore);
    }

    public int getLevel() {
        return level;
    }

    public Maze getMaze() {
        return maze;
    }

    public Pacman getPacman() {
        return pacman;
    }

    public List<Ghost> getGhosts() {
        return ghosts;
    }

    // リスナー設定
    public void setUpdateListener(GameUpdateListener listener) {
        this.updateListener = listener;
    }
}
