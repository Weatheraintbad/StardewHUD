package wb.stardewhud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    private int ticker;

    @Inject(method="tick", require=1, at=@At("HEAD"))
    private void stardew$tick(CallbackInfo ci) {
        if (++ticker % 20 == 0) {
            ClientPlayerEntity p = ((MinecraftClient)(Object)this).player;
            if (p != null && p.getWorld() != null) {
                // 服务端推送逻辑见 NetworkManager.sendToPlayer
                // 这里客户端什么都不用做，纯接收
            }
        }
    }
}