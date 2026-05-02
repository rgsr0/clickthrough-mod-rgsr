package cc.cassian.clickthrough.mixins;

import cc.cassian.clickthrough.ClickThrough;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static cc.cassian.clickthrough.ClickThrough.CONFIG;

@Mixin(PlayerEntity.class)
public class SneakingCancelsInteractionMixin {

    @Inject(method = "isSecondaryUseActive", at = @At("HEAD"), cancellable = true)
    private void noCancelWhenDyeing(CallbackInfoReturnable<Boolean> cir) {
        if (!CONFIG.isActive) return;
        if (((Object) this) instanceof ClientPlayerEntity) {
            if (ClickThrough.isDyeOnSign) {
                cir.setReturnValue(false);
                cir.cancel();
                ClickThrough.isDyeOnSign = false;
            }
        }
    }
}
