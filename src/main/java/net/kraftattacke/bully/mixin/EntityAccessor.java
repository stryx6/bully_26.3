package net.kraftattacke.bully.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Accessor("invulnerableTime")
	void bully$setInvulnerableTime(int invulnerableTime);
}
