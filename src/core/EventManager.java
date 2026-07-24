package core;

import plugin.NotificationPlugin;
import java.util.ArrayList;
import java.util.List;

// ===== イベント管理 (Observer: Subject役) =====
// 複数の NotificationPlugin (Observer) を保持し、
// イベント発生時に一斉配信する
public class EventManager {
    private final List<NotificationPlugin> plugins = new ArrayList<>();
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

    /** プラグインを登録する */
    public void addPlugin(NotificationPlugin plugin) {
        plugins.add(plugin);
        log("[EventManager] + " + plugin.getPluginName() + " を登録しました");
    }

    /** プラグインを登録解除する */
    public void removePlugin(NotificationPlugin plugin) {
        plugins.remove(plugin);
        log("[EventManager] - " + plugin.getPluginName() + " を解除しました");
    }

    /** 登録済みプラグイン数を返す */
    public int getPluginCount() {
        return plugins.size();
    }

    /** 登録済みプラグインの名前リストを返す */
    public List<String> getPluginNames() {
        List<String> names = new ArrayList<>();
        for (NotificationPlugin plugin : plugins) {
            names.add(plugin.getPluginName());
        }
        return names;
    }

    /** 登録済みプラグインの名前を一覧表示する */
    public void listPlugins() {
        for (int i = 0; i < plugins.size(); i++) {
            log("  [" + (i + 1) + "] " + plugins.get(i).getPluginName());
        }
    }

    /**
     * 登録済みの全プラグインに通知を一斉配信する。
     * ※ Object.notifyAll() との名前衝突を避けるため notifyAllPlugins とした
     */
    public void notifyAllPlugins(String message, String priority) {
        log("[EventManager] === 通知配信開始 (" + plugins.size() + "件のプラグイン) ===");
        for (NotificationPlugin plugin : plugins) {
            log("[EventManager] → " + plugin.getPluginName() + " に送信中...");
            plugin.sendNotification(message, priority);
        }
        log("[EventManager] === 通知配信完了 ===");
    }
}
