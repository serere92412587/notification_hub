package plugin;

import java.awt.*;
import java.awt.image.BufferedImage;

// ===== デスクトップOS通知プラグイン (Strategy具象クラス) =====
// java.awt.SystemTray を利用し、Windows画面右下にポップアップ通知を表示する。
// ネットワーク通信を伴わないため、オフライン環境でも100%確実に動作する。
public class DesktopNotifier implements NotificationPlugin {
    private TrayIcon trayIcon;

    public DesktopNotifier() {
        if (SystemTray.isSupported()) {
            try {
                SystemTray tray = SystemTray.getSystemTray();

                // トレイアイコン用の16x16アイコン画像を動的作成（青いベル風スクエア）
                BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = img.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(0, 122, 255)); // Apple風ブルー
                g.fillRoundRect(1, 1, 14, 14, 4, 4);
                g.setColor(Color.WHITE);
                g.drawString("N", 4, 12);
                g.dispose();

                trayIcon = new TrayIcon(img, "通知Hub");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);

            } catch (Exception e) {
                System.err.println("[DesktopNotifier] システムトレイ登録失敗: " + e.getMessage());
                trayIcon = null;
            }
        }
    }

    @Override
    public String getPluginName() {
        return "デスクトップOS通知";
    }

    @Override
    public String sendNotification(String message, String priority) {
        if (trayIcon != null && SystemTray.isSupported()) {
            TrayIcon.MessageType type;
            switch (priority.toUpperCase()) {
                case "WARNING":
                    type = TrayIcon.MessageType.WARNING;
                    break;
                case "CRITICAL":
                    type = TrayIcon.MessageType.ERROR;
                    break;
                default:
                    type = TrayIcon.MessageType.INFO;
                    break;
            }

            trayIcon.displayMessage("通知Hub [" + priority + "]", message, type);
            return "[Desktop] 成功 (OSポップアップ表示)";
        } else {
            return "[Desktop] 失敗: システムトレイ非対応環境";
        }
    }
}
