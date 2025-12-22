package wb.stardewhud.hud.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import wb.stardewhud.StardewHUD;
import wb.stardewhud.hud.HudRenderer;

public class ItemCounterComponent {
    private final HudRenderer hudRenderer;
    // 移除final修饰符，允许更新
    private String itemId;

    private Item item;
    private int itemCount = 0;

    // 使用HudRenderer中的常量
    private final int COUNTER_WIDTH = HudRenderer.getCounterWidth();
    private final int COUNTER_HEIGHT = HudRenderer.getCounterHeight();

    // === 边距常量 ===
    private static final int ITEM_LEFT_MARGIN = 8;      // 物品图标左侧边距
    private static final int TEXT_RIGHT_MARGIN = 7;    // 个位数最右侧与计数器栏最右侧的距离
    private static final float TEXT_SCALE = 1.5f;       // 数字字号缩放因子

    // === 字体颜色配置 ===
    private static final int TEXT_COLOR = 0xFF8B0000;   // 深红色 (DarkRed) - 修复：添加alpha通道FF

    // === 阴影颜色配置 ===
    private static final int SHADOW_COLOR = 0xFFFFFFFF; // 白色 - 修复：添加alpha通道FF

    // === 是否启用阴影 ===
    private static final boolean ENABLE_SHADOW = false;  // 禁用文字阴影

    // === 物品图标大小配置 ===
    private static final int ITEM_ICON_SIZE = 16;       // 物品图标大小（16x16像素）

    // === 物品图标垂直偏移 ===
    private static final int ITEM_VERTICAL_OFFSET = 4;  // 物品图标垂直偏移（微调位置）

    // === 新增：缩放补偿偏移 ===
    private static final int SCALE_COMPENSATION = 4;    // 缩放导致的额外偏移补偿值

    public ItemCounterComponent(HudRenderer hudRenderer, String itemId) {
        this.hudRenderer = hudRenderer;
        this.itemId = itemId; // 不再是final
        parseItemId();
    }

    public void render(DrawContext context, int x, int y) {
        // 在渲染前检查配置是否已更新
        String configItemId = StardewHUD.getConfig().counterItemId;
        if (!configItemId.equals(this.itemId)) {
            this.itemId = configItemId;
            parseItemId(); // 重新解析物品ID
        }

        // === 使用正确的透明度渲染背景 ===
        float alpha = hudRenderer.getConfig().backgroundAlpha;

        // 渲染计数器背景
        context.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        context.drawTexture(HudRenderer.COUNTER_BG, x, y, 0, 0, COUNTER_WIDTH, COUNTER_HEIGHT, COUNTER_WIDTH, COUNTER_HEIGHT);

        // === 关键修复：立即重置着色器颜色，避免影响后续渲染 ===
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // 渲染物品图标和数量
        if (item != null) {
            // === 物品图标在计数器栏最左侧 ===
            int itemX = x + ITEM_LEFT_MARGIN;
            int itemY = y + (COUNTER_HEIGHT - ITEM_ICON_SIZE) / 2 + ITEM_VERTICAL_OFFSET;

            // === 确保物品图标透明度正常 ===
            // 由于上面已经重置了着色器颜色，物品图标会正常渲染
            ItemStack stack = new ItemStack(item, 1);
            context.drawItem(stack, itemX, itemY);

            // === 统计数字在右侧（考虑缩放补偿）===
            MinecraftClient client = hudRenderer.getClient();
            String countText = String.valueOf(itemCount);

            // === 考虑缩放影响的位置计算 ===
            int textX = calculateScaledRightAlignedPosition(client, countText, x);
            int textY = y + (COUNTER_HEIGHT - 8) / 2 + 3; // 垂直居中（文字高度约8像素）

            // === 应用字号缩放、颜色和阴影 ===
            drawScaledTextWithCustomShadow(context, countText, textX, textY, TEXT_SCALE, TEXT_COLOR, SHADOW_COLOR, ENABLE_SHADOW);
        } else {
            // 物品ID无效时显示"?"
            MinecraftClient client = hudRenderer.getClient();
            String text = "?";
            int textWidth = client.textRenderer.getWidth(text);

            // 居中显示问号
            int textX = x + (COUNTER_WIDTH - textWidth) / 2;
            int textY = y + (COUNTER_HEIGHT - 8) / 2;

            // === 应用字号缩放、颜色和阴影 ===
            drawScaledTextWithCustomShadow(context, text, textX, textY, TEXT_SCALE, TEXT_COLOR, SHADOW_COLOR, ENABLE_SHADOW);
        }
    }

