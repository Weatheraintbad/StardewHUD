package wb.stardewhud.hud.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.client.network.ClientPlayerEntity;
import wb.stardewhud.StardewHUD;
import wb.stardewhud.hud.HudRenderer;

public class TimeDisplayComponent {
    private final HudRenderer hudRenderer;

    // 星期名称 - 软编码
    private static final String[] WEEKDAY_KEYS = {
            "weekday.stardewhud.monday",
            "weekday.stardewhud.tuesday",
            "weekday.stardewhud.wednesday",
            "weekday.stardewhud.thursday",
            "weekday.stardewhud.friday",
            "weekday.stardewhud.saturday",
            "weekday.stardewhud.sunday"
    };

    // 当前游戏数据
    private long currentDay = 1; // 从1开始
    private String currentWeekdayKey = WEEKDAY_KEYS[0];
    private String currentTime = "00:00";

    // 存储当前游戏日的时间刻度基准
    private long lastCalculatedDay = -1; // 记录上一次计算的游戏日（从0开始）

    // === 字号控制 ===
    private static final float TEXT_SCALE = 1.1f;

    // === 文字颜色配置 ===
    private static final int GAME_INFO_TEXT_COLOR = 0x1a1a1a;     // 游戏日和星期文字颜色（黑色）
    private static final int TIME_TEXT_COLOR = 0x1a1a1a;         // 时间文字颜色（黑色）

    // === 阴影配置 ===
    private static final boolean ENABLE_GAME_INFO_SHADOW = false; // 游戏日和星期是否启用阴影
    private static final boolean ENABLE_TIME_SHADOW = false;      // 时间是否启用阴影
    private static final int SHADOW_COLOR = 0x808080;             // 阴影颜色（如果启用）

    public TimeDisplayComponent(HudRenderer hudRenderer) {
        this.hudRenderer = hudRenderer;
    }

    public void renderGameInfo(DrawContext context, int infoBoxStartX, int infoBoxWidth, int y) {
        // 获取本地化的星期名称
        String weekdayText = Text.translatable(currentWeekdayKey).getString();
        // 格式化游戏日信息 - 现在使用本地化的格式
        String gameInfo = Text.translatable("time.stardewhud.gameDay", currentDay, weekdayText).getString();

        // === 添加infoBoxCenterX计算===
        int infoBoxCenterX = calculateInfoBoxCenterX(infoBoxStartX, infoBoxWidth);
        int centeredX = calculateCenteredXForText(context, gameInfo, infoBoxCenterX);

        // === 使用带颜色和阴影配置的绘制方法 ===
        drawTextWithConfig(context, gameInfo, centeredX, y, TEXT_SCALE,
                GAME_INFO_TEXT_COLOR, ENABLE_GAME_INFO_SHADOW);
    }

    public void renderTime(DrawContext context, int infoBoxStartX, int infoBoxWidth, int y) {
        int infoBoxCenterX = calculateInfoBoxCenterX(infoBoxStartX, infoBoxWidth);
        int centeredX = calculateCenteredXForText(context, currentTime, infoBoxCenterX);

        drawTextWithConfig(context, currentTime, centeredX, y, TEXT_SCALE,
                TIME_TEXT_COLOR, ENABLE_TIME_SHADOW);
    }

    // === 带配置的文字绘制方法 ===
    private void drawTextWithConfig(DrawContext context, String text, int x, int y,
                                    float scale, int textColor, boolean enableShadow) {
        MinecraftClient client = hudRenderer.getClient();

        // 保存当前变换状态
        context.getMatrices().push();

        // 应用缩放
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1.0f);

        context.drawText(client.textRenderer, text, 0, 0, textColor, enableShadow);

