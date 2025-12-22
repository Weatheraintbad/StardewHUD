package wb.stardewhud.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import wb.stardewhud.StardewHUD;
import wb.stardewhud.config.ModConfig;
import wb.stardewhud.hud.components.*;

public class HudRenderer {
    private final ModConfig config;
    private final MinecraftClient client;

    // HUD组件
    private final ClockComponent clock;
    private final TimeDisplayComponent timeDisplay;
    private final WeatherComponent weather;
    private final FortuneComponent fortune;
    private final ItemCounterComponent itemCounter;

    // 纹理标识符
    public static final Identifier CLOCK_BG = new Identifier(StardewHUD.MOD_ID, "textures/gui/clock_bg.png");
    public static final Identifier INFO_BG = new Identifier(StardewHUD.MOD_ID, "textures/gui/info_bg.png");
    public static final Identifier COUNTER_BG = new Identifier(StardewHUD.MOD_ID, "textures/gui/counter_bg.png");

    // HUD尺寸常量（原始尺寸，未缩放）
    private static final int CLOCK_WIDTH = 40;
    private static final int CLOCK_HEIGHT = 65;
    private static final int INFO_WIDTH = 80;
    private static final int INFO_HEIGHT = 65;
    private static final int COUNTER_WIDTH = 100;
    private static final int COUNTER_HEIGHT = 32;
    private static final int COUNTER_TOP_MARGIN = -2;

    // 总宽度和高度（原始尺寸）
    private static final int TOTAL_WIDTH = CLOCK_WIDTH + INFO_WIDTH;
    private static final int TOTAL_HEIGHT = INFO_HEIGHT + COUNTER_TOP_MARGIN + COUNTER_HEIGHT;

    public HudRenderer(ModConfig config) {
        this.config = config;
        this.client = MinecraftClient.getInstance();

        // 初始化组件
        this.clock = new ClockComponent(this);
        this.timeDisplay = new TimeDisplayComponent(this);
        this.weather = new WeatherComponent(this);
        this.fortune = new FortuneComponent(this);
        this.itemCounter = new ItemCounterComponent(this, config.counterItemId);
    }

    public void render(DrawContext context, float tickDelta) {
        if (!shouldRender()) return;

        // 计算屏幕尺寸
        int screenWidth = client.getWindow().getScaledWidth();
        int margin = 10; // 边距

        // 计算缩放后的总尺寸
        int scaledTotalWidth = (int)(TOTAL_WIDTH * config.scale);
        int scaledTotalHeight = (int)(TOTAL_HEIGHT * config.scale);

        // 计算位置（现在从右侧计算X坐标）
        int x, y;
        if (config.position.x == 0 && config.position.y == 0) {
            // 自动定位到右上角
            x = screenWidth - scaledTotalWidth - margin;
            y = margin;
        } else {
            // 使用配置的位置
            // X: 从右侧计算（屏幕宽度 - 配置的X值）
            // Y: 从顶部计算（直接使用配置的Y值）
            x = screenWidth - config.position.x;
            y = config.position.y;
        }

        // 保存当前变换状态
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(config.scale, config.scale, 1.0f);

        try {
            // 1. 渲染计数器（如果启用）
            if (config.showItemCounter) {
                int counterX = CLOCK_WIDTH + INFO_WIDTH - COUNTER_WIDTH;
                int counterY = INFO_HEIGHT + COUNTER_TOP_MARGIN;
                itemCounter.render(context, counterX, counterY);
            }

            // 2. 渲染时钟组件（如果启用）
            if (config.showClock) {
                clock.render(context, 0, 0, tickDelta);
            }

            // 3. 渲染信息框背景（如果启用了任何信息框组件）
            if (config.showTimeDisplay || config.showWeather || config.showFortune) {
                // 渲染信息框背景
                renderInfoBackground(context, CLOCK_WIDTH, 0);

                // 渲染天气和运势图标
                if (config.showWeather || config.showFortune) {
                    int iconY = 26;
                    int iconSpacing = 24;
                    int firstIconX = CLOCK_WIDTH + 15;

                    if (config.showWeather) {
                        weather.render(context, firstIconX, iconY);
                    }

                    if (config.showFortune) {
                        fortune.render(context, firstIconX + iconSpacing, iconY);
                    }
                }

                // 渲染时间显示
                if (config.showTimeDisplay) {
                    // 第一层日期显示水平居中
                    timeDisplay.renderGameInfo(context, CLOCK_WIDTH, INFO_WIDTH, 10);

                    // 第三层时间显示水平居中
                    timeDisplay.renderTime(context, CLOCK_WIDTH - 4, INFO_WIDTH, 48);
                }
            }

        } finally {
            context.getMatrices().pop();
        }
    }

    private void renderInfoBackground(DrawContext context, int x, int y) {
        // 渲染信息框背景
        context.setShaderColor(1.0f, 1.0f, 1.0f, config.backgroundAlpha);
        context.drawTexture(INFO_BG, x, y, 0, 0, INFO_WIDTH, INFO_HEIGHT, INFO_WIDTH, INFO_HEIGHT);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void update() {
        // 更新所有组件的数据
        ClientWorld world = client.world;
        if (world != null) {
            if (config.showClock) clock.update();
            if (config.showTimeDisplay) timeDisplay.update(world);
            if (config.showWeather) weather.update(world);
            if (config.showFortune) fortune.update();
            if (config.showItemCounter) itemCounter.update();
        }
    }

    public boolean shouldRender() {
        return config.enabled && client.world != null && !client.options.hudHidden;
    }

    public ModConfig getConfig() {
        return config;
    }

    public MinecraftClient getClient() {
        return client;
    }

    // 获取缩放后的HUD尺寸
    public int getHudWidth() {
        return (int)(TOTAL_WIDTH * config.scale);
    }

    public int getHudHeight() {
        return (int)(TOTAL_HEIGHT * config.scale);
    }

    // 向其他组件暴露原始尺寸常量
    public static int getClockWidth() {
        return CLOCK_WIDTH;
    }

    public static int getClockHeight() {
        return CLOCK_HEIGHT;
    }

    public static int getInfoWidth() {
        return INFO_WIDTH;
    }

    public static int getInfoHeight() {
        return INFO_HEIGHT;
    }

    public static int getCounterWidth() {
        return COUNTER_WIDTH;
    }

    public static int getCounterHeight() {
        return COUNTER_HEIGHT;
    }

    public static int getTotalWidth() {
        return TOTAL_WIDTH;
    }

    public static int getTotalHeight() {
        return TOTAL_HEIGHT;
    }
}