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

    private static final int CLOCK_WIDTH = 35;
    private static final int CLOCK_HEIGHT = 60;
    private static final int CENTER_X = CLOCK_WIDTH;
    private static final int CENTER_Y = CLOCK_HEIGHT / 2;

    private static final Identifier CLOCK_HAND = new Identifier(StardewHUD.MOD_ID, "textures/gui/clock_hand.png");

    public ClockComponent(HudRenderer hudRenderer) {
        this.hudRenderer = hudRenderer;
    }

    public void render(DrawContext context, int x, int y, float tickDelta) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, hudRenderer.getConfig().backgroundAlpha);
        context.drawTexture(HudRenderer.CLOCK_BG, x, y, 0, 0, CLOCK_WIDTH, CLOCK_HEIGHT, CLOCK_WIDTH, CLOCK_HEIGHT);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        renderClockHand(context, x + CENTER_X, y + CENTER_Y, currentAngle);
    }

    private void renderClockHand(DrawContext context, int centerX, int centerY, float angle) {
        context.getMatrices().push();
        context.getMatrices().translate(centerX, centerY, 0);
        context.getMatrices().multiply(
                net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(angle)
        );

        int handLength = 25;
        context.drawTexture(CLOCK_HAND, -handLength, -1, 0, 0, handLength, 3, handLength, 3);

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

        long offsetFrom1800 = (timeOfDay + 12000) % 24000;
        float progress = offsetFrom1800 / 24000.0f;

        float angle = 90.0f - progress * 180.0f;

        return angle;
    }
}