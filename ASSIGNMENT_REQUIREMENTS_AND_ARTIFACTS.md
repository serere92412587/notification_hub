# 演習課題要件 ＆ 開発アーティファクト統合ドキュメント (Notification Hub)

本ドキュメントは、演習課題の公式要件、開発プロセスで作成された全アーティファクト（分析レポート・実装計画・タスクログ・デモ手順・レポート下書き）を1つに集約した総合参照資料です。

---

## 📋 1. 演習課題の公式要件と適合チェック

### 演習課題の要件一覧
- **基本言語**: Java言語（一部他言語との連携可、外部ライブラリ不使用推進）
- **テーマ設定**: ありふれたゲーム（オセロ、テトリス等）や単純な入出力練習問題は不可。「他人が機能拡張して初めて完成する」独自性・実用性のあるアプリ。
- **デザインパターン要件**: **GoFのデザインパターンを3種類以上導入**し、種類と導入意図をレポートで説明すること。
- **拡張デモ要件**: コードの拡張性を担保し、**機能拡張前後の振る舞いの違いを、実行結果の違い（出力結果）としてデモンストレーションできる**ようにすること。

### 適合チェック結果
| 要件 | 本プロジェクト (Notification Hub) の対応 | 判定 |
|---|---|---|
| **デザインパターン3種** | 1. **Strategy** (`NotificationPlugin`)<br>2. **Observer** (`EventManager`)<br>3. **Factory Method** (`PluginFactory`) | **完全クリア ✅** |
| **拡張前後の実行結果デモ** | `config.properties` の `enabled.plugins` 設定の変更のみ（**コアコード無修正**）で通知先が増減し、ログ・OSポップアップ出力が変化する。 | **完全クリア ✅** |
| **オリジナルテーマ性** | 各種Web API（Discord/Telegram）やOSトレイ通知、ファイル自動監視を束ねるリアルタイム通知Hub | **完全クリア ✅** |
| **コード流用の禁止** | Java標準ライブラリ（`java.net.http`, `javax.swing`, `java.awt.SystemTray`）のみによる完全自作アーキテクチャ | **完全クリア ✅** |

---

## 🏗️ 2. アーキテクチャ ＆ デザインパターン設計

### 全体構造図
```
                       ┌──────────────────────┐
                       │   PluginFactory      │ (Factory Method)
                       │  (config.properties) │
                       └──────────┬───────────┘
                                  │ 生成
                                  ▼
 ┌──────────────┐     ┌──────────────────────┐     ┌──────────────────────┐
 │ FileWatcher  ├────>│    EventManager      │────>│ NotificationPlugin   │ (Strategy)
 │ ManualTrigger│通知 │   (Subject/Observer) │一斉 │  - DiscordNotifier   │
 └──────────────┘     └──────────────────────┘配信 │  - TelegramNotifier  │
                           (Observer)              │  - DesktopNotifier   │
                                                   └──────────────────────┘
```

1. **Strategy パターン** (`plugin.NotificationPlugin`)
   - 各通知方式（Discord, Telegram, OSデスクトップ通知）の処理を抽象化。
   - 新規通知先の追加時に既存コードを変更せず新クラスを追加するだけで対応可能（開放閉鎖の原則）。

2. **Observer パターン** (`core.EventManager`)
   - `EventManager` が Subject となり、登録された `NotificationPlugin` (Observer) へ一斉配信。
   - GUI上のチェックボックスから、実行時に動的な登録（`addPlugin`）・解除（`removePlugin`）が可能。

3. **Factory Method パターン** (`factory.PluginFactory`)
   - `config.properties` の設定値から有効化するプラグイン群を動的に生成。

---

## 📝 3. 開発アーティファクトログ (履歴・進捗)

### フェーズ別達成タスク
- [x] **Phase 1**: Strategy土台構築 (`NotificationPlugin`, `DiscordNotifier`, `TelegramNotifier`)
- [x] **Phase 2**: Observerパターン構築 (`EventManager`)
- [x] **Phase 3**: コンソール手動トリガー (`ManualTrigger`)
- [x] **Phase 4**: フォルダ自動監視トリガー (`FileWatcher`)
- [x] **Phase 5**: Factory Method導入 (`PluginFactory`, `config.properties`)
- [x] **Phase 6**: GUI構築 & スタイリング (Java Swing ダークモード, `NotificationHubGUI`)
- [x] **Phase 7**: OS標準トレイポップアップ通知プラグイン (`DesktopNotifier`) 追加
- [x] **Phase 8**: GUIでのリアルタイム Observer 着脱機能 (JCheckBox 連動) の追加
- [x] **Phase 9**: 設定サンプル・ドキュメント整備 (`config.properties.example`, `README.md`, `demo_and_report_guide.md`)

---

## 🎬 4. 発表会デモ実演スクリプト (1〜2分)

1. **Before状態（1つのプラグインのみ）で起動**
   - `src/config.properties` を `enabled.plugins=desktop` に設定して起動。
   - 手動送信または `./watch` にファイルを置くと、画面右下にOSポップアップ通知のみが表示される。
2. **After状態（設定変更による機能拡張）**
   - `config.properties` を `enabled.plugins=discord,telegram,desktop` に書き換えて再起動。
   - **「Javaコードを1行も触らず設定ファイル変更だけで通知先が拡張された」** ことをアピールし、一斉送信する。
3. **GUIでの Observer リアルタイム着脱**
   - GUI上の「✔ デスクトップOS通知」のチェックを外し送信。
   - ログに `[Observer解除]` と表示され、動的に通知対象から除外される様子を実演。

---

## 📄 5. 課題提出レポート用文章 (コピペ用)

### 採用したデザインパターンとその効果
- **Strategy パターン**: `plugin.NotificationPlugin` インターフェースにより通知送信処理を分離・抽象化し、既存コードに手を加えずに新しい通知プラグインを追加可能とした（開放閉鎖の原則）。
- **Observer パターン**: `core.EventManager` を Subject とし、複数の `NotificationPlugin` を動的に保持・一斉配信する設計とした。GUIからリアルタイムで送信対象の登録・解除を行えるようにした。
- **Factory Method パターン**: `factory.PluginFactory` を通じて `config.properties` の設定値に基づき動的にオブジェクトを生成し、コードの再コンパイルなしで振る舞いを変更可能とした。
