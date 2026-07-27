package core;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// ===== ファイル監視トリガー (FileWatcher) =====
// java.nio.file.WatchService を使い、特定フォルダ（例: ./watch）への
// ファイル追加（ENTRY_CREATE）を常時監視して自動通知を行う。
public class FileWatcher {
    private final EventManager manager;
    private volatile Path watchDir;
    private WatchService watchService;
    private ExecutorService executor;
    private volatile boolean running = false;

    public FileWatcher(EventManager manager, String dirPath) {
        this.manager = manager;
        this.watchDir = Paths.get(dirPath);
    }

    /** 監視フォルダパスを取得 */
    public synchronized Path getWatchDir() {
        return watchDir;
    }

    /** 監視フォルダパスを変更（監視中の場合は自動再起動） */
    public synchronized void setWatchDir(Path newDir) {
        if (newDir == null) return;
        boolean wasRunning = running;
        if (wasRunning) {
            stop();
        }
        this.watchDir = newDir;
        System.out.println("[FileWatcher] 監視フォルダを変更しました: " + watchDir.toAbsolutePath());
        if (wasRunning) {
            start();
        }
    }

    public synchronized void setWatchDir(String dirPath) {
        if (dirPath != null && !dirPath.trim().isEmpty()) {
            setWatchDir(Paths.get(dirPath));
        }
    }

    /** 監視が動いているか */
    public boolean isRunning() {
        return running;
    }

    /** ファイル監視をバックグラウンドスレッドで開始 */
    public synchronized void start() {
        if (running) {
            System.out.println("[FileWatcher] 既に監視動作中です");
            return;
        }

        try {
            // 監視対象ディレクトリが存在しなければ作成
            if (!Files.exists(watchDir)) {
                Files.createDirectories(watchDir);
                System.out.println("[FileWatcher] 監視フォルダを作成しました: " + watchDir.toAbsolutePath());
            }

            watchService = FileSystems.getDefault().newWatchService();
            watchDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            running = true;
            executor = Executors.newSingleThreadExecutor();
            executor.submit(this::watchLoop);

            System.out.println("[FileWatcher] ファイル監視を開始しました (対象: " + watchDir.toAbsolutePath() + ")");

        } catch (IOException e) {
            System.err.println("[FileWatcher] 監視開始に失敗しました: " + e.getMessage());
        }
    }

    /** ファイル監視ループ（別スレッドで実行） */
    private void watchLoop() {
        while (running) {
            try {
                WatchKey key = watchService.take(); // イベント発生までブロック
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context();

                        String msg = "新しいファイルが検知されました: " + fileName;
                        System.out.println("[FileWatcher] 検知: " + fileName);
                        manager.notifyAllPlugins(msg, "FILE_WATCHER");
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            } catch (ClosedWatchServiceException e) {
                // stop() で正常に終了した場合
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("[FileWatcher] エラーが発生しました: " + e.getMessage());
            }
        }
    }

    /** ファイル監視を停止 */
    public synchronized void stop() {
        if (!running) return;

        running = false;
        try {
            if (watchService != null) {
                watchService.close();
            }
        } catch (IOException e) {
            System.err.println("[FileWatcher] 停止時の例外: " + e.getMessage());
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }

        System.out.println("[FileWatcher] ファイル監視を停止しました");
    }
}
