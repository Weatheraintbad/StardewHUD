package wb.stardewhud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wb.stardewhud.StardewHUD;
import wb.stardewhud.config.ModConfig;

@Mixin(InGameHud.class)
public abstract class EffectHudMixin {

    @Inject(method = "renderStatusEffectOverlay",
            at = @At("HEAD"),
            cancellable = true)
    private void onRenderStatusEffectOverlay(CallbackInfo ci) {
        ModConfig config = StardewHUD.getConfig();

        // 如果HUD启用且隐藏效果选项开启，并且HUD在效果区域，则取消渲染
        if (config.enabled && config.hideVanillaEffects) {
            if (shouldHideVanillaEffects(config)) {
                ci.cancel();
            }
        }
    }

    private boolean shouldHideVanillaEffects(ModConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();

        // 计算HUD位置
        int hudX, hudY, hudWidth, hudHeight;

        var hudRenderer = StardewHUD.getHudRenderer();
        if (hudRenderer == null) return false;

        hudWidth = hudRenderer.getHudWidth();
        hudHeight = hudRenderer.getHudHeight();

        var pos = config.position;
        if (pos.x == 0 && pos.y == 0) {
            // 自动定位到右上角
            int margin = 10;
            hudX = screenWidth - hudWidth - margin;
            hudY = margin;
        } else {
            // 从右侧计算
            hudX = screenWidth - pos.x;
            hudY = pos.y;
        }

        // 原版效果区域（右上角，大约100x100像素）
        int effectAreaLeft = screenWidth - 100;
        int effectAreaTop = 0;
        int effectAreaRight = screenWidth;
        int effectAreaBottom = 100;

        // 检查是否重叠
        boolean isOverlapping =
                hudX < effectAreaRight &&
                        hudX + hudWidth > effectAreaLeft &&
                        hudY < effectAreaBottom &&
                        hudY + hudHeight > effectAreaTop;

        return isOverlapping;
    }
}