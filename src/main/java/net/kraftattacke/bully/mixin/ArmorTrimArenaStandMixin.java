package net.kraftattacke.bully.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Protects only the exported exhibition's stands, including against creative attacks. */
@Mixin(ArmorStand.class)
public abstract class ArmorTrimArenaStandMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void bully$protectExhibition(ServerLevel level, DamageSource source, float amount,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (((ArmorStand) (Object) this).entityTags().contains("bully:trim_arena")) {
            cir.setReturnValue(false);
        }
    }
}
