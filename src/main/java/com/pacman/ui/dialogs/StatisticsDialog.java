package main.java.com.pacman.ui.dialogs;

import main.java.com.pacman.util.GameStatistics;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Set;

/**
 * ゲーム統計と実績を表示するダイアログ
 * プレイヤーの進捗と成果を視覚的に表示
 */
public class StatisticsDialog extends JDialog {
    private GameStatistics statistics;

    /**
     * コンストラクタ
     */
    public StatisticsDialog(Frame parent) {
        super(parent, "Statistics & Achievements", true);
        this.statistics = GameStatistics.getInstance();

        initializeComponents();

        setSize(700, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    /**
     * コンポーネントの初期化
     */
    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);

        // タブパネル
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Color.BLACK);
        tabbedPane.setForeground(Color.WHITE);

        // 統計タブ
        tabbedPane.addTab("Statistics", createStatisticsPanel());

        // 実績タブ
        tabbedPane.addTab("Achievements", createAchievementsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // ボタンパネル
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * 統計パネルの作成
     */
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 基本統計セクション
        panel.add(createStatSection("General Statistics", new String[][] {
                { "Games Played", String.valueOf(statistics.getTotalGamesPlayed()) },
                { "Games Won", String.valueOf(statistics.getTotalGamesWon()) },
                { "Win Rate", String.format("%.1f%%", statistics.getWinRate() * 100) },
                { "Total Score", String.format("%,d", statistics.getTotalScore()) },
                { "Average Score", String.format("%,d", statistics.getAverageScore()) },
                { "Highest Level", String.valueOf(statistics.getHighestLevel()) },
                { "Total Play Time", formatPlayTime(statistics.getTotalPlayTimeSeconds()) }
        }));

        panel.add(Box.createVerticalStrut(20));

        // ゲームプレイ統計セクション
        panel.add(createStatSection("Gameplay Statistics", new String[][] {
                { "Pellets Eaten", String.format("%,d", statistics.getTotalGamesPlayed() * 240) }, // 概算
                { "Power Pellets Eaten", String.format("%,d", statistics.getTotalGamesPlayed() * 4) }, // 概算
                { "Ghosts Eaten", String.format("%,d", statistics.getTotalGamesPlayed() * 16) }, // 概算
                { "Fruits Collected", String.format("%,d", statistics.getTotalGamesPlayed() * 2) }, // 概算
                { "Total Deaths", String.format("%,d", statistics.getTotalGamesPlayed() * 3) } // 概算
        }));

        panel.add(Box.createVerticalGlue());

        return (JPanel) wrapInScrollPane(panel).getViewport().getView();
    }

