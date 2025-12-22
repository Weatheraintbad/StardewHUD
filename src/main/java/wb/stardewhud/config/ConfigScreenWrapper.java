package wb.stardewhud.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import wb.stardewhud.StardewHUD;

public class ConfigScreenWrapper {

    public static Screen createConfigScreen(Screen parent) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();

        ModConfig config = StardewHUD.getConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.stardewhud.config"))
                .setSavingRunnable(config::save)
                .setTransparentBackground(true)
                .setDoesConfirmSave(true);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // 基础设置分类
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.stardewhud.general"));

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("option.stardewhud.enabled"),
                        config.enabled)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.stardewhud.enabled"))
                .setSaveConsumer(newValue -> config.enabled = newValue)
                .build());

        general.addEntry(entryBuilder.startStrField(
                        Text.translatable("option.stardewhud.counterItemId"),
                        config.counterItemId)
                .setDefaultValue("minecraft:diamond")
                .setTooltip(Text.translatable("tooltip.stardewhud.counterItemId"))
                .setSaveConsumer(newValue -> config.counterItemId = newValue)
                .build());

        general.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.stardewhud.backgroundAlpha"),
                        config.backgroundAlpha)
                .setDefaultValue(1.0f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setTooltip(Text.translatable("tooltip.stardewhud.backgroundAlpha"))
                .setSaveConsumer(newValue -> {
                    float safeValue = Math.max(0.0f, Math.min(newValue, 1.0f));
                    config.backgroundAlpha = safeValue;
                })
                .build());

        // 位置和缩放分类
        ConfigCategory position = builder.getOrCreateCategory(Text.translatable("category.stardewhud.position"));

        // X位置 - 从屏幕右侧计算
        position.addEntry(entryBuilder.startIntField(
                        Text.translatable("option.stardewhud.positionX"),
                        config.position.x)
                .setDefaultValue(0)
                .setMin(0)
                .setMax(screenWidth)
                .setTooltip(Text.translatable("tooltip.stardewhud.positionX"))
                .setSaveConsumer(newValue -> {
                    int safeX = Math.max(0, Math.min(newValue, screenWidth));
                    config.position.x = safeX;
                })
                .build());

        // Y位置 - 从屏幕顶部计算
        position.addEntry(entryBuilder.startIntField(
                        Text.translatable("option.stardewhud.positionY"),
                        config.position.y)
                .setDefaultValue(0)
                .setMin(0)
                .setMax(1000)
                .setTooltip(Text.translatable("tooltip.stardewhud.positionY"))
                .setSaveConsumer(newValue -> {
                    int safeY = Math.max(0, Math.min(newValue, 1000));
                    config.position.y = safeY;
                })
                .build());

        // 缩放 - 百分比输入
        int scalePercent = (int)(config.scale * 100);
        position.addEntry(entryBuilder.startIntField(
                        Text.translatable("option.stardewhud.scalePercent"),
                        scalePercent)
                .setDefaultValue(100)
                .setMin(10)
                .setMax(500)
                .setTooltip(Text.translatable("tooltip.stardewhud.scalePercent"))
                .setSaveConsumer(newValue -> {
                    int safePercent = Math.max(10, Math.min(newValue, 500));
                    config.scale = safePercent / 100.0f;
                })
                .build());

        // 组件可见性分类
        ConfigCategory visibility = builder.getOrCreateCategory(Text.translatable("category.stardewhud.visibility"));

        visibility.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("option.stardewhud.showClock"),
                        config.showClock)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.stardewhud.showClock"))
                .setSaveConsumer(newValue -> config.showClock = newValue)
                .build());

        visibility.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("option.stardewhud.showTimeDisplay"),
                        config.showTimeDisplay)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.stardewhud.showTimeDisplay"))
                .setSaveConsumer(newValue -> config.showTimeDisplay = newValue)
                .build());

        visibility.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("option.stardewhud.showWeather"),
                        config.showWeather)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.stardewhud.showWeather"))
                .setSaveConsumer(newValue -> config.showWeather = newValue)
                .build());

        visibility.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("option.stardewhud.showFortune"),
                        config.showFortune)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.stardewhud.showFortune"))
                .setSaveConsumer(newValue -> config.showFortune = newValue)
                .build());

        visibility.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("option.stardewhud.showItemCounter"),
                        config.showItemCounter)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.stardewhud.showItemCounter"))
                .setSaveConsumer(newValue -> config.showItemCounter = newValue)
                .build());

        // 原版效果控制分类
        ConfigCategory effects = builder.getOrCreateCategory(Text.translatable("category.stardewhud.effects"));

        // 添加说明文本
        effects.addEntry(entryBuilder.startTextDescription(
                        Text.translatable("tip.stardewhud.effects"))
                .build());

        effects.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("option.stardewhud.hideVanillaEffects"),
                        config.hideVanillaEffects)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("tooltip.stardewhud.hideVanillaEffects"))
                .setSaveConsumer(newValue -> config.hideVanillaEffects = newValue)
                .build());

        return builder.build();
    }
}