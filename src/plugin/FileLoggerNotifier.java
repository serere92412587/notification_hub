package plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// ===== ローカルファイルログ保存プラグイン (Strategy具象クラス) =====
// 通知が発生するたびにローカルのログファイル（デフォルト: logs/notification.log）へ
// タイムスタンプ・優先度・メッセージを自動追記保存する。
public class FileLoggerNotifier implements NotificationPlugin {
    private final Path logFilePath;
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FileLoggerNotifier() {
        this("logs/notification.log");
    }

    public FileLoggerNotifier(String filePath) {
        String path = (filePath != null && !filePath.trim().isEmpty()) ? filePath.trim() : "logs/notification.log";
        this.logFilePath = Paths.get(path);
    }

    @Override
    public String getPluginName() {
        return "ローカルファイルログ";
    }

    @Override
    public String sendNotification(String message, String priority) {
        try {
            // 親ディレクトリが存在しない場合は自動作成
            Path parentDir = logFilePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
            String logEntry = String.format("[%s] [%s] %s%n", timestamp, priority.toUpperCase(), message);

            Files.writeString(
                    logFilePath,
                    logEntry,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND
            );

            return "[FileLogger] 成功 (" + logFilePath.toAbsolutePath() + " に追記)";

        } catch (IOException e) {
            return "[FileLogger] 失敗: " + e.getMessage();
        }
    }
}
