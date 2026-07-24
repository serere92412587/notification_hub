import config.AppConfig;
import core.EventManager;
import plugin.DiscordNotifier;
import plugin.TelegramNotifier;

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

        // --- 一斉通知テスト ---
        manager.notifyAllPlugins("EventManager経由のテスト通知です", "INFO");
    }
}