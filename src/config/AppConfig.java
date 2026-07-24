package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private final Properties properties = new Properties();

    public AppConfig(String fileName) {
        // src/main/resources 配下に置くとクラスパス経由で読み込める
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException(
                        "設定ファイルが見つかりません: " + fileName
                                + "（config.properties.example を参考に作成してください）");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("設定ファイルの読み込みに失敗しました", e);
        }
    }

    public String get(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("設定値が未設定です: " + key);
        }
        return value;
    }
}
