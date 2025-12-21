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

    // HUD尺寸常量（更新后）
    private static final int CLOCK_WIDTH = 40;     // 时钟背景宽度
    private static final int CLOCK_HEIGHT = 65;    // 时钟背景高度
    private static final int INFO_WIDTH = 80;      // 信息框宽度
    private static final int INFO_HEIGHT = 65;     // 信息框高度
    private static final int COUNTER_WIDTH = 100;  // 计数器框宽度
    private static final int COUNTER_HEIGHT = 32;  // 计数器框高度
    private static final int COUNTER_TOP_MARGIN = -2; // 计数器顶部与信息框的间距

    // 总宽度和高度（更新后）
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

        // 计算屏幕右上角位置
        int screenWidth = client.getWindow().getScaledWidth();
        int margin = 10; // 边距

        // 使用配置的位置，但如果位置为默认值(0,0)，则自动计算右上角位置
        int x, y;
        if (config.position.x == 0 && config.position.y == 0) {
            x = screenWidth - (int)(TOTAL_WIDTH * config.scale) - margin;
            y = margin;
        } else {
            x = config.position.x;
            y = config.position.y;
        }

        // 保存当前变换状态
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(config.scale, config.scale, 1.0f);

        try {
            // 1. 渲染计数器
            // 计数器靠右侧：计数器宽度100，总宽度118，所以计数器左偏移=18
            int counterX = CLOCK_WIDTH + INFO_WIDTH - COUNTER_WIDTH;
            int counterY = INFO_HEIGHT + COUNTER_TOP_MARGIN; // 使用新的间距
            itemCounter.render(context, counterX, counterY);

            // 2. 渲染时钟组件（38x60）
            clock.render(context, 0, 0, tickDelta);

            // === 天气和运势图标位置（如果需要调整也可以在这里改）===
            int iconY = 26;
            int iconSpacing = 24;
            int firstIconX = CLOCK_WIDTH + 15;
            weather.render(context, firstIconX, iconY);
            fortune.render(context, firstIconX + iconSpacing, iconY);

            // 3. 渲染信息框背景（80x60）- 现在在计数器之后渲染，会盖住计数器的一部分
            renderInfoBackground(context, CLOCK_WIDTH, 0);
            int gameInfoTextWidth = timeDisplay.getGameInfoTextWidth(context);
            int gameInfoX = CLOCK_WIDTH + (INFO_WIDTH - gameInfoTextWidth) / 2;

            // === 第一层日期显示水平居中 ===
            timeDisplay.renderGameInfo(context, CLOCK_WIDTH, INFO_WIDTH, 10); // 传入信息框参数用于居中计算

            // === 第三层时间显示水平居中 ===
            timeDisplay.renderTime(context, CLOCK_WIDTH - 4, INFO_WIDTH, 48); // 传入信息框参数用于居中计算

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
            clock.update();
            timeDisplay.update(world);
            weather.update(world);
            fortune.update();
            itemCounter.update();
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

    // 获取HUD尺寸用于自动定位
    public int getHudWidth() {
        return (int)(TOTAL_WIDTH * config.scale);
    }

    public int getHudHeight() {
        return (int)(TOTAL_HEIGHT * config.scale);
    }

    // 向其他组件暴露尺寸常量
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
}