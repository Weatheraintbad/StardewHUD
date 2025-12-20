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
    private final String itemId;

    private Item item;
    private int itemCount = 0;

    // 计数器尺寸
    private static final int COUNTER_WIDTH = 100;
    private static final int COUNTER_HEIGHT = 20;

    public ItemCounterComponent(HudRenderer hudRenderer, String itemId) {
        this.hudRenderer = hudRenderer;
        this.itemId = itemId;
        parseItemId();
    }

    public void render(DrawContext context, int x, int y) {
        // 渲染计数器背景（悬挂式）
        context.setShaderColor(1.0f, 1.0f, 1.0f, hudRenderer.getConfig().backgroundAlpha);
        context.drawTexture(HudRenderer.COUNTER_BG, x, y, 0, 0, COUNTER_WIDTH, COUNTER_HEIGHT, COUNTER_WIDTH, COUNTER_HEIGHT);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // 渲染物品图标和数量（居中显示）
        if (item != null) {
            // 计算居中位置
            int centerX = x + COUNTER_WIDTH / 2;
            int centerY = y + COUNTER_HEIGHT / 2;

            // 渲染物品图标（16x16）
            ItemStack stack = new ItemStack(item, 1);
            context.drawItem(stack, centerX - 20, centerY - 8); // 图标左对齐

            // 渲染数量（右对齐）
            MinecraftClient client = hudRenderer.getClient();
            String countText = String.valueOf(itemCount);
            int textWidth = client.textRenderer.getWidth(countText);
            context.drawTextWithShadow(
                    client.textRenderer,
                    countText,
                    centerX + 4, // 图标右侧4像素
                    centerY - 4, // 垂直居中
                    0xFFFFFF
            );
        } else {
            // 物品ID无效时显示"?"
            MinecraftClient client = hudRenderer.getClient();
            String text = "?";
            int textWidth = client.textRenderer.getWidth(text);
            context.drawTextWithShadow(
                    client.textRenderer,
                    text,
                    x + (COUNTER_WIDTH - textWidth) / 2,
                    y + (COUNTER_HEIGHT - 8) / 2,
                    0xFF5555
            );
        }
    }

    public void update() {
        // 重新解析物品ID（以防配置更改）
        parseItemId();

        // 统计背包中的物品数量
        countItemsInInventory();
    }

    private void parseItemId() {
        try {
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

        // 主背包（包括快捷栏，共36格）
        for (ItemStack stack : client.player.getInventory().main) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                itemCount += stack.getCount();
            }
        }

        // 副手
        for (ItemStack stack : client.player.getInventory().offHand) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                itemCount += stack.getCount();
            }
        }

    }

    public void setItemId(String newItemId) {
        // 可以通过配置界面调用此方法更改物品
        if (!newItemId.equals(this.itemId)) {
            // 需要更新配置
            StardewHUD.getConfig().counterItemId = newItemId;
            parseItemId();
        }
    }
}