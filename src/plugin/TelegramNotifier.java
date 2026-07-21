package plugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class TelegramNotifier implements NotificationPlugin {
    private final String botToken;
    private final String chatId;

    public TelegramNotifier(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
    }

    @Override
    public String getPluginName() {
        return "Telegram通知";
    }

    @Override
    public void sendNotification(String message, String priority) {
        try {
            String text = "[" + priority + "] " + message;
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://api.telegram.org/bot" + botToken
                    + "/sendMessage?chat_id=" + chatId + "&text=" + encodedText;

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[Telegram] status=" + response.statusCode());
            System.out.println("[Telegram] body=" + response.body());
        } catch (Exception e) {
            System.out.println("Telegram通知に失敗しました: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}