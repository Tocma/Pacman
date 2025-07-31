# Classic Pacman - Java Edition

1980 年のアーケード版パックマンを Java Swing と Maven で忠実に再現したゲームです。

## 特徴

- オリジナルに忠実な 28×31 グリッドの迷路
- 4 種類のゴースト（Blinky, Pinky, Inky, Clyde）それぞれ独自の AI
- パワーペレットによるゴースト撃退システム
- スコアリングシステムと残機管理
- キーボード操作（矢印キー）

## 必要環境

- Java 17 以降
- Maven 3.6 以降
- Visual Studio Code（推奨）または任意の Java IDE

## プロジェクト構造

```
pacman-java/
├── pom.xml                 # Maven設定ファイル
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
│                   │   └── Maze.java
│                   └── ui/                    # UI関連
│                       ├── GameWindow.java
│                       └── GamePanel.java
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

- **矢印キー**: パックマンを上下左右に移動
- **P**: ゲームの一時停止/再開
- **Space**: ゲームオーバー後の再スタート
- **Ctrl+N**: 新しいゲームを開始
- **Ctrl+Q**: ゲームを終了

## ゲームルール

### 目的

迷路内のすべてのペレット（ドット）を食べてレベルをクリアする

### スコアリング

- 通常ペレット: 10 ポイント
- パワーペレット: 50 ポイント
- ゴースト撃退: 200, 400, 800, 1600 ポイント（連続撃退でボーナス増加）

### ゴーストの特徴

- **Blinky（赤）**: 常にパックマンを直接追跡
- **Pinky（ピンク）**: パックマンの進行方向の先を狙う
- **Inky（青）**: Blinky との連携で複雑な動きをする
- **Clyde（オレンジ）**: 距離に応じて追跡と逃走を切り替える

## 開発者向け情報

### VSCode での開発

1. Java Extension Pack をインストール
2. プロジェクトフォルダを開く
3. `src/main/java/com/pacman/Main.java`を開いて F5 でデバッグ実行

### カスタマイズ可能な要素

- `Maze.java`: 迷路レイアウトの変更
- `Ghost.java`: ゴーストの速度や AI パラメータ
- `GamePanel.java`: 描画スタイルやカラー

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

## ライセンス

このプロジェクトは教育目的で作成されました。オリジナルのパックマンは株式会社バンダイナムコエンターテインメントの著作物です。

## 今後の拡張予定

- [ ] サウンド効果の追加
- [ ] ハイスコアの永続化
- [ ] 追加ステージ
- [ ] フルーツボーナスの実装
- [ ] 2 プレイヤーモード
