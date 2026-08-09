package com.optimizer.clientoptimizer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Не даёт клиенту "материализовывать" сущности, которые:
 *  - находятся дальше настраиваемой дистанции от игрока, или
 *  - превышают общий лимит одновременно отслеживаемых клиентом сущностей.
 *
 * Игроки (в т.ч. другие в мультиплеере) никогда не отбрасываются.
 * Предметы на земле обрабатываются отдельным ItemEntityOptimizer.
 *
 * Важно: это клиентская, "косметическая" оптимизация. Сущность на сервере
 * продолжает жить как обычно, отброшенная копия просто не появляется в
 * локальном мире игрока, пока не попадёт в радиус/лимит заново (например,
 * после перезахода в чанк).
 */
public class EntityCullingOptimizer {

    private final Set<UUID> tracked = ConcurrentHashMap.newKeySet();
    private final AtomicInteger trackedCount = new AtomicInteger(0);

    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent event) {
        if (!Config.ENTITY_CULLING_ENABLED.get()) return;
        if (!event.getLevel().isClientSide()) return;

        Entity entity = event.getEntity();
        if (entity instanceof Player) return;
        if (entity instanceof ItemEntity) return; // отдельный оптимизатор

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        double maxDist = Config.ENTITY_RENDER_DISTANCE.get();
        if (entity.distanceToSqr(mc.player) > maxDist * maxDist) {
            event.setCanceled(true);
            return;
        }

        int max = Config.MAX_RENDERED_ENTITIES_PER_TICK.get();
        if (Config.GUI_OPTIMIZATION_ENABLED.get() && PerformanceState.isGuiReducedModeActive()) {
            max = Math.min(max, Config.ENTITY_CAP_WHILE_GUI_OPEN.get());
        }
        if (trackedCount.get() >= max) {
            event.setCanceled(true);
            return;
        }

        if (tracked.add(entity.getUUID())) {
            trackedCount.incrementAndGet();
        }
    }

    @SubscribeEvent
    public void onLeave(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) return;
        if (tracked.remove(event.getEntity().getUUID())) {
            trackedCount.decrementAndGet();
        }
    }
}
