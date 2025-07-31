package main.java.com.pacman.ui;

import main.java.com.pacman.game.*;
import main.java.com.pacman.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * ゲーム画面を描画するパネルクラス
 * Swingを使用してゲームのビジュアル表現を管理
 */
public class GamePanel extends JPanel implements Game.GameUpdateListener {
    // ゲームインスタンス
    private Game game;

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

    /**
     * コンストラクタ
     */
    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(BACKGROUND_COLOR);
        setFocusable(true);

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
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                        game.handleKeyPress(Direction.UP);
                        break;
                    case KeyEvent.VK_DOWN:
                        game.handleKeyPress(Direction.DOWN);
                        break;
                    case KeyEvent.VK_LEFT:
                        game.handleKeyPress(Direction.LEFT);
                        break;
                    case KeyEvent.VK_RIGHT:
                        game.handleKeyPress(Direction.RIGHT);
                        break;
                    case KeyEvent.VK_SPACE:
                        if (game.getState() == GameState.GAME_OVER) {
                            game.newGame();
                        }
                        break;
                    case KeyEvent.VK_P:
                        game.togglePause();
                        break;
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
     * 描画処理
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // アンチエイリアシング有効化
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // 迷路の描画
        drawMaze(g2d);

        // パックマンの描画
        drawPacman(g2d);

        // ゴーストの描画
        drawGhosts(g2d);

        // UI要素の描画
        drawUI(g2d);

        // 状態に応じたオーバーレイ
        drawStateOverlay(g2d);
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
     * 壁の描画
     */
    private void drawWall(Graphics2D g, int x, int y) {
        g.setColor(WALL_COLOR);
        g.fillRect(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4);

        // 簡単な3D効果
        g.setColor(WALL_COLOR.brighter());
        g.drawLine(x + 2, y + 2, x + TILE_SIZE - 3, y + 2);
        g.drawLine(x + 2, y + 2, x + 2, y + TILE_SIZE - 3);
    }

    /**
     * ペレットの描画
     */
    private void drawPellet(Graphics2D g, int x, int y, boolean isPowerPellet) {
        g.setColor(PELLET_COLOR);

        if (isPowerPellet) {
            // パワーペレット（大きく点滅）
            int size = 12 + (int) (Math.sin(System.currentTimeMillis() * 0.005) * 2);
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
     * パックマンの描画
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

            g.setColor(Color.YELLOW);

            // 口の開閉アニメーション
            int mouthAngle = 45 - (pacman.getAnimationFrame() * 15);
            int startAngle = getStartAngle(pacman.getCurrentDirection(), mouthAngle);

            g.fillArc(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4,
                    startAngle, 360 - (mouthAngle * 2));
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
     * ゴーストの描画
     */
    private void drawGhosts(Graphics2D g) {
        List<Ghost> ghosts = game.getGhosts();

        for (Ghost ghost : ghosts) {
            int x = (int) (ghost.getX() * TILE_SIZE);
            int y = (int) (ghost.getY() * TILE_SIZE) + TILE_SIZE * 2;

            if (ghost.getState() == Ghost.GhostState.EATEN) {
                // 目玉だけ描画
                drawGhostEyes(g, x, y);
            } else {
                // ゴースト本体
                g.setColor(ghost.getCurrentColor());

                // 体
                g.fillArc(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE / 2 - 2, 0, 180);
                g.fillRect(x + 2, y + TILE_SIZE / 2, TILE_SIZE - 4, TILE_SIZE / 2 - 2);

                // 波打つ下部
                for (int i = 0; i < 3; i++) {
                    int waveX = x + 3 + i * 5;
                    g.fillArc(waveX, y + TILE_SIZE - 6, 5, 6, 0, 180);
                }

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

        // 残機
        g.drawString("LIVES: " + game.getPacman().getLives(), 10, 45);
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
            default:
                // 他の状態は特に描画しない
                break;
        }
    }

    /**
     * READY メッセージ
     */
    private void drawReadyMessage(Graphics2D g) {
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
        g.setColor(Color.YELLOW);
        g.setFont(READY_FONT);
        String message = "PAUSED";
        FontMetrics fm = g.getFontMetrics();
        int x = (PANEL_WIDTH - fm.stringWidth(message)) / 2;
        int y = PANEL_HEIGHT / 2;
        g.drawString(message, x, y);
    }

    /**
     * GAME OVER メッセージ
     */
    private void drawGameOverMessage(Graphics2D g) {
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
    }

    /**
     * LEVEL CLEAR メッセージ
     */
    private void drawLevelClearMessage(Graphics2D g) {
        g.setColor(Color.GREEN);
        g.setFont(READY_FONT);
        String message = "LEVEL CLEAR!";
        FontMetrics fm = g.getFontMetrics();
        int x = (PANEL_WIDTH - fm.stringWidth(message)) / 2;
        int y = PANEL_HEIGHT / 2;
        g.drawString(message, x, y);
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
}