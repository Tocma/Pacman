package main.java.com.pacman.effects;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * ゲーム内のビジュアルエフェクトを管理するクラス
 * スコア表示、パーティクル、フェード効果などを制御
 */
public class EffectManager {
    // エフェクトのリスト
    private List<Effect> activeEffects;
    private List<ScorePopup> scorePopups;
    private List<Particle> particles;

    // フェード効果
    private float fadeAlpha = 0.0f;
    private boolean fadeIn = false;
    private boolean fadeOut = false;
    private float fadeSpeed = 0.02f;

    /**
     * エフェクトの基底クラス
     */
    public abstract class Effect {
        protected float x, y;
        protected int lifeTime;
        protected int maxLifeTime;
        protected boolean alive = true;

        public Effect(float x, float y, int lifeTime) {
            this.x = x;
            this.y = y;
            this.lifeTime = lifeTime;
            this.maxLifeTime = lifeTime;
        }

        public abstract void update();

        public abstract void render(Graphics2D g);

        public boolean isAlive() {
            return alive;
        }
    }

    /**
     * スコアポップアップエフェクト
     */
    public class ScorePopup extends Effect {
        private String scoreText;
        private Color color;
        private Font font;
        private float velocity;

        public ScorePopup(float x, float y, int score, Color color) {
            super(x, y, 60); // 60フレーム（約1秒）表示
            this.scoreText = String.valueOf(score);
            this.color = color;
            this.font = new Font("Arial", Font.BOLD, 14);
            this.velocity = -1.0f; // 上方向への移動速度
        }

        @Override
        public void update() {
            y += velocity;
            velocity *= 0.95f; // 減速効果

            lifeTime--;
            if (lifeTime <= 0) {
                alive = false;
            }
        }

