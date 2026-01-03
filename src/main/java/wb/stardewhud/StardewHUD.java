package wb.stardewhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wb.stardewhud.config.ModConfig;
import wb.stardewhud.hud.HudRenderer;

public class StardewHUD implements ClientModInitializer {
    public static final String MOD_ID = "stardewhud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static HudRenderer hudRenderer;
    private static ModConfig config;

    @Override
    public void onInitializeClient() {
        LOGGER.info("StardewHUD 正在初始化...");

        if (isModLoaded("modmenu")) {
            LOGGER.info("检测到 ModMenu，配置界面将可用");
        } else {
            LOGGER.warn("未检测到 ModMenu，配置界面将不可用");
        }

        if (isModLoaded("cloth-config")) {
            LOGGER.info("检测到 Cloth Config，高级配置界面将可用");
        } else {
            LOGGER.warn("未检测到 Cloth Config，配置界面将不可用");
        }

        // 初始化配置
        config = new ModConfig();
        config.load();

        // 初始化HUD渲染器
        hudRenderer = new HudRenderer(config);

        // 注册HUD渲染事件
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            float tickDelta = tickCounter.getTickDelta(false);
            // 渲染实际的HUD
            if (hudRenderer.shouldRender()) {
                hudRenderer.render(drawContext, tickDelta);
            }
        });

        // 注册客户端tick事件用于更新数据
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            hudRenderer.update();
        });

        LOGGER.info("StardewHUD 初始化完成！");
    }

    public static HudRenderer getHudRenderer() {
        return hudRenderer;
    }

    public static ModConfig getConfig() {
        if (config == null) {
            LOGGER.warn("配置还未初始化，正在创建默认配置...");
            config = new ModConfig();
            config.load();
        }
        return config;
    }

    // 检查其他模组是否存在的工具方法
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}