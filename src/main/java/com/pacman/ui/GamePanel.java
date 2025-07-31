package com.pacman.ui;

import com.pacman.game.*;
import com.pacman.model.*;
import com.pacman.effects.EffectManager;
import com.pacman.util.GameSettings;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * ゲーム画面を描画するパネルクラス
 * Swingを使用してゲームのビジュアル表現を管理
 * 拡張版：エフェクト、フルーツ、FPS表示などを追加
 */
public class GamePanel extends JPanel implements Game.GameUpdateListener {
    // ゲームインスタンス
    private Game game;
    private GameSettings settings;

    // 描画定数
    private static final int TILE_SIZE = 20;
    private static final int PANEL_WIDTH = Maze.WIDTH * TILE_SIZE;
    private static final int PANEL_HEIGHT = (Maze.HEIGHT + 3) * TILE_SIZE; // スコア表示用の余白

    // 色定数
    private static final Color WALL_COLOR = new Color(33, 33, 222);
    private static final Color PELLET_COLOR = new Color(255, 255, 255);
    private static final Color BACKGROUND_COLOR = Color.BLACK;

    // フォント
    private static final Font SCORE_FONT = new Font("Arial", Font.BOLD, 16);
    private static final Font READY_FONT = new Font("Arial", Font.BOLD, 24);
    private static final Font GAME_OVER_FONT = new Font("Arial", Font.BOLD, 32);
    private static final Font FPS_FONT = new Font("Arial", Font.PLAIN, 12);

    // アニメーション用
    private float wallPulseAnimation = 0;
    private boolean levelClearFlash = false;

    /**
     * コンストラクタ
     */
    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(BACKGROUND_COLOR);
        setFocusable(true);

        // 設定の取得
        settings = GameSettings.getInstance();

        // ゲームインスタンスの作成
        game = new Game();
        game.setUpdateListener(this);

        // キー入力の設定
        setupKeyboardInput();

