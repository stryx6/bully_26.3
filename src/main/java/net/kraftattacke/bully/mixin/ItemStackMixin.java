package net.kraftattacke.bully.mixin;

import com.sun.jna.platform.unix.X11;
import net.kraftattacke.bully.enchantment.ModEnchantments;
import net.kraftattacke.bully.item.SwordCoating;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	// Runs after every successful melee hit, for player and mob targets alike.
	@Inject(method = "hurtEnemy", at = @At("HEAD"))
	private void bully$applySwordCoating(LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
		if (target.level() instanceof ServerLevel level) {
			SwordCoating.onHit(level, (ItemStack)(Object)this, target, attacker);
		}
	}

	@Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
	private void bully$addSharpnessBonusLine(Item.TooltipContext context, Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir) {
		ItemStack self = (ItemStack)(Object)this;

		// Use the enchantments stored on the stack itself to avoid registry lookups.
		var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(self);
		if (enchantments.isEmpty()) {
			return;
		}

		List<Component> out = new ArrayList<>(cir.getReturnValue());
		boolean changed = false;

		int sharpnessLevel = 0;
		for (Holder<Enchantment> enchantment : enchantments.keySet()) {
			if (enchantment.is(Enchantments.SHARPNESS)) {
				sharpnessLevel = enchantments.getLevel(enchantment);
				break;
			}
		}

		if (sharpnessLevel <= 0) {
			if (changed) {
				cir.setReturnValue(out);
			}
			return;
		}

		// Vanilla Sharpness formula in 1.21.x: +1.0 at level 1, then +0.5 per level above first.
		float bonus = 1.0F + (0.5F * (sharpnessLevel - 1));
		String bonusText = formatBonus(bonus);

		Component sharpnessLine = Component
				.translatable("tooltip.bully.sharpness_bonus", bonusText)
				.withStyle(ChatFormatting.BLUE);

		// Prefer putting it under the same header vanilla uses for weapon stats.
		int insertAfter = findHeaderIndex(out, "item.modifiers.mainhand");
		if (insertAfter < 0) {
			// User-facing request wording: "When held:".
			insertAfter = findHeaderIndex(out, "item.modifiers.hand");
		}

		if (insertAfter >= 0) {
			out.add(insertAfter + 1, sharpnessLine);
		} else {
			// Fallback: still show it somewhere rather than hiding it.
			out.add(sharpnessLine);
		}
		changed = true;

		if (changed) {
			cir.setReturnValue(out);
		}
	}

	private static int findHeaderIndex(List<Component> lines, String translationKey) {
		String header = Component.translatable(translationKey).getString();
		for (int i = 0; i < lines.size(); i++) {
			if (header.equals(lines.get(i).getString())) {
				return i;
			}
		}
		return -1;
	}

	private static String formatBonus(float value) {
		// Keep tooltips clean: integers as "3", halves as "3.5".
		if (value == (int)value) {
			return Integer.toString((int)value);
		}
		return String.format(Locale.ROOT, "%.1f", value);
	}
}
