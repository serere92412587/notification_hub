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
                    System.out.println("[システム] [Factory生成] Discord通知 プラグイン");
                    break;
                case "telegram":
                    plugins.add(new TelegramNotifier(
                            config.get("telegram.bot.token"),
                            config.get("telegram.chat.id")
                    ));
                    System.out.println("[システム] [Factory生成] Telegram通知 プラグイン");
                    break;
                case "desktop":
                    plugins.add(new plugin.DesktopNotifier());
                    System.out.println("[システム] [Factory生成] デスクトップOS通知 プラグイン");
                    break;
                case "filelogger":
                case "file_logger":
                case "filelog":
                    String logPath = config.get("file.logger.path");
                    plugins.add(new plugin.FileLoggerNotifier(logPath));
                    System.out.println("[システム] [Factory生成] ローカルファイルログ プラグイン");
                    break;
                case "sound":
                case "audio":
                    plugins.add(new plugin.SoundNotifier());
                    System.out.println("[システム] [Factory生成] サウンド（ビープ音）通知 プラグイン");
                    break;
                default:
                    System.out.println("[システム] [Factory生成] ⚠ 未知のプラグイン: " + trimmed);
                    break;
            }
        }

        if (plugins.isEmpty()) {
            System.out.println("[システム] [Factory生成] ⚠ 有効なプラグインがありません。");
        }

        return plugins;
    }
}
