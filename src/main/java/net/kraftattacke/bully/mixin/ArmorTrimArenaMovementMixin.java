package net.kraftattacke.bully.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class ArmorTrimArenaMovementMixin {
    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void bully$anchorExhibition(MoverType type, Vec3 movement, CallbackInfo ci) {
        if ((Object) this instanceof ArmorStand stand && stand.entityTags().contains("bully:trim_arena")) {
            stand.setDeltaMovement(Vec3.ZERO);
            ci.cancel();
        }
    }
}
