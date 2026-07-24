package core;

// ===== ログ出力リスナー =====
// EventManager のログ出力を外部（GUI等）に転送するためのインターフェース。
// 設定しなければ従来通り System.out.println のみで動作する。
public interface LogListener {
    void onLog(String message);
}
