package wb.stardewhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("StardewHUD/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "stardewhud.json"
    );

    // 配置项
    public boolean enabled = true;
    public HudPosition position = new HudPosition(0, 0); // 默认0,0表示自动定位（右上角）
    public float scale = 1.0f;
    public float backgroundAlpha = 1.0f; // 背景不透明度
    public String counterItemId = "minecraft:diamond";

    // 组件可见性
    public boolean showClock = true;
    public boolean showTimeDisplay = true;
    public boolean showWeather = true;
    public boolean showFortune = true;
    public boolean showItemCounter = true;

    // 原版效果控制
    public boolean hideVanillaEffects = false; // 是否隐藏原版效果图标

    // HUD位置类
    public static class HudPosition {
        public int x; // 从屏幕右侧计算的X坐标
        public int y; // 从屏幕顶部计算的Y坐标

        public HudPosition() {
            this(0, 0);
        }

        public HudPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // 加载配置
    public void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                this.enabled = loaded.enabled;
                this.position = loaded.position != null ? loaded.position : new HudPosition();
                this.scale = Math.max(0.1f, Math.min(loaded.scale, 5.0f));
                this.backgroundAlpha = Math.max(0.0f, Math.min(loaded.backgroundAlpha, 1.0f));
                this.counterItemId = loaded.counterItemId != null ? loaded.counterItemId : "minecraft:diamond";

                // 加载组件可见性
                this.showClock = loaded.showClock;
                this.showTimeDisplay = loaded.showTimeDisplay;
                this.showWeather = loaded.showWeather;
                this.showFortune = loaded.showFortune;
                this.showItemCounter = loaded.showItemCounter;

                // 加载原版效果控制
                this.hideVanillaEffects = loaded.hideVanillaEffects;

                LOGGER.info("配置已加载");
            } catch (IOException e) {
                LOGGER.error("加载配置时出错: ", e);
            }
        } else {
            save();
        }
    }

    // 保存配置
    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
            LOGGER.info("配置已保存");
        } catch (IOException e) {
            LOGGER.error("保存配置时出错: ", e);
        }
    }
}