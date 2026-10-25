package net.kraftattacke.bully.client;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.kraftattacke.bully.config.BullyConfig;
import net.kraftattacke.bully.item.MagicWandItem;
import net.kraftattacke.bully.item.SwordCoating;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.Screen;

/** Builds the Cloth Config screen over config/bully.json. Saving writes the file and re-applies the values live. */
public final class BullyConfigScreen {
	private BullyConfigScreen() {
	}

	public static Screen create(Screen parent) {
		BullyConfig cfg = BullyConfig.get();
		BullyConfig def = BullyConfig.defaults();
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.literal("Bully"));
		ConfigEntryBuilder e = builder.entryBuilder();

		ConfigCategory coating = builder.getOrCreateCategory(Component.literal("Sword Coating"));
		addInt(coating, e, "Hits per potion", cfg.swordCoating.hitsPerPotion, def.swordCoating.hitsPerPotion, v -> cfg.swordCoating.hitsPerPotion = v);
		addFloat(coating, e, "Duration scale", cfg.swordCoating.durationScale, def.swordCoating.durationScale, v -> cfg.swordCoating.durationScale = v);
		addInt(coating, e, "Anvil cost", cfg.swordCoating.anvilCost, def.swordCoating.anvilCost, v -> cfg.swordCoating.anvilCost = v);
		addInt(coating, e, "Instant hits per potion", cfg.swordCoating.instantHitsPerPotion, def.swordCoating.instantHitsPerPotion, v -> cfg.swordCoating.instantHitsPerPotion = v);
		addDouble(coating, e, "Instant effect scale", cfg.swordCoating.instantEffectScale, def.swordCoating.instantEffectScale, v -> cfg.swordCoating.instantEffectScale = v);

		ConfigCategory wand = builder.getOrCreateCategory(Component.literal("Magic Wand"));
		addInt(wand, e, "Min charge ticks", cfg.magicWand.minChargeTicks, def.magicWand.minChargeTicks, v -> cfg.magicWand.minChargeTicks = v);
		addInt(wand, e, "Full charge ticks", cfg.magicWand.fullChargeTicks, def.magicWand.fullChargeTicks, v -> cfg.magicWand.fullChargeTicks = v);
		addInt(wand, e, "Charge step ticks", cfg.magicWand.chargeStepTicks, def.magicWand.chargeStepTicks, v -> cfg.magicWand.chargeStepTicks = v);

		var el = cfg.magicWand.emberLance;
		var eld = def.magicWand.emberLance;
		ConfigCategory ember = builder.getOrCreateCategory(Component.literal("Ember Lance"));
		addDouble(ember, e, "Base range", el.baseRange, eld.baseRange, v -> el.baseRange = v);
		addDouble(ember, e, "Charged range bonus", el.chargedRangeBonus, eld.chargedRangeBonus, v -> el.chargedRangeBonus = v);
		addFloat(ember, e, "Base damage", el.baseDamage, eld.baseDamage, v -> el.baseDamage = v);
		addFloat(ember, e, "Charged damage bonus", el.chargedDamageBonus, eld.chargedDamageBonus, v -> el.chargedDamageBonus = v);
		addFloat(ember, e, "Base fire seconds", el.baseFireSeconds, eld.baseFireSeconds, v -> el.baseFireSeconds = v);
		addFloat(ember, e, "Charged fire seconds bonus", el.chargedFireSecondsBonus, eld.chargedFireSecondsBonus, v -> el.chargedFireSecondsBonus = v);
		addInt(ember, e, "Cooldown ticks", el.cooldownTicks, eld.cooldownTicks, v -> el.cooldownTicks = v);
		addInt(ember, e, "Shield burn base damage", el.shieldBurnBaseDamage, eld.shieldBurnBaseDamage, v -> el.shieldBurnBaseDamage = v);
		addInt(ember, e, "Shield burn charged damage bonus", el.shieldBurnChargedDamageBonus, eld.shieldBurnChargedDamageBonus, v -> el.shieldBurnChargedDamageBonus = v);
		addInt(ember, e, "Shield stun base ticks", el.shieldStunBaseTicks, eld.shieldStunBaseTicks, v -> el.shieldStunBaseTicks = v);
		addInt(ember, e, "Shield stun charged ticks bonus", el.shieldStunChargedTicksBonus, eld.shieldStunChargedTicksBonus, v -> el.shieldStunChargedTicksBonus = v);

