package com.pacman.ui;

import com.pacman.ui.dialogs.*;
import com.pacman.util.GameSettings;
import com.pacman.util.GameStatistics;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * ゲームのメインウィンドウクラス
 * JFrameを継承し、ゲーム全体のウィンドウを管理
 * 拡張版：設定、ハイスコア、統計などのダイアログを統合
 */
public class GameWindow extends JFrame {

    private GamePanel gamePanel;
    private GameSettings settings;

    // メニューアイテム（状態更新用）
    private JCheckBoxMenuItem soundMenuItem;
    private JCheckBoxMenuItem fpsMenuItem;
    private JCheckBoxMenuItem particlesMenuItem;

    /**
     * コンストラクタ
     */
    public GameWindow() {
        settings = GameSettings.getInstance();

        initializeWindow();
        createMenuBar();
        initializeGamePanel();
        applySettings();

        // 初回起動時の説明を表示
        GameStatistics statistics = GameStatistics.getInstance();
        if (statistics.getTotalGamesPlayed() == 0) {
            showInstructions();
        }
    }

    /**
     * ウィンドウの初期設定
     */
    private void initializeWindow() {
        setTitle("Classic Pacman - Java Edition");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        // ウィンドウアイコンの設定
        try {
            Image icon = createPacmanIcon();
            setIconImage(icon);
        } catch (Exception e) {
            // アイコン設定失敗時は無視
        }

        // ウィンドウが閉じられる時の処理
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }

