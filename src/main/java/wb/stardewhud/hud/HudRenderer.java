package wb.stardewhud.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import wb.stardewhud.config.ConfigManager;
import wb.stardewhud.config.ModConfig;
import wb.stardewhud.util.DateTimeHelper;

import java.util.List;

public class HudRenderer {
    private static long day = 0;
    private static int timeOfDay = 0;

    public static void init() {
        HudRenderCallback.EVENT.register(HudRenderer::render);
    }

    public static void setDayTime(long d, int t) {
        day = d;
        timeOfDay = t;
    }

    private static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options.hudHidden || mc.currentScreen != null) return;
        if (!ConfigManager.get().enabled) return;

        MatrixStack matrices = ctx.getMatrices();
        matrices.push();
        matrices.scale(ConfigManager.get().scale, ConfigManager.get().scale, 1);

        int x = ConfigManager.get().posX;
        int y = ConfigManager.get().posY;
        if (x == -1) x = mc.getWindow().getScaledWidth() - 120;

        /* ========== 计数器列表安全 ========== */
        List<ModConfig.Counter> counters = ConfigManager.get().counters;
        if (counters == null || counters.isEmpty()) {
            matrices.pop();
            return;                 // 彻底没数据就不画
        }

        int bg = ConfigManager.get().bgColor;
        ctx.fill(x, y, x + 115, y + 50 + counters.size() * 12, bg);

        int tc = ConfigManager.get().textColor;
        /* 日期 */
        ctx.drawText(mc.textRenderer,
                "Day " + day + " (" + DateTimeHelper.getWeekDay(day) + ")",
                x + 5, y + 5, tc, false);
        /* 时间 */
        ctx.drawText(mc.textRenderer,
                DateTimeHelper.getHourMin(timeOfDay),
                x + 5, y + 17, tc, false);

        /* 计数器（带空元素保护） */
        int yy = y + 32;
        for (ModConfig.Counter c : counters) {
            if (c == null || c.name == null) continue;   // 跳过空位
            ctx.drawText(mc.textRenderer,
                    c.name + ": " + c.value,
                    x + 5, yy, c.color, false);
            yy += 12;
        }

        matrices.pop();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}