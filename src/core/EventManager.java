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
            log("[EventManager] + " + plugin.getPluginName() + " を配信対象に登録しました");
        }
    }

    /** プラグインを配信対象から解除する */
    public void removePlugin(NotificationPlugin plugin) {
        if (activePlugins.remove(plugin)) {
            log("[EventManager] - " + plugin.getPluginName() + " を配信対象から解除しました");
        }
    }

    /** プラグインの有効/無効を切り替える (Observer の動的登録・解除) */
    public void setPluginEnabled(NotificationPlugin plugin, boolean enabled) {
        if (enabled) {
            if (!activePlugins.contains(plugin)) {
                activePlugins.add(plugin);
                log("[EventManager] [Observer有効化] + " + plugin.getPluginName() + " を登録しました");
            }
        } else {
            if (activePlugins.remove(plugin)) {
                log("[EventManager] [Observer解除] - " + plugin.getPluginName() + " を削除しました");
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
    public void notifyAllPlugins(String message, String priority) {
        log("[EventManager] === 通知配信開始 (" + activePlugins.size() + "件のアクティブ・プラグイン) ===");
        if (activePlugins.isEmpty()) {
            log("[EventManager] ⚠ 有効なプラグインがありません。通知はスキップされました。");
        }
        for (NotificationPlugin plugin : activePlugins) {
            log("[EventManager] → " + plugin.getPluginName() + " に送信中...");
            String result = plugin.sendNotification(message, priority);
            log("  └ " + result);
        }
        log("[EventManager] === 通知配信完了 ===");
    }
}
