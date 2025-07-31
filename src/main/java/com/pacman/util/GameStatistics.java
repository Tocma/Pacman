package main.java.com.pacman.util;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ゲームプレイの統計情報を追跡・管理するクラス
 * プレイ時間、クリア数、実績などを記録
 */
public class GameStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String STATS_FILE = "pacman_statistics.dat";

    // 基本統計
    private int totalGamesPlayed;
    private int totalGamesWon;
    private int totalScore;
    private int highestLevel;
    private long totalPlayTimeSeconds;
    private LocalDateTime firstPlayDate;
    private LocalDateTime lastPlayDate;

    // 詳細統計
    private int totalPelletsEaten;
    private int totalPowerPelletsEaten;
    private int totalGhostsEaten;
    private int totalFruitsCollected;
    private int totalDeaths;
    private Map<String, Integer> fruitCollectionCount;

    // 実績トラッキング
    private Set<String> unlockedAchievements;

    // セッション統計（現在のゲームセッション）
    private transient int sessionScore;
    private transient int sessionPelletsEaten;
    private transient int sessionGhostsEaten;
    private transient LocalDateTime sessionStartTime;

    // シングルトンインスタンス
    private static GameStatistics instance;

    /**
     * 実績の定義
     */
    public enum Achievement {
        FIRST_WIN("First Victory", "Complete your first game"),
        PELLET_MASTER("Pellet Master", "Eat 10,000 pellets"),
        GHOST_HUNTER("Ghost Hunter", "Eat 1,000 ghosts"),
        FRUIT_COLLECTOR("Fruit Collector", "Collect 100 fruits"),
        SCORE_10K("Score Master", "Reach 10,000 points in a single game"),
        SCORE_50K("Score Champion", "Reach 50,000 points in a single game"),
        SCORE_100K("Score Legend", "Reach 100,000 points in a single game"),
        LEVEL_10("Level 10", "Reach level 10"),
        LEVEL_20("Level 20", "Reach level 20"),
        PERFECT_LEVEL("Perfect Clear", "Clear a level without dying"),
        SPEED_RUN("Speed Runner", "Clear level 1 in under 30 seconds"),
        MARATHON("Marathon Player", "Play for 1 hour total"),
        DEDICATED("Dedicated Player", "Play 100 games"),
        GHOST_COMBO("Ghost Combo", "Eat all 4 ghosts in one power pellet"),
        SURVIVOR("Survivor", "Survive for 5 minutes without dying");

        private final String name;
        private final String description;

        Achievement(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * プライベートコンストラクタ（シングルトンパターン）
     */
    private GameStatistics() {
        initializeStatistics();
        loadStatistics();
    }

    /**
     * シングルトンインスタンスの取得
     */
    public static synchronized GameStatistics getInstance() {
        if (instance == null) {
            instance = new GameStatistics();
        }
        return instance;
    }

    /**
     * 統計の初期化
     */
    private void initializeStatistics() {
        totalGamesPlayed = 0;
        totalGamesWon = 0;
        totalScore = 0;
        highestLevel = 0;
        totalPlayTimeSeconds = 0;
        firstPlayDate = null;
        lastPlayDate = null;

        totalPelletsEaten = 0;
        totalPowerPelletsEaten = 0;
        totalGhostsEaten = 0;
        totalFruitsCollected = 0;
        totalDeaths = 0;

        fruitCollectionCount = new HashMap<>();
        unlockedAchievements = new HashSet<>();

        sessionScore = 0;
        sessionPelletsEaten = 0;
        sessionGhostsEaten = 0;
    }

    /**
     * 統計ファイルから読み込み
     */
    private void loadStatistics() {
        File file = new File(STATS_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            GameStatistics loaded = (GameStatistics) ois.readObject();
            copyFrom(loaded);
        } catch (Exception e) {
            System.err.println("統計ファイル読み込みエラー: " + e.getMessage());
        }
    }

    /**
     * 統計ファイルへ保存
     */
    public void saveStatistics() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STATS_FILE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("統計ファイル保存エラー: " + e.getMessage());
        }
    }

    /**
     * 他のインスタンスから統計をコピー
     */
    private void copyFrom(GameStatistics other) {
        this.totalGamesPlayed = other.totalGamesPlayed;
        this.totalGamesWon = other.totalGamesWon;
        this.totalScore = other.totalScore;
        this.highestLevel = other.highestLevel;
        this.totalPlayTimeSeconds = other.totalPlayTimeSeconds;
        this.firstPlayDate = other.firstPlayDate;
        this.lastPlayDate = other.lastPlayDate;

        this.totalPelletsEaten = other.totalPelletsEaten;
        this.totalPowerPelletsEaten = other.totalPowerPelletsEaten;
        this.totalGhostsEaten = other.totalGhostsEaten;
        this.totalFruitsCollected = other.totalFruitsCollected;
        this.totalDeaths = other.totalDeaths;

        this.fruitCollectionCount = new HashMap<>(other.fruitCollectionCount);
        this.unlockedAchievements = new HashSet<>(other.unlockedAchievements);
    }

    /**
     * ゲームセッション開始
     */
    public void startGameSession() {
        totalGamesPlayed++;
        sessionScore = 0;
        sessionPelletsEaten = 0;
        sessionGhostsEaten = 0;
        sessionStartTime = LocalDateTime.now();

        if (firstPlayDate == null) {
            firstPlayDate = sessionStartTime;
        }
        lastPlayDate = sessionStartTime;
    }

    /**
     * ゲームセッション終了
     */
    public void endGameSession(boolean won, int finalScore, int level) {
        if (sessionStartTime != null) {
            long sessionDuration = Duration.between(sessionStartTime, LocalDateTime.now()).getSeconds();
            totalPlayTimeSeconds += sessionDuration;
        }

        if (won) {
            totalGamesWon++;
        }

        totalScore += finalScore;
        sessionScore = finalScore;

        if (level > highestLevel) {
            highestLevel = level;
        }

        // 実績のチェック
        checkAchievements(won, finalScore, level);

        saveStatistics();
    }

    /**
     * ペレット取得記録
     */
    public void recordPelletEaten(boolean isPowerPellet) {
        if (isPowerPellet) {
            totalPowerPelletsEaten++;
        } else {
            totalPelletsEaten++;
        }
        sessionPelletsEaten++;
    }

    /**
     * ゴースト撃退記録
     */
    public void recordGhostEaten() {
        totalGhostsEaten++;
        sessionGhostsEaten++;
    }

    /**
     * フルーツ取得記録
     */
    public void recordFruitCollected(String fruitType) {
        totalFruitsCollected++;
        fruitCollectionCount.merge(fruitType, 1, Integer::sum);
    }

    /**
     * 死亡記録
     */
    public void recordDeath() {
        totalDeaths++;
    }

    /**
     * 実績のチェックと解除
     */
    private void checkAchievements(boolean won, int score, int level) {
        // First Victory
        if (won && totalGamesWon == 1) {
            unlockAchievement(Achievement.FIRST_WIN.name());
        }

        // Pellet Master
        if (totalPelletsEaten >= 10000) {
            unlockAchievement(Achievement.PELLET_MASTER.name());
        }

        // Ghost Hunter
        if (totalGhostsEaten >= 1000) {
            unlockAchievement(Achievement.GHOST_HUNTER.name());
        }

        // Fruit Collector
        if (totalFruitsCollected >= 100) {
            unlockAchievement(Achievement.FRUIT_COLLECTOR.name());
        }

        // Score achievements
        if (score >= 10000) {
            unlockAchievement(Achievement.SCORE_10K.name());
        }
        if (score >= 50000) {
            unlockAchievement(Achievement.SCORE_50K.name());
        }
        if (score >= 100000) {
            unlockAchievement(Achievement.SCORE_100K.name());
        }

        // Level achievements
        if (level >= 10) {
            unlockAchievement(Achievement.LEVEL_10.name());
        }
        if (level >= 20) {
            unlockAchievement(Achievement.LEVEL_20.name());
        }

        // Marathon Player
        if (totalPlayTimeSeconds >= 3600) { // 1 hour
            unlockAchievement(Achievement.MARATHON.name());
        }

        // Dedicated Player
        if (totalGamesPlayed >= 100) {
            unlockAchievement(Achievement.DEDICATED.name());
        }
    }

    /**
     * 実績の解除
     */
    private void unlockAchievement(String achievementName) {
        if (!unlockedAchievements.contains(achievementName)) {
            unlockedAchievements.add(achievementName);
            // 実績解除の通知（UIに通知する仕組みを追加可能）
            System.out.println("Achievement Unlocked: " + achievementName);
        }
    }

    /**
     * 特定の実績が解除されているかチェック
     */
    public boolean isAchievementUnlocked(Achievement achievement) {
        return unlockedAchievements.contains(achievement.name());
    }

    /**
     * 統計の概要を取得
     */
    public String getStatisticsSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== GAME STATISTICS ===\n\n");

        sb.append("Games Played: ").append(totalGamesPlayed).append("\n");
        sb.append("Games Won: ").append(totalGamesWon).append("\n");
        sb.append("Win Rate: ").append(String.format("%.1f%%", getWinRate() * 100)).append("\n");
        sb.append("Total Score: ").append(totalScore).append("\n");
        sb.append("Average Score: ").append(getAverageScore()).append("\n");
        sb.append("Highest Level: ").append(highestLevel).append("\n");
        sb.append("Total Play Time: ").append(formatPlayTime()).append("\n\n");

        sb.append("Pellets Eaten: ").append(totalPelletsEaten).append("\n");
        sb.append("Power Pellets Eaten: ").append(totalPowerPelletsEaten).append("\n");
        sb.append("Ghosts Eaten: ").append(totalGhostsEaten).append("\n");
        sb.append("Fruits Collected: ").append(totalFruitsCollected).append("\n");
        sb.append("Total Deaths: ").append(totalDeaths).append("\n\n");

        sb.append("Achievements Unlocked: ").append(unlockedAchievements.size())
                .append(" / ").append(Achievement.values().length).append("\n");

        return sb.toString();
    }

    // ヘルパーメソッド

    public double getWinRate() {
        return totalGamesPlayed > 0 ? (double) totalGamesWon / totalGamesPlayed : 0.0;
    }

    public int getAverageScore() {
        return totalGamesPlayed > 0 ? totalScore / totalGamesPlayed : 0;
    }

    private String formatPlayTime() {
        long hours = totalPlayTimeSeconds / 3600;
        long minutes = (totalPlayTimeSeconds % 3600) / 60;
        long seconds = totalPlayTimeSeconds % 60;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    // ゲッターメソッド
    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }

    public int getTotalGamesWon() {
        return totalGamesWon;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getHighestLevel() {
        return highestLevel;
    }

    public long getTotalPlayTimeSeconds() {
        return totalPlayTimeSeconds;
    }

    public Set<String> getUnlockedAchievements() {
        return new HashSet<>(unlockedAchievements);
    }
}