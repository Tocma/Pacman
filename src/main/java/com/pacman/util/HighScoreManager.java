package com.pacman.util;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ハイスコアの保存と読み込みを管理するクラス
 * スコアデータはローカルファイルに永続化される
 */
public class HighScoreManager {
    // ハイスコアエントリ
    public static class ScoreEntry implements Serializable, Comparable<ScoreEntry> {
        private static final long serialVersionUID = 1L;

        private String playerName;
        private int score;
        private int level;
        private LocalDateTime dateTime;

        public ScoreEntry(String playerName, int score, int level) {
            this.playerName = playerName;
            this.score = score;
            this.level = level;
            this.dateTime = LocalDateTime.now();
        }

        // ゲッター
        public String getPlayerName() {
            return playerName;
        }

        public int getScore() {
            return score;
        }

        public int getLevel() {
            return level;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        @Override
        public int compareTo(ScoreEntry other) {
            // スコアの降順でソート
            return Integer.compare(other.score, this.score);
        }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            return String.format("%-10s %8d  Lv%-3d  %s",
                    playerName, score, level, dateTime.format(formatter));
        }
    }

    // 定数
    private static final String HIGH_SCORE_FILE = "pacman_highscores.dat";
    private static final int MAX_ENTRIES = 10;

    // ハイスコアリスト
    private List<ScoreEntry> highScores;

    // シングルトンインスタンス
    private static HighScoreManager instance;

    /**
     * プライベートコンストラクタ（シングルトンパターン）
     */
    private HighScoreManager() {
        highScores = new ArrayList<>();
        loadHighScores();
    }

    /**
     * シングルトンインスタンスの取得
     */
    public static synchronized HighScoreManager getInstance() {
        if (instance == null) {
            instance = new HighScoreManager();
        }
        return instance;
    }

    /**
     * ハイスコアファイルから読み込み
     */
    @SuppressWarnings("unchecked")
    private void loadHighScores() {
        File file = new File(HIGH_SCORE_FILE);
        if (!file.exists()) {
            // ファイルが存在しない場合は初期データを作成
            initializeDefaultScores();
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            highScores = (List<ScoreEntry>) ois.readObject();
        } catch (Exception e) {
            System.err.println("ハイスコア読み込みエラー: " + e.getMessage());
            initializeDefaultScores();
        }
    }

    /**
     * ハイスコアファイルへ保存
     */
    private void saveHighScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HIGH_SCORE_FILE))) {
            oos.writeObject(highScores);
        } catch (IOException e) {
            System.err.println("ハイスコア保存エラー: " + e.getMessage());
        }
    }

    /**
     * デフォルトのハイスコアを初期化
     */
    private void initializeDefaultScores() {
        highScores.clear();
        // デモ用の初期スコア
        highScores.add(new ScoreEntry("NAMCO", 10000, 5));
        highScores.add(new ScoreEntry("PLAYER1", 8000, 4));
        highScores.add(new ScoreEntry("PLAYER2", 6000, 3));
        highScores.add(new ScoreEntry("PLAYER3", 4000, 2));
        highScores.add(new ScoreEntry("PLAYER4", 2000, 1));
        Collections.sort(highScores);
    }

    /**
     * 新しいスコアを追加
     * 
     * @return ハイスコアに登録された場合はその順位（1〜10）、そうでない場合は-1
     */
    public int addScore(String playerName, int score, int level) {
        ScoreEntry newEntry = new ScoreEntry(playerName, score, level);

        // スコアが最低ハイスコアより低い場合
        if (highScores.size() >= MAX_ENTRIES &&
                score <= highScores.get(highScores.size() - 1).getScore()) {
            return -1;
        }

        // 新しいエントリを追加してソート
        highScores.add(newEntry);
        Collections.sort(highScores);

        // 最大エントリ数を超えた場合は削除
        while (highScores.size() > MAX_ENTRIES) {
            highScores.remove(highScores.size() - 1);
        }

        // 保存
        saveHighScores();

        // 順位を返す
        for (int i = 0; i < highScores.size(); i++) {
            if (highScores.get(i) == newEntry) {
                return i + 1;
            }
        }

        return -1;
    }

    /**
     * ハイスコアに入るかチェック
     */
    public boolean isHighScore(int score) {
        if (highScores.size() < MAX_ENTRIES) {
            return true;
        }
        return score > highScores.get(highScores.size() - 1).getScore();
    }

    /**
     * 最高スコアを取得
     */
    public int getTopScore() {
        if (highScores.isEmpty()) {
            return 0;
        }
        return highScores.get(0).getScore();
    }

    /**
     * ハイスコアリストを取得（読み取り専用）
     */
    public List<ScoreEntry> getHighScores() {
        return Collections.unmodifiableList(highScores);
    }

    /**
     * ハイスコアをクリア
     */
    public void clearHighScores() {
        highScores.clear();
        saveHighScores();
    }

    /**
     * ハイスコア表示用のフォーマット済み文字列を取得
     */
    public String getFormattedHighScores() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== HIGH SCORES ===\n\n");
        sb.append("Rank  Name       Score    Level  Date\n");
        sb.append("----  ---------- -------- -----  -------------------\n");

        for (int i = 0; i < highScores.size(); i++) {
            sb.append(String.format("%2d.   %s\n", i + 1, highScores.get(i).toString()));
        }

        return sb.toString();
    }
}