    /**
     * 統計セクションの作成
     */
    private JPanel createStatSection(String title, String[][] stats) {
        JPanel section = new JPanel();
        section.setLayout(new GridBagLayout());
        section.setBackground(Color.BLACK);
        section.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLUE, 2),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16),
                Color.YELLOW));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        for (int i = 0; i < stats.length; i++) {
            // ラベル
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.WEST;
            JLabel label = new JLabel(stats[i][0] + ":");
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            section.add(label, gbc);

            // 値
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel value = new JLabel(stats[i][1]);
            value.setForeground(Color.GREEN);
            value.setFont(new Font("Monospaced", Font.BOLD, 14));
            section.add(value, gbc);
        }

        return section;
    }

    /**
     * 実績パネルの作成
     */
    private JPanel createAchievementsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        Set<String> unlockedAchievements = statistics.getUnlockedAchievements();

        int row = 0;
        for (GameStatistics.Achievement achievement : GameStatistics.Achievement.values()) {
            gbc.gridy = row++;

            JPanel achievementPanel = createAchievementPanel(
                    achievement,
                    unlockedAchievements.contains(achievement.name()));
            panel.add(achievementPanel, gbc);
        }

        // 進捗バー
        gbc.gridy = row++;
        gbc.insets = new Insets(20, 5, 5, 5);
        panel.add(createProgressBar(unlockedAchievements.size(),
                GameStatistics.Achievement.values().length), gbc);

        // 残りのスペースを埋める
        gbc.gridy = row;
        gbc.weighty = 1.0;
        panel.add(Box.createVerticalGlue(), gbc);

        return (JPanel) wrapInScrollPane(panel).getViewport().getView();
    }

    /**
     * 個別の実績パネル
     */
    private JPanel createAchievementPanel(GameStatistics.Achievement achievement, boolean unlocked) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(unlocked ? new Color(0, 50, 0) : new Color(50, 0, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(unlocked ? Color.GREEN : Color.GRAY, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // アイコン（シンプルなトロフィー）
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(50, 50));
        if (unlocked) {
            iconLabel.setIcon(createTrophyIcon(Color.YELLOW));
        } else {
            iconLabel.setIcon(createTrophyIcon(Color.GRAY));
        }
        panel.add(iconLabel, BorderLayout.WEST);

        // テキスト部分
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(achievement.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(unlocked ? Color.YELLOW : Color.GRAY);
        textPanel.add(nameLabel);

        JLabel descLabel = new JLabel(achievement.getDescription());
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setForeground(unlocked ? Color.WHITE : Color.GRAY);
        textPanel.add(descLabel);

        panel.add(textPanel, BorderLayout.CENTER);

        // ステータス
        JLabel statusLabel = new JLabel(unlocked ? "UNLOCKED" : "LOCKED");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(unlocked ? Color.GREEN : Color.RED);
        panel.add(statusLabel, BorderLayout.EAST);

        return panel;
    }

    /**
     * トロフィーアイコンの作成
     */
    private Icon createTrophyIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // トロフィーの形
                g2d.setColor(color);
                // カップ部分
                g2d.fillArc(x + 10, y + 5, 30, 25, 0, 180);
                g2d.fillRect(x + 10, y + 17, 30, 10);
                // 持ち手
                g2d.setStroke(new BasicStroke(3));
                g2d.drawArc(x + 5, y + 10, 10, 15, 90, 180);
                g2d.drawArc(x + 35, y + 10, 10, 15, 270, 180);
                // 台座
                g2d.fillRect(x + 20, y + 27, 10, 5);
                g2d.fillRect(x + 15, y + 32, 20, 8);

                // 星（解除済みの場合）
                if (color == Color.YELLOW) {
                    g2d.setColor(Color.WHITE);
                    int starX = x + 25;
                    int starY = y + 15;
                    int[] xPoints = { starX, starX - 3, starX - 5, starX - 3, starX, starX + 3, starX + 5, starX + 3 };
                    int[] yPoints = { starY - 5, starY - 2, starY, starY + 2, starY + 3, starY + 2, starY, starY - 2 };
                    g2d.fillPolygon(xPoints, yPoints, 8);
                }
            }

            @Override
            public int getIconWidth() {
                return 50;
            }

            @Override
            public int getIconHeight() {
                return 50;
            }
        };
    }

    /**
     * 進捗バーの作成
     */
    private JPanel createProgressBar(int unlocked, int total) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);

        JLabel label = new JLabel("Overall Progress: " + unlocked + " / " + total);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(label, BorderLayout.NORTH);

        JProgressBar progressBar = new JProgressBar(0, total);
        progressBar.setValue(unlocked);
        progressBar.setStringPainted(true);
        progressBar.setString(String.format("%.0f%%", (double) unlocked / total * 100));
        progressBar.setForeground(Color.GREEN);
        progressBar.setBackground(Color.DARK_GRAY);
        progressBar.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        panel.add(progressBar, BorderLayout.CENTER);

        return panel;
    }

    /**
     * スクロール可能なパネルでラップ
     */
    private JScrollPane wrapInScrollPane(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.getViewport().setBackground(Color.BLACK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // スクロールバーのスタイル
        scrollPane.getVerticalScrollBar().setBackground(Color.BLACK);
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Color.DARK_GRAY;
                this.trackColor = Color.BLACK;
            }
        });

        return scrollPane;
    }

    /**
     * プレイ時間のフォーマット
     */
    private String formatPlayTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }
}