        // 恢复变换状态
        context.getMatrices().pop();
    }

    // === 计算信息框中心X坐标 ===
    private int calculateInfoBoxCenterX(int infoBoxStartX, int infoBoxWidth) {
        return infoBoxStartX + infoBoxWidth / 2;
    }

    // === 基于中心点计算文本居中位置 ===
    private int calculateCenteredXForText(DrawContext context, String text, int centerX) {
        MinecraftClient client = hudRenderer.getClient();
        int textWidth = client.textRenderer.getWidth(text);
        return centerX - textWidth / 2;
    }

    private int calculateCenteredX(DrawContext context, String text, int infoBoxStartX, int infoBoxWidth) {
        MinecraftClient client = hudRenderer.getClient();
        int textWidth = client.textRenderer.getWidth(text);
        return infoBoxStartX + (infoBoxWidth - textWidth) / 2;
    }

    private void drawTextWithShadowScaled(DrawContext context, String text, int x, int y, float scale) {
        // 默认使用游戏信息配置
        drawTextWithConfig(context, text, x, y, scale, GAME_INFO_TEXT_COLOR, ENABLE_GAME_INFO_SHADOW);
    }

    private void drawTextWithShadowScaled(DrawContext context, String text, int x, int y, float scale, int color) {
        drawTextWithConfig(context, text, x, y, scale, color, false);
    }

    public void update(World world) {
        if (world == null) return;

        long timeOfDay = world.getTimeOfDay();
        currentTime = formatGameTime(timeOfDay);

        updateCurrentDay(timeOfDay);

        updateWeekday();
    }

    private void updateCurrentDay(long timeOfDay) {
        // 计算当前游戏日（从0开始）
        long dayFromTicks = timeOfDay / 24000L;

        if (dayFromTicks != lastCalculatedDay) {
            // 第一次初始化或天数发生变化
            if (lastCalculatedDay == -1) {
                // 第一次计算，直接设置当前日
                currentDay = dayFromTicks;
                StardewHUD.LOGGER.info("初始化游戏日: 第{}天 (游戏时间: {}, 计算天数: {})",
                        currentDay, timeOfDay, dayFromTicks);
            } else {
                // 天数增加
                long dayDifference = dayFromTicks - lastCalculatedDay;
                if (dayDifference > 0) {
                    // 天数增加，需要同步更新显示的currentDay
                    currentDay += dayDifference;
                    StardewHUD.LOGGER.info("检测到新的一天: 第{}天 (增加{}天, 计算天数: {})",
                            currentDay, dayDifference, dayFromTicks);
                } else if (dayDifference < 0) {
                    // 时间倒流（使用命令或重新加载世界）
                    currentDay = dayFromTicks;
                    if (currentDay < 1) currentDay = 1; // 确保不小于1
                    StardewHUD.LOGGER.warn("检测到时间倒流，重新设置游戏日: 第{}天 (计算天数: {})",
                            currentDay, dayFromTicks);
                }
            }

            lastCalculatedDay = dayFromTicks;
        }
    }

    private void updateWeekday() {
        int weekdayIndex = (int) ((currentDay - 1) % 7);
        if (weekdayIndex < 0) weekdayIndex += 7; // 确保非负
        currentWeekdayKey = WEEKDAY_KEYS[weekdayIndex];
    }

    private String formatGameTime(long timeOfDay) {
        long hour = (timeOfDay / 1000L + 6L) % 24L;
        long minute = (timeOfDay % 1000L) * 60L / 1000L;
        return String.format("%02d:%02d", hour, minute);
    }

    // 强制设置游戏日（测试）
    public void setCurrentDay(long day) {
        this.currentDay = Math.max(1, day);
        this.lastCalculatedDay = 0;
        updateWeekday();
        StardewHUD.LOGGER.info("手动设置天数: 第{}天", currentDay);
    }

    public void syncWithWorldTime(World world) {
        if (world == null) return;

        long timeOfDay = world.getTimeOfDay();
        long dayFromTicks = timeOfDay / 24000L;
        this.currentDay = dayFromTicks; // 从0开始显示
        this.lastCalculatedDay = dayFromTicks;
        updateWeekday();
        StardewHUD.LOGGER.info("同步游戏日到世界时间: 第{}天 (游戏时间: {}, 计算天数: {})",
                currentDay, timeOfDay, dayFromTicks);
    }

    // 获取当前游戏日
    public long getCurrentDay() {
        return currentDay;
    }

    // 获取当前星期
    public String getCurrentWeekday() {
        return Text.translatable(currentWeekdayKey).getString();
    }

    // 获取当前星期键（用于调试）
    public String getCurrentWeekdayKey() {
        return currentWeekdayKey;
    }

    // === 获取文字宽度的方法（用于HudRenderer中计算位置）===
    public int getTextWidth(DrawContext context, String text) {
        MinecraftClient client = hudRenderer.getClient();
        return client.textRenderer.getWidth(text);
    }

    // === 获取游戏日文字宽度 ===
    public int getGameInfoTextWidth(DrawContext context) {
        String weekdayText = Text.translatable(currentWeekdayKey).getString();
        String gameInfo = Text.translatable("time.stardewhud.gameDay", currentDay, weekdayText).getString();
        return getTextWidth(context, gameInfo);
    }

    // === 获取时间文字宽度 ===
    public int getTimeTextWidth(DrawContext context) {
        return getTextWidth(context, currentTime);
    }

    // === 获取配置值的方法（调试或外部访问）===
    public static int getGameInfoTextColor() {
        return GAME_INFO_TEXT_COLOR;
    }

    public static int getTimeTextColor() {
        return TIME_TEXT_COLOR;
    }

    public static boolean isGameInfoShadowEnabled() {
        return ENABLE_GAME_INFO_SHADOW;
    }

    public static boolean isTimeShadowEnabled() {
        return ENABLE_TIME_SHADOW;
    }

    public static float getTextScale() {
        return TEXT_SCALE;
    }
}