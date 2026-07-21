package plugin;

// ===== 共通インターフェース (Strategy) =====
public interface NotificationPlugin {
    String getPluginName();

    void sendNotification(String message, String priority);
}