package core;

import java.util.List;

// ===== 通知配信結果サマリー DTO =====
public class NotificationSummary {
    private final int totalTargets;
    private final int successCount;
    private final List<String> targetPluginNames;

    public NotificationSummary(int totalTargets, int successCount, List<String> targetPluginNames) {
        this.totalTargets = totalTargets;
        this.successCount = successCount;
        this.targetPluginNames = targetPluginNames;
    }

    public int getTotalTargets() {
        return totalTargets;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public List<String> getTargetPluginNames() {
        return targetPluginNames;
    }

    public boolean isMultipleTargets() {
        return totalTargets > 1;
    }
}
