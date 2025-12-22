package wb.stardewhud.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import wb.stardewhud.StardewHUD;
import wb.stardewhud.config.ModConfig;

public class EffectHudManager {

    public static boolean shouldShowVanillaEffects() {
        ModConfig config = StardewHUD.getConfig();
        if (!config.enabled) return true; // 如果我们的HUD禁用，显示原版效果

        // 检查我们的HUD是否与效果图标区域重叠
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();

        int hudX, hudY, hudWidth, hudHeight;

        // 获取我们的HUD位置和尺寸
        HudRenderer hudRenderer = StardewHUD.getHudRenderer();
        hudWidth = hudRenderer.getHudWidth();
        hudHeight = hudRenderer.getHudHeight();

        if (config.position.x == 0 && config.position.y == 0) {
            // 自动定位到右上角
            int margin = 10;
            hudX = screenWidth - hudWidth - margin;
            hudY = margin;
        } else {
            // 从右侧计算
            hudX = screenWidth - config.position.x;
            hudY = config.position.y;
        }

        // 效果图标区域：右上角，大约 100x100 像素
        int effectAreaX = screenWidth - 100;
        int effectAreaY = 0;
        int effectAreaWidth = 100;
        int effectAreaHeight = 100;

        // 检查是否重叠
        boolean isOverlapping =
                hudX < effectAreaX + effectAreaWidth &&
                        hudX + hudWidth > effectAreaX &&
                        hudY < effectAreaY + effectAreaHeight &&
                        hudY + hudHeight > effectAreaY;

        return !isOverlapping;
    }

    public static int getModifiedEffectY() {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenHeight = client.getWindow().getScaledHeight();

        // 将效果图标移动到右下角（从底部向上60像素，留出经验条和饥饿条的空间）
        return screenHeight - 60;
    }
}