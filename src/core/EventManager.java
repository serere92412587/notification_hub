package core;

import plugin.NotificationPlugin;
import java.util.ArrayList;
import java.util.List;

// ===== イベント管理 (Observer: Subject役) =====
// 複数の NotificationPlugin (Observer) を保持し、
// イベント発生時に一斉配信する
public class EventManager {
    private final List<NotificationPlugin> plugins = new ArrayList<>();

    /** プラグインを登録する */
    public void addPlugin(NotificationPlugin plugin) {
        plugins.add(plugin);
        System.out.println("[EventManager] + " + plugin.getPluginName() + " を登録しました");
    }

    /** プラグインを登録解除する */
    public void removePlugin(NotificationPlugin plugin) {
        plugins.remove(plugin);
        System.out.println("[EventManager] - " + plugin.getPluginName() + " を解除しました");
    }

    /** 登録済みプラグイン数を返す */
    public int getPluginCount() {
        return plugins.size();
    }

    /**
     * 登録済みの全プラグインに通知を一斉配信する。
     * ※ Object.notifyAll() との名前衝突を避けるため notifyAllPlugins とした
     */
    public void notifyAllPlugins(String message, String priority) {
        System.out.println("[EventManager] === 通知配信開始 (" + plugins.size() + "件のプラグイン) ===");
        for (NotificationPlugin plugin : plugins) {
            System.out.println("[EventManager] → " + plugin.getPluginName() + " に送信中...");
            plugin.sendNotification(message, priority);
        }
        System.out.println("[EventManager] === 通知配信完了 ===");
    }
}
