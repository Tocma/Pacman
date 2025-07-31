package main.java.com.pacman.game;

/**
 * ゲームの状態を表す列挙型
 * ゲームの進行状態を管理し、適切な処理を行うために使用
 */
public enum GameState {
    /** ゲーム開始前の初期状態 */
    READY,

    /** ゲームプレイ中 */
    PLAYING,

    /** パックマンがゴーストに捕まった後の一時停止状態 */
    PACMAN_DIED,

    /** レベルクリア時の状態 */
    LEVEL_CLEAR,

    /** ゲームオーバー状態 */
    GAME_OVER,

    /** ゲーム一時停止状態 */
    PAUSED
}