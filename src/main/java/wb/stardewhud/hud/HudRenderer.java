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
    public static final Identifier CONNECTOR = new Identifier(StardewHUD.MOD_ID, "textures/gui/connector.png");

    // HUD尺寸常量
    private static final int CLOCK_WIDTH = 35;     // 时钟背景宽度
    private static final int CLOCK_HEIGHT = 60;    // 时钟背景高度
    private static final int INFO_WIDTH = 80;      // 信息框宽度
    private static final int INFO_HEIGHT = 60;     // 信息框高度
    private static final int COUNTER_WIDTH = 100;  // 计数器框宽度
    private static final int COUNTER_HEIGHT = 20;  // 计数器框高度
    private static final int CONNECTOR_HEIGHT = 5; // 连接器高度

    // 总宽度和高度
    private static final int TOTAL_WIDTH = CLOCK_WIDTH + INFO_WIDTH;
    private static final int TOTAL_HEIGHT = INFO_HEIGHT + CONNECTOR_HEIGHT + COUNTER_HEIGHT;

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
            // 1. 渲染时钟组件（35x60）
            clock.render(context, 0, 0, tickDelta);

            // 2. 渲染信息框背景（80x60）
            renderInfoBackground(context, CLOCK_WIDTH, 0);

            // 3. 渲染连接器（两个，连接计数器栏的1/3和2/3位置）
            renderConnectors(context);

            // 4. 渲染时间信息（第1层）
            timeDisplay.renderGameInfo(context, CLOCK_WIDTH + 10, 5);

            // 5. 渲染天气和运势图标（第2层）
            int iconY = 28;
            int iconSpacing = 20;
            int firstIconX = CLOCK_WIDTH + 15;
            weather.render(context, firstIconX, iconY);
            fortune.render(context, firstIconX + iconSpacing, iconY);

            // 6. 渲染游戏内时间（第3层）
            timeDisplay.renderTime(context, CLOCK_WIDTH + 10, 45);

            // 7. 渲染计数器（第4层，靠右侧悬挂）
            // 计数器靠右侧：计数器宽度100，总宽度115，所以计数器左偏移=15
            int counterX = CLOCK_WIDTH + INFO_WIDTH - COUNTER_WIDTH;
            itemCounter.render(context, counterX, INFO_HEIGHT + CONNECTOR_HEIGHT);

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

    private void renderConnectors(DrawContext context) {
        // 计算计数器框的位置（靠右侧）
        int counterX = CLOCK_WIDTH + INFO_WIDTH - COUNTER_WIDTH;
        int connectorY = INFO_HEIGHT; // 连接器Y位置（信息框底部）

        // 连接器宽度
        int connectorWidth = 2;

        // 计算连接器位置（基于计数器框的1/3和2/3位置）
        // 连接器1：在计数器框左边缘向右1/3宽度处
        int connector1X = counterX + (COUNTER_WIDTH / 3) - (connectorWidth / 2);
        // 连接器2：在计数器框左边缘向右2/3宽度处
        int connector2X = counterX + (COUNTER_WIDTH * 2 / 3) - (connectorWidth / 2);

        // 渲染两个连接器
        context.setShaderColor(1.0f, 1.0f, 1.0f, config.backgroundAlpha);
        context.drawTexture(CONNECTOR, connector1X, connectorY, 0, 0, connectorWidth, CONNECTOR_HEIGHT, 2, CONNECTOR_HEIGHT);
        context.drawTexture(CONNECTOR, connector2X, connectorY, 0, 0, connectorWidth, CONNECTOR_HEIGHT, 2, CONNECTOR_HEIGHT);
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
}