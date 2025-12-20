package wb.stardewhud;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import wb.stardewhud.config.ClothConfigScreenFactory;
import wb.stardewhud.config.ConfigManager;
import wb.stardewhud.hud.HudRenderer;
import wb.stardewhud.network.NetworkManager;

public class StardewHUD implements ClientModInitializer, ModMenuApi {
    public static final String MOD_ID = "stardewhud";

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ClothConfigScreenFactory::create;
    }

    @Override
    public void onInitializeClient() {
        ConfigManager.init();      // 读取或创建配置
        NetworkManager.initClient(); // 注册网络包
        HudRenderer.init();        // 注册 HUD 渲染事件
    }
}