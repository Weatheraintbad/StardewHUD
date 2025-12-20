package wb.stardewhud.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import wb.stardewhud.StardewHUD;

public class DayTimeSyncPayload {
    public static final Identifier ID = new Identifier(StardewHUD.MOD_ID, "day_time");

    public final long day;
    public final int timeOfDay;

    public DayTimeSyncPayload(long day, int timeOfDay) {
        this.day = day;
        this.timeOfDay = timeOfDay;
    }

    /* 编码 */
    public static void write(PacketByteBuf buf, DayTimeSyncPayload pkt) {
        buf.writeLong(pkt.day);
        buf.writeInt(pkt.timeOfDay);
    }

    /* 解码 */
    public static DayTimeSyncPayload read(PacketByteBuf buf) {
        return new DayTimeSyncPayload(buf.readLong(), buf.readInt());
    }
}