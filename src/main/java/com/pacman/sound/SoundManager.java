package main.java.com.pacman.sound;

import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ゲーム内のすべての音楽と効果音を管理するクラス
 * WAVファイル形式の音声ファイルを再生
 */
public class SoundManager {
    // サウンドタイプの定義
    public enum SoundType {
        GAME_START, // ゲーム開始音
        PELLET_EAT, // ペレット取得音
        POWER_PELLET, // パワーペレット取得音
        GHOST_EAT, // ゴースト捕食音
        PACMAN_DEATH, // パックマン死亡音
        LEVEL_CLEAR, // レベルクリア音
        EXTRA_LIFE, // エクストラライフ取得音
        SIREN, // 背景のサイレン音（ループ）
        POWER_MODE, // パワーモード中のBGM（ループ）
        INTERMISSION // 幕間音楽
    }

    // サウンドクリップのキャッシュ
    private Map<SoundType, Clip> soundClips;
    private Map<SoundType, AudioInputStream> soundStreams;

    // 音量設定（0.0f ～ 1.0f）
    private float masterVolume = 0.7f;
    private float effectVolume = 0.8f;
    private float musicVolume = 0.6f;

    // 現在再生中のBGM
    private Clip currentBGM;
    private boolean soundEnabled = true;

    // シングルトンインスタンス
    private static SoundManager instance;

    /**
     * プライベートコンストラクタ（シングルトンパターン）
     */
    private SoundManager() {
        soundClips = new HashMap<>();
        soundStreams = new HashMap<>();
        // サウンドファイルの読み込みは実際のファイルが用意された時に行う
        // 現在は仮想的な実装として、実際の音声ファイルなしで動作するように設計
    }

    /**
     * シングルトンインスタンスの取得
     */
    public static synchronized SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * 効果音の再生
     */
    public void playSound(SoundType soundType) {
        if (!soundEnabled)
            return;

        try {
            Clip clip = soundClips.get(soundType);
            if (clip != null) {
                // 再生位置をリセット
                clip.setFramePosition(0);

                // 音量設定
                setClipVolume(clip, effectVolume * masterVolume);

                // 再生開始
                clip.start();
            }
        } catch (Exception e) {
            System.err.println("サウンド再生エラー: " + soundType + " - " + e.getMessage());
        }
    }

    /**
     * BGMの再生（ループ）
     */
    public void playBGM(SoundType bgmType) {
        if (!soundEnabled)
            return;

        // 現在のBGMを停止
        stopBGM();

        try {
            Clip clip = soundClips.get(bgmType);
            if (clip != null) {
                currentBGM = clip;
                clip.setFramePosition(0);
                setClipVolume(clip, musicVolume * masterVolume);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.err.println("BGM再生エラー: " + bgmType + " - " + e.getMessage());
        }
    }

    /**
     * BGMの停止
     */
    public void stopBGM() {
        if (currentBGM != null && currentBGM.isRunning()) {
            currentBGM.stop();
            currentBGM.setFramePosition(0);
        }
    }

    /**
     * すべての音声を停止
     */
    public void stopAllSounds() {
        stopBGM();
        for (Clip clip : soundClips.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
        }
    }

    /**
     * クリップの音量設定
     */
    private void setClipVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            // 音量をデシベルに変換（0.0 ～ 1.0 → -80.0 ～ 0.0 dB）
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            dB = Math.max(dB, gainControl.getMinimum());
            dB = Math.min(dB, gainControl.getMaximum());

            gainControl.setValue(dB);
        }
    }

    /**
     * サウンドファイルの読み込み（将来的な実装用）
     */
    public void loadSoundFile(SoundType soundType, String filePath) {
        try {
            File soundFile = new File(filePath);
            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                soundClips.put(soundType, clip);
                soundStreams.put(soundType, audioStream);
            }
        } catch (Exception e) {
            System.err.println("サウンドファイル読み込みエラー: " + filePath + " - " + e.getMessage());
        }
    }

    /**
     * 仮想サウンドの生成（実際のファイルがない場合のデモ用）
     */
    public void generateVirtualSounds() {
        // 実際のサウンドファイルがない場合でも、
        // ゲームが動作するように仮想的なサウンドを生成
        // 本番環境では実際のWAVファイルを使用

        for (SoundType soundType : SoundType.values()) {
            try {
                // 短い無音のクリップを作成（実装のプレースホルダー）
                AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
                byte[] data = new byte[format.getFrameSize() * 1000]; // 約0.02秒の無音
                AudioInputStream stream = new AudioInputStream(
                        new ByteArrayInputStream(data),
                        format,
                        data.length / format.getFrameSize());

                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                soundClips.put(soundType, clip);

            } catch (Exception e) {
                System.err.println("仮想サウンド生成エラー: " + e.getMessage());
            }
        }
    }

    // ゲッター・セッター
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public void setEffectVolume(float volume) {
        this.effectVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        // 現在再生中のBGMの音量を更新
        if (currentBGM != null && currentBGM.isRunning()) {
            setClipVolume(currentBGM, musicVolume * masterVolume);
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) {
            stopAllSounds();
        }
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * リソースのクリーンアップ
     */
    public void dispose() {
        stopAllSounds();

        for (Clip clip : soundClips.values()) {
            clip.close();
        }

        for (AudioInputStream stream : soundStreams.values()) {
            try {
                stream.close();
            } catch (IOException e) {
                // 無視
            }
        }

        soundClips.clear();
        soundStreams.clear();
    }
}