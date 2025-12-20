package wb.stardewhud.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wb.stardewhud.config.ConfigManager;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    /* offerOrDrop 是 void，用 CallbackInfo + HEAD 即可 */
    @Inject(method = "offerOrDrop(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"))
    private void stardew$trackGain(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        System.out.println("[StarDew] offerOrDrop: " + stack.getItem());
        if (((Object) this instanceof PlayerInventory inv) &&
                inv.player instanceof ServerPlayerEntity player) {
            String targetId = ConfigManager.get().counters.get(0).itemId;
            if (targetId.equals(stack.getItem().toString())) {
                ConfigManager.addAndSync(player, stack.getCount());
            }
        }

    }
}