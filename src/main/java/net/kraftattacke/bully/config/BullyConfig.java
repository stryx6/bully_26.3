package net.kraftattacke.bully.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.kraftattacke.bully.Bully;

/**
 * All tunable gameplay values, saved as config/bully.json. Edit the file and restart (or re-open a world)
 * to apply. Fields are public so GSON reads and writes them; defaults here match the original hard-coded values.
 */
public final class BullyConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static BullyConfig instance;

	/** Bully menu (dupe + command) is off by default. On a server, only the server's config/bully.json controls this. */
	public boolean bullyMenuEnabled = false;
	/** Utility menu (portable workbenches) is on by default. */
	public boolean utilityMenuEnabled = true;
	public Portable portable = new Portable();
	public SwordCoating swordCoating = new SwordCoating();
	public MagicWand magicWand = new MagicWand();

	public static final class Portable {
		/** Portable anvil degrades in this fraction of a placed anvil's uses (0.75 = needs 75% as many uses). */
		public float anvilDegradeUseFraction = 0.75F;
	}

	public static final class SwordCoating {
		public int hitsPerPotion = 10;
		public float durationScale = 0.25F;
		public int anvilCost = 0;
		public int instantHitsPerPotion = 3;
		public double instantEffectScale = 0.5D;
	}

	public static final class MagicWand {
		public int minChargeTicks = 10;
		public int fullChargeTicks = 200;
		public int chargeStepTicks = 20;
		public EmberLance emberLance = new EmberLance();
		public FrostBurst frostBurst = new FrostBurst();
		public LifeBloom lifeBloom = new LifeBloom();
	}

	public static final class EmberLance {
		public double baseRange = 40.0;
		public double chargedRangeBonus = 20.0;
		public float baseDamage = 10.0F;
		public float chargedDamageBonus = 50.0F;
		public float baseFireSeconds = 5.0F;
		public float chargedFireSecondsBonus = 5.0F;
		public int cooldownTicks = 10;
		public int shieldBurnBaseDamage = 10;
		public int shieldBurnChargedDamageBonus = 14;
		public int shieldStunBaseTicks = 50;
		public int shieldStunChargedTicksBonus = 50;
	}

	public static final class FrostBurst {
		public double baseRange = 30.0;
		public double chargedRangeBonus = 15.0;
		public double baseRadius = 3.0;
		public double chargedRadiusBonus = 3.0;
		public float baseDamage = 6.0F;
		public float chargedDamageBonus = 6.0F;
		public int cooldownTicks = 30;
		public float slownessSeconds = 5.0F;
		public float chargedSlownessSecondsBonus = 25.0F;
	}

	public static final class LifeBloom {
		public double baseRange = 20.0;
		public double chargedRangeBonus = 10.0;
		public double baseRadius = 4.0;
		public double chargedRadiusBonus = 4.0;
		public float selfHeal = 3.0F;
		public float chargedSelfHealBonus = 5.0F;
		public float allyHeal = 3.0F;
		public float chargedAllyHealBonus = 3.0F;
		public float hostileDamage = 3.0F;
		public float chargedHostileDamageBonus = 3.0F;
		public int cooldownTicks = 50;
		public float saturationSeconds = 5.0F;
		public float chargedSaturationSecondsBonus = 5.0F;
		public float regenSeconds = 5.0F;
		public float chargedRegenSecondsBonus = 5.0F;
		public float absorptionSeconds = 6.0F;
		public float chargedAbsorptionSecondsBonus = 4.0F;
		public float poisonSeconds = 5.0F;
		public float chargedPoisonSecondsBonus = 25.0F;
		public float witherSeconds = 5.0F;
		public float chargedWitherSecondsBonus = 25.0F;
		public float darknessSeconds = 5.0F;
		public float chargedDarknessSecondsBonus = 25.0F;
		public float grievousWoundsSeconds = 5.0F;
		public float chargedGrievousWoundsSecondsBonus = 10.0F;
		public float weaknessSeconds = 5.0F;
		public float chargedWeaknessSecondsBonus = 25.0F;
	}

	public static BullyConfig get() {
		if (instance == null) {
			load();
		}
		return instance;
	}

	/** A fresh instance with the hard-coded defaults, used as the "reset" value in the config screen. */
	public static BullyConfig defaults() {
		return new BullyConfig();
	}

	/** Reads config/bully.json, creating it from defaults when missing or unreadable. */
	public static void load() {
		Path path = configPath();
		if (Files.exists(path)) {
			try {
				instance = GSON.fromJson(Files.readString(path), BullyConfig.class);
			} catch (Exception error) {
				Bully.LOGGER.warn("Could not read {}, using defaults: {}", path, error.toString());
			}
		}
		if (instance == null) {
			instance = new BullyConfig();
		}
		save();
	}

	/** Writes the current values back to config/bully.json. */
	public static void save() {
		Path path = configPath();
		try {
			Files.createDirectories(path.getParent());
			Files.writeString(path, GSON.toJson(instance));
		} catch (IOException error) {
			Bully.LOGGER.warn("Could not write {}: {}", path, error.toString());
		}
	}

	private static Path configPath() {
		return FabricLoader.getInstance().getConfigDir().resolve("bully.json");
	}

	private BullyConfig() {
	}
}
