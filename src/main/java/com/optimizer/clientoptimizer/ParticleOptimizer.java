package com.optimizer.clientoptimizer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Пока игрок активно ломает блок (зажата ЛКМ по блоку), временно понижает
 * настройку частиц игры (Options#particles), чтобы уменьшить количество
 * частиц от крошки блока и, если в модпаке есть другие источники частиц,
 * снизить общую нагрузку. После того как добыча прекращается, исходная
 * настройка частиц игрока восстанавливается автоматически.
 *
 * Используется только публичный/официальный API (Options.particles()),
 * без рефлексии и миксинов — поэтому решение стабильно между сборками Forge.
 */
public class ParticleOptimizer {

    private boolean overridden = false;
    private ParticleStatus originalStatus = null;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            restoreIfNeeded(mc);
            return;
        }

        boolean guiReduced = Config.GUI_OPTIMIZATION_ENABLED.get()
                && PerformanceState.isGuiReducedModeActive();

        boolean mining = Config.PARTICLE_OPTIMIZATION_ENABLED.get()
                && mc.options.keyAttack.isDown()
                && mc.hitResult instanceof BlockHitResult blockHit
                && blockHit.getType() == HitResult.Type.BLOCK;

        if (mining || guiReduced) {
            if (!overridden) {
                originalStatus = mc.options.particles().get();
                overridden = true;
            }
            int level = guiReduced ? Config.PARTICLE_LEVEL_WHILE_GUI_OPEN.get() : Config.PARTICLE_LEVEL_WHILE_MINING.get();
            mc.options.particles().set(levelFromConfig(level));
        } else {
            restoreIfNeeded(mc);
        }
    }

    private void restoreIfNeeded(Minecraft mc) {
        if (overridden && originalStatus != null) {
            mc.options.particles().set(originalStatus);
        }
        overridden = false;
        originalStatus = null;
    }

    private static ParticleStatus levelFromConfig(int level) {
        return switch (level) {
            case 0 -> ParticleStatus.ALL;
            case 1 -> ParticleStatus.DECREASED;
            default -> ParticleStatus.MINIMAL;
        };
    }
}
