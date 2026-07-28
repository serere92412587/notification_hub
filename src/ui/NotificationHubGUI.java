package ui;

import core.EventManager;
import core.FileWatcher;
import plugin.NotificationPlugin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// ===== GUI メインウィンドウ =====
// ダークモードスタイリング & リアルタイム Observer 切り替え機能付き GUI
public class NotificationHubGUI {
    private final EventManager manager;
    private final FileWatcher fileWatcher;

    // UI カラーテーマ (Catppuccin Macchiato 風 Dark Palette)
    private static final Color COLOR_BG = new Color(30, 30, 46);         // #1E1E2E
    private static final Color COLOR_PANEL = new Color(36, 37, 54);      // #242536
    private static final Color COLOR_PANEL_INNER = new Color(48, 52, 70); // #303446
    private static final Color COLOR_TEXT = new Color(205, 214, 244);     // #CDD6F4
    private static final Color COLOR_MUTED = new Color(166, 173, 200);    // #A6ADC8
    private static final Color COLOR_ACCENT = new Color(137, 180, 250);   // #89B4FA (Soft Blue)
    private static final Color COLOR_SUCCESS = new Color(166, 227, 161);  // #A6E3A1 (Green)
    private static final Color COLOR_DANGER = new Color(243, 139, 168);   // #F38BA8 (Red)

    // UI コンポーネント
    private JFrame frame;
    private JTextArea logArea;
    private JTextField messageField;
    private JButton sendButton;
    private ButtonGroup priorityGroup;
    private JPanel pluginPanel;
    private JButton watchToggleButton;
    private JLabel watchStatusLabel;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public NotificationHubGUI(EventManager manager, FileWatcher fileWatcher) {
        this.manager = manager;
        this.fileWatcher = fileWatcher;
    }

    /** GUI を構築して表示する */
    public void show() {
        // Look & Feel
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // メインフレーム
        frame = new JFrame("通知Hub - スマート通知管理システム");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(720, 640);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);

