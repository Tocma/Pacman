# Classic Pacman - Java Edition v2.0

1980 年のアーケード版パックマンを Java Swing と Maven で忠実に再現し、現代的な拡張機能を追加したゲームです。

## 新機能（v2.0）

### 🎵 サウンドシステム

- 動的に生成される効果音と BGM
- ペレット取得、パワーペレット、ゴースト捕食、死亡音など
- 音量調整機能（マスター、効果音、BGM 別）
- サウンドの ON/OFF 切り替え

### ✨ ビジュアルエフェクト

- パーティクルエフェクト（爆発、光の輪など）
- スコアポップアップアニメーション
- フェードイン/フェードアウト効果
- レベルクリア時の虹色エフェクト
- ゴーストの怯えモード時の点滅効果

### 🍒 フルーツボーナス

- レベルに応じた 8 種類のフルーツ
- チェリー（100 点）からキー（5000 点）まで
- アニメーション付きの出現と消滅

### 🏆 ハイスコア管理

- トップ 10 のハイスコアを永続保存
- プレイヤー名の登録
- 日時記録付き
- ハイスコアのクリア機能

### 📊 統計と実績システム

- 詳細なゲームプレイ統計
  - 総プレイ時間、勝率、平均スコア
  - ペレット・ゴースト・フルーツの累計
- 15 種類の実績
  - First Victory - 初勝利
  - Pellet Master - 10,000 個のペレット
  - Ghost Hunter - 1,000 体のゴースト撃退
  - Score Legend - 100,000 点達成
  - その他多数

### ⚙️ カスタマイズ可能な設定

- 難易度選択（Easy/Normal/Hard/Extreme）
- キーボード設定のカスタマイズ
- 表示設定（FPS 表示、パーティクルエフェクト）
- プレイヤー名の保存

## 基本機能

- オリジナルに忠実な 28×31 グリッドの迷路
- 4 種類のゴースト（Blinky, Pinky, Inky, Clyde）それぞれ独自の AI
- パワーペレットによるゴースト撃退システム
- スコアリングシステムと残機管理
- キーボード操作（矢印キーまたはカスタムキー）

## 必要環境

- Java 17 以降
- Maven 3.6 以降
- Visual Studio Code（推奨）または任意の Java IDE

## プロジェクト構造

```
pacman-java/
├── pom.xml                         # Maven設定ファイル
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── pacman/
│                   ├── Main.java              # エントリーポイント
│                   ├── game/                  # ゲームロジック
│                   │   ├── Game.java
│                   │   ├── GameState.java
│                   │   └── Direction.java
│                   ├── model/                 # ゲームエンティティ
│                   │   ├── Pacman.java
│                   │   ├── Ghost.java
│                   │   ├── Blinky.java
│                   │   ├── Pinky.java
│                   │   ├── Inky.java
│                   │   ├── Clyde.java
│                   │   ├── Maze.java
│                   │   └── Fruit.java        # NEW
│                   ├── sound/                 # サウンドシステム（NEW）
│                   │   ├── SoundManager.java
│                   │   └── SoundGenerator.java
│                   ├── effects/               # エフェクトシステム（NEW）
│                   │   └── EffectManager.java
│                   ├── util/                  # ユーティリティ（NEW）
│                   │   ├── GameSettings.java
│                   │   ├── GameStatistics.java
│                   │   └── HighScoreManager.java
│                   └── ui/                    # UI関連
│                       ├── GameWindow.java
│                       ├── GamePanel.java
│                       └── dialogs/           # ダイアログ（NEW）
│                           ├── SettingsDialog.java
│                           ├── HighScoreDialog.java
│                           └── StatisticsDialog.java
```

## ビルドと実行

### 1. プロジェクトのビルド

```bash
# プロジェクトディレクトリに移動
cd pacman-java

# Mavenでビルド
mvn clean compile
```

### 2. 実行方法

#### 方法 1: Maven から直接実行

```bash
mvn exec:java -Dexec.mainClass="com.pacman.Main"
```

#### 方法 2: 実行可能 JAR を作成して実行

```bash
# 実行可能JARを作成
mvn clean package

# JARファイルを実行
java -jar target/pacman-java-1.0.0-jar-with-dependencies.jar
```

## 操作方法

### 基本操作

- **矢印キー**: パックマンを上下左右に移動（カスタマイズ可能）
- **P**: ゲームの一時停止/再開
- **Esc**: ゲームの一時停止
- **Space**: ゲームオーバー後の再スタート

### メニュー操作

- **Ctrl+N**: 新しいゲームを開始
- **Ctrl+H**: ハイスコアを表示
- **Ctrl+S**: 設定ダイアログを開く
- **Ctrl+T**: 統計と実績を表示
- **Ctrl+Q**: ゲームを終了
- **F1**: 操作説明を表示

## ゲームルール

### 目的

迷路内のすべてのペレット（ドット）を食べてレベルをクリアする

### スコアリング