		var fb = cfg.magicWand.frostBurst;
		var fbd = def.magicWand.frostBurst;
		ConfigCategory frost = builder.getOrCreateCategory(Component.literal("Frost Burst"));
		addDouble(frost, e, "Base range", fb.baseRange, fbd.baseRange, v -> fb.baseRange = v);
		addDouble(frost, e, "Charged range bonus", fb.chargedRangeBonus, fbd.chargedRangeBonus, v -> fb.chargedRangeBonus = v);
		addDouble(frost, e, "Base radius", fb.baseRadius, fbd.baseRadius, v -> fb.baseRadius = v);
		addDouble(frost, e, "Charged radius bonus", fb.chargedRadiusBonus, fbd.chargedRadiusBonus, v -> fb.chargedRadiusBonus = v);
		addFloat(frost, e, "Base damage", fb.baseDamage, fbd.baseDamage, v -> fb.baseDamage = v);
		addFloat(frost, e, "Charged damage bonus", fb.chargedDamageBonus, fbd.chargedDamageBonus, v -> fb.chargedDamageBonus = v);
		addInt(frost, e, "Cooldown ticks", fb.cooldownTicks, fbd.cooldownTicks, v -> fb.cooldownTicks = v);
		addFloat(frost, e, "Slowness seconds", fb.slownessSeconds, fbd.slownessSeconds, v -> fb.slownessSeconds = v);
		addFloat(frost, e, "Charged slowness seconds bonus", fb.chargedSlownessSecondsBonus, fbd.chargedSlownessSecondsBonus, v -> fb.chargedSlownessSecondsBonus = v);

