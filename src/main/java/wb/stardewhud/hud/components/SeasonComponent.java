package wb.stardewhud.hud.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
import wb.stardewhud.StardewHUD;
import wb.stardewhud.hud.HudRenderer;

public class SeasonComponent {
    private final HudRenderer hudRenderer;

    // 季节图标 - 使用Identifier.of()方法
    private static final Identifier SPRING_ICON =
            Identifier.of(StardewHUD.MOD_ID, "textures/icons/fortune/spring.png");
    private static final Identifier SUMMER_ICON =
            Identifier.of(StardewHUD.MOD_ID, "textures/icons/fortune/summer.png");
    private static final Identifier AUTUMN_ICON =
            Identifier.of(StardewHUD.MOD_ID, "textures/icons/fortune/autumn.png");
    private static final Identifier WINTER_ICON =
            Identifier.of(StardewHUD.MOD_ID, "textures/icons/fortune/winter.png");

    private long lastCalculatedDay = -1;
    private Identifier currentSeasonIcon = SPRING_ICON;

    public SeasonComponent(HudRenderer hudRenderer) {
        this.hudRenderer = hudRenderer;
    }

    public void render(DrawContext context, int x, int y) {
        context.drawTexture(currentSeasonIcon, x - 8, y - 2, 0, 0, 41, 17, 41, 17);
    }

    public void update() {
        MinecraftClient client = hudRenderer.getClient();
        if (client == null || client.player == null || client.world == null) {
            return;
        }

        updateSeasonIcon(client.world.getTimeOfDay());
    }

    // 根据时间切换季节图标
    private void updateSeasonIcon(long timeOfDay) {
        long dayFromTicks = timeOfDay / 24000L;

        if (dayFromTicks != lastCalculatedDay) {
            long oldDay = lastCalculatedDay;
            lastCalculatedDay = dayFromTicks;

            // 从配置中获取每个季节持续的天数
            int seasonDays = StardewHUD.getConfig().seasonDays;

            // 计算季节索引（使用配置的天数）
            int seasonIndex = (int)((dayFromTicks / seasonDays) % 4);

            // 获取新季节图标
            Identifier newSeasonIcon = getSeasonIcon(seasonIndex);

            // 只在季节变化时更新
            if (newSeasonIcon != currentSeasonIcon) {
                Identifier oldIcon = currentSeasonIcon;
                currentSeasonIcon = newSeasonIcon;

                StardewHUD.LOGGER.info("季节图标切换: [{}] 第{}天 -> 第{}天, 图标: {} -> {} (每个季节{}天)",
                        getSeasonName(seasonIndex),
                        oldDay, dayFromTicks,
                        getFileName(oldIcon), getFileName(newSeasonIcon),
                        seasonDays);
            } else {
                StardewHUD.LOGGER.debug("游戏日变化: 第{}天 -> 第{}天, 季节保持: {} (每个季节{}天)",
                        oldDay, dayFromTicks, getSeasonName(seasonIndex), seasonDays);
            }
        }
    }

    private Identifier getSeasonIcon(int seasonIndex) {
        switch (seasonIndex) {
            case 0: return SPRING_ICON;
            case 1: return SUMMER_ICON;
            case 2: return AUTUMN_ICON;
            case 3: return WINTER_ICON;
            default: return SPRING_ICON;
        }
    }

    private String getSeasonName(int seasonIndex) {
        switch (seasonIndex) {
            case 0: return "春季";
            case 1: return "夏季";
            case 2: return "秋季";
            case 3: return "冬季";
            default: return "未知";
        }
    }

    private String getFileName(Identifier id) {
        if (id == null) return "null";
        String path = id.getPath();
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    public void reset() {
        lastCalculatedDay = -1;
        currentSeasonIcon = SPRING_ICON;
        StardewHUD.LOGGER.debug("已重置SeasonComponent数据");
    }
}