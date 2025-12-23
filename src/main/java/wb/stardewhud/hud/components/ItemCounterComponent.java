package wb.stardewhud.hud.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.inventory.SimpleInventory;
import wb.stardewhud.StardewHUD;
import wb.stardewhud.hud.HudRenderer;

// 需要导入钱袋相关的类 - 请根据您的mod实际包名调整
// import yoscoins.item.MoneyPouchItem;
// import yoscoins.item.YosCoinsItems;

public class ItemCounterComponent {
    private final HudRenderer hudRenderer;
    // 移除final修饰符，允许更新
    private String itemId;

    private Item item;
    private int itemCount = 0;

    // 添加：强制重新统计的标志
    private int lastSnapTick = -1;

    // 使用HudRenderer中的常量
    private final int COUNTER_WIDTH = HudRenderer.getCounterWidth();
    private final int COUNTER_HEIGHT = HudRenderer.getCounterHeight();

    // === 边距常量 ===
    private static final int ITEM_LEFT_MARGIN = 9;      // 物品图标左侧边距
    private static final int TEXT_RIGHT_MARGIN = 8;    // 个位数最右侧与计数器栏最右侧的距离
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

    /* 对外接口：强制重新统计 */
    public void markInventoryChanged() {
        lastSnapTick = -1;   // 强制下一帧重新统计
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

    public void update() {
        // 在更新时也检查配置
        String configItemId = StardewHUD.getConfig().counterItemId;
        if (!configItemId.equals(this.itemId)) {
            this.itemId = configItemId;
        }
        parseItemId();
        snapshotItems();
    }

    /* ---------- 实时统计 ---------- */
    private void snapshotItems() {
        MinecraftClient mc = hudRenderer.getClient();
        if (mc.player == null || item == null) return;

        long now = mc.player.age;
        if (lastSnapTick == now) return;        // 同帧复用
        lastSnapTick = (int)now;

        // 重置计数器
        itemCount = 0;

        /* 统计所有物品栏槽位（散装物品） */
        countItemsInInventorySlots(mc.player.getInventory().main);
        countItemsInInventorySlots(mc.player.getInventory().offHand);
        countItemsInInventorySlots(mc.player.getInventory().armor);

        /* 统计钱袋内的物品（如果当前统计的物品是钱币） */
        // 检查是否是钱币类物品
        if (isCoinItem()) {
            countItemsInMoneyPouches(mc);
        }
    }

    /* 统计普通物品栏槽位 */
    private void countItemsInInventorySlots(Iterable<ItemStack> slots) {
        for (ItemStack stack : slots) {
            if (!stack.isEmpty() && stack.getItem() == item) {
                itemCount += stack.getCount();
            }
        }
    }

    /* 统计钱袋内的物品 */
    private void countItemsInMoneyPouches(MinecraftClient mc) {
        // 检查主物品栏中的钱袋
        for (ItemStack stack : mc.player.getInventory().main) {
            if (isMoneyPouch(stack)) {
                // 读取钱袋内部物品
                SimpleInventory pouch = readMoneyPouchInventory(stack);
                if (pouch != null) {
                    for (ItemStack pouchItem : pouch.stacks) {
                        if (!pouchItem.isEmpty() && pouchItem.getItem() == item) {
                            itemCount += pouchItem.getCount();
                        }
                    }
                }
            }
        }

        // 检查副手物品栏中的钱袋
        for (ItemStack stack : mc.player.getInventory().offHand) {
            if (isMoneyPouch(stack)) {
                // 读取钱袋内部物品
                SimpleInventory pouch = readMoneyPouchInventory(stack);
                if (pouch != null) {
                    for (ItemStack pouchItem : pouch.stacks) {
                        if (!pouchItem.isEmpty() && pouchItem.getItem() == item) {
                            itemCount += pouchItem.getCount();
                        }
                    }
                }
            }
        }
    }

    /* 检查是否是钱币类物品 */
    private boolean isCoinItem() {
        // 根据YosCoins模组的物品ID判断
        String itemIdString = Registries.ITEM.getId(item).toString();
        return itemIdString.equals("yoscoins:copper_coin") ||
                itemIdString.equals("yoscoins:silver_coin") ||
                itemIdString.equals("yoscoins:gold_coin");
    }

    /* 检查是否是钱袋物品 */
    private boolean isMoneyPouch(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String itemIdString = Registries.ITEM.getId(stack.getItem()).toString();
        return itemIdString.equals("yoscoins:money_pouch");
    }

    /* 读取钱袋内部物品栏 - 需要根据实际的钱袋API实现 */
    private SimpleInventory readMoneyPouchInventory(ItemStack moneyPouch) {
        try {
            // 方法1：如果钱袋模组提供了静态方法（参考示例）
            // return MoneyPouchItem.readInv(moneyPouch);

            // 方法2：通过反射调用（更通用）
            Class<?> moneyPouchClass = Class.forName("yoscoins.item.MoneyPouchItem");
            java.lang.reflect.Method readInvMethod = moneyPouchClass.getMethod("readInv", ItemStack.class);
            return (SimpleInventory) readInvMethod.invoke(null, moneyPouch);

        } catch (Exception e) {
            // 方法3：如果以上方法都失败，尝试使用NBT直接读取
            StardewHUD.LOGGER.debug("无法通过API读取钱袋内容，尝试NBT方式: {}", e.getMessage());
            return readMoneyPouchFromNBT(moneyPouch);
        }
    }

    /* 备用方法：通过NBT读取钱袋内容 */
    private SimpleInventory readMoneyPouchFromNBT(ItemStack moneyPouch) {
        if (!moneyPouch.hasNbt()) {
            return new SimpleInventory(0);
        }

        try {
            // 钱袋的NBT结构可能不同，这里是一个通用示例
            // 实际需要根据YosCoins模组的NBT结构调整
            net.minecraft.nbt.NbtCompound nbt = moneyPouch.getNbt();
            if (nbt != null && nbt.contains("Items")) {
                net.minecraft.nbt.NbtList itemList = nbt.getList("Items", net.minecraft.nbt.NbtElement.COMPOUND_TYPE);
                SimpleInventory inventory = new SimpleInventory(itemList.size());

                for (int i = 0; i < itemList.size(); i++) {
                    net.minecraft.nbt.NbtCompound itemNbt = itemList.getCompound(i);
                    ItemStack itemStack = ItemStack.fromNbt(itemNbt);
                    inventory.setStack(i, itemStack);
                }
                return inventory;
            }
        } catch (Exception e) {
            StardewHUD.LOGGER.warn("通过NBT读取钱袋失败: {}", e.getMessage());
        }

        return new SimpleInventory(0);
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

    public void setItemId(String newItemId) {
        // 直接更新配置，不再与this.itemId比较
        StardewHUD.getConfig().counterItemId = newItemId;
        // 立即应用更改
        this.itemId = newItemId;
        parseItemId();
        // 强制重新统计
        markInventoryChanged();
    }
}