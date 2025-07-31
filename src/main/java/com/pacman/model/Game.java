package main.java.com.pacman.model;

import main.java.com.pacman.model.*;
import main.java.com.pacman.sound.SoundManager;
import main.java.com.pacman.effects.EffectManager;
import main.java.com.pacman.game.Direction;
import main.java.com.pacman.game.GameState;
import main.java.com.pacman.util.*;
import javax.swing.Timer;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * ゲーム全体のロジックを管理するクラス
 * ゲームループ、衝突判定、スコア管理、状態遷移などを担当
 * 拡張版：サウンド、エフェクト、フルーツ、統計機能を統合
 */
public class Game {
    // ゲームエンティティ
    private Maze maze;
    private Pacman pacman;
    private List<Ghost> ghosts;
    private Fruit fruit;

    // マネージャー
    private SoundManager soundManager;
    private EffectManager effectManager;
    private HighScoreManager highScoreManager;
    private GameSettings settings;
    private GameStatistics statistics;

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
    private int pelletsEatenThisLevel;
    private int consecutiveGhostsEaten;

    // FPS計測用
    private long lastFrameTime;
    private int frameCount;
    private int currentFPS;

    // 定数
    private static final int GAME_SPEED = 16; // 約60FPS
    private static final int PELLET_SCORE = 10;
    private static final int POWER_PELLET_SCORE = 50;
    private static final int GHOST_EATEN_BASE_SCORE = 200;
    private static final int READY_STATE_DURATION = 180; // 3秒
    private static final int DEATH_ANIMATION_DURATION = 120; // 2秒
    private static final int FRUIT_SPAWN_PELLET_COUNT = 70; // 70個目と170個目で出現

    // ゲーム更新リスナー（UIへの通知用）
    private GameUpdateListener updateListener;

    /**
     * ゲーム更新リスナーインターフェース
     */
    public interface GameUpdateListener {
        void onGameUpdate();

        void onGameOver();

        void onLevelComplete();

        void onHighScore(int rank);

        void onAchievementUnlocked(String achievement);
    }

    /**
     * コンストラクタ
     */
    public Game() {
        // マネージャーの初期化
        soundManager = SoundManager.getInstance();
        effectManager = new EffectManager();
        highScoreManager = HighScoreManager.getInstance();
        settings = GameSettings.getInstance();
        statistics = GameStatistics.getInstance();

        // 仮想サウンドの生成（実際のサウンドファイルがない場合）
        soundManager.generateVirtualSounds();

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

        // 難易度設定の適用
        GameSettings.Difficulty difficulty = settings.getDifficulty();
        pacman.setLives(difficulty.getStartingLives());

        // ゴーストの作成
        ghosts = new ArrayList<>();
        List<Point> ghostPositions = maze.getGhostStartPositions();
        ghosts.add(new Blinky(ghostPositions.get(0).x, ghostPositions.get(0).y));
        ghosts.add(new Pinky(ghostPositions.get(1).x, ghostPositions.get(1).y));
        ghosts.add(new Inky(ghostPositions.get(2).x, ghostPositions.get(2).y));
        ghosts.add(new Clyde(ghostPositions.get(3).x, ghostPositions.get(3).y));

        // ゴースト速度の調整
        float speedMultiplier = difficulty.getSpeedMultiplier();
        for (Ghost ghost : ghosts) {
            ghost.speed *= speedMultiplier;
        }

        // フルーツの作成
        fruit = new Fruit();

        // ゲーム状態の初期化
        state = GameState.READY;
        score = 0;
        highScore = highScoreManager.getTopScore();
        level = 1;
        stateTimer = 0;
        powerPelletTimer = 0;
        ghostEatenMultiplier = 1;
        pelletsEatenThisLevel = 0;
        consecutiveGhostsEaten = 0;

        // エフェクトのクリア
        effectManager.clear();

        // 統計セッションの開始
        statistics.startGameSession();
    }

    /**
     * ゲームタイマーの設定
     */
    private void setupGameTimer() {
        lastFrameTime = System.currentTimeMillis();
        frameCount = 0;

        gameTimer = new Timer(GAME_SPEED, (ActionEvent e) -> {
            updateGame();
            updateFPS();

            if (updateListener != null) {
                updateListener.onGameUpdate();
            }
        });
    }

