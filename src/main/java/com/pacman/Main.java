package main.java.com.pacman;

import main.java.com.pacman.ui.GameWindow;
import javax.swing.*;

/**
 * アプリケーションのエントリーポイント
 * Swingアプリケーションの適切な初期化と起動を行う
 */
public class Main {

    /**
     * メインメソッド
     * 
     * @param args コマンドライン引数（使用しない）
     */
    public static void main(String[] args) {
        // Swingアプリケーションは Event Dispatch Thread (EDT) で実行する必要がある
        SwingUtilities.invokeLater(() -> {
            try {
                // システムのルック＆フィールを使用
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // ルック＆フィールの設定に失敗した場合はデフォルトを使用
                System.err.println("Failed to set system look and feel: " + e.getMessage());
            }

            // ゲームウィンドウの作成と表示
            GameWindow gameWindow = new GameWindow();
            gameWindow.setVisible(true);

            // ゲームを自動的に開始
            gameWindow.startGame();
        });
    }
}