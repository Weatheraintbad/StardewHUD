package wb.stardewhud.hud.components;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import wb.stardewhud.StardewHUD;
import wb.stardewhud.hud.HudRenderer;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FortuneComponent {
    private final HudRenderer hudRenderer;

    // 图标路径
    private static final String FORTUNE_ICON_PATH_TEMPLATE =
            StardewHUD.MOD_ID + ":textures/icons/fortune/%s.png";

    // 默认图标
    private static final Identifier DEFAULT_ICON =
            new Identifier(StardewHUD.MOD_ID, "textures/icons/fortune/default.png");

    private String positiveEffectId = null;
    private String negativeEffectId = null;
    private long lastSyncedDay = -1;
    private boolean hasSyncedData = false;

    private final Map<String, Identifier> iconCache = new HashMap<>();

    private static final Identifier EFFECTS_SYNC_PACKET =
            new Identifier("everysingleday", "daily_effects_sync");
    private static final Identifier REQUEST_EFFECTS_PACKET =
            new Identifier("stardewhud", "request_daily_effects");

    public FortuneComponent(HudRenderer hudRenderer) {
        this.hudRenderer = hudRenderer;

        setupNetworkListener();
    }

    private void setupNetworkListener() {
        if (StardewHUD.isModLoaded("everysingleday")) {
            try {
                ClientPlayNetworking.registerGlobalReceiver(EFFECTS_SYNC_PACKET,
                        (client, handler, buf, responseSender) -> {
                            NbtCompound nbt = buf.readNbt();
                            if (nbt != null) {
                                client.execute(() -> {
                                    this.onDailyEffectsReceived(nbt);
                                });
                            }
                        });

                StardewHUD.LOGGER.info("已注册EverySingleDay数据同步监听器");
            } catch (Exception e) {
                StardewHUD.LOGGER.warn("无法注册EverySingleDay网络监听: {}", e.getMessage());
            }
        }
    }

    private void onDailyEffectsReceived(NbtCompound nbt) {
        positiveEffectId = nbt.getString("positive_effect");
        if (positiveEffectId.isEmpty()) positiveEffectId = null;

        negativeEffectId = nbt.getString("negative_effect");
        if (negativeEffectId.isEmpty()) negativeEffectId = null;

        lastSyncedDay = nbt.getLong("last_day");
        hasSyncedData = true;

        StardewHUD.LOGGER.debug("收到每日效果: 正面={}, 负面={}",
                positiveEffectId, negativeEffectId);

        preloadEffectIcons();
    }

    public void render(DrawContext context, int x, int y) {
        // 如果没有同步数据，显示默认图标
        if (!hasSyncedData) {
            context.drawTexture(DEFAULT_ICON, x, y, 0, 0, 32, 32, 32, 32);
            return;
        }

        // 渲染效果图标
        int iconSize = 14;
        int spacing = 3;

        if (positiveEffectId != null) {
            Identifier icon = getEffectIcon(positiveEffectId);
            context.drawTexture(icon, x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }

        if (negativeEffectId != null) {
            Identifier icon = getEffectIcon(negativeEffectId);
            context.drawTexture(icon, x + iconSize + spacing, y, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }
    }

    private Identifier getEffectIcon(String effectId) {
        if (effectId == null || effectId.isEmpty()) {
            return DEFAULT_ICON;
        }

        if (iconCache.containsKey(effectId)) {
            return iconCache.get(effectId);
        }

        Identifier icon = loadEffectIcon(effectId);
        iconCache.put(effectId, icon);
        return icon;
    }

    private Identifier loadEffectIcon(String effectId) {
        String iconPath = String.format(FORTUNE_ICON_PATH_TEMPLATE, effectId);
        return new Identifier(iconPath);
    }

    public void update() {
        MinecraftClient client = hudRenderer.getClient();
        ClientPlayerEntity player = client.player;

        if (player == null || client.world == null) {
            return;
        }

        // 检查是否需要请求数据
        long currentDay = client.world.getTime() / 24000L;
        if (!hasSyncedData || currentDay != lastSyncedDay) {
            // 尝试从EverySingleDay获取数据
            if (StardewHUD.isModLoaded("everysingleday")) {
                requestDailyEffectsData();
            }
        }
    }

    private void requestDailyEffectsData() {
        // 检查是否连接到服务器并且网络连接可用
        if (!ClientPlayNetworking.canSend(REQUEST_EFFECTS_PACKET)) {
            return;
        }

        // 发送请求到服务器
        PacketByteBuf buf = PacketByteBufs.create();
        ClientPlayNetworking.send(REQUEST_EFFECTS_PACKET, buf);

        StardewHUD.LOGGER.debug("已发送每日效果数据请求");
    }

    private void preloadEffectIcons() {
        if (positiveEffectId != null && !iconCache.containsKey(positiveEffectId)) {
            iconCache.put(positiveEffectId, loadEffectIcon(positiveEffectId));
        }
        if (negativeEffectId != null && !iconCache.containsKey(negativeEffectId)) {
            iconCache.put(negativeEffectId, loadEffectIcon(negativeEffectId));
        }
    }

    // 备用方案：反射调用API
    private void tryReflectAPI() {
        if (!StardewHUD.isModLoaded("everysingleday")) {
            return;
        }

        try {
            // 尝试加载EverySingleDay类
            Class<?> everySingleDayClass = Class.forName(
                    "weatheraintbad.everysingleday.EverySingleDay"
            );

            // 尝试找到API静态类
            for (Class<?> innerClass : everySingleDayClass.getDeclaredClasses()) {
                if (innerClass.getSimpleName().equals("API")) {
                    // 尝试调用静态方法
                    Method getPositiveEffectMethod = innerClass.getMethod(
                            "getPositiveEffect",
                            net.minecraft.server.network.ServerPlayerEntity.class
                    );

                    // 注意：我们只有ClientPlayerEntity，所以这个方法可能不可用
                    StardewHUD.LOGGER.info("找到EverySingleDay API类");
                    break;
                }
            }

        } catch (Exception e) {
            StardewHUD.LOGGER.debug("反射调用EverySingleDay API失败: {}", e.getMessage());
        }
    }

    public void setEffectsForTesting(String positiveId, String negativeId) {
        this.positiveEffectId = positiveId;
        this.negativeEffectId = negativeId;
        this.hasSyncedData = true;
        this.lastSyncedDay = -1; // 设置为-1以便下次更新时重新请求

        if (positiveId != null) {
            getEffectIcon(positiveId);
        }
        if (negativeId != null) {
            getEffectIcon(negativeId);
        }

        StardewHUD.LOGGER.info("手动设置测试效果: 正面={}, 负面={}", positiveId, negativeId);
    }

    public void reset() {
        positiveEffectId = null;
        negativeEffectId = null;
        lastSyncedDay = -1;
        hasSyncedData = false;
        iconCache.clear();

        StardewHUD.LOGGER.debug("已重置FortuneComponent数据");
    }

    public boolean isValid() {
        return hasSyncedData && (positiveEffectId != null || negativeEffectId != null);
    }

    public String getPositiveEffectId() {
        return positiveEffectId;
    }

    public String getNegativeEffectId() {
        return negativeEffectId;
    }

    public boolean hasEffectsData() {
        return hasSyncedData;
    }

    public long getLastSyncedDay() {
        return lastSyncedDay;
    }
}