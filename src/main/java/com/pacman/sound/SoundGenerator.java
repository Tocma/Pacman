package main.java.com.pacman.sound;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * シンプルなサウンドエフェクトを生成するユーティリティクラス
 * 実際のサウンドファイルがない場合に、基本的な効果音を動的に生成
 */
public class SoundGenerator {

    // オーディオフォーマット
    private static final float SAMPLE_RATE = 44100.0f;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    /**
     * サウンドエフェクトのタイプ
     */
    public enum SoundEffect {
        BEEP(440, 0.1f), // シンプルなビープ音
        PELLET(800, 0.05f), // ペレット取得音（高音の短いビープ）
        POWER_PELLET(400, 0.2f), // パワーペレット（低音の長めのビープ）
        GHOST_EAT(300, 0.3f), // ゴースト捕食（低音）
        DEATH(200, 0.5f), // 死亡音（下降音）
        LEVEL_CLEAR(600, 0.4f), // レベルクリア（上昇音）
        FRUIT(1000, 0.15f), // フルーツ取得（高音）
        SIREN(220, 1.0f), // サイレン（繰り返し音）
        START(523, 0.3f); // ゲーム開始（C音）

        private final float frequency;
        private final float duration;

        SoundEffect(float frequency, float duration) {
            this.frequency = frequency;
            this.duration = duration;
        }
    }

    /**
     * 指定されたサウンドエフェクトのClipを生成
     */
    public static Clip generateSoundClip(SoundEffect effect) {
        try {
            byte[] soundData = null;

            switch (effect) {
                case PELLET:
                    soundData = generateSimpleTone(effect.frequency, effect.duration, 0.3f);
                    break;
                case POWER_PELLET:
                    soundData = generatePowerPelletSound();
                    break;
                case GHOST_EAT:
                    soundData = generateGhostEatSound();
                    break;
                case DEATH:
                    soundData = generateDeathSound();
                    break;
                case LEVEL_CLEAR:
                    soundData = generateLevelClearSound();
                    break;
                case FRUIT:
                    soundData = generateFruitSound();
                    break;
                case SIREN:
                    soundData = generateSirenSound();
                    break;
                case START:
                    soundData = generateStartSound();
                    break;
                default:
                    soundData = generateSimpleTone(effect.frequency, effect.duration, 0.5f);
            }

            // AudioInputStreamを作成
            AudioFormat format = new AudioFormat(
                    SAMPLE_RATE,
                    SAMPLE_SIZE_IN_BITS,
                    CHANNELS,
                    SIGNED,
                    BIG_ENDIAN);

            AudioInputStream audioStream = new AudioInputStream(
                    new ByteArrayInputStream(soundData),
                    format,
                    soundData.length / format.getFrameSize());

            // Clipを作成して返す
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            return clip;

        } catch (Exception e) {
            System.err.println("サウンド生成エラー: " + e.getMessage());
            return null;
        }
    }

    /**
     * シンプルなトーン音を生成
     */
    private static byte[] generateSimpleTone(float frequency, float duration, float amplitude) {
        int numSamples = (int) (duration * SAMPLE_RATE);
        byte[] soundData = new byte[numSamples * 2]; // 16ビット = 2バイト

        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i / (SAMPLE_RATE / frequency);
            short sample = (short) (Math.sin(angle) * 32767 * amplitude);

            // エンベロープ（フェードイン/アウト）
            float envelope = 1.0f;
            if (i < numSamples * 0.1) {
                envelope = i / (numSamples * 0.1f);
            } else if (i > numSamples * 0.9) {
                envelope = (numSamples - i) / (numSamples * 0.1f);
            }
            sample = (short) (sample * envelope);

            // バイト配列に変換（リトルエンディアン）
            soundData[i * 2] = (byte) (sample & 0xFF);
            soundData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        return soundData;
    }

