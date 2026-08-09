package com.optimizer.clientoptimizer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Client Optimizer
 *
 * Чисто клиентский мод. Он:
 *  - не регистрирует блоки/предметы/рецепты,
 *  - не требует установки на сервер (можно спокойно заходить на ванильные сервера),
 *  - весь свой код исполняет только если Distribution == CLIENT.
 *
 * Что делает:
 *  1. ItemEntityOptimizer  — ограничивает количество одновременно
 *     обрабатываемых/анимируемых предметов, лежащих на земле, и
 *     "схлопывает" визуально предметы, лежащие плотной кучей.
 *  2. EntityCullingOptimizer — отменяет рендер сущностей, которые находятся
 *     дальше настраиваемой дистанции или скрыты от игрока, снижая нагрузку
 *     на рендер в толпе мобов.
 *  3. ParticleOptimizer — временно понижает уровень частиц игры, пока игрок
 *     активно ломает блоки, и возвращает исходную настройку, когда добыча
 *     заканчивается.
 *  4. GuiOptimizer — при открытии инвентаря/сундука/верстака и т.п. включает
 *     экономный режим (см. ParticleOptimizer и EntityCullingOptimizer) на
 *     время, пока экран открыт.
 */
@Mod(ClientOptimizerMod.MOD_ID)
public class ClientOptimizerMod {

    public static final String MOD_ID = "clientoptimizer";

    public ClientOptimizerMod() {

        // Говорим Forge не требовать этот мод на сервере и не ругаться на
        // отсутствие серверной версии — стандартный приём для клиентских модов.
        ModLoadingContext.get().registerExtensionPoint(
                IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remoteVersion, isServer) -> true)
        );

        // Весь функционал мода имеет смысл только на клиенте.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            Config.register();

            MinecraftForge.EVENT_BUS.register(new ItemEntityOptimizer());
            MinecraftForge.EVENT_BUS.register(new EntityCullingOptimizer());
            MinecraftForge.EVENT_BUS.register(new ParticleOptimizer());
            MinecraftForge.EVENT_BUS.register(new GuiOptimizer());
        }
    }
}
