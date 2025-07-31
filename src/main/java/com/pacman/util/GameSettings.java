package com.pacman.util;

import java.io.*;
import java.util.Properties;

/**
 * ゲームの各種設定を管理するクラス
 * 設定はプロパティファイルに永続化される
 */
public class GameSettings {
    // 設定ファイル名
    private static final String SETTINGS_FILE = "pacman_settings.properties";

    // 設定項目のキー
    private static final String KEY_MASTER_VOLUME = "audio.master_volume";
    private static final String KEY_EFFECT_VOLUME = "audio.effect_volume";
    private static final String KEY_MUSIC_VOLUME = "audio.music_volume";
    private static final String KEY_SOUND_ENABLED = "audio.enabled";

    private static final String KEY_DIFFICULTY = "game.difficulty";
    private static final String KEY_LIVES = "game.starting_lives";
    private static final String KEY_GHOST_SPEED = "game.ghost_speed_multiplier";
    private static final String KEY_SHOW_FPS = "display.show_fps";
    private static final String KEY_FULLSCREEN = "display.fullscreen";
    private static final String KEY_PARTICLE_EFFECTS = "display.particle_effects";

    private static final String KEY_PLAYER_NAME = "player.default_name";
    private static final String KEY_KEY_UP = "controls.up";
    private static final String KEY_KEY_DOWN = "controls.down";
    private static final String KEY_KEY_LEFT = "controls.left";
    private static final String KEY_KEY_RIGHT = "controls.right";

    // 難易度レベル
    public enum Difficulty {
        EASY(0.8f, 4, "Easy"),
        NORMAL(1.0f, 3, "Normal"),
        HARD(1.2f, 2, "Hard"),
        EXTREME(1.5f, 1, "Extreme");

        private final float speedMultiplier;
        private final int startingLives;
        private final String displayName;

        Difficulty(float speedMultiplier, int startingLives, String displayName) {
            this.speedMultiplier = speedMultiplier;
            this.startingLives = startingLives;
            this.displayName = displayName;
        }

        public float getSpeedMultiplier() {
            return speedMultiplier;
        }

        public int getStartingLives() {
            return startingLives;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // プロパティオブジェクト
    private Properties properties;

    // シングルトンインスタンス
    private static GameSettings instance;

    /**
     * プライベートコンストラクタ（シングルトンパターン）
     */
    private GameSettings() {
        properties = new Properties();
        loadSettings();
    }

    /**
     * シングルトンインスタンスの取得
     */
    public static synchronized GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }

