package wb.stardewhud.hud.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import wb.stardewhud.StardewHUD;
import wb.stardewhud.hud.HudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;

public class ClockComponent {
    private final HudRenderer hudRenderer;
    private float currentAngle = 0.0f;

    // 时钟尺寸常量
    private static final int CLOCK_WIDTH = 40;
    private static final int CLOCK_HEIGHT = 65;
    private static final int CENTER_X = CLOCK_WIDTH; // 圆心在右边框中心
    private static final int CENTER_Y = CLOCK_HEIGHT / 2; // 圆心Y坐标（30）

    // 指针纹理尺寸
    private static final int HAND_LENGTH = 29; // 指针长度
    private static final int HAND_HEIGHT = 11; // 指针高度（厚度）

    // 纹理标识符 - 使用Identifier.of()方法
    private static final Identifier CLOCK_HAND = Identifier.of(StardewHUD.MOD_ID, "textures/gui/clock_hand.png");

    public ClockComponent(HudRenderer hudRenderer) {
        this.hudRenderer = hudRenderer;
    }

    public void render(DrawContext context, int x, int y, float tickDelta) {
        // 渲染时钟背景（35x60半圆）
        context.setShaderColor(1.0f, 1.0f, 1.0f, hudRenderer.getConfig().backgroundAlpha);
        context.drawTexture(HudRenderer.CLOCK_BG, x, y, 0, 0, CLOCK_WIDTH, CLOCK_HEIGHT, CLOCK_WIDTH, CLOCK_HEIGHT);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // 渲染时钟指针
        renderClockHand(context, x + CENTER_X, y + CENTER_Y, currentAngle);
    }

    private void renderClockHand(DrawContext context, int centerX, int centerY, float angle) {
        // 保存变换状态
        context.getMatrices().push();

        // 将原点移动到圆心，再向左偏一点
        context.getMatrices().translate(centerX - 2, centerY, 0);

        // 旋转到指定角度
        context.getMatrices().multiply(
                net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(angle)
        );

        int drawX = -HAND_LENGTH;
        int drawY = -HAND_HEIGHT / 2;

        context.drawTexture(CLOCK_HAND, drawX, drawY, 0, 0, HAND_LENGTH, HAND_HEIGHT, HAND_LENGTH, HAND_HEIGHT);

        context.getMatrices().pop();
    }

    public void update() {
        MinecraftClient client = hudRenderer.getClient();
        World world = client.world;

        if (world != null) {
            long timeOfDay = world.getTimeOfDay() % 24000;
            currentAngle = calculateClockAngle(timeOfDay);
        }
    }

    private float calculateClockAngle(long timeOfDay) {
        // 时间映射：左半圆内逆时针旋转
        // 18:00（12000刻）→ 90°（顶部）
        // 24:00（18000刻）→ 135°（左上）
        // 6:00（0刻）→ 180°（左侧）
        // 12:00（6000刻）→ 225°（左下）
        // 18:00（12000刻）→ 270°（底部，跳回90°）

        long offsetFrom1800 = (timeOfDay + 12000) % 24000;
        float progress = offsetFrom1800 / 24000.0f;
        float angle = 90.0f - progress * 180.0f;

        return angle;
    }
}