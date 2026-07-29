package ui;

import core.EventManager;
import core.FileWatcher;
import core.NotificationSummary;
import plugin.NotificationPlugin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ===== ⚡ NotifyHub Pro - 高機能SaaS風ダッシュボード GUI =====
// Slate Dark テーマ, 2カラムレイアウト, リッチアナウンス投稿, Slack風タイムラインカード表示
public class NotificationHubGUI {
    private final EventManager manager;
    private final FileWatcher fileWatcher;

    // カラーパレット (Slate Dark & Modern SaaS Theme)
    private static final Color COLOR_BG = new Color(15, 23, 42);          // #0F172A Slate 900
    private static final Color COLOR_SIDEBAR = new Color(30, 41, 59);     // #1E293B Slate 800
    private static final Color COLOR_CARD = new Color(30, 41, 59);        // #1E293B
    private static final Color COLOR_CARD_INNER = new Color(51, 65, 85);   // #334155 Slate 700
    private static final Color COLOR_TEXT_PRIMARY = new Color(248, 250, 252); // #F8FAFC
    private static final Color COLOR_TEXT_MUTED = new Color(148, 163, 184);   // #94A3B8
    private static final Color COLOR_ACCENT = new Color(56, 189, 248);       // #38BDF8 Sky Blue
    private static final Color COLOR_SUCCESS = new Color(74, 222, 128);      // #4ADE80 Emerald Green
    private static final Color COLOR_WARNING = new Color(250, 204, 21);      // #FACC15 Yellow
    private static final Color COLOR_DANGER = new Color(248, 113, 113);      // #F87171 Red

    // UI コンポーネント
    private JFrame frame;
    private JTextArea messageField;
    private JButton broadcastButton;
    private ButtonGroup priorityGroup;
    private Map<String, JRadioButton> priorityRadios = new HashMap<>();

    private JPanel pluginListPanel;
    private JLabel watchStatusLabel;
    private JButton watchToggleButton;
    private JLabel totalBroadcastCountLabel;
    private JLabel successRateLabel;

    private JPanel timelineContainer;
    private JScrollPane timelineScroll;

    private int totalBroadcastCount = 0;
    private int totalSuccessCount = 0;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public NotificationHubGUI(EventManager manager, FileWatcher fileWatcher) {
        this.manager = manager;
        this.fileWatcher = fileWatcher;
    }

    /** GUI の表示・初期化 */
    public void show() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        frame = new JFrame("NotifyHub Pro v2.0 - 統合アナウンス & 自動監視ダッシュボード");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(960, 680);
        frame.setMinimumSize(new Dimension(860, 600));
        frame.setLocationRelativeTo(null);

        // メインコンテナ (2カラム構造)
        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.setBackground(COLOR_BG);

        // === 左サイドバー (システム & 制御パネル) ===
        JPanel sidebar = createSidebarPanel();
        sidebar.setPreferredSize(new Dimension(270, 680));

        // === 右エリア (投稿 & タイムライン表示) ===
        JPanel contentArea = createContentAreaPanel();

        rootPanel.add(sidebar, BorderLayout.WEST);
        rootPanel.add(contentArea, BorderLayout.CENTER);

        frame.setContentPane(rootPanel);