        // メインパネル
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        // === 1. プラグインリアルタイム管理パネル (Observerパターンの動的可視化) ===
        pluginPanel = new JPanel();
        pluginPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));
        pluginPanel.setBackground(COLOR_PANEL);
        pluginPanel.setBorder(createDarkTitledBorder("登録済み通知プラグイン (チェックでリアルタイムON/OFF切替)"));
        refreshPluginList();

        // === 2. ファイル監視パネル (FileWatcher) ===
        JPanel watcherPanel = new JPanel(new BorderLayout(10, 0));
        watcherPanel.setBackground(COLOR_PANEL);
        watcherPanel.setBorder(createDarkTitledBorder("ファイル自動監視 (FileWatcher)"));
        watcherPanel.setPreferredSize(new Dimension(680, 60));

        String dirPath = fileWatcher != null ? fileWatcher.getWatchDir().toAbsolutePath().toString() : "./watch";
        watchStatusLabel = new JLabel("  🔴 停止中 (対象: " + dirPath + ")");
        watchStatusLabel.setForeground(COLOR_MUTED);
        watchStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

        JButton selectDirButton = createStyledButton("📁 フォルダ変更", COLOR_ACCENT, Color.BLACK);
        selectDirButton.addActionListener(this::onSelectDirClicked);

        watchToggleButton = createStyledButton("監視開始", COLOR_SUCCESS, Color.BLACK);
        watchToggleButton.addActionListener(this::onWatchToggleClicked);

        JPanel watcherBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        watcherBtnPanel.setBackground(COLOR_PANEL);
        watcherBtnPanel.add(selectDirButton);
        watcherBtnPanel.add(watchToggleButton);

        watcherPanel.add(watchStatusLabel, BorderLayout.CENTER);
        watcherPanel.add(watcherBtnPanel, BorderLayout.EAST);

        // === 3. メッセージ入力エリア ===
        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.setBackground(COLOR_PANEL);
        inputPanel.setBorder(createDarkTitledBorder("手動通知送信"));

        // メッセージ入力行
        JPanel messageRow = new JPanel(new BorderLayout(8, 0));
        messageRow.setBackground(COLOR_PANEL);

        JLabel msgLabel = new JLabel("メッセージ: ");
        msgLabel.setForeground(COLOR_TEXT);
        messageRow.add(msgLabel, BorderLayout.WEST);

        messageField = new JTextField();
        messageField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        messageField.setBackground(COLOR_PANEL_INNER);
        messageField.setForeground(COLOR_TEXT);
        messageField.setCaretColor(COLOR_TEXT);
        messageField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_MUTED, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));
        messageRow.add(messageField, BorderLayout.CENTER);

        sendButton = createStyledButton("送信 🚀", COLOR_ACCENT, Color.BLACK);
        sendButton.setPreferredSize(new Dimension(90, 32));
        messageRow.add(sendButton, BorderLayout.EAST);

        // 優先度選択行
        JPanel priorityRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        priorityRow.setBackground(COLOR_PANEL);

        JLabel prioLabel = new JLabel("優先度: ");
        prioLabel.setForeground(COLOR_TEXT);
        priorityRow.add(prioLabel);

        priorityGroup = new ButtonGroup();
        String[] priorities = {"INFO", "WARNING", "CRITICAL"};
        for (String p : priorities) {
            JRadioButton radio = new JRadioButton(p);
            radio.setActionCommand(p);
            radio.setForeground(COLOR_TEXT);
            radio.setBackground(COLOR_PANEL);
            radio.setFocusPainted(false);
            if (p.equals("INFO")) radio.setSelected(true);
            priorityGroup.add(radio);
            priorityRow.add(radio);
        }

        JPanel inputContent = new JPanel(new BorderLayout(0, 6));
        inputContent.setBackground(COLOR_PANEL);
        inputContent.setBorder(new EmptyBorder(6, 6, 6, 6));
        inputContent.add(messageRow, BorderLayout.CENTER);
        inputContent.add(priorityRow, BorderLayout.SOUTH);
        inputPanel.add(inputContent, BorderLayout.CENTER);

        // 上部・中央レイアウト統合
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(COLOR_BG);
        topContainer.add(pluginPanel);
        topContainer.add(Box.createVerticalStrut(8));
        topContainer.add(watcherPanel);
        topContainer.add(Box.createVerticalStrut(8));
        topContainer.add(inputPanel);

        // === 4. 下部: ログ表示エリア ===
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(COLOR_PANEL_INNER);
        logArea.setForeground(COLOR_TEXT);
        logArea.setCaretColor(COLOR_TEXT);
        logArea.setMargin(new Insets(6, 8, 6, 8));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(680, 220));
        logScroll.setBorder(createDarkTitledBorder("リアルタイム配信ログ"));
        logScroll.getViewport().setBackground(COLOR_PANEL_INNER);

        // レイアウト追加
        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(logScroll, BorderLayout.CENTER);

        frame.setContentPane(mainPanel);

        // ウィンドウ閉時のシャットダウン
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (fileWatcher != null && fileWatcher.isRunning()) {
                    fileWatcher.stop();
                }
            }
        });

        // EventManager ログ連携
        manager.setLogListener(message -> {
            SwingUtilities.invokeLater(() -> {
                String time = LocalTime.now().format(TIME_FMT);
                logArea.append("[" + time + "] " + message + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        });

        sendButton.addActionListener(this::onSendClicked);
        messageField.addActionListener(this::onSendClicked);

        frame.setVisible(true);
        appendLog("ダークモード GUI システム起動完了");
    }

    /** プラグイン一覧チェックボックスを生成・再描画 (Observer動的切替) */
    private void refreshPluginList() {
        pluginPanel.removeAll();
        List<NotificationPlugin> allPlugins = manager.getAllPlugins();

        if (allPlugins.isEmpty()) {
            JLabel emptyLabel = new JLabel("（有効なプラグインが登録されていません）");
            emptyLabel.setForeground(COLOR_MUTED);
            pluginPanel.add(emptyLabel);
        } else {
            for (NotificationPlugin plugin : allPlugins) {
                boolean isEnabled = manager.isPluginEnabled(plugin);
                JCheckBox checkBox = new JCheckBox(plugin.getPluginName(), isEnabled);
                checkBox.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                checkBox.setForeground(isEnabled ? COLOR_TEXT : COLOR_MUTED);
                checkBox.setBackground(COLOR_PANEL);
                checkBox.setFocusPainted(false);

                // チェックボックスの変更イベントで Observer の動的着脱を実行！
                checkBox.addActionListener(e -> {
                    boolean checked = checkBox.isSelected();
                    manager.setPluginEnabled(plugin, checked);
                    checkBox.setForeground(checked ? COLOR_TEXT : COLOR_MUTED);
                });

                pluginPanel.add(checkBox);
            }
        }
        pluginPanel.revalidate();
        pluginPanel.repaint();
    }

    /** フォルダ選択ボタン押下時 */
    private void onSelectDirClicked(ActionEvent e) {
        if (fileWatcher == null) return;

        JFileChooser chooser = new JFileChooser(fileWatcher.getWatchDir().toFile());
        chooser.setDialogTitle("監視対象フォルダを選択");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = chooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedDir = chooser.getSelectedFile();
            if (selectedDir != null) {
                fileWatcher.setWatchDir(selectedDir.toPath());
                updateWatchStatusLabel();
                appendLog("📁 監視対象フォルダを切り替えました: " + selectedDir.getAbsolutePath());
            }
        }
    }

    /** ファイル監視トグルボタン押下時 */
    private void onWatchToggleClicked(ActionEvent e) {
        if (fileWatcher == null) return;

        if (fileWatcher.isRunning()) {
            fileWatcher.stop();
        } else {
            fileWatcher.start();
        }
        updateWatchStatusLabel();
    }

    /** 監視ステータス表示の更新 */
    private void updateWatchStatusLabel() {
        if (fileWatcher == null) return;

        String dirPath = fileWatcher.getWatchDir().toAbsolutePath().toString();
        if (fileWatcher.isRunning()) {
            watchToggleButton.setText("監視停止");
            watchToggleButton.setBackground(COLOR_DANGER);
            watchStatusLabel.setText("  🟢 監視中... (対象: " + dirPath + ")");
            watchStatusLabel.setForeground(COLOR_SUCCESS);
        } else {
            watchToggleButton.setText("監視開始");
            watchToggleButton.setBackground(COLOR_SUCCESS);
            watchStatusLabel.setText("  🔴 停止中 (対象: " + dirPath + ")");
            watchStatusLabel.setForeground(COLOR_MUTED);
        }
    }

    /** 送信ボタン押下時 */
    private void onSendClicked(ActionEvent e) {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            appendLog("⚠ メッセージが空です");
            return;
        }

        String priority = priorityGroup.getSelection().getActionCommand();
        appendLog("手動送信を開始: [" + priority + "] " + message);

        sendButton.setEnabled(false);
        messageField.setEnabled(false);

        SwingWorker<core.NotificationSummary, Void> worker = new SwingWorker<>() {
            @Override
            protected core.NotificationSummary doInBackground() {
                return manager.notifyAllPlugins(message, priority);
            }

            @Override
            protected void done() {
                sendButton.setEnabled(true);
                messageField.setEnabled(true);
                messageField.setText("");
                messageField.requestFocusInWindow();

                try {
                    core.NotificationSummary summary = get();
                    if (summary != null && summary.getTotalTargets() > 0) {
                        showSummaryDialog(summary);
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    /** 配信結果のサマリーダイアログを表示（拡張前後の視覚化） */
    private void showSummaryDialog(core.NotificationSummary summary) {
        StringBuilder sb = new StringBuilder();
        if (summary.isMultipleTargets()) {
            sb.append("🎉 【一括同斉配信が完了しました！】\n");
            sb.append("----------------─────────────────────\n");
            sb.append("合計 ").append(summary.getTotalTargets()).append(" 件の通知先へ一斉に配信されました。\n\n");
            sb.append("■ 配信対象プラグイン:\n");
            for (String name : summary.getTargetPluginNames()) {
                sb.append("  ✔ ").append(name).append("\n");
            }
            sb.append("\n(※ コアコード無修正でマルチ配信へ拡張された状態です)");
            JOptionPane.showMessageDialog(frame, sb.toString(), "配信結果サマリー (機能拡張適用中)", JOptionPane.INFORMATION_MESSAGE);
        } else {
            sb.append("📢 【送信完了】\n");
            sb.append("----------------─────────────────────\n");
            sb.append("配信対象: 1件 (").append(summary.getTargetPluginNames().get(0)).append(")\n\n");
            sb.append("(※ 単一プラグインのみ動作する拡張前の状態です)");
            JOptionPane.showMessageDialog(frame, sb.toString(), "送信結果サマリー (拡張前状態)", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void appendLog(String message) {
        String time = LocalTime.now().format(TIME_FMT);
        logArea.append("[" + time + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    /** ダークテーマ枠線の生成ヘルパー */
    private TitledBorder createDarkTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                new LineBorder(COLOR_MUTED, 1, true),
                title
        );
        border.setTitleColor(COLOR_ACCENT);
        border.setTitleFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        return border;
    }

    /** カスタムボタン生成ヘルパー */
    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        return btn;
    }
}