        // ダブルバッファリング有効化
        setDoubleBuffered(true);
    }

    /**
     * キーボード入力の設定
     */
    private void setupKeyboardInput() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();

                // カスタムキー設定の対応
                if (keyCode == settings.getKeyUp()) {
                    game.handleKeyPress(Direction.UP);
                } else if (keyCode == settings.getKeyDown()) {
                    game.handleKeyPress(Direction.DOWN);
                } else if (keyCode == settings.getKeyLeft()) {
                    game.handleKeyPress(Direction.LEFT);
                } else if (keyCode == settings.getKeyRight()) {
                    game.handleKeyPress(Direction.RIGHT);
                } else {
                    // その他の特殊キー
                    switch (keyCode) {
                        case KeyEvent.VK_SPACE:
                            if (game.getState() == GameState.GAME_OVER) {
                                game.newGame();
                            }
                            break;
                        case KeyEvent.VK_P:
                            game.togglePause();
                            break;
                        case KeyEvent.VK_ESCAPE:
                            game.togglePause();
                            break;
                    }
                }
            }
        });
    }

    /**
     * ゲームの開始
     */
    public void startGame() {
        game.start();
        requestFocus();
    }

    /**
     * ゲームインスタンスの取得
     */
    public Game getGame() {
        return game;
    }

    /**
     * 描画処理
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // アンチエイリアシング有効化
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // アニメーション更新
        updateAnimations();

        // 迷路の描画
        drawMaze(g2d);

        // フルーツの描画
        drawFruit(g2d);

        // パックマンの描画
        drawPacman(g2d);

        // ゴーストの描画
        drawGhosts(g2d);

        // エフェクトの描画
        game.getEffectManager().render(g2d);

        // UI要素の描画
        drawUI(g2d);

        // FPS表示
        if (settings.isShowFPS()) {
            drawFPS(g2d);
        }

        // 状態に応じたオーバーレイ
        drawStateOverlay(g2d);
    }

    /**
     * アニメーションの更新
     */
    private void updateAnimations() {
        // 壁のパルスアニメーション（パワーペレット取得時）
        wallPulseAnimation = (float) (Math.sin(System.currentTimeMillis() * 0.005) * 0.5 + 0.5);

        // レベルクリア時のフラッシュ
        if (game.getState() == GameState.LEVEL_CLEAR) {
            levelClearFlash = (System.currentTimeMillis() / 200) % 2 == 0;
        } else {
            levelClearFlash = false;
        }
    }

    /**
     * 迷路の描画
     */
    private void drawMaze(Graphics2D g) {
        Maze maze = game.getMaze();

        for (int y = 0; y < Maze.HEIGHT; y++) {
            for (int x = 0; x < Maze.WIDTH; x++) {
                int tile = maze.getTile(x, y);
                int screenX = x * TILE_SIZE;
                int screenY = y * TILE_SIZE + TILE_SIZE * 2; // スコア表示分のオフセット

                switch (tile) {
                    case Maze.WALL:
                        drawWall(g, screenX, screenY);
                        break;
                    case Maze.PELLET:
                        drawPellet(g, screenX, screenY, false);
                        break;
                    case Maze.POWER_PELLET:
                        drawPellet(g, screenX, screenY, true);
                        break;
                    case Maze.GHOST_HOUSE_DOOR:
                        drawGhostHouseDoor(g, screenX, screenY);
                        break;
                }
            }
        }
    }

    /**
     * 壁の描画（改良版）
     */
    private void drawWall(Graphics2D g, int x, int y) {
        Color wallColor = WALL_COLOR;

        // レベルクリア時のフラッシュ効果
        if (levelClearFlash) {
            wallColor = Color.WHITE;
        }

        g.setColor(wallColor);
        g.fillRect(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4);

        // 簡単な3D効果
        g.setColor(wallColor.brighter());
        g.drawLine(x + 2, y + 2, x + TILE_SIZE - 3, y + 2);
        g.drawLine(x + 2, y + 2, x + 2, y + TILE_SIZE - 3);

        // 影効果
        g.setColor(wallColor.darker());
        g.drawLine(x + TILE_SIZE - 3, y + 3, x + TILE_SIZE - 3, y + TILE_SIZE - 3);
        g.drawLine(x + 3, y + TILE_SIZE - 3, x + TILE_SIZE - 3, y + TILE_SIZE - 3);
    }

    /**
     * ペレットの描画
     */
    private void drawPellet(Graphics2D g, int x, int y, boolean isPowerPellet) {
        g.setColor(PELLET_COLOR);

        if (isPowerPellet) {
            // パワーペレット（大きく点滅）
            int size = 12 + (int) (Math.sin(System.currentTimeMillis() * 0.005) * 2);

            // グロー効果
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g.setColor(Color.YELLOW);
            g.fillOval(x + TILE_SIZE / 2 - size, y + TILE_SIZE / 2 - size, size * 2, size * 2);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g.setColor(PELLET_COLOR);
            g.fillOval(x + TILE_SIZE / 2 - size / 2, y + TILE_SIZE / 2 - size / 2, size, size);
        } else {
            // 通常ペレット
            g.fillOval(x + TILE_SIZE / 2 - 2, y + TILE_SIZE / 2 - 2, 4, 4);
        }
    }

    /**
     * ゴーストハウスのドア描画
     */
    private void drawGhostHouseDoor(Graphics2D g, int x, int y) {
        g.setColor(Color.PINK);
        g.fillRect(x + 4, y + TILE_SIZE / 2 - 2, TILE_SIZE - 8, 4);
    }

    /**
     * フルーツの描画
     */
    private void drawFruit(Graphics2D g) {
        Fruit fruit = game.getFruit();
        if (fruit != null && fruit.isVisible()) {
            fruit.render(g, TILE_SIZE);
        }
    }

    /**
     * パックマンの描画（改良版）
     */
    private void drawPacman(Graphics2D g) {
        Pacman pacman = game.getPacman();
        if (!pacman.isAlive() && game.getState() == GameState.PACMAN_DIED) {
            // 死亡アニメーション
            drawPacmanDeath(g, pacman);
        } else {
            // 通常の描画
            int x = (int) (pacman.getX() * TILE_SIZE);
            int y = (int) (pacman.getY() * TILE_SIZE) + TILE_SIZE * 2;

            // 影の描画
            g.setColor(new Color(0, 0, 0, 50));
            g.fillOval(x + 2, y + TILE_SIZE - 4, TILE_SIZE - 4, 6);

            g.setColor(Color.YELLOW);

            // 口の開閉アニメーション
            int mouthAngle = 45 - (pacman.getAnimationFrame() * 15);
            int startAngle = getStartAngle(pacman.getCurrentDirection(), mouthAngle);

            g.fillArc(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4,
                    startAngle, 360 - (mouthAngle * 2));

            // ハイライト効果
            g.setColor(new Color(255, 255, 200));
            g.fillArc(x + 4, y + 4, TILE_SIZE / 2, TILE_SIZE / 2,
                    startAngle + 20, 40);
        }
    }

    /**
     * パックマンの死亡アニメーション
     */
    private void drawPacmanDeath(Graphics2D g, Pacman pacman) {
        int x = (int) (pacman.getX() * TILE_SIZE);
        int y = (int) (pacman.getY() * TILE_SIZE) + TILE_SIZE * 2;

        g.setColor(Color.YELLOW);

        // 徐々に消えていくアニメーション
        int animProgress = Math.min(90, (int) (System.currentTimeMillis() % 1000) / 11);
        g.fillArc(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4,
                90 - animProgress, 360 - (animProgress * 2));
    }

    /**
     * 方向に応じた口の開始角度を計算
     */
    private int getStartAngle(Direction direction, int mouthAngle) {
        switch (direction) {
            case RIGHT:
                return mouthAngle;
            case UP:
                return 90 + mouthAngle;
            case LEFT:
                return 180 + mouthAngle;
            case DOWN:
                return 270 + mouthAngle;
            default:
                return mouthAngle;
        }
    }

    /**
     * ゴーストの描画（改良版）
     */
    private void drawGhosts(Graphics2D g) {
        List<Ghost> ghosts = game.getGhosts();

        for (Ghost ghost : ghosts) {
            int x = (int) (ghost.getX() * TILE_SIZE);
            int y = (int) (ghost.getY() * TILE_SIZE) + TILE_SIZE * 2;

            // 影の描画
            g.setColor(new Color(0, 0, 0, 50));
            g.fillOval(x + 2, y + TILE_SIZE - 4, TILE_SIZE - 4, 6);

            if (ghost.getState() == Ghost.GhostState.EATEN) {
                // 目玉だけ描画
                drawGhostEyes(g, x, y);
            } else {
                // ゴースト本体
                Color ghostColor = ghost.getCurrentColor();

                // 怯えモードの残り時間が少ない時の白色点滅を強調
                if (ghost.getState() == Ghost.GhostState.FRIGHTENED &&
                        ghostColor == Color.WHITE) {
                    // 白く光る効果
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                }

                g.setColor(ghostColor);

                // 体
                g.fillArc(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE / 2 - 2, 0, 180);
                g.fillRect(x + 2, y + TILE_SIZE / 2, TILE_SIZE - 4, TILE_SIZE / 2 - 2);

                // 波打つ下部
                for (int i = 0; i < 3; i++) {
                    int waveX = x + 3 + i * 5;
                    int waveY = y + TILE_SIZE - 6;
                    // アニメーション効果
                    waveY += Math.sin((System.currentTimeMillis() * 0.01 + i * 30)) * 2;
                    g.fillArc(waveX, waveY, 5, 6, 0, 180);
                }

                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                // 目
                if (ghost.getState() != Ghost.GhostState.FRIGHTENED) {
                    drawGhostEyes(g, x, y);
                } else {
                    // 怯えモードの目
                    g.setColor(Color.WHITE);
                    g.fillRect(x + 5, y + 6, 3, 3);
                    g.fillRect(x + 12, y + 6, 3, 3);
                }
            }
        }
    }

    /**
     * ゴーストの目を描画
     */
    private void drawGhostEyes(Graphics2D g, int x, int y) {
        // 白目
        g.setColor(Color.WHITE);
        g.fillOval(x + 4, y + 5, 6, 6);
        g.fillOval(x + 10, y + 5, 6, 6);

        // 瞳
        g.setColor(Color.BLUE);
        g.fillOval(x + 6, y + 7, 3, 3);
        g.fillOval(x + 12, y + 7, 3, 3);
    }

    /**
     * UI要素の描画
     */
    private void drawUI(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(SCORE_FONT);

        // スコア
        g.drawString("SCORE: " + game.getScore(), 10, 25);

        // ハイスコア
        g.drawString("HIGH: " + game.getHighScore(), PANEL_WIDTH / 2 - 40, 25);

        // レベル
        g.drawString("LEVEL: " + game.getLevel(), PANEL_WIDTH - 100, 25);

        // 残機（パックマンアイコンで表示）
        g.drawString("LIVES: ", 10, 45);
        for (int i = 0; i < game.getPacman().getLives(); i++) {
            int lifeX = 70 + i * 25;
            int lifeY = 35;
            g.setColor(Color.YELLOW);
            g.fillArc(lifeX, lifeY, 15, 15, 30, 300);
        }

        // パワーペレット効果の残り時間バー（表示中のみ）
        drawPowerPelletTimer(g);
    }

    /**
     * パワーペレット効果の残り時間表示
     */
    private void drawPowerPelletTimer(Graphics2D g) {
        // 実装には内部タイマーへのアクセスが必要
        // 現在は省略
    }

    /**
     * FPS表示
     */
    private void drawFPS(Graphics2D g) {
        g.setColor(Color.GREEN);
        g.setFont(FPS_FONT);
        g.drawString("FPS: " + game.getCurrentFPS(), PANEL_WIDTH - 60, PANEL_HEIGHT - 10);
    }

    /**
     * 状態に応じたオーバーレイ描画
     */
    private void drawStateOverlay(Graphics2D g) {
        switch (game.getState()) {
            case READY:
                drawReadyMessage(g);
                break;
            case PAUSED:
                drawPausedMessage(g);
                break;
            case GAME_OVER:
                drawGameOverMessage(g);
                break;
            case LEVEL_CLEAR:
                drawLevelClearMessage(g);
                break;
        }
    }

    /**
     * READY メッセージ
     */
    private void drawReadyMessage(Graphics2D g) {
        // 半透明の背景
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRect(0, PANEL_HEIGHT / 2 - 30, PANEL_WIDTH, 60);

        g.setColor(Color.YELLOW);
        g.setFont(READY_FONT);
        String message = "READY!";
        FontMetrics fm = g.getFontMetrics();
        int x = (PANEL_WIDTH - fm.stringWidth(message)) / 2;
        int y = PANEL_HEIGHT / 2;
        g.drawString(message, x, y);
    }

    /**
     * PAUSED メッセージ
     */
    private void drawPausedMessage(Graphics2D g) {
        // 半透明の背景
        g.setColor(new Color(0, 0, 0, 192));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        g.setColor(Color.YELLOW);
        g.setFont(READY_FONT);
        String message = "PAUSED";
        FontMetrics fm = g.getFontMetrics();
        int x = (PANEL_WIDTH - fm.stringWidth(message)) / 2;
        int y = PANEL_HEIGHT / 2;
        g.drawString(message, x, y);

        // サブメッセージ
        g.setFont(SCORE_FONT);
        g.setColor(Color.WHITE);
        String submsg = "Press P to resume";
        x = (PANEL_WIDTH - g.getFontMetrics().stringWidth(submsg)) / 2;
        g.drawString(submsg, x, y + 30);
    }

    /**
     * GAME OVER メッセージ
     */
    private void drawGameOverMessage(Graphics2D g) {
        // 暗い背景
        g.setColor(new Color(0, 0, 0, 192));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        g.setColor(Color.RED);
        g.setFont(GAME_OVER_FONT);
        String message = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        int x = (PANEL_WIDTH - fm.stringWidth(message)) / 2;
        int y = PANEL_HEIGHT / 2;
        g.drawString(message, x, y);

        g.setFont(SCORE_FONT);
        g.setColor(Color.WHITE);
        String restart = "Press SPACE to restart";
        x = (PANEL_WIDTH - g.getFontMetrics().stringWidth(restart)) / 2;
        g.drawString(restart, x, y + 40);

        // 最終スコア
        String finalScore = "Final Score: " + game.getScore();
        x = (PANEL_WIDTH - g.getFontMetrics().stringWidth(finalScore)) / 2;
        g.drawString(finalScore, x, y + 70);
    }

    /**
     * LEVEL CLEAR メッセージ
     */
    private void drawLevelClearMessage(Graphics2D g) {
        // アニメーション効果のある背景
        float alpha = (float) (Math.sin(System.currentTimeMillis() * 0.005) * 0.2 + 0.3);
        g.setColor(new Color(0, 0, 0, (int) (alpha * 255)));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        // 虹色のテキスト効果
        g.setFont(READY_FONT);
        String message = "LEVEL CLEAR!";
        FontMetrics fm = g.getFontMetrics();
        int x = (PANEL_WIDTH - fm.stringWidth(message)) / 2;
        int y = PANEL_HEIGHT / 2;

        // グラデーション効果
        for (int i = 0; i < message.length(); i++) {
            float hue = (float) ((System.currentTimeMillis() * 0.001 + i * 0.1) % 1.0);
            g.setColor(Color.getHSBColor(hue, 1.0f, 1.0f));
            g.drawString(message.substring(i, i + 1),
                    x + fm.stringWidth(message.substring(0, i)), y);
        }
    }

    // Game.GameUpdateListener の実装
    @Override
    public void onGameUpdate() {
        repaint();
    }

    @Override
    public void onGameOver() {
        repaint();
    }

    @Override
    public void onLevelComplete() {
        repaint();
    }

    @Override
    public void onHighScore(int rank) {
        // ハイスコア達成時の処理（後でダイアログ表示を追加）
        JOptionPane.showMessageDialog(this,
                "Congratulations! You achieved rank #" + rank + " on the high score list!",
                "New High Score!",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void onAchievementUnlocked(String achievement) {
        // 実績解除時の処理（後で通知システムを追加）
        System.out.println("Achievement Unlocked: " + achievement);
    }
}