package wb.stardewhud.hud.components;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import wb.stardewhud.StardewHUD;
import wb.stardewhud.hud.HudRenderer;

public class FortuneComponent {
    private final HudRenderer hudRenderer;

    // 运势图标纹理
    private static final Identifier FORTUNE_1_A = new Identifier(StardewHUD.MOD_ID, "textures/icons/fortune/fortune1_a.png");
    private static final Identifier FORTUNE_1_B = new Identifier(StardewHUD.MOD_ID, "textures/icons/fortune/fortune1_b.png");
    private static final Identifier FORTUNE_2_A = new Identifier(StardewHUD.MOD_ID, "textures/icons/fortune/fortune2_a.png");
    private static final Identifier FORTUNE_2_B = new Identifier(StardewHUD.MOD_ID, "textures/icons/fortune/fortune2_b.png");

    // 当前运势状态
    private FortuneType fortune1 = FortuneType.NEUTRAL;
    private FortuneType fortune2 = FortuneType.NEUTRAL;

    public enum FortuneType {
        LUCKY,      // 幸运
        NEUTRAL,    // 普通
        UNLUCKY     // 不幸
    }

    public FortuneComponent(HudRenderer hudRenderer) {
        this.hudRenderer = hudRenderer;
    }

    public void render(DrawContext context, int x, int y) {
        // 渲染两个运势图标，间距15像素
        renderFortuneIcon(context, fortune1, x, y);
        renderFortuneIcon(context, fortune2, x + 17, y);
    }

    private void renderFortuneIcon(DrawContext context, FortuneType fortune, int x, int y) {
        Identifier iconTexture = getFortuneIcon(fortune);
        if (iconTexture != null) {
            // 直接使用给定的坐标，不进行额外偏移
            context.drawTexture(iconTexture, x, y, 0, 0, 14, 14, 14, 14);
        }
    }

    private Identifier getFortuneIcon(FortuneType fortune) {
        // 配置显示的图标
        // 暂时使用简单映射
        switch (fortune) {
            case LUCKY:
                return FORTUNE_1_A;  // 使用第一个幸运图标
            case UNLUCKY:
                return FORTUNE_1_B;  // 使用第一个不幸图标
            case NEUTRAL:
            default:
                return FORTUNE_2_A;  // 使用第二个普通图标
        }
    }

    public void update() {
        // 每日运势模组接口
        if (!StardewHUD.isModLoaded("daily_luck_mod")) {
            long worldTime = hudRenderer.getClient().world != null ? hudRenderer.getClient().world.getTime() : 0;
            int randomSeed = (int)(worldTime % 100);

            if (randomSeed < 30) {
                fortune1 = FortuneType.LUCKY;
                fortune2 = FortuneType.LUCKY;
            } else if (randomSeed < 60) {
                fortune1 = FortuneType.NEUTRAL;
                fortune2 = FortuneType.NEUTRAL;
            } else {
                fortune1 = FortuneType.UNLUCKY;
                fortune2 = FortuneType.UNLUCKY;
            }
            return;
        }

        // TODO: 如果有安装运势模组，调用其API获取运势
    }

    // 为其他模组提供的API接口
    public void setFortune(FortuneType type1, FortuneType type2) {
        this.fortune1 = type1;
        this.fortune2 = type2;
    }
}