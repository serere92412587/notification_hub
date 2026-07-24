package ui;

import core.EventManager;
import core.LogListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// ===== GUI メインウィンドウ =====
// Swing ベースの通知Hub操作画面。
// EventManager を通じてプラグインへの通知送信・ログ表示を行う。
public class NotificationHubGUI {
    private final EventManager manager;

    // UI コンポーネント
    private JFrame frame;
    private JTextArea logArea;
    private JTextField messageField;
    private JButton sendButton;
    private ButtonGroup priorityGroup;
    private JPanel pluginPanel;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public NotificationHubGUI(EventManager manager) {
        this.manager = manager;
    }

    /** GUI を構築して表示する */
    public void show() {
        // --- Look & Feel をシステムのネイティブに設定 ---
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 失敗しても続行（デフォルトのメタルL&Fで表示）
        }

        // --- メインフレーム ---
        frame = new JFrame("通知Hub - スマート通知管理システム");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640, 520);
        frame.setLocationRelativeTo(null); // 画面中央に配置
        frame.setResizable(true);

        // --- メインパネル ---
        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // === 上部: プラグイン一覧パネル ===
        pluginPanel = new JPanel();
        pluginPanel.setLayout(new BoxLayout(pluginPanel, BoxLayout.Y_AXIS));
        TitledBorder pluginBorder = BorderFactory.createTitledBorder("登録済みプラグイン");
        pluginPanel.setBorder(pluginBorder);
        refreshPluginList();

        // === 中央: メッセージ入力エリア ===
        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        TitledBorder inputBorder = BorderFactory.createTitledBorder("通知送信");
        inputPanel.setBorder(inputBorder);

        // メッセージ入力行
        JPanel messageRow = new JPanel(new BorderLayout(8, 0));
        messageRow.add(new JLabel("メッセージ: "), BorderLayout.WEST);
        messageField = new JTextField();
        messageField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        messageRow.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("送信");
        sendButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        sendButton.setPreferredSize(new Dimension(80, 30));
        messageRow.add(sendButton, BorderLayout.EAST);

        // 優先度選択行
        JPanel priorityRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        priorityRow.add(new JLabel("優先度: "));
        priorityGroup = new ButtonGroup();
        String[] priorities = {"INFO", "WARNING", "CRITICAL"};
        for (String p : priorities) {
            JRadioButton radio = new JRadioButton(p);
            radio.setActionCommand(p);
            if (p.equals("INFO")) {
                radio.setSelected(true);
            }
            priorityGroup.add(radio);
            priorityRow.add(radio);
        }

        JPanel inputContent = new JPanel(new BorderLayout(0, 4));
        inputContent.setBorder(new EmptyBorder(4, 4, 4, 4));
        inputContent.add(messageRow, BorderLayout.CENTER);
        inputContent.add(priorityRow, BorderLayout.SOUTH);
        inputPanel.add(inputContent, BorderLayout.CENTER);

        // 上部+中央をまとめる
        JPanel topPanel = new JPanel(new BorderLayout(0, 8));
        topPanel.add(pluginPanel, BorderLayout.NORTH);
        topPanel.add(inputPanel, BorderLayout.CENTER);

        // === 下部: ログ表示エリア ===
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(600, 200));
        TitledBorder logBorder = BorderFactory.createTitledBorder("通知ログ");
        logScroll.setBorder(logBorder);

        // --- レイアウト組み立て ---
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(logScroll, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);

        // --- EventManager にログリスナーを設定 ---
        manager.setLogListener(message -> {
            // SwingWorker のスレッドから呼ばれるので、EDT に戻す
            SwingUtilities.invokeLater(() -> {
                String time = LocalTime.now().format(TIME_FMT);
                logArea.append("[" + time + "] " + message + "\n");
                // 自動スクロール
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        });

        // --- イベントリスナー設定 ---
        sendButton.addActionListener(this::onSendClicked);
        messageField.addActionListener(this::onSendClicked); // Enter キーでも送信

        // --- 表示 ---
        frame.setVisible(true);
        appendLog("アプリケーション起動完了");
    }

    /** 送信ボタン押下時の処理 */
    private void onSendClicked(ActionEvent e) {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            appendLog("⚠ メッセージが空です");
            return;
        }

        String priority = priorityGroup.getSelection().getActionCommand();
        appendLog("送信開始: [" + priority + "] " + message);

        // UI をフリーズさせないために SwingWorker で別スレッド実行
        sendButton.setEnabled(false);
        messageField.setEnabled(false);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                manager.notifyAllPlugins(message, priority);
                return null;
            }

            @Override
            protected void done() {
                sendButton.setEnabled(true);
                messageField.setEnabled(true);
                messageField.setText("");
                messageField.requestFocusInWindow();
            }
        };
        worker.execute();
    }

    /** プラグイン一覧を再描画する */
    private void refreshPluginList() {
        pluginPanel.removeAll();
        List<String> names = manager.getPluginNames();
        if (names.isEmpty()) {
            pluginPanel.add(new JLabel("  （プラグイン未登録）"));
        } else {
            for (String name : names) {
                JLabel label = new JLabel("  ✔ " + name);
                label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
                pluginPanel.add(label);
            }
        }
        pluginPanel.revalidate();
        pluginPanel.repaint();
    }

    /** ログエリアにメッセージを追加する */
    private void appendLog(String message) {
        String time = LocalTime.now().format(TIME_FMT);
        logArea.append("[" + time + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
