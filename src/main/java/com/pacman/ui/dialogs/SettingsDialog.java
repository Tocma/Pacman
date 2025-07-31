package main.java.com.pacman.ui.dialogs;

import main.java.com.pacman.sound.SoundManager;
import main.java.com.pacman.util.GameSettings;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ゲーム設定ダイアログ
 * 音量、難易度、表示設定、コントロール設定などを管理
 */
public class SettingsDialog extends JDialog {
    private GameSettings settings;
    private SoundManager soundManager;

    // 音量スライダー
    private JSlider masterVolumeSlider;
    private JSlider effectVolumeSlider;
    private JSlider musicVolumeSlider;
    private JCheckBox soundEnabledCheckBox;

    // ゲーム設定
    private JComboBox<GameSettings.Difficulty> difficultyCombo;
    private JTextField playerNameField;

    // 表示設定
    private JCheckBox showFPSCheckBox;
    private JCheckBox particleEffectsCheckBox;

    // コントロール設定
    private JButton upKeyButton;
    private JButton downKeyButton;
    private JButton leftKeyButton;
    private JButton rightKeyButton;
    private int tempUpKey, tempDownKey, tempLeftKey, tempRightKey;

    // ボタン
    private JButton applyButton;
    private JButton cancelButton;
    private JButton defaultsButton;

