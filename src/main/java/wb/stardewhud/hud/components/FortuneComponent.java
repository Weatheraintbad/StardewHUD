package wb.stardewhud.hud.components;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import wb.stardewhud.StardewHUD;
import wb.stardewhud.hud.HudRenderer;

import java.util.HashMap;
import java.util.Map;

public class FortuneComponent {
    private final HudRenderer hudRenderer;

    // 图标路径模板
    private static final String FORTUNE_ICON_PATH_TEMPLATE =
            StardewHUD.MOD_ID + ":textures/icons/fortune/%s.png";

    // 默认图标
    private static final Identifier DEFAULT_ICON =
            Identifier.of(StardewHUD.MOD_ID, "textures/icons/fortune/default.png");

    private String positiveEffectId = null;
    private String negativeEffectId = null;
    private long lastSyncedDay = -1;
    private boolean hasSyncedData = false;

    private final Map<String, Identifier> iconCache = new HashMap<>();

    // 1.21.1 新的网络系统 - 需要创建Payload类型
    private static final Identifier EFFECTS_SYNC_PACKET_ID =
            Identifier.of("everysingleday", "daily_effects_sync");
    private static final Identifier REQUEST_EFFECTS_PACKET_ID =
            Identifier.of("stardewhud", "request_daily_effects");

    // 创建一个简单的自定义Payload
    public record EffectsSyncPayload(NbtCompound data) implements CustomPayload {
        public static final CustomPayload.Id<EffectsSyncPayload> ID =
                new CustomPayload.Id<>(EFFECTS_SYNC_PACKET_ID);

        public static final PacketCodec<PacketByteBuf, EffectsSyncPayload> CODEC =
                PacketCodec.ofStatic(
                        (buf, payload) -> buf.writeNbt(payload.data),
                        buf -> new EffectsSyncPayload(buf.readNbt())
                );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record RequestEffectsPayload() implements CustomPayload {
        public static final CustomPayload.Id<RequestEffectsPayload> ID =
                new CustomPayload.Id<>(REQUEST_EFFECTS_PACKET_ID);

        public static final PacketCodec<PacketByteBuf, RequestEffectsPayload> CODEC =
                PacketCodec.unit(new RequestEffectsPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public FortuneComponent(HudRenderer hudRenderer) {
        this.hudRenderer = hudRenderer;
        setupNetworkListener();
    }

    private void setupNetworkListener() {
        if (StardewHUD.isModLoaded("everysingleday")) {
            try {
                // 1.21.1 使用新的Payload系统
                ClientPlayNetworking.registerGlobalReceiver(EffectsSyncPayload.ID,
                        (payload, context) -> {
                            NbtCompound nbt = payload.data();
                            if (nbt != null) {
                                context.client().execute(() -> this.onDailyEffectsReceived(nbt));
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

        StardewHUD.LOGGER.debug("收到每日效果: 正面={}, 负面={}", positiveEffectId, negativeEffectId);
        preloadEffectIcons();
    }

    public void render(DrawContext context, int x, int y) {
        // 只渲染效果图标，不处理季节
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

    public void update() {
        MinecraftClient client = hudRenderer.getClient();
        if (client == null || client.player == null || client.world == null) {
            return;
        }

        // 处理数据同步
        long currentDay = client.world.getTime() / 24000L;
        if (!hasSyncedData || currentDay != lastSyncedDay) {
            if (StardewHUD.isModLoaded("everysingleday")) {
                requestDailyEffectsData();
            }
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
        return Identifier.of(iconPath);
    }

    private void requestDailyEffectsData() {
        if (!ClientPlayNetworking.canSend(RequestEffectsPayload.ID)) {
            return;
        }

        // 1.21.1 发送自定义Payload
        ClientPlayNetworking.send(new RequestEffectsPayload());
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

    public void setEffectsForTesting(String positiveId, String negativeId) {
        this.positiveEffectId = positiveId;
        this.negativeEffectId = negativeId;
        this.hasSyncedData = true;
        this.lastSyncedDay = -1;

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