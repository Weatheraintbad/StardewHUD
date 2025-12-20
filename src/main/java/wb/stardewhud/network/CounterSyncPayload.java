package wb.stardewhud.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import wb.stardewhud.StardewHUD;
import wb.stardewhud.config.ModConfig;

public class CounterSyncPayload {
    public static final Identifier ID = new Identifier(StardewHUD.MOD_ID, "counter");

    public final String id;
    public final int value;

    public CounterSyncPayload(String id, int value) {
        this.id = id;
        this.value = value;
    }

    public static void write(PacketByteBuf buf, CounterSyncPayload pkt) {
        buf.writeString(pkt.id);
        buf.writeVarInt(pkt.value);
    }
    public static CounterSyncPayload read(PacketByteBuf buf) {
        return new CounterSyncPayload(buf.readString(), buf.readVarInt());
    }
}