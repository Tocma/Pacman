package com.pacman.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * フルーツボーナスアイテムを管理するクラス
 * レベルに応じて異なるフルーツが出現し、それぞれ異なるポイントを持つ
 */
public class Fruit {
    // フルーツの種類
    public enum FruitType {
        CHERRY(100, Color.RED, "Cherry"),
        STRAWBERRY(300, new Color(255, 105, 180), "Strawberry"),
        ORANGE(500, Color.ORANGE, "Orange"),
        APPLE(700, Color.GREEN, "Apple"),
        MELON(1000, new Color(50, 205, 50), "Melon"),
        GALAXIAN(2000, Color.CYAN, "Galaxian"),
        BELL(3000, Color.YELLOW, "Bell"),
        KEY(5000, new Color(255, 215, 0), "Key");

        private final int points;
        private final Color color;
        private final String name;

        FruitType(int points, Color color, String name) {
            this.points = points;
            this.color = color;
            this.name = name;
        }

        public int getPoints() {
            return points;
        }

        public Color getColor() {
            return color;
        }

        public String getName() {
            return name;
        }
    }

    // フルーツの状態
    private FruitType type;
    private int x, y;
    private boolean visible;
    private int displayTimer;
    private static final int DISPLAY_DURATION = 600; // 10秒間表示

    // アニメーション用
    private float animationTimer; // animationOffsetからanimationTimerに変更
    private float animationSpeed = 0.1f;

    /**
     * コンストラクタ
     */
    public Fruit() {
        this.visible = false;
        this.displayTimer = 0;
        this.animationTimer = 0; // animationOffsetからanimationTimerに変更
    }

    /**
     * フルーツを出現させる
     * 
     * @param level 現在のレベル
     * @param x     X座標
     * @param y     Y座標
     */
    public void spawn(int level, int x, int y) {
        this.x = x;
        this.y = y;
        this.visible = true;
        this.displayTimer = DISPLAY_DURATION;

        // レベルに応じたフルーツタイプを設定
        this.type = getFruitTypeByLevel(level);
    }

    /**
     * レベルに応じたフルーツタイプを取得
     */
    private FruitType getFruitTypeByLevel(int level) {
        if (level <= 0)
            return FruitType.CHERRY;

        // オリジナルのパックマンのフルーツ出現パターン
        switch ((level - 1) % 8) {
            case 0:
                return FruitType.CHERRY;
            case 1:
                return FruitType.STRAWBERRY;
            case 2:
                return FruitType.ORANGE;
            case 3:
                return FruitType.APPLE;
            case 4:
                return FruitType.MELON;
            case 5:
                return FruitType.GALAXIAN;
            case 6:
                return FruitType.BELL;
            case 7:
                return FruitType.KEY;
            default:
                return FruitType.KEY;
        }
    }

    /**
     * フルーツの更新処理
     */
    public void update() {
        if (!visible)
            return;

        // タイマーのカウントダウン
        displayTimer--;
        if (displayTimer <= 0) {
            visible = false;
        }

        // アニメーション更新（上下に揺れる）
        animationTimer += animationSpeed;
    }

    /**
     * フルーツの描画
     */
    public void render(Graphics2D g, int tileSize) {
        if (!visible)
            return;

        // animationOffsetをsin波で計算
        float animationOffset = (float) Math.sin(animationTimer) * 3;

        int screenX = x * tileSize + tileSize / 2;
        int screenY = (int) (y * tileSize + tileSize / 2 + animationOffset);

        // 点滅効果（残り時間が少ない時）
        if (displayTimer < 120 && (displayTimer / 10) % 2 == 0) {
            return; // 点滅のため描画をスキップ
        }

        // フルーツの形を描画（簡略化したシンボル）
        g.setColor(type.getColor());

        switch (type) {
            case CHERRY:
                drawCherry(g, screenX, screenY, tileSize);
                break;
            case STRAWBERRY:
                drawStrawberry(g, screenX, screenY, tileSize);
                break;
            case ORANGE:
                drawOrange(g, screenX, screenY, tileSize);
                break;
            case APPLE:
                drawApple(g, screenX, screenY, tileSize);
                break;
            case MELON:
                drawMelon(g, screenX, screenY, tileSize);
                break;
            case GALAXIAN:
                drawGalaxian(g, screenX, screenY, tileSize);
                break;
            case BELL:
                drawBell(g, screenX, screenY, tileSize);
                break;
            case KEY:
                drawKey(g, screenX, screenY, tileSize);
                break;
        }
    }

