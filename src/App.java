import config.AppConfig;
import core.EventManager;
import core.FileWatcher;
import core.ManualTrigger;
import factory.PluginFactory;
import plugin.NotificationPlugin;
import ui.NotificationHubGUI;

import java.util.List;

public class App {
    public static void main(String[] args) {
        AppConfig config = new AppConfig("config.properties");

        // --- EventManager（Observer の Subject）を作成 ---
        EventManager manager = new EventManager();

        // --- PluginFactory（Factory Method）でプラグインを動的生成・登録 ---
        List<NotificationPlugin> plugins = PluginFactory.createPlugins(config);
        for (NotificationPlugin plugin : plugins) {
            manager.addPlugin(plugin);
        }

        // --- FileWatcher（ファイル自動監視トリガー）を作成 ---
        FileWatcher fileWatcher = new FileWatcher(manager, "./watch");

        // --- 起動モード切替 ---
        if (args.length > 0 && args[0].equals("--cui")) {
            // CUI モード（手動トリガー + オプションで監視起動可能）
            new ManualTrigger(manager).start();
        } else {
            // GUI モード（デフォルト）
            new NotificationHubGUI(manager, fileWatcher).show();
        }
    }
}
