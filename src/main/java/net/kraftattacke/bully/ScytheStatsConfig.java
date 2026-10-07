package net.kraftattacke.bully;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScytheStatsConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Logger LOGGER = LoggerFactory.getLogger("bully");

	private ScytheStatsConfig() {
	}

	public static ScytheStats load() {
		Path path = FabricLoader.getInstance()
				.getConfigDir()
				.resolve("bully")
				.resolve("scythe_stats.json");

		ScytheStats defaults = ScytheStats.defaults();
		try {
			Files.createDirectories(path.getParent());
			if (!Files.exists(path)) {
				writeDefaults(path, defaults);
				return defaults;
			}

			String raw = Files.readString(path, StandardCharsets.UTF_8);
			JsonObject obj = GSON.fromJson(raw, JsonObject.class);
			if (obj == null) {
				return defaults;
			}

			int durability = getInt(obj, "durability", defaults.durability());
			int enchantability = getInt(obj, "enchantability", defaults.enchantability());
			double attackDamage = getDouble(obj, "attack_damage", defaults.attackDamage());
			double attackSpeed = getDouble(obj, "attack_speed", defaults.attackSpeed());

			// Enforce vanilla constraints (Enchantable requires value > 0).
			if (durability <= 0) durability = defaults.durability();
			if (enchantability <= 0) enchantability = defaults.enchantability();

			return new ScytheStats(durability, enchantability, attackDamage, attackSpeed);
		} catch (IOException | JsonParseException e) {
			LOGGER.warn("Failed to load scythe stats config at {}. Using defaults.", path, e);
			return defaults;
		}
	}

	private static void writeDefaults(Path path, ScytheStats defaults) throws IOException {
		JsonObject obj = new JsonObject();
		obj.addProperty("durability", defaults.durability());
		obj.addProperty("enchantability", defaults.enchantability());
		obj.addProperty("attack_damage", defaults.attackDamage());
		obj.addProperty("attack_speed", defaults.attackSpeed());
		Files.writeString(path, GSON.toJson(obj) + System.lineSeparator(), StandardCharsets.UTF_8);
	}

	private static int getInt(JsonObject obj, String key, int fallback) {
		try {
			return obj.has(key) ? obj.get(key).getAsInt() : fallback;
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}

	private static double getDouble(JsonObject obj, String key, double fallback) {
		try {
			return obj.has(key) ? obj.get(key).getAsDouble() : fallback;
		} catch (RuntimeException ignored) {
			return fallback;
		}
	}
}
