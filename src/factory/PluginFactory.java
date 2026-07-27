package factory;

import config.AppConfig;
import plugin.NotificationPlugin;
import plugin.DiscordNotifier;
import plugin.TelegramNotifier;

import java.util.ArrayList;
import java.util.List;

// ===== プラグイン生成 (Factory Method) =====
// config.properties の enabled.plugins の値から、
// 有効化するプラグインを動的に生成する。
// 設定ファイルの書き換えだけでプラグインのON/OFFが可能。
//
// ■ 拡張方法:
//   1. NotificationPlugin を実装した新クラスを plugin パッケージに追加
//   2. このファイルの switch 文に case を1つ追加
//   3. config.properties の enabled.plugins にプラグイン名を追加
//   → コアロジック（EventManager 等）は一切変更不要
public class PluginFactory {

    /**
     * config の enabled.plugins に記載されたプラグインを生成して返す。
     * 例: enabled.plugins=discord,telegram
     */
    public static List<NotificationPlugin> createPlugins(AppConfig config) {
        List<NotificationPlugin> plugins = new ArrayList<>();
        String enabledValue = config.get("enabled.plugins");
        String[] names = enabledValue.split(",");

        for (String name : names) {
            String trimmed = name.trim().toLowerCase();
            switch (trimmed) {
                case "discord":
                    plugins.add(new DiscordNotifier(config.get("discord.webhook.url")));
                    System.out.println("[PluginFactory] discord プラグインを生成しました");
                    break;
                case "telegram":
                    plugins.add(new TelegramNotifier(
                            config.get("telegram.bot.token"),
                            config.get("telegram.chat.id")
                    ));
                    System.out.println("[PluginFactory] telegram プラグインを生成しました");
                    break;
                case "desktop":
                    plugins.add(new plugin.DesktopNotifier());
                    System.out.println("[PluginFactory] desktop プラグインを生成しました");
                    break;
                default:
                    System.out.println("[PluginFactory] ⚠ 未知のプラグイン: " + trimmed + "（スキップ）");
                    break;
            }
        }

        if (plugins.isEmpty()) {
            System.out.println("[PluginFactory] ⚠ 有効なプラグインがありません。enabled.plugins を確認してください。");
        }

        return plugins;
    }
}
