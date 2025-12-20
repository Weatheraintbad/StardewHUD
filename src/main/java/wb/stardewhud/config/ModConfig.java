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
    public HudPosition position = new HudPosition(0, 0); // 默认0,0表示自动定位
    public float scale = 1.0f;
    public float backgroundAlpha = 1.0f;// 背景不透明度
    public String counterItemId = "minecraft:diamond";

    // HUD位置类
    public static class HudPosition {
        public int x;
        public int y;

        public HudPosition() {
            this(20, 20);
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
                this.scale = loaded.scale;
                this.backgroundAlpha = loaded.backgroundAlpha;
                this.counterItemId = loaded.counterItemId != null ? loaded.counterItemId : "minecraft:diamond";
                LOGGER.info("配置已加载");
            } catch (IOException e) {
                LOGGER.error("加载配置时出错: ", e);
            }
        } else {
            // 文件不存在，保存默认配置
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