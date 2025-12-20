package wb.stardewhud.util;

public class DateTimeHelper {
    private static final String[] WEEK = {
            "Mon","Tue","Wed","Thu","Fri","Sat","Sun"
    };
    public static String getWeekDay(long day) {
        return WEEK[(int)(day % 7)];
    }
    public static String getHourMin(int timeOfDay) { // 0-23999
        int hour = timeOfDay / 1000;          // 0-23
        int min  = (int)((timeOfDay % 1000) / 1000f * 60);
        return String.format("%02d:%02d", hour, min);
    }
}