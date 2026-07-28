package core;

import java.util.List;

// ===== 通知配信結果サマリー DTO =====
public class NotificationSummary {

    public static class PluginResult {
        private final String pluginName;
        private final boolean success;
        private final String detailMessage;

        public PluginResult(String pluginName, boolean success, String detailMessage) {
            this.pluginName = pluginName;
            this.success = success;
            this.detailMessage = detailMessage;
        }

        public String getPluginName() {
            return pluginName;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getDetailMessage() {
            return detailMessage;
        }
    }

    private final int totalTargets;
    private final int successCount;
    private final List<PluginResult> results;

    public NotificationSummary(int totalTargets, int successCount, List<PluginResult> results) {
        this.totalTargets = totalTargets;
        this.successCount = successCount;
        this.results = results;
    }

    public int getTotalTargets() {
        return totalTargets;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public List<PluginResult> getResults() {
        return results;
    }

    public boolean isMultipleTargets() {
        return totalTargets > 1;
    }
}
