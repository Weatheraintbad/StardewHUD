package wb.stardewhud.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ClothConfigScreenFactory {
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Stardew HUD 设置"))
                .setSavingRunnable(ConfigManager::save); // 点【完成】时落盘

        ConfigCategory main = builder.getOrCreateCategory(Text.literal("通用"));
        ConfigEntryBuilder entry = ConfigEntryBuilder.create();

        ModConfig.Counter c = ConfigManager.get().counters.get(0); // 只有一个

        /* 物品 ID 输入框 */
        main.addEntry(entry.startStrField(Text.literal("要统计的物品 ID"), c.itemId)
                .setDefaultValue("minecraft:emerald")
                .setTooltip(Text.literal("例如 minecraft:diamond、minecraft:gold_ingot"))
                .setSaveConsumer(newId -> c.itemId = newId) // 实时写回配置
                .build());

        /* 如果你还想让玩家改名字、颜色、缩放，继续 .addEntry 即可 */
        return builder.build();
    }
}