- 通常ペレット: 10 ポイント
- パワーペレット: 50 ポイント
- ゴースト撃退: 200, 400, 800, 1600 ポイント（連続撃退でボーナス増加）
- フルーツボーナス:
  - チェリー: 100 ポイント
  - イチゴ: 300 ポイント
  - オレンジ: 500 ポイント
  - リンゴ: 700 ポイント
  - メロン: 1000 ポイント
  - ギャラクシアン: 2000 ポイント
  - ベル: 3000 ポイント
  - キー: 5000 ポイント

### ゴーストの特徴

- **Blinky（赤）**: 常にパックマンを直接追跡、残りペレット数が少なくなると加速
- **Pinky（ピンク）**: パックマンの進行方向の 4 マス先を狙う
- **Inky（青）**: Blinky との連携で複雑な動きをする
- **Clyde（オレンジ）**: 距離に応じて追跡と逃走を切り替える（8 マス以内で逃走）

### 難易度設定

- **Easy**: 残機 4、ゴースト速度 80%
- **Normal**: 残機 3、ゴースト速度 100%
- **Hard**: 残機 2、ゴースト速度 120%
- **Extreme**: 残機 1、ゴースト速度 150%

## 開発者向け情報

### VSCode での開発

1. Java Extension Pack をインストール
2. プロジェクトフォルダを開く
3. `src/main/java/com/pacman/Main.java`を開いて F5 でデバッグ実行

### カスタマイズ可能な要素

- `Maze.java`: 迷路レイアウトの変更
- `Ghost.java`: ゴーストの速度や AI パラメータ
- `GamePanel.java`: 描画スタイルやカラー
- `SoundGenerator.java`: 効果音の周波数や長さ
- `EffectManager.java`: パーティクルエフェクトのパラメータ

### 設定ファイル

ゲームは以下の設定ファイルを自動生成します：

- `pacman_settings.properties`: ゲーム設定
- `pacman_highscores.dat`: ハイスコアデータ
- `pacman_statistics.dat`: 統計と実績データ

## トラブルシューティング

### ビルドエラーが発生する場合

```bash
# Mavenの依存関係をクリーンアップ
mvn clean
mvn dependency:purge-local-repository
```

### 実行時にクラスが見つからない場合

```bash
# クラスパスを明示的に指定
java -cp target/classes com.pacman.Main
```

### 音が出ない場合

- 設定メニューからサウンドが有効になっているか確認
- システムの音量設定を確認
- Java Sound API がシステムでサポートされているか確認

### パフォーマンスが悪い場合

- 設定メニューからパーティクルエフェクトを無効にする
- より高性能な JVM オプションで実行:
  ```bash
  java -Xmx512m -XX:+UseG1GC -jar target/pacman-java-1.0.0-jar-with-dependencies.jar
  ```

## 実績一覧

| 実績名           | 説明                 | 解除条件                        |
| ---------------- | -------------------- | ------------------------------- |
| First Victory    | 初めてゲームをクリア | ゲームを 1 回クリア             |
| Pellet Master    | ペレットマスター     | 10,000 個のペレットを食べる     |
| Ghost Hunter     | ゴーストハンター     | 1,000 体のゴーストを撃退        |
| Fruit Collector  | フルーツコレクター   | 100 個のフルーツを収集          |
| Score Master     | スコアマスター       | 1 ゲームで 10,000 点達成        |
| Score Champion   | スコアチャンピオン   | 1 ゲームで 50,000 点達成        |
| Score Legend     | スコアレジェンド     | 1 ゲームで 100,000 点達成       |
| Level 10         | レベル 10 到達       | レベル 10 に到達                |
| Level 20         | レベル 20 到達       | レベル 20 に到達                |
| Perfect Clear    | パーフェクトクリア   | 死なずにレベルをクリア          |
| Speed Runner     | スピードランナー     | レベル 1 を 30 秒以内にクリア   |
| Marathon Player  | マラソンプレイヤー   | 総プレイ時間 1 時間             |
| Dedicated Player | 熱心なプレイヤー     | 100 ゲームプレイ                |
| Ghost Combo      | ゴーストコンボ       | 1 つのパワーペレットで 4 体撃退 |
| Survivor         | サバイバー           | 5 分間死なずに生存              |

## ライセンス

このプロジェクトは教育目的で作成されました。オリジナルのパックマンは株式会社バンダイナムコエンターテインメントの著作物です。

## 更新履歴

### v2.0.0 (2024)

- サウンドシステムの実装
- ビジュアルエフェクトの追加
- フルーツボーナスシステム
- ハイスコア永続化
- 統計と実績システム
- 設定のカスタマイズ機能
- UI の大幅改善

### v1.0.0 (2024)

- 初回リリース
- 基本的なゲームプレイ機能
- 4 種類のゴースト AI
- スコアシステム

## 今後の追加予定

- [ ] ネットワーク対戦モード
- [ ] リプレイ機能
- [ ] カスタムマップエディタ
- [ ] モバイル版の開発
- [ ] より多様な実績
- [ ] ボスキャラクター
- [ ] パワーアップアイテムの追加
