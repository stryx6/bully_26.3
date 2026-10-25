package net.kraftattacke.bully.effect;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.kraftattacke.bully.Bully;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.Potion;

public final class ModEffects {
	// Dark wound red, a color no vanilla effect uses.
	private static final int GRIEVOUS_WOUNDS_COLOR = 0x6B0F1A;

	/** Level I halves all healing, level II blocks it. Regeneration cannot be applied at any level. */
	public static final Holder<MobEffect> GRIEVOUS_WOUNDS = Registry.registerForHolder(
			BuiltInRegistries.MOB_EFFECT,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "grievous_wounds"),
			new GrievousWoundsEffect()
	);

	public static final Holder<Potion> GRIEVOUS_WOUNDS_POTION = registerPotion("grievous_wounds", 1200, 0);
	public static final Holder<Potion> LONG_GRIEVOUS_WOUNDS_POTION = registerPotion("long_grievous_wounds", 3000, 0);
	public static final Holder<Potion> STRONG_GRIEVOUS_WOUNDS_POTION = registerPotion("strong_grievous_wounds", 900, 1);

	private ModEffects() {
	}

	public static void register() {
		Bully.LOGGER.info("Registering Mod Effects for {}", Bully.MOD_ID);

		// Counter to Grievous Wounds: a honey bottle cures it, on top of vanilla's Poison cure.
		DefaultItemComponentEvents.MODIFY.register(context -> context.modify(Items.HONEY_BOTTLE, builder -> {
			Consumable honey = Consumables.HONEY_BOTTLE;
			List<ConsumeEffect> effects = new ArrayList<>(honey.onConsumeEffects());
			effects.add(new RemoveStatusEffectsConsumeEffect(GRIEVOUS_WOUNDS));
			builder.set(DataComponents.CONSUMABLE, new Consumable(
					honey.consumeSeconds(), honey.animation(), honey.sound(), honey.hasConsumeParticles(), effects));
		}));
	}

	public static float healMultiplier(LivingEntity entity) {
		MobEffectInstance wounds = entity.getEffect(GRIEVOUS_WOUNDS);
		if (wounds == null) {
			return 1.0F;
		}
		return wounds.getAmplifier() == 0 ? 0.5F : 0.0F;
	}

	private static Holder<Potion> registerPotion(String id, int durationTicks, int amplifier) {
		return Registry.registerForHolder(
				BuiltInRegistries.POTION,
				Identifier.fromNamespaceAndPath(Bully.MOD_ID, id),
				new Potion("grievous_wounds", new MobEffectInstance(GRIEVOUS_WOUNDS, durationTicks, amplifier))
		);
	}

	private static final class GrievousWoundsEffect extends MobEffect {
		private GrievousWoundsEffect() {
			super(MobEffectCategory.HARMFUL, GRIEVOUS_WOUNDS_COLOR);
		}

		@Override
		public void onEffectStarted(LivingEntity entity, int amplifier) {
			entity.removeEffect(MobEffects.REGENERATION);
		}
	}
}
