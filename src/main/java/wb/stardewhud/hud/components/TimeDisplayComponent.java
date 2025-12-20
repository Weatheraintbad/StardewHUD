package wb.stardewhud.hud.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import wb.stardewhud.StardewHUD;
import wb.stardewhud.hud.HudRenderer;

public class TimeDisplayComponent {
    private final HudRenderer hudRenderer;

    // 星期名称
    private static final String[] WEEKDAYS = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};

    // 当前游戏数据
    private int currentDay = 1;
    private String currentWeekday = "星期一";
    private String currentTime = "00:00";

    // 用于检测天数变化
    private long lastWorldTime = -1;

    public TimeDisplayComponent(HudRenderer hudRenderer) {
        this.hudRenderer = hudRenderer;
    }

    public void renderGameInfo(DrawContext context, int x, int y) {
        // 渲染游戏日和星期（第1层）
        String gameInfo = String.format("%d日 %s", currentDay, currentWeekday);
        drawTextWithShadow(context, gameInfo, x, y);
    }

    public void renderTime(DrawContext context, int x, int y) {
        // 渲染游戏内时间（第3层）
        drawTextWithShadow(context, currentTime, x, y);
    }

    private void drawTextWithShadow(DrawContext context, String text, int x, int y) {
        MinecraftClient client = hudRenderer.getClient();
        context.drawTextWithShadow(client.textRenderer, text, x, y, 0xFFFFFF);
    }

    public void update(World world) {
        if (world == null) return;

        // 获取当前游戏时间
        long timeOfDay = world.getTimeOfDay();

        // 更新当前时间显示
        currentTime = formatGameTime(timeOfDay);

        // 使用timeOfDay的变化检测天数
        updateDayFromTimeChange(timeOfDay);
    }

    private void updateDayFromTimeChange(long timeOfDay) {
        if (lastWorldTime == -1) {
            lastWorldTime = timeOfDay;
            return;
        }

        // 计算时间差（考虑回滚）
        long timeDiff;
        if (timeOfDay < lastWorldTime) {
            // 时间回滚，可能是新的一天
            timeDiff = timeOfDay + (24000 - lastWorldTime);
        } else {
            timeDiff = timeOfDay - lastWorldTime;
        }

        // 如果时间差过大（超过2000刻），服务器跳时或者新的一天
        if (timeDiff > 2000) {
            // 可能是新的一天开始，或者是时间被修改
            currentDay++;
            updateWeekday();
        }
        // 如果检测到正常的时间回滚（从接近24000到接近0）
        else if (lastWorldTime > 23000 && timeOfDay < 1000) {
            // 正常的新的一天
            currentDay++;
            updateWeekday();
        }

        lastWorldTime = timeOfDay;
    }

    private void updateWeekday() {
        // 计算星期
        int weekdayIndex = (currentDay - 1) % 7;
        currentWeekday = WEEKDAYS[weekdayIndex];
    }

    private String formatGameTime(long timeOfDay) {
        // 将游戏刻转换为24小时制时间
        // Minecraft时间系统：
        // 0刻 = 6:00 (日出)
        // 6000刻 = 12:00 (正午)
        // 12000刻 = 18:00 (日落)
        // 18000刻 = 0:00 (午夜)

        // 计算小时 (0-23)
        long hour = (timeOfDay / 1000L + 6L) % 24L;

        // 计算分钟 (0-59)
        long minute = (timeOfDay % 1000L) * 60L / 1000L;

        return String.format("%02d:%02d", hour, minute);
    }

    // 强制设置游戏日（用于测试）
    public void setCurrentDay(int day) {
        this.currentDay = day;
        updateWeekday();
    }

    // 获取当前游戏日
    public int getCurrentDay() {
        return currentDay;
    }

    // 获取当前星期
    public String getCurrentWeekday() {
        return currentWeekday;
    }
}