    // === 修复：考虑缩放影响的位置计算 ===
    private int calculateScaledRightAlignedPosition(MinecraftClient client, String text, int counterX) {
        // 1. 计算原始文字宽度
        int originalWidth = client.textRenderer.getWidth(text);

        // 2. 计算缩放后的实际显示宽度
        //    缩放后宽度 = 原始宽度 × 缩放比例
        float scaledWidth = originalWidth * TEXT_SCALE;

        // 3. 计算个位数目标位置（计数器右边界 - 右边距）
        int targetRightEdge = counterX + COUNTER_WIDTH - TEXT_RIGHT_MARGIN;

        // 4. 文字绘制起始位置 = 目标右边界 - 缩放后宽度
        //    这样缩放后的文字最右侧就会在targetRightEdge位置
        int calculatedX = (int)(targetRightEdge - scaledWidth);

        // 5. 添加额外补偿（根据测试调整）
        return calculatedX - SCALE_COMPENSATION;
    }

    private int calculateEmpiricalRightAlignedPosition(MinecraftClient client, String text, int counterX) {
        int originalWidth = client.textRenderer.getWidth(text);
        int targetRightEdge = counterX + COUNTER_WIDTH - TEXT_RIGHT_MARGIN;

        // 基于测试的经验公式
        // 对于1.5倍缩放，实际显示宽度大约是原始宽度的1.5倍
        // 但缩放中心点可能影响最终位置

        // 方案A：简单补偿
        float scaleFactor = TEXT_SCALE;
        int scaledWidth = (int)(originalWidth * scaleFactor);

        // 根据数字位数微调（可选）
        int length = text.length();
        int lengthAdjustment = 0;
        if (length == 1) lengthAdjustment = 0;
        else if (length == 2) lengthAdjustment = -2;
        else if (length == 3) lengthAdjustment = -4;

        return targetRightEdge - scaledWidth + lengthAdjustment;
    }

    // === 最简单的解决方案：如果缩放导致问题，降低缩放值 ===
    private int calculateSimpleRightAlignedPosition(MinecraftClient client, String text, int counterX) {
        // 暂时使用较小的缩放或原始大小进行位置计算
        float tempScaleForCalculation = 1.0f; // 使用1.0进行计算
        int originalWidth = client.textRenderer.getWidth(text);
        int scaledWidth = (int)(originalWidth * tempScaleForCalculation);

        int targetRightEdge = counterX + COUNTER_WIDTH - TEXT_RIGHT_MARGIN;
        return targetRightEdge - scaledWidth;
    }

