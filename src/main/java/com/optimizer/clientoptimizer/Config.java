package com.optimizer.clientoptimizer;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config {

    public static final ForgeConfigSpec CLIENT_SPEC;

    // --- Предметы на земле ---
    public static final ForgeConfigSpec.BooleanValue ITEM_OPTIMIZATION_ENABLED;
    public static final ForgeConfigSpec.IntValue MAX_VISIBLE_ITEM_ENTITIES;
    public static final ForgeConfigSpec.DoubleValue ITEM_MERGE_VISUAL_RADIUS;

    // --- Прочие сущности ---
    public static final ForgeConfigSpec.BooleanValue ENTITY_CULLING_ENABLED;
    public static final ForgeConfigSpec.DoubleValue ENTITY_RENDER_DISTANCE;
    public static final ForgeConfigSpec.IntValue MAX_RENDERED_ENTITIES_PER_TICK;

    // --- Частицы при добыче блоков ---
    public static final ForgeConfigSpec.BooleanValue PARTICLE_OPTIMIZATION_ENABLED;
    public static final ForgeConfigSpec.IntValue PARTICLE_LEVEL_WHILE_MINING;

    // --- Инвентарь / контейнеры ---
    public static final ForgeConfigSpec.BooleanValue GUI_OPTIMIZATION_ENABLED;
    public static final ForgeConfigSpec.IntValue PARTICLE_LEVEL_WHILE_GUI_OPEN;
    public static final ForgeConfigSpec.IntValue ENTITY_CAP_WHILE_GUI_OPEN;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Оптимизация предметов, лежащих на земле").push("items");
        ITEM_OPTIMIZATION_ENABLED = builder
                .comment("Включить оптимизацию предметов на земле")
                .define("enabled", true);
        MAX_VISIBLE_ITEM_ENTITIES = builder
                .comment("Максимум предметов, которые будут полноценно рендериться одновременно.",
                        "Остальные (самые дальние от игрока) будут скрыты.")
                .defineInRange("maxVisibleItemEntities", 100, 1, 5000);
        ITEM_MERGE_VISUAL_RADIUS = builder
                .comment("Радиус (в блоках), в котором предметы одного типа визуально схлопываются в один рендер.")
                .defineInRange("visualMergeRadius", 0.6, 0.0, 8.0);
        builder.pop();

        builder.comment("Оптимизация рендера сущностей").push("entities");
        ENTITY_CULLING_ENABLED = builder
                .comment("Включить отбрасывание рендера дальних/загороженных сущностей")
                .define("enabled", true);
        ENTITY_RENDER_DISTANCE = builder
                .comment("Дистанция (в блоках), дальше которой второстепенные сущности не рендерятся.")
                .defineInRange("entityRenderDistance", 48.0, 8.0, 512.0);
        MAX_RENDERED_ENTITIES_PER_TICK = builder
                .comment("Жёсткий лимит на количество не-игровых сущностей, рендерящихся за кадр.")
                .defineInRange("maxRenderedEntities", 150, 10, 5000);
        builder.pop();

        builder.comment("Оптимизация частиц при ломании блоков").push("particles");
        PARTICLE_OPTIMIZATION_ENABLED = builder
                .comment("Автоматически понижать настройку частиц, пока игрок ломает блок")
                .define("enabled", true);
        PARTICLE_LEVEL_WHILE_MINING = builder
                .comment("Уровень частиц во время добычи: 0 = ALL, 1 = DECREASED, 2 = MINIMAL")
                .defineInRange("miningParticleLevel", 2, 0, 2);
        builder.pop();

        builder.comment("Оптимизация при открытом инвентаре/сундуках/верстаке и т.п.").push("gui");
        GUI_OPTIMIZATION_ENABLED = builder
                .comment("Включить снижение нагрузки, пока открыт инвентарь или другой контейнер")
                .define("enabled", true);
        PARTICLE_LEVEL_WHILE_GUI_OPEN = builder
                .comment("Уровень частиц, пока открыт инвентарь: 0 = ALL, 1 = DECREASED, 2 = MINIMAL")
                .defineInRange("guiParticleLevel", 2, 0, 2);
        ENTITY_CAP_WHILE_GUI_OPEN = builder
                .comment("Лимит новых сущностей, которые клиент разрешит подгрузить, пока открыт инвентарь.",
                        "Уже отрисованные рядом сущности это не убирает — сильнее всего помогает,",
                        "когда лаги вызваны постоянным потоком новых мобов/предметов рядом (спавнер, ферма и т.п.).")
                .defineInRange("guiEntityJoinCap", 20, 0, 5000);
        builder.pop();

        CLIENT_SPEC = builder.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}
