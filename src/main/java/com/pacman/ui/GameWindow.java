package main.java.com.pacman.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * ゲームのメインウィンドウクラス
 * JFrameを継承し、ゲーム全体のウィンドウを管理
 */
public class GameWindow extends JFrame {

    private GamePanel gamePanel;

    /**
     * コンストラクタ
     */
    public GameWindow() {
        initializeWindow();
        createMenuBar();
        initializeGamePanel();
        showInstructions();
    }

    /**
     * ウィンドウの初期設定
     */
    private void initializeWindow() {
        setTitle("Classic Pacman - Java Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // ウィンドウアイコンの設定（オプション）
        try {
            // シンプルなパックマンアイコンを作成
            Image icon = createPacmanIcon();
            setIconImage(icon);
        } catch (Exception e) {
            // アイコン設定失敗時は無視
        }

        // ウィンドウが閉じられる時の処理
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 必要に応じてゲームの状態を保存
                System.exit(0);
            }
        });
    }

    /**
     * メニューバーの作成
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // ゲームメニュー
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic('G');

        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
        newGameItem.addActionListener((ActionEvent e) -> {
            gamePanel.startGame();
        });
        gameMenu.add(newGameItem);

        JMenuItem pauseItem = new JMenuItem("Pause/Resume");
        pauseItem.setAccelerator(KeyStroke.getKeyStroke("P"));
        pauseItem.addActionListener((ActionEvent e) -> {
            // GamePanelがキー入力を処理するため、ここでは特に処理なし
        });
        gameMenu.add(pauseItem);

        gameMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        exitItem.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });
        gameMenu.add(exitItem);

        menuBar.add(gameMenu);

        // ヘルプメニュー
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');

        JMenuItem instructionsItem = new JMenuItem("Instructions");
        instructionsItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
        instructionsItem.addActionListener((ActionEvent e) -> {
            showInstructionsDialog();
        });
        helpMenu.add(instructionsItem);

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener((ActionEvent e) -> {
            showAboutDialog();
        });
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
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
     * ゲーム開始時の説明表示
     */
    private void showInstructions() {
        String message = "Welcome to Classic Pacman!\n\n" +
                "Use arrow keys to control Pacman\n" +
                "Press P to pause/resume\n" +
                "Press SPACE to restart after game over\n\n" +
                "Good luck!";

        JOptionPane.showMessageDialog(this, message, "Instructions",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 操作説明ダイアログ
     */
    private void showInstructionsDialog() {
        String instructions = "<html><body style='width: 300px; padding: 10px;'>" +
                "<h2>How to Play</h2>" +
                "<p><b>Objective:</b> Eat all the pellets while avoiding the ghosts!</p>" +
                "<br>" +
                "<p><b>Controls:</b></p>" +
                "<ul>" +
                "<li>Arrow Keys - Move Pacman</li>" +
                "<li>P - Pause/Resume</li>" +
                "<li>Space - New Game (when game over)</li>" +
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
                "</ul>" +
                "<br>" +
                "<p><b>Ghost Behaviors:</b></p>" +
                "<ul>" +
                "<li><font color='red'>Blinky (Red)</font> - Chases you directly</li>" +
                "<li><font color='#FF69B4'>Pinky (Pink)</font> - Tries to get ahead of you</li>" +
                "<li><font color='cyan'>Inky (Cyan)</font> - Unpredictable movements</li>" +
                "<li><font color='orange'>Clyde (Orange)</font> - Switches between chase and flee</li>" +
                "</ul>" +
                "</body></html>";

        JLabel label = new JLabel(instructions);
        JOptionPane.showMessageDialog(this, label, "Game Instructions",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Aboutダイアログ
     */
    private void showAboutDialog() {
        String about = "<html><body style='width: 250px; padding: 10px; text-align: center;'>" +
                "<h2>Classic Pacman</h2>" +
                "<p>Java Edition v1.0.0</p>" +
                "<br>" +
                "<p>A faithful recreation of the 1980 arcade classic.</p>" +
                "<br>" +
                "<p>Built with Java Swing and Maven</p>" +
                "<br>" +
                "<p style='font-size: 10px; color: gray;'>" +
                "Original game by Namco (1980)<br>" +
                "This is an educational recreation" +
                "</p>" +
                "</body></html>";

        JLabel label = new JLabel(about);
        JOptionPane.showMessageDialog(this, label, "About Pacman",
                JOptionPane.INFORMATION_MESSAGE);
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
}