    /**
     * パワーペレット音（振動効果）
     */
    private static byte[] generatePowerPelletSound() {
        float duration = 0.3f;
        int numSamples = (int) (duration * SAMPLE_RATE);
        byte[] soundData = new byte[numSamples * 2];

        for (int i = 0; i < numSamples; i++) {
            // 周波数変調
            float modulation = (float) Math.sin(2 * Math.PI * 10 * i / SAMPLE_RATE);
            float frequency = 400 + modulation * 100;

            double angle = 2.0 * Math.PI * i / (SAMPLE_RATE / frequency);
            short sample = (short) (Math.sin(angle) * 20000);

            // エンベロープ
            float envelope = (float) Math.exp(-i / (numSamples * 0.5));
            sample = (short) (sample * envelope);

            soundData[i * 2] = (byte) (sample & 0xFF);
            soundData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        return soundData;
    }

    /**
     * ゴースト捕食音（下降音）
     */
    private static byte[] generateGhostEatSound() {
        float duration = 0.3f;
        int numSamples = (int) (duration * SAMPLE_RATE);
        byte[] soundData = new byte[numSamples * 2];

        for (int i = 0; i < numSamples; i++) {
            // 周波数が下降
            float frequency = 600 - (i / (float) numSamples) * 300;

            double angle = 2.0 * Math.PI * i / (SAMPLE_RATE / frequency);
            short sample = (short) (Math.sin(angle) * 25000);

            // エンベロープ
            float envelope = 1.0f - (i / (float) numSamples) * 0.5f;
            sample = (short) (sample * envelope);

            soundData[i * 2] = (byte) (sample & 0xFF);
            soundData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        return soundData;
    }

    /**
     * 死亡音（複雑な下降音）
     */
    private static byte[] generateDeathSound() {
        float duration = 0.8f;
        int numSamples = (int) (duration * SAMPLE_RATE);
        byte[] soundData = new byte[numSamples * 2];

        for (int i = 0; i < numSamples; i++) {
            float progress = i / (float) numSamples;

            // 複数の周波数を混合
            float freq1 = 400 * (1 - progress * 0.5f);
            float freq2 = 600 * (1 - progress * 0.7f);

            double angle1 = 2.0 * Math.PI * i / (SAMPLE_RATE / freq1);
            double angle2 = 2.0 * Math.PI * i / (SAMPLE_RATE / freq2);

            short sample = (short) ((Math.sin(angle1) * 15000 + Math.sin(angle2) * 10000) *
                    (1 - progress));

            soundData[i * 2] = (byte) (sample & 0xFF);
            soundData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        return soundData;
    }

    /**
     * レベルクリア音（上昇音階）
     */
    private static byte[] generateLevelClearSound() {
        float duration = 0.6f;
        int numSamples = (int) (duration * SAMPLE_RATE);
        byte[] soundData = new byte[numSamples * 2];

        // 音階（C, E, G, C）
        float[] frequencies = { 261.63f, 329.63f, 392.00f, 523.25f };
        int samplesPerNote = numSamples / frequencies.length;

        for (int i = 0; i < numSamples; i++) {
            int noteIndex = i / samplesPerNote;
            if (noteIndex >= frequencies.length)
                noteIndex = frequencies.length - 1;

            float frequency = frequencies[noteIndex];
            double angle = 2.0 * Math.PI * i / (SAMPLE_RATE / frequency);
            short sample = (short) (Math.sin(angle) * 20000);

            // 各音符のエンベロープ
            int notePosition = i % samplesPerNote;
            float envelope = 1.0f;
            if (notePosition < samplesPerNote * 0.1) {
                envelope = notePosition / (samplesPerNote * 0.1f);
            }

            sample = (short) (sample * envelope);

            soundData[i * 2] = (byte) (sample & 0xFF);
            soundData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        return soundData;
    }

    /**
     * フルーツ取得音（きらきら音）
     */
    private static byte[] generateFruitSound() {
        float duration = 0.2f;
        int numSamples = (int) (duration * SAMPLE_RATE);
        byte[] soundData = new byte[numSamples * 2];

        for (int i = 0; i < numSamples; i++) {
            // 高周波数の組み合わせ
            float freq1 = 800 + (float) Math.sin(i * 0.01) * 200;
            float freq2 = 1200 + (float) Math.sin(i * 0.02) * 300;

            double angle1 = 2.0 * Math.PI * i / (SAMPLE_RATE / freq1);
            double angle2 = 2.0 * Math.PI * i / (SAMPLE_RATE / freq2);

            short sample = (short) ((Math.sin(angle1) * 10000 + Math.sin(angle2) * 8000) *
                    Math.exp(-i / (numSamples * 0.3)));

            soundData[i * 2] = (byte) (sample & 0xFF);
            soundData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        return soundData;
    }

    /**
     * サイレン音（繰り返し音）
     */
    private static byte[] generateSirenSound() {
        float duration = 2.0f; // 2秒のループ
        int numSamples = (int) (duration * SAMPLE_RATE);
        byte[] soundData = new byte[numSamples * 2];

        for (int i = 0; i < numSamples; i++) {
            // サイレンの周波数変化
            float modulation = (float) Math.sin(2 * Math.PI * 2 * i / SAMPLE_RATE);
            float frequency = 220 + modulation * 50;

            double angle = 2.0 * Math.PI * i / (SAMPLE_RATE / frequency);
            short sample = (short) (Math.sin(angle) * 15000);

            soundData[i * 2] = (byte) (sample & 0xFF);
            soundData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        return soundData;
    }

    /**
     * ゲーム開始音（ファンファーレ）
     */
    private static byte[] generateStartSound() {
        float duration = 0.8f;
        int numSamples = (int) (duration * SAMPLE_RATE);
        byte[] soundData = new byte[numSamples * 2];

        // 開始音のメロディ
        float[] frequencies = { 523.25f, 523.25f, 659.25f, 783.99f };
        float[] durations = { 0.15f, 0.15f, 0.15f, 0.35f };

        int sampleIndex = 0;
        for (int note = 0; note < frequencies.length && sampleIndex < numSamples; note++) {
            int noteSamples = (int) (durations[note] * SAMPLE_RATE);

            for (int i = 0; i < noteSamples && sampleIndex < numSamples; i++) {
                double angle = 2.0 * Math.PI * i / (SAMPLE_RATE / frequencies[note]);
                short sample = (short) (Math.sin(angle) * 20000);

                // エンベロープ
                float envelope = 1.0f;
                if (i < noteSamples * 0.1) {
                    envelope = i / (noteSamples * 0.1f);
                } else if (i > noteSamples * 0.8) {
                    envelope = (noteSamples - i) / (noteSamples * 0.2f);
                }

                sample = (short) (sample * envelope);

                soundData[sampleIndex * 2] = (byte) (sample & 0xFF);
                soundData[sampleIndex * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
                sampleIndex++;
            }
        }

        return soundData;
    }

    /**
     * SoundManagerに生成したサウンドを登録
     */
    public static void registerGeneratedSounds(SoundManager soundManager) {
        // 各サウンドタイプに対応する効果音を生成して登録
        registerSound(soundManager, SoundManager.SoundType.PELLET_EAT, SoundEffect.PELLET);
        registerSound(soundManager, SoundManager.SoundType.POWER_PELLET, SoundEffect.POWER_PELLET);
        registerSound(soundManager, SoundManager.SoundType.GHOST_EAT, SoundEffect.GHOST_EAT);
        registerSound(soundManager, SoundManager.SoundType.PACMAN_DEATH, SoundEffect.DEATH);
        registerSound(soundManager, SoundManager.SoundType.LEVEL_CLEAR, SoundEffect.LEVEL_CLEAR);
        registerSound(soundManager, SoundManager.SoundType.EXTRA_LIFE, SoundEffect.FRUIT);
        registerSound(soundManager, SoundManager.SoundType.SIREN, SoundEffect.SIREN);
        registerSound(soundManager, SoundManager.SoundType.GAME_START, SoundEffect.START);
        registerSound(soundManager, SoundManager.SoundType.POWER_MODE, SoundEffect.POWER_PELLET);
    }

    /**
     * 個別のサウンドを登録
     */
    private static void registerSound(SoundManager soundManager,
            SoundManager.SoundType soundType,
            SoundEffect effect) {
        Clip clip = generateSoundClip(effect);
        if (clip != null) {
            // SoundManagerのsoundClipsマップに直接アクセスする方法が必要
            // 実際の実装では、SoundManagerにsetClipメソッドを追加する必要がある
            System.out.println("Generated sound for: " + soundType);
        }
    }
}