		var lb = cfg.magicWand.lifeBloom;
		var lbd = def.magicWand.lifeBloom;
		ConfigCategory life = builder.getOrCreateCategory(Component.literal("Life Bloom"));
		addDouble(life, e, "Base range", lb.baseRange, lbd.baseRange, v -> lb.baseRange = v);
		addDouble(life, e, "Charged range bonus", lb.chargedRangeBonus, lbd.chargedRangeBonus, v -> lb.chargedRangeBonus = v);
		addDouble(life, e, "Base radius", lb.baseRadius, lbd.baseRadius, v -> lb.baseRadius = v);
		addDouble(life, e, "Charged radius bonus", lb.chargedRadiusBonus, lbd.chargedRadiusBonus, v -> lb.chargedRadiusBonus = v);
		addFloat(life, e, "Self heal", lb.selfHeal, lbd.selfHeal, v -> lb.selfHeal = v);
		addFloat(life, e, "Charged self heal bonus", lb.chargedSelfHealBonus, lbd.chargedSelfHealBonus, v -> lb.chargedSelfHealBonus = v);
		addFloat(life, e, "Ally heal", lb.allyHeal, lbd.allyHeal, v -> lb.allyHeal = v);
		addFloat(life, e, "Charged ally heal bonus", lb.chargedAllyHealBonus, lbd.chargedAllyHealBonus, v -> lb.chargedAllyHealBonus = v);
		addFloat(life, e, "Hostile damage", lb.hostileDamage, lbd.hostileDamage, v -> lb.hostileDamage = v);
		addFloat(life, e, "Charged hostile damage bonus", lb.chargedHostileDamageBonus, lbd.chargedHostileDamageBonus, v -> lb.chargedHostileDamageBonus = v);
		addInt(life, e, "Cooldown ticks", lb.cooldownTicks, lbd.cooldownTicks, v -> lb.cooldownTicks = v);
		addFloat(life, e, "Saturation seconds", lb.saturationSeconds, lbd.saturationSeconds, v -> lb.saturationSeconds = v);
		addFloat(life, e, "Charged saturation seconds bonus", lb.chargedSaturationSecondsBonus, lbd.chargedSaturationSecondsBonus, v -> lb.chargedSaturationSecondsBonus = v);
		addFloat(life, e, "Regen seconds", lb.regenSeconds, lbd.regenSeconds, v -> lb.regenSeconds = v);
		addFloat(life, e, "Charged regen seconds bonus", lb.chargedRegenSecondsBonus, lbd.chargedRegenSecondsBonus, v -> lb.chargedRegenSecondsBonus = v);
		addFloat(life, e, "Absorption seconds", lb.absorptionSeconds, lbd.absorptionSeconds, v -> lb.absorptionSeconds = v);
		addFloat(life, e, "Charged absorption seconds bonus", lb.chargedAbsorptionSecondsBonus, lbd.chargedAbsorptionSecondsBonus, v -> lb.chargedAbsorptionSecondsBonus = v);
		addFloat(life, e, "Poison seconds", lb.poisonSeconds, lbd.poisonSeconds, v -> lb.poisonSeconds = v);
		addFloat(life, e, "Charged poison seconds bonus", lb.chargedPoisonSecondsBonus, lbd.chargedPoisonSecondsBonus, v -> lb.chargedPoisonSecondsBonus = v);
		addFloat(life, e, "Wither seconds", lb.witherSeconds, lbd.witherSeconds, v -> lb.witherSeconds = v);
		addFloat(life, e, "Charged wither seconds bonus", lb.chargedWitherSecondsBonus, lbd.chargedWitherSecondsBonus, v -> lb.chargedWitherSecondsBonus = v);
		addFloat(life, e, "Darkness seconds", lb.darknessSeconds, lbd.darknessSeconds, v -> lb.darknessSeconds = v);
		addFloat(life, e, "Charged darkness seconds bonus", lb.chargedDarknessSecondsBonus, lbd.chargedDarknessSecondsBonus, v -> lb.chargedDarknessSecondsBonus = v);
		addFloat(life, e, "Grievous Wounds seconds", lb.grievousWoundsSeconds, lbd.grievousWoundsSeconds, v -> lb.grievousWoundsSeconds = v);
		addFloat(life, e, "Charged Grievous Wounds seconds bonus", lb.chargedGrievousWoundsSecondsBonus, lbd.chargedGrievousWoundsSecondsBonus, v -> lb.chargedGrievousWoundsSecondsBonus = v);
		addFloat(life, e, "Weakness seconds", lb.weaknessSeconds, lbd.weaknessSeconds, v -> lb.weaknessSeconds = v);
		addFloat(life, e, "Charged weakness seconds bonus", lb.chargedWeaknessSecondsBonus, lbd.chargedWeaknessSecondsBonus, v -> lb.chargedWeaknessSecondsBonus = v);

		builder.setSavingRunnable(() -> {
			BullyConfig.save();
			SwordCoating.reload();
			MagicWandItem.applyConfig();
		});
		return builder.build();
	}

	private static void addInt(ConfigCategory cat, ConfigEntryBuilder e, String label, int current, int def, IntConsumer save) {
		cat.addEntry(e.startIntField(Component.literal(label), current).setDefaultValue(def).setSaveConsumer(save::accept).build());
	}

	private static void addFloat(ConfigCategory cat, ConfigEntryBuilder e, String label, float current, float def, Consumer<Float> save) {
		cat.addEntry(e.startFloatField(Component.literal(label), current).setDefaultValue(def).setSaveConsumer(save).build());
	}

	private static void addDouble(ConfigCategory cat, ConfigEntryBuilder e, String label, double current, double def, Consumer<Double> save) {
		cat.addEntry(e.startDoubleField(Component.literal(label), current).setDefaultValue(def).setSaveConsumer(save).build());
	}
}
