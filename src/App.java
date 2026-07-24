import config.AppConfig;
import core.EventManager;
import core.ManualTrigger;
import plugin.DiscordNotifier;
import plugin.TelegramNotifier;
import ui.NotificationHubGUI;

public class App {
    public static void main(String[] args) {
        AppConfig config = new AppConfig("config.properties");

        // --- EventManager（Observer の Subject）を作成 ---
        EventManager manager = new EventManager();

        // --- プラグイン（Observer）を登録 ---
        manager.addPlugin(new DiscordNotifier(config.get("discord.webhook.url")));
        manager.addPlugin(new TelegramNotifier(
                config.get("telegram.bot.token"),
                config.get("telegram.chat.id")
        ));

        // --- 起動モード切替 ---
        if (args.length > 0 && args[0].equals("--cui")) {
            // CUI モード（従来の手動トリガー）
            new ManualTrigger(manager).start();
        } else {
            // GUI モード（デフォルト）
            new NotificationHubGUI(manager).show();
        }
    }
}
