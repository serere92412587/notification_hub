import plugin.NotificationPlugin;
import plugin.DiscordNotifier;
import plugin.TelegramNotifier;

public class App {
    public static void main(String[] args) {
        AppConfig config = new AppConfig("config.properties");

        String discordUrl = config.get("discord.webhook.url");
        String telegramToken = config.get("telegram.bot.token");
        String telegramChatId = config.get("telegram.chat.id");

        NotificationPlugin discord = new DiscordNotifier(discordUrl);
        NotificationPlugin telegram = new TelegramNotifier(telegramToken, telegramChatId);

        // ここから EventManager に登録していく

        // ここから動作確認：まずは直接呼んでみる
        System.out.println("=== 動作確認開始 ===");
        discord.sendNotification("テスト通知だよ", "INFO");
        telegram.sendNotification("テスト通知だよ", "INFO");
        System.out.println("=== 動作確認終了 ===");
    }
}