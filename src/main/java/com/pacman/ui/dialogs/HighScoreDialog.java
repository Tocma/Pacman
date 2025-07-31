package com.pacman.ui.dialogs;

import com.pacman.util.HighScoreManager;
import com.pacman.util.GameSettings;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * ハイスコア表示ダイアログ
 * ハイスコアリストの表示と新しいハイスコアの入力を管理
 */
public class HighScoreDialog extends JDialog {
    private HighScoreManager highScoreManager;
    private GameSettings settings;

    private JTable scoreTable;
    private DefaultTableModel tableModel;

    // 新しいハイスコア入力用
    private boolean isNewHighScore;
    private int newScore;
    private int newLevel;
    private int newRank;

    /**
     * 通常の表示用コンストラクタ
     */
    public HighScoreDialog(Frame parent) {
        super(parent, "High Scores", true);
        this.highScoreManager = HighScoreManager.getInstance();
        this.settings = GameSettings.getInstance();
        this.isNewHighScore = false;

        initializeComponents();
        loadHighScores();

        setSize(600, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    /**
     * 新しいハイスコア入力用コンストラクタ
     */
    public HighScoreDialog(Frame parent, int score, int level) {
        super(parent, "New High Score!", true);
        this.highScoreManager = HighScoreManager.getInstance();
        this.settings = GameSettings.getInstance();
        this.isNewHighScore = true;
        this.newScore = score;
        this.newLevel = level;

        // ハイスコアかチェック
        if (highScoreManager.isHighScore(score)) {
            showNewHighScoreInput();
        } else {
            // ハイスコアではない場合は通常表示
            this.isNewHighScore = false;
            initializeComponents();
            loadHighScores();
        }

        setSize(600, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    /**
     * コンポーネントの初期化（通常表示）
     */
    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // タイトル
        JLabel titleLabel = new JLabel("HIGH SCORES", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(Color.BLACK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // スコアテーブル
        createScoreTable();
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ボタンパネル
        JPanel buttonPanel = new JPanel();

        JButton clearButton = new JButton("Clear High Scores");
        clearButton.addActionListener(e -> clearHighScores());
        buttonPanel.add(clearButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 背景色
        mainPanel.setBackground(Color.BLACK);
        buttonPanel.setBackground(Color.BLACK);

        setContentPane(mainPanel);
    }

    /**
     * スコアテーブルの作成
     */
    private void createScoreTable() {
        String[] columnNames = { "Rank", "Name", "Score", "Level", "Date" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        scoreTable = new JTable(tableModel);
        scoreTable.setBackground(Color.BLACK);
        scoreTable.setForeground(Color.WHITE);
        scoreTable.setGridColor(Color.DARK_GRAY);
        scoreTable.setSelectionBackground(Color.BLUE);
        scoreTable.setSelectionForeground(Color.YELLOW);
        scoreTable.setFont(new Font("Monospaced", Font.PLAIN, 14));
        scoreTable.setRowHeight(25);

        // ヘッダーのカスタマイズ
        JTableHeader header = scoreTable.getTableHeader();
        header.setBackground(Color.BLUE);
        header.setForeground(Color.YELLOW);
        header.setFont(new Font("Arial", Font.BOLD, 14));

        // 列幅の調整
        TableColumnModel columnModel = scoreTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50); // Rank
        columnModel.getColumn(1).setPreferredWidth(150); // Name
        columnModel.getColumn(2).setPreferredWidth(100); // Score
        columnModel.getColumn(3).setPreferredWidth(80); // Level
        columnModel.getColumn(4).setPreferredWidth(180); // Date

        // センタリング
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < scoreTable.getColumnCount(); i++) {
            if (i != 1) { // Name以外はセンタリング
                scoreTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // 特別な行のハイライト（新しいハイスコアの場合）
        if (isNewHighScore && newRank > 0) {
            scoreTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value,
                            isSelected, hasFocus, row, column);

                    if (row == newRank - 1) {
                        c.setBackground(Color.GREEN.darker());
                        c.setForeground(Color.WHITE);
                        ((JLabel) c).setFont(((JLabel) c).getFont().deriveFont(Font.BOLD));
                    } else if (!isSelected) {
                        c.setBackground(Color.BLACK);
                        c.setForeground(Color.WHITE);
                    }

                    if (column != 1) { // Name以外はセンタリング
                        ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                    }

                    return c;
                }
            });
        }
    }

    /**
     * ハイスコアの読み込み
     */
    private void loadHighScores() {
        tableModel.setRowCount(0);

        List<HighScoreManager.ScoreEntry> scores = highScoreManager.getHighScores();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

        int rank = 1;
        for (HighScoreManager.ScoreEntry entry : scores) {
            Object[] row = {
                    rank,
                    entry.getPlayerName(),
                    String.format("%,d", entry.getScore()),
                    entry.getLevel(),
                    entry.getDateTime().format(formatter)
            };
            tableModel.addRow(row);
            rank++;
        }

        // 空の行を10行まで埋める
        while (tableModel.getRowCount() < 10) {
            Object[] emptyRow = {
                    tableModel.getRowCount() + 1,
                    "---",
                    "---",
                    "---",
                    "---"
            };
            tableModel.addRow(emptyRow);
        }
    }

    /**
     * 新しいハイスコア入力画面
     */
    private void showNewHighScoreInput() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(Color.BLACK);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // おめでとうメッセージ
        JLabel congratsLabel = new JLabel("CONGRATULATIONS!");
        congratsLabel.setFont(new Font("Arial", Font.BOLD, 28));
        congratsLabel.setForeground(Color.YELLOW);
        congratsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.add(congratsLabel);

        inputPanel.add(Box.createVerticalStrut(20));

        // スコア表示
        JLabel scoreLabel = new JLabel("Your Score: " + String.format("%,d", newScore));
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.add(scoreLabel);

        JLabel levelLabel = new JLabel("Level Reached: " + newLevel);
        levelLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        levelLabel.setForeground(Color.WHITE);
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.add(levelLabel);

        inputPanel.add(Box.createVerticalStrut(30));

        // 名前入力
        JLabel nameLabel = new JLabel("Enter your name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.add(nameLabel);

        inputPanel.add(Box.createVerticalStrut(10));

        JTextField nameField = new JTextField(settings.getPlayerName());
        nameField.setMaximumSize(new Dimension(200, 30));
        nameField.setFont(new Font("Arial", Font.PLAIN, 16));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        inputPanel.add(nameField);

        // フォーカスと全選択
        SwingUtilities.invokeLater(() -> {
            nameField.requestFocus();
            nameField.selectAll();
        });

        // ダイアログ表示
        int result = JOptionPane.showConfirmDialog(this, inputPanel,
                "New High Score!",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) {
                playerName = "ANONYMOUS";
            }

            // ハイスコア登録
            newRank = highScoreManager.addScore(playerName, newScore, newLevel);

            // プレイヤー名を設定に保存
            settings.setPlayerName(playerName);
            settings.saveSettings();
        }

        // 通常のハイスコア表示に切り替え
        isNewHighScore = false;
        initializeComponents();
        loadHighScores();
    }

    /**
     * ハイスコアのクリア
     */
    private void clearHighScores() {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear all high scores?\nThis action cannot be undone.",
                "Clear High Scores",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            highScoreManager.clearHighScores();
            loadHighScores();
            JOptionPane.showMessageDialog(this,
                    "High scores have been cleared.",
                    "High Scores Cleared",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}