    /**
     * FPSの更新
     */
    private void updateFPS() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= 1000) {
            currentFPS = frameCount;
            frameCount = 0;
            lastFrameTime = currentTime;
        }
    }

    /**
     * ゲームの更新処理（メインゲームループ）
     */
    private void updateGame() {
        stateTimer++;

        // エフェクトの更新（すべての状態で更新）
        effectManager.update();

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
        if (stateTimer == 1) {
            // ゲーム開始音
            soundManager.playSound(SoundManager.SoundType.GAME_START);
            effectManager.startFadeIn();
        }

        if (stateTimer >= READY_STATE_DURATION) {
            state = GameState.PLAYING;
            stateTimer = 0;
            // BGM開始
            soundManager.playBGM(SoundManager.SoundType.SIREN);
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
            pelletsEatenThisLevel++;
            incrementGhostDotCounters();

            // 効果音
            soundManager.playSound(SoundManager.SoundType.PELLET_EAT);
            statistics.recordPelletEaten(false);

            // フルーツ出現チェック
            checkFruitSpawn();

        } else if (consumedTile == Maze.POWER_PELLET) {
            score += POWER_PELLET_SCORE;
            pelletsEatenThisLevel++;
            startPowerPelletMode();
            incrementGhostDotCounters();

            // 効果音とエフェクト
            soundManager.playSound(SoundManager.SoundType.POWER_PELLET);
            if (settings.isParticleEffectsEnabled()) {
                effectManager.createPowerPelletEffect(
                        (float) (pacman.getX() * 20),
                        (float) (pacman.getY() * 20 + 40));
            }
            statistics.recordPelletEaten(true);
        }

        // フルーツの更新と衝突判定
        updateFruit();

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
                consecutiveGhostsEaten = 0;
                // BGMを通常に戻す
                soundManager.playBGM(SoundManager.SoundType.SIREN);
            }
        }

        // レベルクリアチェック
        if (maze.isAllPelletsConsumed()) {
            state = GameState.LEVEL_CLEAR;
            stateTimer = 0;
            soundManager.stopBGM();
            soundManager.playSound(SoundManager.SoundType.LEVEL_CLEAR);

            // レベルクリアエフェクト
            if (settings.isParticleEffectsEnabled()) {
                effectManager.createLevelClearEffect(
                        Maze.WIDTH * 10,
                        Maze.HEIGHT * 10);
            }
        }

        // ハイスコア更新チェック
        if (score > highScore) {
            highScore = score;
        }
    }

    /**
     * フルーツの出現チェック
     */
    private void checkFruitSpawn() {
        if ((pelletsEatenThisLevel == FRUIT_SPAWN_PELLET_COUNT ||
                pelletsEatenThisLevel == FRUIT_SPAWN_PELLET_COUNT * 2) &&
                !fruit.isVisible()) {
            // 迷路の中央付近に出現
            fruit.spawn(level, 14, 20);
        }
    }

    /**
     * フルーツの更新と衝突判定
     */
    private void updateFruit() {
        fruit.update();

        if (fruit.isVisible() && fruit.checkCollision(
                (int) Math.round(pacman.getX()),
                (int) Math.round(pacman.getY()))) {

            int fruitScore = fruit.collect();
            score += fruitScore;

            // エフェクトと効果音
            soundManager.playSound(SoundManager.SoundType.EXTRA_LIFE);
            effectManager.addScorePopup(
                    fruit.getX() * 20,
                    fruit.getY() * 20 + 40,
                    fruitScore);

            statistics.recordFruitCollected(fruit.getType().getName());
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
            }

            ghost.update(maze, pacman, ghosts);

            // 食べられたゴーストがゴーストハウスに到達したかチェック
            if (ghost.getState() == Ghost.GhostState.EATEN) {
                Point ghostPos = ghost.getGridPosition();
                if (ghostPos.x == 14 && ghostPos.y == 14) {
                    // ゴーストハウスに到達したらリスポーン
                    ghost.state = Ghost.GhostState.EXITING_HOUSE;
                    ghost.speed = Ghost.NORMAL_SPEED * settings.getDifficulty().getSpeedMultiplier();
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
        consecutiveGhostsEaten = 0;

        for (Ghost ghost : ghosts) {
            ghost.startFrightened();
        }

        // BGM切り替え
        soundManager.playBGM(SoundManager.SoundType.POWER_MODE);
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
                    int ghostScore = GHOST_EATEN_BASE_SCORE * ghostEatenMultiplier;
                    score += ghostScore;
                    ghostEatenMultiplier *= 2; // 200, 400, 800, 1600
                    consecutiveGhostsEaten++;

                    // エフェクトと効果音
                    soundManager.playSound(SoundManager.SoundType.GHOST_EAT);
                    effectManager.addScorePopup(
                            (float) (ghost.getX() * 20),
                            (float) (ghost.getY() * 20 + 40),
                            ghostScore);

                    if (settings.isParticleEffectsEnabled()) {
                        effectManager.createGhostEatenEffect(
                                (float) (ghost.getX() * 20),
                                (float) (ghost.getY() * 20 + 40));
                    }

                    statistics.recordGhostEaten();

                    // 4体連続で食べた場合の実績
                    if (consecutiveGhostsEaten == 4) {
                        // Ghost Combo実績のチェック（GameStatistics内で処理）
                    }

                } else if (ghost.getState() != Ghost.GhostState.EATEN) {
                    // パックマンが捕まった
                    pacman.die();
                    state = GameState.PACMAN_DIED;
                    stateTimer = 0;

                    // 効果音
                    soundManager.stopBGM();
                    soundManager.playSound(SoundManager.SoundType.PACMAN_DEATH);
                    statistics.recordDeath();
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
                handleGameOver();
            }
        }
    }

    /**
     * ゲームオーバー処理
     */
    private void handleGameOver() {
        state = GameState.GAME_OVER;
        gameTimer.stop();
        soundManager.stopBGM();

        // 統計の記録
        statistics.endGameSession(false, score, level);

        // ハイスコアチェック
        if (highScoreManager.isHighScore(score)) {
            String playerName = settings.getPlayerName();
            int rank = highScoreManager.addScore(playerName, score, level);

            if (updateListener != null && rank > 0) {
                updateListener.onHighScore(rank);
            }
        }

        if (updateListener != null) {
            updateListener.onGameOver();
        }

        // フェードアウト効果
        effectManager.startFadeOut();
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
        pelletsEatenThisLevel = 0;
        state = GameState.READY;
        stateTimer = 0;

        // ゴースト速度の増加（レベルが上がるごとに少しずつ速くなる）
        float levelSpeedBonus = 1.0f + (level - 1) * 0.02f;
        for (Ghost ghost : ghosts) {
            ghost.speed = Ghost.NORMAL_SPEED * settings.getDifficulty().getSpeedMultiplier() * levelSpeedBonus;
        }

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

        // フルーツのリセット
        fruit = new Fruit();

        // エフェクトのクリア
        effectManager.clear();
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
            soundManager.stopBGM();
        } else if (state == GameState.PAUSED) {
            state = GameState.PLAYING;
            if (powerPelletTimer > 0) {
                soundManager.playBGM(SoundManager.SoundType.POWER_MODE);
            } else {
                soundManager.playBGM(SoundManager.SoundType.SIREN);
            }
        }
    }

    /**
     * 新しいゲームの開始
     */
    public void newGame() {
        gameTimer.stop();

        // 前回のゲーム終了処理
        if (state == GameState.PLAYING) {
            statistics.endGameSession(false, score, level);
        }

        soundManager.stopAllSounds();
        effectManager.clear();

        initializeGame();
        gameTimer.start();
    }

    /**
     * ゲームの終了処理
     */
    public void dispose() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }

        // 統計の保存
        if (state == GameState.PLAYING) {
            statistics.endGameSession(false, score, level);
        }

        soundManager.dispose();
    }

    // ゲッターメソッド
    public GameState getState() {
        return state;
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    public int getLevel() {
        return level;
    }

    public int getCurrentFPS() {
        return currentFPS;
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

    public Fruit getFruit() {
        return fruit;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    // リスナー設定
    public void setUpdateListener(GameUpdateListener listener) {
        this.updateListener = listener;
    }
}