        // イベント連携
        manager.setLogListener(message -> {
            // 背景からの単体ログ出力
            SwingUtilities.invokeLater(() -> addSystemLogCard(message));
        });

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (fileWatcher != null && fileWatcher.isRunning()) {
                    fileWatcher.stop();
                }
            }
        });

        frame.setVisible(true);
        addSystemLogCard("ダッシュボードシステム起動完了 (プロ仕様SaaS UIモード)");
    }

    // ===== 1. 左サイドバー作成 =====
    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COLOR_SIDEBAR);
        sidebar.setBorder(new EmptyBorder(16, 16, 16, 16));

        // 1-1. ブランドロゴ
        JLabel logoLabel = new JLabel("⚡ NOTIFY HUB");
        logoLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        logoLabel.setForeground(COLOR_ACCENT);

        JLabel subLogoLabel = new JLabel("Pro Edition v2.0");
        subLogoLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        subLogoLabel.setForeground(COLOR_TEXT_MUTED);

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        logoPanel.setBackground(COLOR_SIDEBAR);
        logoPanel.add(logoLabel);

        // 1-2. システムステータスカード
        JPanel statusCard = createRoundedPanel(COLOR_CARD_INNER);
        statusCard.setLayout(new BorderLayout(8, 0));
        statusCard.setBorder(new EmptyBorder(8, 10, 8, 10));
        statusCard.setMaximumSize(new Dimension(240, 42));

        JLabel statusIndicator = new JLabel("🟢 ONLINE");
        statusIndicator.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        statusIndicator.setForeground(COLOR_SUCCESS);

        JLabel statusSub = new JLabel("All Services Active");
        statusSub.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        statusSub.setForeground(COLOR_TEXT_MUTED);

        statusCard.add(statusIndicator, BorderLayout.WEST);
        statusCard.add(statusSub, BorderLayout.EAST);

        // 1-3. Observer 動的切替プラグインパネル
        JLabel pluginTitle = createSectionTitle("🔌 ACTIVE PLUGINS (Observer)");
        pluginListPanel = new JPanel();
        pluginListPanel.setLayout(new BoxLayout(pluginListPanel, BoxLayout.Y_AXIS));
        pluginListPanel.setBackground(COLOR_SIDEBAR);
        refreshPluginList();

        // 1-4. FileWatcher 自動監視パネル
        JLabel watcherTitle = createSectionTitle("📂 FILE WATCHER (自動監視)");
        JPanel watcherCard = createRoundedPanel(COLOR_CARD_INNER);
        watcherCard.setLayout(new BorderLayout(0, 6));
        watcherCard.setBorder(new EmptyBorder(10, 10, 10, 10));

        String watchDirStr = fileWatcher != null ? fileWatcher.getWatchDir().toAbsolutePath().toString() : "./watch";
        watchStatusLabel = new JLabel("🔴 停止中 (" + getTruncatedPath(watchDirStr) + ")");
        watchStatusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        watchStatusLabel.setForeground(COLOR_TEXT_MUTED);

        JButton selectDirBtn = createStyledButton("📁 監視フォルダ変更", COLOR_CARD, COLOR_TEXT_PRIMARY);
        selectDirBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        selectDirBtn.addActionListener(this::onSelectDirClicked);

        watchToggleButton = createStyledButton("監視開始", COLOR_SUCCESS, Color.BLACK);
        watchToggleButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        watchToggleButton.addActionListener(this::onWatchToggleClicked);

        JPanel watcherBtnRow = new JPanel(new GridLayout(1, 2, 4, 0));
        watcherBtnRow.setBackground(COLOR_CARD_INNER);
        watcherBtnRow.add(selectDirBtn);
        watcherBtnRow.add(watchToggleButton);

        watcherCard.add(watchStatusLabel, BorderLayout.NORTH);
        watcherCard.add(watcherBtnRow, BorderLayout.SOUTH);

        // 1-5. ライブ統計カード
        JLabel analyticsTitle = createSectionTitle("📈 LIVE ANALYTICS");
        JPanel analyticsCard = createRoundedPanel(COLOR_CARD_INNER);
        analyticsCard.setLayout(new GridLayout(2, 1, 4, 4));
        analyticsCard.setBorder(new EmptyBorder(8, 10, 8, 10));

        totalBroadcastCountLabel = new JLabel("総一括配信数: 0 件");
        totalBroadcastCountLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        totalBroadcastCountLabel.setForeground(COLOR_TEXT_PRIMARY);

        successRateLabel = new JLabel("配信成功率: 100%");
        successRateLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        successRateLabel.setForeground(COLOR_SUCCESS);

        analyticsCard.add(totalBroadcastCountLabel);
        analyticsCard.add(successRateLabel);

        // サイドバー組み立て
        sidebar.add(logoPanel);
        sidebar.add(subLogoLabel);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(statusCard);
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(pluginTitle);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(pluginListPanel);
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(watcherTitle);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(watcherCard);
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(analyticsTitle);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(analyticsCard);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    // ===== 2. 右コンテンツエリア作成 =====
    private JPanel createContentAreaPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(0, 12));
        contentPanel.setBackground(COLOR_BG);
        contentPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // 2-1. メッセージ投稿カード
        JPanel composerCard = createRoundedPanel(COLOR_CARD);
        composerCard.setLayout(new BorderLayout(8, 8));
        composerCard.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel composerTitle = new JLabel("📢 メッセージ投稿 & 一括配信 (Broadcast)");
        composerTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        composerTitle.setForeground(COLOR_TEXT_PRIMARY);

        messageField = new JTextArea(3, 40);
        messageField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        messageField.setBackground(COLOR_BG);
        messageField.setForeground(COLOR_TEXT_PRIMARY);
        messageField.setCaretColor(COLOR_TEXT_PRIMARY);
        messageField.setLineWrap(true);
        messageField.setWrapStyleWord(true);
        messageField.setMargin(new Insets(6, 8, 6, 8));
        messageField.setBorder(new LineBorder(COLOR_CARD_INNER, 1, true));

        JScrollPane messageScroll = new JScrollPane(messageField);
        messageScroll.setBorder(null);

        // 優先度タグ ＆ 送信ボタン行
        JPanel actionRow = new JPanel(new BorderLayout(8, 0));
        actionRow.setBackground(COLOR_CARD);

        JPanel priorityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        priorityPanel.setBackground(COLOR_CARD);

        JLabel prioTag = new JLabel("優先度: ");
        prioTag.setForeground(COLOR_TEXT_MUTED);
        prioTag.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        priorityPanel.add(prioTag);

        priorityGroup = new ButtonGroup();
        String[] priorities = {"INFO", "WARNING", "CRITICAL"};
        for (String p : priorities) {
            JRadioButton radio = new JRadioButton(p);
            radio.setActionCommand(p);
            radio.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
            radio.setForeground(COLOR_TEXT_PRIMARY);
            radio.setBackground(COLOR_CARD);
            radio.setFocusPainted(false);
            if (p.equals("INFO")) radio.setSelected(true);
            priorityGroup.add(radio);
            priorityRadios.put(p, radio);
            priorityPanel.add(radio);
        }

        broadcastButton = createStyledButton("🚀 一括配信を実行 (Broadcast)", COLOR_ACCENT, Color.BLACK);
        broadcastButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        broadcastButton.setPreferredSize(new Dimension(210, 36));
        broadcastButton.addActionListener(this::onBroadcastClicked);

        actionRow.add(priorityPanel, BorderLayout.WEST);
        actionRow.add(broadcastButton, BorderLayout.EAST);

        composerCard.add(composerTitle, BorderLayout.NORTH);
        composerCard.add(messageScroll, BorderLayout.CENTER);
        composerCard.add(actionRow, BorderLayout.SOUTH);

        // 2-2. タイムラインアクティビティエリア
        timelineContainer = new JPanel();
        timelineContainer.setLayout(new BoxLayout(timelineContainer, BoxLayout.Y_AXIS));
        timelineContainer.setBackground(COLOR_BG);

        timelineScroll = new JScrollPane(timelineContainer);
        timelineScroll.setBackground(COLOR_BG);
        timelineScroll.getViewport().setBackground(COLOR_BG);
        timelineScroll.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(COLOR_CARD_INNER, 1, true),
                "📜 リアルタイム配信アクティビティ (タイムライン)",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(Font.SANS_SERIF, Font.BOLD, 12), COLOR_ACCENT
        ));

        contentPanel.add(composerCard, BorderLayout.NORTH);
        contentPanel.add(timelineScroll, BorderLayout.CENTER);

        return contentPanel;
    }

    // ===== 3. イベント処理 =====

    private void onBroadcastClicked(ActionEvent e) {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "メッセージを入力してください。", "入力警告", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String priority = priorityGroup.getSelection().getActionCommand();
        broadcastButton.setEnabled(false);
        messageField.setEnabled(false);

        SwingWorker<NotificationSummary, Void> worker = new SwingWorker<>() {
            @Override
            protected NotificationSummary doInBackground() {
                return manager.notifyAllPlugins(message, priority);
            }

            @Override
            protected void done() {
                broadcastButton.setEnabled(true);
                messageField.setEnabled(true);
                messageField.setText("");

                try {
                    NotificationSummary summary = get();
                    if (summary != null) {
                        totalBroadcastCount++;
                        totalSuccessCount += summary.getSuccessCount();
                        updateAnalytics();

                        // タイムラインカードに投稿を追加
                        addTimelineCard("ADMIN", message, priority, summary);

                        // サマリーポップアップ
                        showSummaryDialog(summary);
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    /** タイムラインにSlack風カードを追加 */
    private void addTimelineCard(String author, String message, String priority, NotificationSummary summary) {
        JPanel card = createRoundedPanel(COLOR_CARD);
        card.setLayout(new BorderLayout(8, 6));
        card.setBorder(new EmptyBorder(10, 12, 10, 12));
        card.setMaximumSize(new Dimension(800, 110));

        // ヘッダー (アバター, 時刻, 優先度タグ)
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setBackground(COLOR_CARD);

        JLabel avatar = new JLabel("👤 [" + author + "]");
        avatar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        avatar.setForeground(COLOR_ACCENT);

        JLabel timeLabel = new JLabel(LocalTime.now().format(TIME_FMT));
        timeLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        timeLabel.setForeground(COLOR_TEXT_MUTED);

        JLabel prioBadge = new JLabel(" [" + priority + "] ");
        prioBadge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        prioBadge.setOpaque(true);
        if ("CRITICAL".equalsIgnoreCase(priority)) {
            prioBadge.setBackground(COLOR_DANGER);
            prioBadge.setForeground(Color.WHITE);
        } else if ("WARNING".equalsIgnoreCase(priority)) {
            prioBadge.setBackground(COLOR_WARNING);
            prioBadge.setForeground(Color.BLACK);
        } else {
            prioBadge.setBackground(COLOR_ACCENT);
            prioBadge.setForeground(Color.BLACK);
        }

        header.add(avatar);
        header.add(timeLabel);
        header.add(prioBadge);

        // 本文
        JLabel msgLabel = new JLabel("<html><body>" + escapeHtml(message) + "</body></html>");
        msgLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        msgLabel.setForeground(COLOR_TEXT_PRIMARY);

        // フッター (各プラグインの結果ステータスバッジ)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        footer.setBackground(COLOR_CARD);

        JLabel resHeader = new JLabel("配信結果:");
        resHeader.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        resHeader.setForeground(COLOR_TEXT_MUTED);
        footer.add(resHeader);

        if (summary != null && summary.getResults() != null) {
            for (NotificationSummary.PluginResult res : summary.getResults()) {
                JLabel badge = new JLabel(res.getPluginName() + (res.isSuccess() ? " 🟢" : " ❌"));
                badge.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
                badge.setForeground(res.isSuccess() ? COLOR_SUCCESS : COLOR_DANGER);
                footer.add(badge);
            }
        }

        card.add(header, BorderLayout.NORTH);
        card.add(msgLabel, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        timelineContainer.add(card, 0); // 先頭に挿入
        timelineContainer.add(Box.createVerticalStrut(8), 1);
        timelineContainer.revalidate();
        timelineContainer.repaint();
    }

    /** システムログ用簡易カード */
    private void addSystemLogCard(String text) {
        JPanel card = createRoundedPanel(COLOR_CARD_INNER);
        card.setLayout(new BorderLayout(6, 2));
        card.setBorder(new EmptyBorder(6, 10, 6, 10));
        card.setMaximumSize(new Dimension(800, 40));

        JLabel sysLabel = new JLabel("🤖 [SYSTEM] " + LocalTime.now().format(TIME_FMT) + "  " + text);
        sysLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        sysLabel.setForeground(COLOR_TEXT_MUTED);

        card.add(sysLabel, BorderLayout.CENTER);

        timelineContainer.add(card, 0);
        timelineContainer.add(Box.createVerticalStrut(6), 1);
        timelineContainer.revalidate();
        timelineContainer.repaint();
    }

    private void onSelectDirClicked(ActionEvent e) {
        if (fileWatcher == null) return;
        JFileChooser chooser = new JFileChooser(fileWatcher.getWatchDir().toFile());
        chooser.setDialogTitle("監視対象フォルダを選択");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            java.io.File selected = chooser.getSelectedFile();
            if (selected != null) {
                fileWatcher.setWatchDir(selected.toPath());
                updateWatchStatusLabel();
                addSystemLogCard("監視フォルダ変更: " + selected.getAbsolutePath());
            }
        }
    }

    private void onWatchToggleClicked(ActionEvent e) {
        if (fileWatcher == null) return;
        if (fileWatcher.isRunning()) {
            fileWatcher.stop();
        } else {
            fileWatcher.start();
        }
        updateWatchStatusLabel();
    }

    private void updateWatchStatusLabel() {
        if (fileWatcher == null) return;
        String dirPath = fileWatcher.getWatchDir().toAbsolutePath().toString();
        if (fileWatcher.isRunning()) {
            watchToggleButton.setText("監視停止");
            watchToggleButton.setBackground(COLOR_DANGER);
            watchStatusLabel.setText("🟢 監視中 (" + getTruncatedPath(dirPath) + ")");
            watchStatusLabel.setForeground(COLOR_SUCCESS);
        } else {
            watchToggleButton.setText("監視開始");
            watchToggleButton.setBackground(COLOR_SUCCESS);
            watchStatusLabel.setText("🔴 停止中 (" + getTruncatedPath(dirPath) + ")");
            watchStatusLabel.setForeground(COLOR_TEXT_MUTED);
        }
    }

    private void refreshPluginList() {
        pluginListPanel.removeAll();
        List<NotificationPlugin> all = manager.getAllPlugins();
        if (all.isEmpty()) {
            JLabel empty = new JLabel("（プラグイン未登録）");
            empty.setForeground(COLOR_TEXT_MUTED);
            pluginListPanel.add(empty);
        } else {
            for (NotificationPlugin plugin : all) {
                boolean enabled = manager.isPluginEnabled(plugin);
                JCheckBox cb = new JCheckBox(plugin.getPluginName(), enabled);
                cb.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                cb.setForeground(enabled ? COLOR_TEXT_PRIMARY : COLOR_TEXT_MUTED);
                cb.setBackground(COLOR_SIDEBAR);
                cb.setFocusPainted(false);

                cb.addActionListener(e -> {
                    boolean checked = cb.isSelected();
                    manager.setPluginEnabled(plugin, checked);
                    cb.setForeground(checked ? COLOR_TEXT_PRIMARY : COLOR_TEXT_MUTED);
                });
                pluginListPanel.add(cb);
            }
        }
        pluginListPanel.revalidate();
        pluginListPanel.repaint();
    }

    private void showSummaryDialog(NotificationSummary summary) {
        StringBuilder sb = new StringBuilder();
        int failCount = summary.getTotalTargets() - summary.getSuccessCount();

        if (summary.isMultipleTargets()) {
            sb.append("🎉 【一括同斉配信が完了しました！】\n");
            sb.append("-----------------------------------------\n");
            sb.append("合計 ").append(summary.getTotalTargets()).append(" 件の通知先へ送信処理を行いました。\n");
            sb.append("（成功: ").append(summary.getSuccessCount()).append(" 件 / 失敗: ").append(failCount).append(" 件）\n\n");
            sb.append("■ 配信結果詳細:\n");

            for (NotificationSummary.PluginResult res : summary.getResults()) {
                if (res.isSuccess()) {
                    sb.append("  ✔ ").append(res.getPluginName()).append(": 成功\n");
                } else {
                    String cleanMsg = res.getDetailMessage();
                    if (cleanMsg.contains(": ")) cleanMsg = cleanMsg.substring(cleanMsg.indexOf(": ") + 2);
                    sb.append("  ❌ ").append(res.getPluginName()).append(": 失敗 (").append(cleanMsg).append(")\n");
                }
            }
            if (failCount > 0) {
                sb.append("\n💡 ヒント: 失敗した通知は、学内プロキシ等の影響によるタイムアウトの可能性があります。");
            }
            sb.append("\n(※ コアコード無修正でマルチ配信へ拡張された状態です)");
            JOptionPane.showMessageDialog(frame, sb.toString(), "配信結果サマリー (機能拡張適用中)", JOptionPane.INFORMATION_MESSAGE);
        } else {
            sb.append("📢 【送信完了】\n");
            sb.append("-----------------------------------------\n");
            NotificationSummary.PluginResult res = summary.getResults().get(0);
            sb.append("配信対象: ").append(res.getPluginName()).append(res.isSuccess() ? " (成功)\n" : " (失敗)\n");
            sb.append("\n(※ 単一プラグインのみ動作する拡張前の状態です)");
            JOptionPane.showMessageDialog(frame, sb.toString(), "送信結果サマリー (拡張前状態)", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateAnalytics() {
        totalBroadcastCountLabel.setText("総一括配信数: " + totalBroadcastCount + " 件");
        int rate = totalBroadcastCount > 0 ? (totalSuccessCount * 100 / (totalBroadcastCount * manager.getPluginCount())) : 100;
        successRateLabel.setText("配信成功率: " + Math.min(100, Math.max(0, rate)) + "%");
    }

    private String getTruncatedPath(String path) {
        if (path.length() > 22) {
            return "..." + path.substring(path.length() - 19);
        }
        return path;
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    private JLabel createSectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        label.setForeground(COLOR_ACCENT);
        return label;
    }

    private JPanel createRoundedPanel(Color bg) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(fg);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
