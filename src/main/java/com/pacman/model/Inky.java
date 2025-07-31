package main.java.com.pacman.model;

import main.java.com.pacman.game.Direction;
import java.awt.Color;
import java.awt.Point;

/**
 * Inky（青ゴースト）- "Bashful"
 * 最も複雑なAIを持つゴースト
 * Blinkyの位置とパックマンの位置を使って目標地点を計算する
 */
public class Inky extends Ghost {

    public Inky(int startX, int startY) {
        super("Inky", new Color(0, 255, 255), startX, startY); // シアン色
    }

    /**
     * 散開モードでの目標位置
     * Inkyは右下コーナーを目指す
     */
    @Override
    protected Point getScatterTarget() {
        return new Point(Maze.WIDTH - 1, Maze.HEIGHT - 1);
    }

    /**
     * 追跡モードでの目標位置
     * 複雑なアルゴリズム：
     * 1. パックマンの2タイル先の位置を取得
     * 2. Blinkyからその位置へのベクトルを計算
     * 3. そのベクトルを2倍に延長した地点が目標
     */
    @Override
    protected Point getChaseTarget(Pacman pacman) {
        // まずパックマンの2タイル先を計算
        Point pacmanPos = pacman.getGridPosition();
        Direction pacmanDir = pacman.getCurrentDirection();

        int pivotX = pacmanPos.x;
        int pivotY = pacmanPos.y;

        // パックマンの向きに応じて2タイル先を計算
        switch (pacmanDir) {
            case UP:
                pivotY -= 2;
                // オリジナルのバグを再現：上方向の時は左にも2タイルずれる
                pivotX -= 2;
                break;
            case DOWN:
                pivotY += 2;
                break;
            case LEFT:
                pivotX -= 2;
                break;
            case RIGHT:
                pivotX += 2;
                break;
            case NONE:
                // 停止中はパックマンの位置を基準点とする
                break;
        }

        // Blinkyの位置を取得（他のゴーストのリストから探す必要があるが、
        // ここでは簡略化のため、Blinkyがいない場合はパックマンを直接狙う）
        // 実際のゲームループでBlinkyの参照を渡す必要がある

        // 暫定的な実装：Blinkyの位置が取得できない場合は基準点をそのまま使用
        int targetX = pivotX * 2 - (int) x; // 自分の位置から基準点への延長
        int targetY = pivotY * 2 - (int) y;

        return new Point(targetX, targetY);
    }

    /**
     * Inkyは30個のペレットが食べられた後に出る
     */
    @Override
    protected boolean shouldExitHouse() {
        return dotCounter >= 30;
    }

    /**
     * Blinkyの位置を使った正確な計算（Game.javaから呼び出される）
     * 
     * @param pacman パックマン
     * @param blinky Blinkyゴースト
     * @return 目標位置
     */
    public Point getChaseTargetWithBlinky(Pacman pacman, Ghost blinky) {
        // パックマンの2タイル先を計算
        Point pacmanPos = pacman.getGridPosition();
        Direction pacmanDir = pacman.getCurrentDirection();

        int pivotX = pacmanPos.x;
        int pivotY = pacmanPos.y;

        switch (pacmanDir) {
            case UP:
                pivotY -= 2;
                pivotX -= 2; // オリジナルのバグ
                break;
            case DOWN:
                pivotY += 2;
                break;
            case LEFT:
                pivotX -= 2;
                break;
            case RIGHT:
                pivotX += 2;
                break;
            case NONE:
                // 停止中はパックマンの位置を基準点とする
                break;
        }

        // Blinkyの位置から基準点へのベクトルを2倍に延長
        Point blinkyPos = blinky.getGridPosition();
        int targetX = pivotX + (pivotX - blinkyPos.x);
        int targetY = pivotY + (pivotY - blinkyPos.y);

        return new Point(targetX, targetY);
    }
}