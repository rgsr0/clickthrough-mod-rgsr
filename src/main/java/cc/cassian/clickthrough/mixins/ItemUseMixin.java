package cc.cassian.clickthrough.mixins;

import cc.cassian.clickthrough.helpers.ModHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ItemUseMixin {

    @Shadow public RayTraceResult hitResult;
    @Shadow public PlayerEntity player;
    @Shadow public ClientWorld level;

    // Перехватываем в самом начале правого клика — до того как MC решает entity или block
    @Inject(
        method = "startUseItem",
        at = @At("HEAD")
    )
    public void switchCrosshairTargetItemUse(CallbackInfo ci) {
        this.hitResult = ModHelpers.switchCrosshairTarget(hitResult, player, level);
    }
}
