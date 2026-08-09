package com.optimizer.clientoptimizer;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Снижает нагрузку от предметов, лежащих на земле:
 *  - не даёт клиенту одновременно "знать" больше N item-сущностей;
 *  - если рядом уже есть предмет того же типа, новый не добавляется на клиент
 *    (визуально куча предметов выглядит как один стак вместо десятков).
 *
 * Работает только с клиентским представлением мира — на сервере предметы
 * никуда не деваются, подбор и стакинг на сервере не затрагиваются.
 */
public class ItemEntityOptimizer {

    // uuid -> позиция+тип уже "видимых" клиенту предметов
    private final Map<UUID, TrackedItem> visibleItems = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent event) {
        if (!Config.ITEM_OPTIMIZATION_ENABLED.get()) return;
        if (!event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;

        int max = Config.MAX_VISIBLE_ITEM_ENTITIES.get();
        if (visibleItems.size() >= max) {
            event.setCanceled(true);
            return;
        }

        double mergeRadiusSq = Config.ITEM_MERGE_VISUAL_RADIUS.get() * Config.ITEM_MERGE_VISUAL_RADIUS.get();
        ItemStack stack = itemEntity.getItem();

        for (TrackedItem tracked : visibleItems.values()) {
            if (!tracked.sameItem(stack)) continue;
            double dx = tracked.x - itemEntity.getX();
            double dy = tracked.y - itemEntity.getY();
            double dz = tracked.z - itemEntity.getZ();
            double distSq = dx * dx + dy * dy + dz * dz;
            if (mergeRadiusSq <= 0 || distSq <= mergeRadiusSq) {
                // Рядом уже есть визуально идентичный предмет — новый не показываем.
                event.setCanceled(true);
                return;
            }
        }

        visibleItems.put(itemEntity.getUUID(), new TrackedItem(
                stack.getItem(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ()));
    }

    @SubscribeEvent
    public void onLeave(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof ItemEntity)) return;
        if (!event.getLevel().isClientSide()) return;
        visibleItems.remove(event.getEntity().getUUID());
    }

    private static final class TrackedItem {
        final net.minecraft.world.item.Item item;
        final double x, y, z;

        TrackedItem(net.minecraft.world.item.Item item, double x, double y, double z) {
            this.item = item;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        boolean sameItem(ItemStack other) {
            return other.getItem() == item;
        }
    }
}
