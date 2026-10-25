package net.kraftattacke.bully.mixin;

import net.kraftattacke.bully.enchantment.EnchantingContext;
import net.kraftattacke.bully.item.SwordCoating;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.player.Player;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
	@Shadow
	@Final
	private DataSlot cost;

	@Shadow
	private int repairItemCountCost;

	@Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
	private void bully$setAnvilTarget(CallbackInfo ci) {
		ItemCombinerMenuAccessor menu = (ItemCombinerMenuAccessor) this;
		// Slot 0 is the target item being modified in the anvil.
		ItemStack target = menu.bully$getInputSlots().getItem(0);
		ItemStack addition = menu.bully$getInputSlots().getItem(1);

		// Sword + drinkable potion: coat the sword instead of the vanilla repair/enchant logic.
		PotionContents potion = addition.get(DataComponents.POTION_CONTENTS);
		if (target.is(ItemTags.SWORDS) && addition.is(SwordCoating.COATING_POTIONS) && potion != null && potion.hasEffects()) {
			ItemStack result = target.copy();
			result.set(SwordCoating.TYPE, new SwordCoating(potion, SwordCoating.hitsFor(potion)));
			menu.bully$getResultSlots().setItem(0, result);
			this.cost.set(SwordCoating.ANVIL_COST);
			this.repairItemCountCost = 1;
			ci.cancel();
			return;
		}

		EnchantingContext.setTargetStack(target);
	}

	// Vanilla refuses to hand out anvil results that cost 0 levels; coating is free.
	@Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
	private void bully$allowFreeCoating(Player player, boolean hasStack, CallbackInfoReturnable<Boolean> cir) {
		ItemStack result = ((ItemCombinerMenuAccessor) this).bully$getResultSlots().getItem(0);
		if (this.cost.get() == 0 && result.has(SwordCoating.TYPE)
				&& ((ItemCombinerMenuAccessor) this).bully$getInputSlots().getItem(1).is(SwordCoating.COATING_POTIONS)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "createResult", at = @At("RETURN"))
	private void bully$clearAnvilTarget(CallbackInfo ci) {
		EnchantingContext.clearTargetStack();
	}
}