    // 各フルーツの描画メソッド（簡略化したシンボル）

    private void drawCherry(Graphics2D g, int x, int y, int size) {
        // チェリーの実
        g.fillOval(x - size / 4, y - size / 4, size / 2, size / 2);
        g.fillOval(x + size / 6, y - size / 4, size / 2, size / 2);
        // 茎
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.GREEN);
        g.drawLine(x, y - size / 4, x, y - size / 2);
        g.drawLine(x + size / 3, y - size / 4, x, y - size / 2);
    }

    private void drawStrawberry(Graphics2D g, int x, int y, int size) {
        // イチゴの形（逆三角形）
        int[] xPoints = { x, x - size / 3, x + size / 3 };
        int[] yPoints = { y + size / 3, y - size / 3, y - size / 3 };
        g.fillPolygon(xPoints, yPoints, 3);
        // 葉
        g.setColor(Color.GREEN);
        g.fillRect(x - size / 4, y - size / 3 - 3, size / 2, 3);
    }

    private void drawOrange(Graphics2D g, int x, int y, int size) {
        g.fillOval(x - size / 3, y - size / 3, size * 2 / 3, size * 2 / 3);
    }

    private void drawApple(Graphics2D g, int x, int y, int size) {
        g.fillOval(x - size / 3, y - size / 3, size * 2 / 3, size * 2 / 3);
        // 茎
        g.setColor(new Color(139, 69, 19));
        g.fillRect(x - 1, y - size / 2, 2, size / 6);
    }

    private void drawMelon(Graphics2D g, int x, int y, int size) {
        g.fillOval(x - size / 3, y - size / 3, size * 2 / 3, size / 2);
        // 縞模様
        g.setColor(g.getColor().darker());
        for (int i = -size / 3; i < size / 3; i += 4) {
            g.drawLine(x + i, y - size / 4, x + i, y + size / 4);
        }
    }

    private void drawGalaxian(Graphics2D g, int x, int y, int size) {
        // 宇宙船の形
        int[] xPoints = { x, x - size / 3, x + size / 3 };
        int[] yPoints = { y - size / 3, y + size / 3, y + size / 3 };
        g.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawBell(Graphics2D g, int x, int y, int size) {
        // ベルの形
        g.fillArc(x - size / 3, y - size / 3, size * 2 / 3, size * 2 / 3, 0, 180);
        g.fillRect(x - size / 3, y, size * 2 / 3, size / 4);
        // クラッパー
        g.fillOval(x - size / 12, y + size / 4, size / 6, size / 6);
    }

    private void drawKey(Graphics2D g, int x, int y, int size) {
        // 鍵の持ち手
        g.fillOval(x - size / 4, y - size / 3, size / 3, size / 3);
        g.setColor(Color.BLACK);
        g.fillOval(x - size / 6, y - size / 4, size / 6, size / 6);
        g.setColor(type.getColor());
        // 鍵の歯
        g.fillRect(x - 2, y - size / 6, 4, size / 2);
        g.fillRect(x - 4, y + size / 4, 8, 4);
    }

    /**
     * フルーツとの衝突判定
     */
    public boolean checkCollision(double pacmanX, double pacmanY) {
        if (!visible)
            return false;

        // ペレットと同様に、整数座標が完全に一致するかをチェック
        return pacmanX == x && pacmanY == y;
    }

    /**
     * フルーツを取得（消費）
     */
    public int collect() {
        if (!visible)
            return 0;
        visible = false;
        return type.getPoints();
    }

    // ゲッターメソッド
    public boolean isVisible() {
        return visible;
    }

    public FruitType getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}