package plugin;

import java.awt.Toolkit;

// ===== サウンド通知プラグイン (Strategy具象クラス) =====
// 通知発生時に OS / Java の標準ビープ音を再生し、耳でもわかるフィードバックを提供する。
public class SoundNotifier implements NotificationPlugin {

    @Override
    public String getPluginName() {
        return "サウンド（ビープ音）通知";
    }

    @Override
    public String sendNotification(String message, String priority) {
        try {
            // OS 標準のビープ音を鳴らす
            Toolkit.getDefaultToolkit().beep();
            
            // 重要度 CRITICAL の場合は警告感を出すため2回鳴らす
            if ("CRITICAL".equalsIgnoreCase(priority)) {
                try {
                    Thread.sleep(150);
                    Toolkit.getDefaultToolkit().beep();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }

            return "[Sound] 成功 (ビープ音再生)";
        } catch (Exception e) {
            return "[Sound] 失敗: " + e.getMessage();
        }
    }
}
