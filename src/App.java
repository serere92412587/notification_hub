import config.AppConfig;
import core.EventManager;
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

        // --- 起動モード切替 ---
        if (args.length > 0 && args[0].equals("--cui")) {
            // CUI モード（従来の手動トリガー）
            new ManualTrigger(manager).start();
        } else {
            // GUI モード（デフォルト）
            new NotificationHubGUI(manager).show();
        }
    }
}
