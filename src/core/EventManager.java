package core;

import plugin.NotificationPlugin;
import java.util.ArrayList;
import java.util.List;

// ===== イベント管理 (Observer: Subject役) =====
// 複数の NotificationPlugin (Observer) を保持し、
// イベント発生時に一斉配信する。
// GUIからリアルタイムに登録・解除を切り替えることができる。
public class EventManager {
    private final List<NotificationPlugin> allPlugins = new ArrayList<>();
    private final List<NotificationPlugin> activePlugins = new ArrayList<>();
    private LogListener logListener;

    /** ログリスナーを設定する（GUI等からのログ取得用） */
    public void setLogListener(LogListener listener) {
        this.logListener = listener;
    }

    /** ログ出力ヘルパー。コンソールとリスナーの両方に出力する */
    private void log(String message) {
        System.out.println(message);
        if (logListener != null) {
            logListener.onLog(message);
        }
    }

    /** 初期プラグインを登録する（全プラグインリストとアクティブリストの両方に追加） */
    public void addPlugin(NotificationPlugin plugin) {
        if (!allPlugins.contains(plugin)) {
            allPlugins.add(plugin);
        }
        if (!activePlugins.contains(plugin)) {
            activePlugins.add(plugin);
            log("[システム] プラグイン登録: + " + plugin.getPluginName());
        }
    }

    /** プラグインを配信対象から解除する */
    public void removePlugin(NotificationPlugin plugin) {
        if (activePlugins.remove(plugin)) {
            log("[システム] プラグイン解除: - " + plugin.getPluginName());
        }
    }

    /** プラグインの有効/無効を切り替える (Observer の動的登録・解除) */
    public void setPluginEnabled(NotificationPlugin plugin, boolean enabled) {
        if (enabled) {
            if (!activePlugins.contains(plugin)) {
                activePlugins.add(plugin);
                log("[Observer連動] プラグイン有効化 🟢 -> " + plugin.getPluginName());
            }
        } else {
            if (activePlugins.remove(plugin)) {
                log("[Observer連動] プラグイン無効化 🔴 -> " + plugin.getPluginName());
            }
        }
    }

    /** プラグインがアクティブかどうかを判定する */
    public boolean isPluginEnabled(NotificationPlugin plugin) {
        return activePlugins.contains(plugin);
    }

    /** システムに読み込まれている全プラグインのリストを返す */
    public List<NotificationPlugin> getAllPlugins() {
        return new ArrayList<>(allPlugins);
    }

    /** 現在アクティブなプラグイン数を返す */
    public int getPluginCount() {
        return activePlugins.size();
    }

    /** アクティブなプラグインの名前リストを返す */
    public List<String> getPluginNames() {
        List<String> names = new ArrayList<>();
        for (NotificationPlugin plugin : activePlugins) {
            names.add(plugin.getPluginName());
        }
        return names;
    }

    /** 登録済みプラグインの名前を一覧表示する */
    public void listPlugins() {
        for (int i = 0; i < activePlugins.size(); i++) {
            log("  [" + (i + 1) + "] " + activePlugins.get(i).getPluginName());
        }
    }

    /**
     * 現在アクティブな全プラグインに通知を一斉配信する。
     */
    public NotificationSummary notifyAllPlugins(String message, String priority) {
        int total = activePlugins.size();
        log("[一括配信] メッセージ「" + message + "」の配信を開始します (対象: " + total + "件)");

        if (activePlugins.isEmpty()) {
            log("[一括配信] ⚠ 有効なプラグインが選択されていません。送信をスキップします。");
            return new NotificationSummary(0, 0, new ArrayList<>());
        }

        int successCount = 0;
        List<String> targetNames = new ArrayList<>();

        for (int i = 0; i < activePlugins.size(); i++) {
            NotificationPlugin plugin = activePlugins.get(i);
            boolean isLast = (i == activePlugins.size() - 1);
            String prefix = isLast ? "  └─ " : "  ├─ ";

            targetNames.add(plugin.getPluginName());
            log(prefix + "→ [" + plugin.getPluginName() + "] へ送信中...");

            String result = plugin.sendNotification(message, priority);
            log("     └ " + result);

            if (result != null && result.contains("成功")) {
                successCount++;
            }
        }

        log("[一括配信] 処理完了 (成功: " + successCount + "件 / 失敗: " + (total - successCount) + "件)");
        return new NotificationSummary(total, successCount, targetNames);
    }
}
