package com.optimizer.clientoptimizer;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Реагирует на открытие/закрытие инвентаря, сундуков, верстака, печи и т.п.
 * (любой экран, унаследованный от AbstractContainerScreen — это охватывает
 * и ванильные, и почти все модовые контейнеры).
 *
 * Пока такой экран открыт, включает "экономный" режим:
 *  - ParticleOptimizer понижает частицы до уровня guiParticleLevel;
 *  - EntityCullingOptimizer урезает лимит новых сущностей, которые клиент
 *    позволит подгрузить, до guiEntityJoinCap.
 *
 * Важная честная оговорка: это НЕ убирает уже отрисованные рядом с игроком
 * сущности/чанки — на них влияет только более глубокая оптимизация (см.
 * README, раздел про Mixin). Этот модуль лучше всего помогает, когда лаги
 * при открытии инвентаря вызваны потоком новых частиц/мобов рядом с игроком
 * (мобная ферма, спавнер, толпа мобов), а не самим актом отрисовки GUI.
 */
public class GuiOptimizer {

    @SubscribeEvent
    public void onScreenOpen(ScreenEvent.Opening event) {
        if (!Config.GUI_OPTIMIZATION_ENABLED.get()) return;

        Screen newScreen = event.getNewScreen();
        boolean isContainer = newScreen instanceof AbstractContainerScreen<?>;

        PerformanceState.setGuiReducedModeActive(isContainer);
    }

    @SubscribeEvent
    public void onScreenClosing(ScreenEvent.Closing event) {
        Screen closed = event.getScreen();
        if (closed instanceof AbstractContainerScreen<?>) {
            PerformanceState.setGuiReducedModeActive(false);
        }
    }
}
