package com.optimizer.clientoptimizer;

/**
 * Простой общий флаг: открыт ли сейчас у игрока инвентарь/контейнер.
 * Читают ParticleOptimizer и EntityCullingOptimizer, чтобы временно
 * работать в более экономном режиме, пока экран открыт.
 */
final class PerformanceState {

    private PerformanceState() {}

    private static volatile boolean guiReducedModeActive = false;

    static boolean isGuiReducedModeActive() {
        return guiReducedModeActive;
    }

    static void setGuiReducedModeActive(boolean active) {
        guiReducedModeActive = active;
    }
}