    /**
     * メニューバーの作成
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // ゲームメニュー
        menuBar.add(createGameMenu());

        // オプションメニュー
        menuBar.add(createOptionsMenu());

        // 統計メニュー
        menuBar.add(createStatsMenu());

        // ヘルプメニュー
        menuBar.add(createHelpMenu());

        setJMenuBar(menuBar);
    }

    /**
     * ゲームメニューの作成
     */
    private JMenu createGameMenu() {
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic('G');

        // 新しいゲーム
        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        newGameItem.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "Start a new game? Current progress will be lost.",
                    "New Game",
                    JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                gamePanel.getGame().newGame();
                gamePanel.requestFocus();
            }
        });
        gameMenu.add(newGameItem);

        // 一時停止
        JMenuItem pauseItem = new JMenuItem("Pause/Resume");
        pauseItem.setAccelerator(KeyStroke.getKeyStroke("P"));
        pauseItem.addActionListener(e -> {
            gamePanel.getGame().togglePause();
            gamePanel.requestFocus();
        });
        gameMenu.add(pauseItem);

        gameMenu.addSeparator();

        // ハイスコア
        JMenuItem highScoresItem = new JMenuItem("High Scores...");
        highScoresItem.setAccelerator(KeyStroke.getKeyStroke("ctrl H"));
        highScoresItem.addActionListener(e -> showHighScores());
        gameMenu.add(highScoresItem);

        gameMenu.addSeparator();

        // 終了
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        exitItem.addActionListener(e -> confirmExit());
        gameMenu.add(exitItem);

        return gameMenu;
    }

    /**
     * オプションメニューの作成
     */
    private JMenu createOptionsMenu() {
        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setMnemonic('O');

        // 設定
        JMenuItem settingsItem = new JMenuItem("Settings...");
        settingsItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        settingsItem.addActionListener(e -> showSettings());
        optionsMenu.add(settingsItem);

        optionsMenu.addSeparator();

        // クイック設定
        soundMenuItem = new JCheckBoxMenuItem("Sound Enabled");
        soundMenuItem.setSelected(settings.isSoundEnabled());
        soundMenuItem.addActionListener(e -> {
            settings.setSoundEnabled(soundMenuItem.isSelected());
            settings.saveSettings();
            gamePanel.requestFocus();
        });
        optionsMenu.add(soundMenuItem);

        fpsMenuItem = new JCheckBoxMenuItem("Show FPS");
        fpsMenuItem.setSelected(settings.isShowFPS());
        fpsMenuItem.addActionListener(e -> {
            settings.setShowFPS(fpsMenuItem.isSelected());
            settings.saveSettings();
            gamePanel.requestFocus();
        });
        optionsMenu.add(fpsMenuItem);

        particlesMenuItem = new JCheckBoxMenuItem("Particle Effects");
        particlesMenuItem.setSelected(settings.isParticleEffectsEnabled());
        particlesMenuItem.addActionListener(e -> {
            settings.setParticleEffectsEnabled(particlesMenuItem.isSelected());
            settings.saveSettings();
            gamePanel.requestFocus();
        });
        optionsMenu.add(particlesMenuItem);

        return optionsMenu;
    }

    /**
     * 統計メニューの作成
     */
    private JMenu createStatsMenu() {
        JMenu statsMenu = new JMenu("Stats");
        statsMenu.setMnemonic('S');

        // 統計と実績
        JMenuItem statsItem = new JMenuItem("Statistics & Achievements...");
        statsItem.setAccelerator(KeyStroke.getKeyStroke("ctrl T"));
        statsItem.addActionListener(e -> showStatistics());
        statsMenu.add(statsItem);

        return statsMenu;
    }

    /**
     * ヘルプメニューの作成
     */
    private JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');

        // 操作説明
        JMenuItem instructionsItem = new JMenuItem("How to Play...");
        instructionsItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
        instructionsItem.addActionListener(e -> showInstructionsDialog());
        helpMenu.add(instructionsItem);

        helpMenu.addSeparator();

        // About
        JMenuItem aboutItem = new JMenuItem("About...");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        return helpMenu;
    }

    /**
     * ゲームパネルの初期化
     */
    private void initializeGamePanel() {
        gamePanel = new GamePanel();
        add(gamePanel);
        pack();

        // ウィンドウを画面中央に配置
        setLocationRelativeTo(null);
    }

    /**
     * 設定の適用
     */
    private void applySettings() {
        // フルスクリーン設定の適用（今回は未実装）
        // 他の初期設定があればここで適用
    }

    /**
     * ゲーム開始時の説明表示
     */
    private void showInstructions() {
        String message = "<html><body style='width: 300px;'>" +
                "<h2>Welcome to Classic Pacman!</h2>" +
                "<p>Use arrow keys to control Pacman<br>" +
                "Press P to pause/resume<br>" +
                "Press SPACE to restart after game over</p>" +
                "<br>" +
                "<p><b>Good luck and have fun!</b></p>" +
                "</body></html>";

        JOptionPane.showMessageDialog(this, message, "Welcome!",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 設定ダイアログの表示
     */
    private void showSettings() {
        SettingsDialog dialog = new SettingsDialog(this);
        dialog.setVisible(true);

        // 設定が変更された可能性があるので、メニューを更新
        updateMenuItems();
        gamePanel.requestFocus();
    }

    /**
     * ハイスコアダイアログの表示
     */
    private void showHighScores() {
        HighScoreDialog dialog = new HighScoreDialog(this);
        dialog.setVisible(true);
        gamePanel.requestFocus();
    }

    /**
     * 統計ダイアログの表示
     */
    private void showStatistics() {
        StatisticsDialog dialog = new StatisticsDialog(this);
        dialog.setVisible(true);
        gamePanel.requestFocus();
    }

    /**
     * 操作説明ダイアログ
     */
    private void showInstructionsDialog() {
        String instructions = "<html><body style='width: 350px; padding: 10px;'>" +
                "<h2>How to Play Pacman</h2>" +
                "<p><b>Objective:</b> Eat all the pellets while avoiding the ghosts!</p>" +
                "<br>" +
                "<p><b>Controls:</b></p>" +
                "<ul>" +
                "<li><b>Arrow Keys</b> - Move Pacman</li>" +
                "<li><b>P</b> - Pause/Resume</li>" +
                "<li><b>Space</b> - New Game (when game over)</li>" +
                "<li><b>Esc</b> - Pause</li>" +
                "</ul>" +
                "<br>" +
                "<p><b>Scoring:</b></p>" +
                "<ul>" +
                "<li>Pellet - 10 points</li>" +
                "<li>Power Pellet - 50 points</li>" +
                "<li>Ghost (1st) - 200 points</li>" +
                "<li>Ghost (2nd) - 400 points</li>" +
                "<li>Ghost (3rd) - 800 points</li>" +
                "<li>Ghost (4th) - 1600 points</li>" +
                "<li>Fruits - 100-5000 points</li>" +
                "</ul>" +
                "<br>" +
                "<p><b>Ghost Behaviors:</b></p>" +
                "<ul>" +
                "<li><font color='red'><b>Blinky (Red)</b></font> - Chases you directly</li>" +
                "<li><font color='#FF69B4'><b>Pinky (Pink)</b></font> - Tries to get ahead of you</li>" +
                "<li><font color='cyan'><b>Inky (Cyan)</b></font> - Unpredictable movements</li>" +
                "<li><font color='orange'><b>Clyde (Orange)</b></font> - Switches between chase and flee</li>" +
                "</ul>" +
                "<br>" +
                "<p><b>Tips:</b></p>" +
                "<ul>" +
                "<li>Power pellets make ghosts vulnerable</li>" +
                "<li>Eating multiple ghosts gives bonus points</li>" +
                "<li>Fruits appear after eating certain pellets</li>" +
                "<li>Use the tunnels to escape ghosts</li>" +
                "</ul>" +
                "</body></html>";

        JLabel label = new JLabel(instructions);
        JScrollPane scrollPane = new JScrollPane(label);
        scrollPane.setPreferredSize(new Dimension(400, 500));

        JOptionPane.showMessageDialog(this, scrollPane, "How to Play",
                JOptionPane.INFORMATION_MESSAGE);
        gamePanel.requestFocus();
    }

    /**
     * Aboutダイアログ
     */
    private void showAboutDialog() {
        String about = "<html><body style='width: 300px; padding: 10px; text-align: center;'>" +
                "<h2>Classic Pacman</h2>" +
                "<p><b>Java Edition v2.0.0</b></p>" +
                "<br>" +
                "<p>A faithful recreation of the 1980 arcade classic<br>" +
                "with modern enhancements</p>" +
                "<br>" +
                "<p><b>Features:</b></p>" +
                "<ul style='text-align: left;'>" +
                "<li>Authentic ghost AI behaviors</li>" +
                "<li>Sound effects and music</li>" +
                "<li>Particle effects</li>" +
                "<li>High score tracking</li>" +
                "<li>Achievements system</li>" +
                "<li>Customizable controls</li>" +
                "</ul>" +
                "<br>" +
                "<p>Built with Java Swing and Maven</p>" +
                "<br>" +
                "<p style='font-size: 10px; color: gray;'>" +
                "Original game © 1980 Namco<br>" +
                "This is an educational recreation<br>" +
                "Not for commercial use" +
                "</p>" +
                "</body></html>";

        JLabel label = new JLabel(about);
        JOptionPane.showMessageDialog(this, label, "About Classic Pacman",
                JOptionPane.INFORMATION_MESSAGE);
        gamePanel.requestFocus();
    }

    /**
     * メニューアイテムの更新
     */
    private void updateMenuItems() {
        soundMenuItem.setSelected(settings.isSoundEnabled());
        fpsMenuItem.setSelected(settings.isShowFPS());
        particlesMenuItem.setSelected(settings.isParticleEffectsEnabled());
    }

    /**
     * 終了確認
     */
    private void confirmExit() {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?",
                "Exit Game",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            // ゲームの適切な終了処理
            if (gamePanel != null && gamePanel.getGame() != null) {
                gamePanel.getGame().dispose();
            }

            // 設定の保存
            settings.saveSettings();

            System.exit(0);
        }
    }

    /**
     * シンプルなパックマンアイコンを作成
     */
    private Image createPacmanIcon() {
        // 32x32のシンプルなパックマンアイコンを動的に生成
        BufferedImage icon = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();

        // アンチエイリアシング
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // 背景
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, 32, 32);

        // パックマン
        g.setColor(Color.YELLOW);
        g.fillArc(2, 2, 28, 28, 30, 300);

        // 目
        g.setColor(Color.BLACK);
        g.fillOval(10, 8, 4, 4);

        g.dispose();
        return icon;
    }

    /**
     * ゲームを開始
     */
    public void startGame() {
        gamePanel.startGame();
        gamePanel.requestFocus();
    }

    /**
     * メインメソッド（テスト用）
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // 無視
            }

            GameWindow window = new GameWindow();
            window.setVisible(true);
            window.startGame();
        });
    }
}