        @Override
        public void render(Graphics2D g) {
            // フェードアウト効果
            float alpha = (float) lifeTime / maxLifeTime;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            g.setColor(color);
            g.setFont(font);

            // テキストの描画（中央揃え）
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(scoreText);
            g.drawString(scoreText, (int) (x - textWidth / 2), (int) y);

            // アルファ値をリセット
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    /**
     * パーティクルエフェクト
     */
    public class Particle extends Effect {
        private float vx, vy; // 速度
        private Color color;
        private float size;
        private float gravity = 0.1f;

        public Particle(float x, float y, float vx, float vy, Color color, int lifeTime) {
            super(x, y, lifeTime);
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.size = 4.0f;
        }

        @Override
        public void update() {
            x += vx;
            y += vy;
            vy += gravity; // 重力効果

            // サイズの縮小
            size *= 0.98f;

            lifeTime--;
            if (lifeTime <= 0 || size < 0.5f) {
                alive = false;
            }
        }

        @Override
        public void render(Graphics2D g) {
            float alpha = (float) lifeTime / maxLifeTime;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            g.setColor(color);
            g.fillOval((int) (x - size / 2), (int) (y - size / 2), (int) size, (int) size);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    /**
     * コンストラクタ
     */
    public EffectManager() {
        activeEffects = new ArrayList<>();
        scorePopups = new ArrayList<>();
        particles = new ArrayList<>();
    }

    /**
     * スコアポップアップの追加
     */
    public void addScorePopup(float x, float y, int score) {
        Color color = Color.WHITE;
        if (score >= 1600) {
            color = Color.CYAN;
        } else if (score >= 800) {
            color = Color.YELLOW;
        } else if (score >= 400) {
            color = Color.ORANGE;
        } else if (score >= 200) {
            color = Color.GREEN;
        }

        ScorePopup popup = new ScorePopup(x, y, score, color);
        scorePopups.add(popup);
        activeEffects.add(popup);
    }

    /**
     * パーティクル爆発エフェクトの生成
     */
    public void createExplosion(float x, float y, Color color, int particleCount) {
        Random random = new Random();

        for (int i = 0; i < particleCount; i++) {
            float angle = (float) (Math.PI * 2 * i / particleCount);
            float speed = 2.0f + random.nextFloat() * 2.0f;
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed;

            Particle particle = new Particle(x, y, vx, vy, color, 30 + random.nextInt(20));
            particles.add(particle);
            activeEffects.add(particle);
        }
    }

    /**
     * ゴースト消滅エフェクト
     */
    public void createGhostEatenEffect(float x, float y) {
        // 青いパーティクルの爆発
        createExplosion(x, y, Color.CYAN, 12);

        // 追加の白いスパークル
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            float vx = (random.nextFloat() - 0.5f) * 4;
            float vy = (random.nextFloat() - 0.5f) * 4 - 2;
            Particle particle = new Particle(x, y, vx, vy, Color.WHITE, 40);
            particles.add(particle);
            activeEffects.add(particle);
        }
    }

    /**
     * パワーペレット取得エフェクト
     */
    public void createPowerPelletEffect(float x, float y) {
        // 黄色い光の輪
        for (int i = 0; i < 8; i++) {
            float angle = (float) (Math.PI * 2 * i / 8);
            float vx = (float) Math.cos(angle) * 1.5f;
            float vy = (float) Math.sin(angle) * 1.5f;

            Particle particle = new Particle(x, y, vx, vy, Color.YELLOW, 25);
            particles.add(particle);
            activeEffects.add(particle);
        }
    }

    /**
     * レベルクリアエフェクト（虹色の花火）
     */
    public void createLevelClearEffect(float x, float y) {
        Color[] colors = { Color.RED, Color.ORANGE, Color.YELLOW,
                Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA };

        Random random = new Random();
        for (Color color : colors) {
            for (int i = 0; i < 5; i++) {
                float angle = random.nextFloat() * (float) Math.PI * 2;
                float speed = 3.0f + random.nextFloat() * 3.0f;
                float vx = (float) Math.cos(angle) * speed;
                float vy = (float) Math.sin(angle) * speed - 2;

                Particle particle = new Particle(x, y, vx, vy, color, 60 + random.nextInt(30));
                particles.add(particle);
                activeEffects.add(particle);
            }
        }
    }

    /**
     * フェードイン開始
     */
    public void startFadeIn() {
        fadeIn = true;
        fadeOut = false;
        fadeAlpha = 1.0f;
    }

    /**
     * フェードアウト開始
     */
    public void startFadeOut() {
        fadeOut = true;
        fadeIn = false;
        fadeAlpha = 0.0f;
    }

    /**
     * エフェクトの更新
     */
    public void update() {
        // エフェクトの更新と削除
        Iterator<Effect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            Effect effect = iterator.next();
            effect.update();

            if (!effect.isAlive()) {
                iterator.remove();
                if (effect instanceof ScorePopup) {
                    scorePopups.remove(effect);
                } else if (effect instanceof Particle) {
                    particles.remove(effect);
                }
            }
        }

        // フェード効果の更新
        if (fadeIn) {
            fadeAlpha -= fadeSpeed;
            if (fadeAlpha <= 0.0f) {
                fadeAlpha = 0.0f;
                fadeIn = false;
            }
        } else if (fadeOut) {
            fadeAlpha += fadeSpeed;
            if (fadeAlpha >= 1.0f) {
                fadeAlpha = 1.0f;
                fadeOut = false;
            }
        }
    }

    /**
     * エフェクトの描画
     */
    public void render(Graphics2D g) {
        // パーティクルの描画
        for (Particle particle : particles) {
            particle.render(g);
        }

        // スコアポップアップの描画
        for (ScorePopup popup : scorePopups) {
            popup.render(g);
        }

        // フェード効果の描画
        if (fadeAlpha > 0.0f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 800, 600); // 画面サイズに応じて調整
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }

    /**
     * すべてのエフェクトをクリア
     */
    public void clear() {
        activeEffects.clear();
        scorePopups.clear();
        particles.clear();
    }
}