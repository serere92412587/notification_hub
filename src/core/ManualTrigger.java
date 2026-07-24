package core;

import java.util.Scanner;

// ===== 手動トリガー =====
// コンソール入力で通知を手動送信する。
// 「notify メッセージ内容」形式で入力すると、
// EventManager 経由で全プラグインに一斉配信される。
public class ManualTrigger {
    private final EventManager manager;

    public ManualTrigger(EventManager manager) {
        this.manager = manager;
    }

    /** コンソール入力ループを開始する */
    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println();
        System.out.println("===========================================");
        System.out.println("  通知Hub - 手動トリガーモード");
        System.out.println("===========================================");
        System.out.println("  使い方:");
        System.out.println("    notify <メッセージ>  … 全プラグインに通知");
        System.out.println("    list                … 登録済みプラグイン一覧");
        System.out.println("    exit                … 終了");
        System.out.println("===========================================");
        System.out.println();

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("手動トリガーを終了します。");
                break;
            }

            if (input.equalsIgnoreCase("list")) {
                System.out.println("登録済みプラグイン: " + manager.getPluginCount() + "件");
                manager.listPlugins();
                continue;
            }

            if (input.startsWith("notify ")) {
                String message = input.substring("notify ".length()).trim();
                if (message.isEmpty()) {
                    System.out.println("メッセージが空です。notify <メッセージ> の形式で入力してください。");
                    continue;
                }
                manager.notifyAllPlugins(message, "MANUAL");
            } else {
                System.out.println("不明なコマンドです。'notify <メッセージ>' または 'exit' を入力してください。");
            }
        }
        scanner.close();
    }
}
