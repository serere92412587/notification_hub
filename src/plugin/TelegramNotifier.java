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
    public String sendNotification(String message, String priority) {
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
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return "[Telegram] 成功 (HTTP " + response.statusCode() + ")";
            } else {
                return "[Telegram] 失敗 (HTTPエラー " + response.statusCode() + ")";
            }
        } catch (java.net.http.HttpConnectTimeoutException e) {
            return "[Telegram] 失敗: 接続がタイムアウトしました (ネットワーク制限・プロキシ等の影響)";
        } catch (java.net.http.HttpTimeoutException e) {
            return "[Telegram] 失敗: レスポンス応答がタイムアウトしました";
        } catch (Exception e) {
            return "[Telegram] 失敗: 通信エラー (" + e.getMessage() + ")";
        }
    }
}