    /**
     * 設定ファイルから読み込み
     */
    private void loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            // ファイルが存在しない場合はデフォルト設定を使用
            setDefaultSettings();
            saveSettings();
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("設定ファイル読み込みエラー: " + e.getMessage());
            setDefaultSettings();
        }
    }

    /**
     * 設定ファイルへ保存
     */
    public void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(fos, "Pacman Game Settings");
        } catch (IOException e) {
            System.err.println("設定ファイル保存エラー: " + e.getMessage());
        }
    }

    /**
     * デフォルト設定を設定
     */
    private void setDefaultSettings() {
        // オーディオ設定
        properties.setProperty(KEY_MASTER_VOLUME, "0.7");
        properties.setProperty(KEY_EFFECT_VOLUME, "0.8");
        properties.setProperty(KEY_MUSIC_VOLUME, "0.6");
        properties.setProperty(KEY_SOUND_ENABLED, "true");

        // ゲーム設定
        properties.setProperty(KEY_DIFFICULTY, Difficulty.NORMAL.name());
        properties.setProperty(KEY_LIVES, "3");
        properties.setProperty(KEY_GHOST_SPEED, "1.0");

        // 表示設定
        properties.setProperty(KEY_SHOW_FPS, "false");
        properties.setProperty(KEY_FULLSCREEN, "false");
        properties.setProperty(KEY_PARTICLE_EFFECTS, "true");

        // プレイヤー設定
        properties.setProperty(KEY_PLAYER_NAME, "PLAYER");

        // コントロール設定（キーコード）
        properties.setProperty(KEY_KEY_UP, "38"); // Arrow Up
        properties.setProperty(KEY_KEY_DOWN, "40"); // Arrow Down
        properties.setProperty(KEY_KEY_LEFT, "37"); // Arrow Left
        properties.setProperty(KEY_KEY_RIGHT, "39"); // Arrow Right
    }

    // ゲッターメソッド

    public float getMasterVolume() {
        return Float.parseFloat(properties.getProperty(KEY_MASTER_VOLUME, "0.7"));
    }

    public float getEffectVolume() {
        return Float.parseFloat(properties.getProperty(KEY_EFFECT_VOLUME, "0.8"));
    }

    public float getMusicVolume() {
        return Float.parseFloat(properties.getProperty(KEY_MUSIC_VOLUME, "0.6"));
    }

    public boolean isSoundEnabled() {
        return Boolean.parseBoolean(properties.getProperty(KEY_SOUND_ENABLED, "true"));
    }

    public Difficulty getDifficulty() {
        String diffStr = properties.getProperty(KEY_DIFFICULTY, Difficulty.NORMAL.name());
        try {
            return Difficulty.valueOf(diffStr);
        } catch (IllegalArgumentException e) {
            return Difficulty.NORMAL;
        }
    }

    public int getStartingLives() {
        return Integer.parseInt(properties.getProperty(KEY_LIVES, "3"));
    }

    public float getGhostSpeedMultiplier() {
        return Float.parseFloat(properties.getProperty(KEY_GHOST_SPEED, "1.0"));
    }

    public boolean isShowFPS() {
        return Boolean.parseBoolean(properties.getProperty(KEY_SHOW_FPS, "false"));
    }

    public boolean isFullscreen() {
        return Boolean.parseBoolean(properties.getProperty(KEY_FULLSCREEN, "false"));
    }

    public boolean isParticleEffectsEnabled() {
        return Boolean.parseBoolean(properties.getProperty(KEY_PARTICLE_EFFECTS, "true"));
    }

    public String getPlayerName() {
        return properties.getProperty(KEY_PLAYER_NAME, "PLAYER");
    }

    public int getKeyUp() {
        return Integer.parseInt(properties.getProperty(KEY_KEY_UP, "38"));
    }

    public int getKeyDown() {
        return Integer.parseInt(properties.getProperty(KEY_KEY_DOWN, "40"));
    }

    public int getKeyLeft() {
        return Integer.parseInt(properties.getProperty(KEY_KEY_LEFT, "37"));
    }

    public int getKeyRight() {
        return Integer.parseInt(properties.getProperty(KEY_KEY_RIGHT, "39"));
    }

    // セッターメソッド

    public void setMasterVolume(float volume) {
        properties.setProperty(KEY_MASTER_VOLUME, String.valueOf(volume));
    }

    public void setEffectVolume(float volume) {
        properties.setProperty(KEY_EFFECT_VOLUME, String.valueOf(volume));
    }

    public void setMusicVolume(float volume) {
        properties.setProperty(KEY_MUSIC_VOLUME, String.valueOf(volume));
    }

    public void setSoundEnabled(boolean enabled) {
        properties.setProperty(KEY_SOUND_ENABLED, String.valueOf(enabled));
    }

    public void setDifficulty(Difficulty difficulty) {
        properties.setProperty(KEY_DIFFICULTY, difficulty.name());
        // 難易度に応じて関連設定も更新
        properties.setProperty(KEY_LIVES, String.valueOf(difficulty.getStartingLives()));
        properties.setProperty(KEY_GHOST_SPEED, String.valueOf(difficulty.getSpeedMultiplier()));
    }

    public void setShowFPS(boolean show) {
        properties.setProperty(KEY_SHOW_FPS, String.valueOf(show));
    }

    public void setFullscreen(boolean fullscreen) {
        properties.setProperty(KEY_FULLSCREEN, String.valueOf(fullscreen));
    }

    public void setParticleEffectsEnabled(boolean enabled) {
        properties.setProperty(KEY_PARTICLE_EFFECTS, String.valueOf(enabled));
    }

    public void setPlayerName(String name) {
        properties.setProperty(KEY_PLAYER_NAME, name);
    }

    public void setKeyUp(int keyCode) {
        properties.setProperty(KEY_KEY_UP, String.valueOf(keyCode));
    }

    public void setKeyDown(int keyCode) {
        properties.setProperty(KEY_KEY_DOWN, String.valueOf(keyCode));
    }

    public void setKeyLeft(int keyCode) {
        properties.setProperty(KEY_KEY_LEFT, String.valueOf(keyCode));
    }

    public void setKeyRight(int keyCode) {
        properties.setProperty(KEY_KEY_RIGHT, String.valueOf(keyCode));
    }

    /**
     * 設定をリセット
     */
    public void resetToDefaults() {
        setDefaultSettings();
        saveSettings();
    }
}