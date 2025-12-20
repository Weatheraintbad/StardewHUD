package wb.stardewhud.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import wb.stardewhud.config.ConfigManager;
import wb.stardewhud.config.ModConfig;
import wb.stardewhud.hud.HudRenderer;

public class NetworkManager {

    /* ==================== 客户端接收 ==================== */
    public static void initClient() {
        /* 日期时间 */
        ClientPlayNetworking.registerGlobalReceiver(DayTimeSyncPayload.ID,
                (client, handler, buf, responseSender) -> {
                    DayTimeSyncPayload pkt = DayTimeSyncPayload.read(buf);
                    client.execute(() -> HudRenderer.setDayTime(pkt.day, pkt.timeOfDay));
                });

        /* 计数器单项更新 */
        ClientPlayNetworking.registerGlobalReceiver(CounterSyncPayload.ID,
                (client, handler, buf, responseSender) -> {
                    CounterSyncPayload pkt = CounterSyncPayload.read(buf);
                    client.execute(() -> {
                        for (ModConfig.Counter c : ConfigManager.get().counters) {
                            if (c.id.equals(pkt.id)) {
                                c.value = pkt.value;
                                break;
                            }
                        }
                    });
                });
    }

    /* ==================== 服务端发送 ==================== */
    /** 发送日期时间 */
    public static void sendToPlayer(ServerPlayerEntity player) {
        long day = player.getWorld().getTimeOfDay() / 24000L;
        int time = (int) (player.getWorld().getTimeOfDay() % 24000);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        DayTimeSyncPayload.write(buf, new DayTimeSyncPayload(day, time));
        ServerPlayNetworking.send(player, DayTimeSyncPayload.ID, buf);
    }

    /** 发送单个计数器更新 */
    public static void sendCounterSync(ServerPlayerEntity player, ModConfig.Counter counter) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        CounterSyncPayload.write(buf, new CounterSyncPayload(counter.id, counter.value));
        ServerPlayNetworking.send(player, CounterSyncPayload.ID, buf);
    }
}