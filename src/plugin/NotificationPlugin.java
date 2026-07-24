package plugin;

// ===== 共通インターフェース (Strategy) =====
public interface NotificationPlugin {
    String getPluginName();

    String sendNotification(String message, String priority);
}