    /**
     * コンストラクタ
     */
    public SettingsDialog(Frame parent) {
        super(parent, "Game Settings", true);
        settings = GameSettings.getInstance();
        soundManager = SoundManager.getInstance();

        initializeComponents();
        loadCurrentSettings();

        setSize(600, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    /**
     * コンポーネントの初期化
     */
    private void initializeComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // タブパネル
        JTabbedPane tabbedPane = new JTabbedPane();

        // 各タブの作成
        tabbedPane.addTab("Audio", createAudioPanel());
        tabbedPane.addTab("Game", createGamePanel());
        tabbedPane.addTab("Display", createDisplayPanel());
        tabbedPane.addTab("Controls", createControlsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // ボタンパネル
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * オーディオ設定パネル
     */
    private JPanel createAudioPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // サウンド有効/無効
        soundEnabledCheckBox = new JCheckBox("Enable Sound");
        soundEnabledCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(soundEnabledCheckBox);
        panel.add(Box.createVerticalStrut(20));

        // マスター音量
        panel.add(createVolumeSliderPanel("Master Volume:",
                masterVolumeSlider = new JSlider(0, 100, 70)));

        // 効果音音量
        panel.add(createVolumeSliderPanel("Effect Volume:",
                effectVolumeSlider = new JSlider(0, 100, 80)));

        // 音楽音量
        panel.add(createVolumeSliderPanel("Music Volume:",
                musicVolumeSlider = new JSlider(0, 100, 60)));

        // サウンドテストボタン
        JButton testSoundButton = new JButton("Test Sound");
        testSoundButton.addActionListener(e -> {
            soundManager.playSound(SoundManager.SoundType.PELLET_EAT);
        });
        testSoundButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createVerticalStrut(20));
        panel.add(testSoundButton);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * 音量スライダーパネルの作成
     */
    private JPanel createVolumeSliderPanel(String label, JSlider slider) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setMaximumSize(new Dimension(400, 60));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(label);
        titleLabel.setPreferredSize(new Dimension(120, 30));
        panel.add(titleLabel, BorderLayout.WEST);

        slider.setMajorTickSpacing(25);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        // 値ラベル
        JLabel valueLabel = new JLabel(String.valueOf(slider.getValue()) + "%");
        valueLabel.setPreferredSize(new Dimension(50, 30));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        slider.addChangeListener(e -> {
            valueLabel.setText(slider.getValue() + "%");
        });

        panel.add(slider, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.EAST);

        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        return panel;
    }

    /**
     * ゲーム設定パネル
     */
    private JPanel createGamePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // プレイヤー名
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Player Name:"), gbc);

        gbc.gridx = 1;
        playerNameField = new JTextField(20);
        panel.add(playerNameField, gbc);

        // 難易度
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Difficulty:"), gbc);

        gbc.gridx = 1;
        difficultyCombo = new JComboBox<>(GameSettings.Difficulty.values());
        difficultyCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof GameSettings.Difficulty) {
                    GameSettings.Difficulty diff = (GameSettings.Difficulty) value;
                    setText(diff.getDisplayName());
                }
                return this;
            }
        });
        panel.add(difficultyCombo, gbc);

        // 難易度説明
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JTextArea difficultyInfo = new JTextArea(
                "Easy: 4 lives, slower ghosts\n" +
                        "Normal: 3 lives, normal speed\n" +
                        "Hard: 2 lives, faster ghosts\n" +
                        "Extreme: 1 life, very fast ghosts");
        difficultyInfo.setEditable(false);
        difficultyInfo.setBackground(panel.getBackground());
        difficultyInfo.setBorder(BorderFactory.createTitledBorder("Difficulty Info"));
        panel.add(difficultyInfo, gbc);

        return panel;
    }

    /**
     * 表示設定パネル
     */
    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        showFPSCheckBox = new JCheckBox("Show FPS Counter");
        showFPSCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(showFPSCheckBox);

        panel.add(Box.createVerticalStrut(10));

        particleEffectsCheckBox = new JCheckBox("Enable Particle Effects");
        particleEffectsCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(particleEffectsCheckBox);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    /**
     * コントロール設定パネル
     */
    private JPanel createControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 説明
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel instructionLabel = new JLabel("Click a button and press a key to change");
        instructionLabel.setFont(instructionLabel.getFont().deriveFont(Font.ITALIC));
        panel.add(instructionLabel, gbc);

        // 上キー
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Up:"), gbc);

        gbc.gridx = 1;
        upKeyButton = createKeyButton(settings.getKeyUp());
        upKeyButton.addActionListener(e -> captureKey(upKeyButton, "Up"));
        panel.add(upKeyButton, gbc);

        // 下キー
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Down:"), gbc);

        gbc.gridx = 1;
        downKeyButton = createKeyButton(settings.getKeyDown());
        downKeyButton.addActionListener(e -> captureKey(downKeyButton, "Down"));
        panel.add(downKeyButton, gbc);

        // 左キー
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Left:"), gbc);

        gbc.gridx = 1;
        leftKeyButton = createKeyButton(settings.getKeyLeft());
        leftKeyButton.addActionListener(e -> captureKey(leftKeyButton, "Left"));
        panel.add(leftKeyButton, gbc);

        // 右キー
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Right:"), gbc);

        gbc.gridx = 1;
        rightKeyButton = createKeyButton(settings.getKeyRight());
        rightKeyButton.addActionListener(e -> captureKey(rightKeyButton, "Right"));
        panel.add(rightKeyButton, gbc);

        return panel;
    }

    /**
     * キーボタンの作成
     */
    private JButton createKeyButton(int keyCode) {
        JButton button = new JButton(KeyEvent.getKeyText(keyCode));
        button.setPreferredSize(new Dimension(150, 30));
        return button;
    }

    /**
     * キー入力のキャプチャ
     */
    private void captureKey(JButton button, String direction) {
        button.setText("Press a key...");
        button.setFocusable(true);
        button.requestFocus();

        button.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                button.setText(KeyEvent.getKeyText(keyCode));

                // 一時的に保存
                switch (direction) {
                    case "Up":
                        tempUpKey = keyCode;
                        break;
                    case "Down":
                        tempDownKey = keyCode;
                        break;
                    case "Left":
                        tempLeftKey = keyCode;
                        break;
                    case "Right":
                        tempRightKey = keyCode;
                        break;
                }

                button.removeKeyListener(this);
            }
        });
    }

    /**
     * ボタンパネル
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        defaultsButton = new JButton("Reset to Defaults");
        defaultsButton.addActionListener(e -> resetToDefaults());

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> applySettings());

        panel.add(defaultsButton);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(cancelButton);
        panel.add(applyButton);

        return panel;
    }

    /**
     * 現在の設定を読み込み
     */
    private void loadCurrentSettings() {
        // 音量設定
        soundEnabledCheckBox.setSelected(settings.isSoundEnabled());
        masterVolumeSlider.setValue((int) (settings.getMasterVolume() * 100));
        effectVolumeSlider.setValue((int) (settings.getEffectVolume() * 100));
        musicVolumeSlider.setValue((int) (settings.getMusicVolume() * 100));

        // ゲーム設定
        playerNameField.setText(settings.getPlayerName());
        difficultyCombo.setSelectedItem(settings.getDifficulty());

        // 表示設定
        showFPSCheckBox.setSelected(settings.isShowFPS());
        particleEffectsCheckBox.setSelected(settings.isParticleEffectsEnabled());

        // コントロール設定
        tempUpKey = settings.getKeyUp();
        tempDownKey = settings.getKeyDown();
        tempLeftKey = settings.getKeyLeft();
        tempRightKey = settings.getKeyRight();
    }

    /**
     * 設定を適用
     */
    private void applySettings() {
        // 音量設定
        settings.setSoundEnabled(soundEnabledCheckBox.isSelected());
        settings.setMasterVolume(masterVolumeSlider.getValue() / 100.0f);
        settings.setEffectVolume(effectVolumeSlider.getValue() / 100.0f);
        settings.setMusicVolume(musicVolumeSlider.getValue() / 100.0f);

        // サウンドマネージャーに反映
        soundManager.setSoundEnabled(soundEnabledCheckBox.isSelected());
        soundManager.setMasterVolume(masterVolumeSlider.getValue() / 100.0f);
        soundManager.setEffectVolume(effectVolumeSlider.getValue() / 100.0f);
        soundManager.setMusicVolume(musicVolumeSlider.getValue() / 100.0f);

        // ゲーム設定
        settings.setPlayerName(playerNameField.getText());
        settings.setDifficulty((GameSettings.Difficulty) difficultyCombo.getSelectedItem());

        // 表示設定
        settings.setShowFPS(showFPSCheckBox.isSelected());
        settings.setParticleEffectsEnabled(particleEffectsCheckBox.isSelected());

        // コントロール設定
        settings.setKeyUp(tempUpKey);
        settings.setKeyDown(tempDownKey);
        settings.setKeyLeft(tempLeftKey);
        settings.setKeyRight(tempRightKey);

        // 設定を保存
        settings.saveSettings();

        dispose();
    }

    /**
     * デフォルト設定にリセット
     */
    private void resetToDefaults() {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reset all settings to defaults?",
                "Reset Settings",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            settings.resetToDefaults();
            loadCurrentSettings();
        }
    }
}