    // === 专门处理缩放文本绘制的方法 ===
    private void drawScaledTextWithCustomShadow(DrawContext context, String text, int x, int y,
                                                float scale, int textColor, int shadowColor, boolean enableShadow) {
        MinecraftClient client = hudRenderer.getClient();

        // 保存当前变换状态
        context.getMatrices().push();

        // === 确保文字渲染使用完全不透明 ===
        // 重置着色器颜色，避免继承背景的透明度
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // 以左上角为原点（当前方式）
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(scale, scale, 1.0f);

        if (enableShadow) {
            // 确保阴影颜色完全不透明
            int opaqueShadowColor = shadowColor | 0xFF000000;
            context.drawText(client.textRenderer, text, 1, 1, opaqueShadowColor, false);

            // 确保文字颜色完全不透明
            int opaqueTextColor = textColor | 0xFF000000;
            context.drawText(client.textRenderer, text, 0, 0, opaqueTextColor, false);
        } else {
            // 确保文字颜色完全不透明
            int opaqueTextColor = textColor | 0xFF000000;
            context.drawText(client.textRenderer, text, 0, 0, opaqueTextColor, false);
        }

        // 恢复变换状态
        context.getMatrices().pop();

        // === 额外安全措施：再次重置着色器颜色 ===
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

//     === 调试：绘制可视化参考 ===
//    private void drawDebugVisualizations(DrawContext context, int counterX, int counterY, int textX, int textY, String text, MinecraftClient client) {
//        // 重置着色器颜色确保调试图形正确显示
//        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
//
//        int originalWidth = client.textRenderer.getWidth(text);
//        float scaledWidth = originalWidth * TEXT_SCALE;
//
//        // 1. 计数器右边界（红色线）
//        int rightBoundary = counterX + COUNTER_WIDTH;
//        context.fill(rightBoundary - 1, counterY, rightBoundary, counterY + COUNTER_HEIGHT, 0xFFFF0000);
//
//        // 2. 个位数目标位置（绿色线）
//        int targetRightEdge = counterX + COUNTER_WIDTH - TEXT_RIGHT_MARGIN;
//        context.fill(targetRightEdge - 1, counterY, targetRightEdge, counterY + COUNTER_HEIGHT, 0xFF00FF00);
//
//        // 3. 原始文字边界框（黄色框）
//        context.drawBorder(textX, textY - 2, originalWidth, 10, 0xFFFFFF00);
//
//        // 4. 缩放后预估边界框（青色框）
//        int scaledBoxX = (int)(textX - (scaledWidth - originalWidth) / 2);
//        context.drawBorder(scaledBoxX, textY - 4, (int)scaledWidth, 14, 0xFF00FFFF);
//
//        // 5. 实际文字右边界（需要观察实际显示）
//        // 注意：由于缩放，实际显示位置可能不同
//
//        // 6. 显示详细调试信息
//        String debugInfo = String.format("'%s' 原始宽:%d 缩放后:%.1f X:%d 目标:%d 补偿:%d",
//                text, originalWidth, scaledWidth, textX, targetRightEdge, SCALE_COMPENSATION);
//        context.drawText(client.textRenderer, debugInfo, counterX, counterY - 10, 0xFFFFFFFF, false);
//
//        // 7. 缩放信息
//        String scaleInfo = String.format("缩放: %.1fx 计算方式: scaledRightAligned", TEXT_SCALE);
//        context.drawText(client.textRenderer, scaleInfo, counterX, counterY - 20, 0xFFFFFFFF, false);
//    }

    public void update() {
        // 在更新时也检查配置
        String configItemId = StardewHUD.getConfig().counterItemId;
        if (!configItemId.equals(this.itemId)) {
            this.itemId = configItemId;
        }
        parseItemId();
        countItemsInInventory();
    }

    private void parseItemId() {
        try {
            // 使用当前存储的itemId
            Identifier id = Identifier.tryParse(itemId);
            if (id != null && Registries.ITEM.containsId(id)) {
                item = Registries.ITEM.get(id);
            } else {
                item = null;
                StardewHUD.LOGGER.warn("物品ID无效: {}", itemId);
            }
        } catch (Exception e) {
            item = null;
            StardewHUD.LOGGER.error("解析物品ID时出错: {}", itemId, e);
        }
    }

    private void countItemsInInventory() {
        MinecraftClient client = hudRenderer.getClient();
        if (client.player == null || item == null) {
            itemCount = 0;
            return;
        }

        itemCount = 0;

        for (ItemStack stack : client.player.getInventory().main) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                itemCount += stack.getCount();
            }
        }

        for (ItemStack stack : client.player.getInventory().offHand) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                itemCount += stack.getCount();
            }
        }
    }

    public void setItemId(String newItemId) {
        // 直接更新配置，不再与this.itemId比较
        StardewHUD.getConfig().counterItemId = newItemId;
        // 立即应用更改
        this.itemId = newItemId;
        parseItemId();
    }
}