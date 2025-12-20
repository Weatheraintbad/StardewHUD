package wb.stardewhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import wb.stardewhud.network.NetworkManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Path FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("stardewhud.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ModConfig config;

    public static void init() {
        if (Files.exists(FILE)) {
            try {
                config = GSON.fromJson(Files.readString(FILE), ModConfig.class);
            } catch (IOException e) {
                config = new ModConfig();
            }
        } else {
            config = new ModConfig();
            // 仅保留一个默认绿宝石计数器
            config.counters.add(new ModConfig.Counter());
            save();
        }
        // 强制只剩首项（防止用户手动改json多写）
        if (config.counters.size() > 1) {
            config.counters.subList(1, config.counters.size()).clear();
        }
    }

    public static ModConfig get() { return config; }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(config));
        } catch (IOException e) { e.printStackTrace(); }
    }

    /** 服务端专用：给唯一计数器+delta并立即同步到玩家 */
    public static void addAndSync(ServerPlayerEntity player, int delta) {
        ModConfig.Counter c = config.counters.get(0);
        c.value += delta;
        save();
        NetworkManager.sendCounterSync(player, c);
    }
}