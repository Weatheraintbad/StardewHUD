package wb.stardewhud.config;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    @Expose public boolean enabled = true;
    @Expose public int posX = -1, posY = 5;
    @Expose public float scale = 1f;
    @Expose public int textColor = 0xFFFFFF;
    @Expose public int bgColor = 0x90000000;
    @Expose public List<Counter> counters = new ArrayList<>(1); // 固定长度 1

    /* ========== 唯一计数器 ========== */
    public static class Counter {
        @Expose public String id = "gems";
        @Expose public String name = "Emeralds Earned";
        @Expose public String itemId = "minecraft:emerald";
        @Expose public int value = 0;
        @Expose public int color = 0x00D600;
    }
}