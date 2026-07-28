package plugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DiscordNotifier implements NotificationPlugin {
    private final String webhookUrl;

    public DiscordNotifier(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    @Override
    public String getPluginName() {
        return "Discord通知";
    }

    @Override
    public String sendNotification(String message, String priority) {
        try {
            String safeMessage = escapeJson(message);
            String jsonBody = "{\"content\": \"[" + priority + "] " + safeMessage + "\"}";

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5)) // 接続待ちの上限
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(5)) // レスポンス待ちの上限
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return "[Discord] 成功 (HTTP " + response.statusCode() + ")";
            } else {
                return "[Discord] 失敗 (HTTPエラー " + response.statusCode() + ")";
            }
        } catch (java.net.http.HttpConnectTimeoutException e) {
            return "[Discord] 失敗: 接続がタイムアウトしました (ネットワーク制限・プロキシ等の影響)";
        } catch (java.net.http.HttpTimeoutException e) {
            return "[Discord] 失敗: レスポンス応答がタイムアウトしました";
        } catch (Exception e) {
            return "[Discord] 失敗: 通信エラー (" + e.getMessage() + ")";
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}