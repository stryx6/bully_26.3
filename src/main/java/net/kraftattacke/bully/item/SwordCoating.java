package net.kraftattacke.bully.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kraftattacke.bully.Bully;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.kraftattacke.bully.mixin.EntityAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

/** A potion applied to a sword in the anvil; every hit passes its effects on at reduced duration. */
public record SwordCoating(PotionContents contents, int hits) {
	public static final TagKey<Item> COATING_POTIONS = TagKey.create(
			BuiltInRegistries.ITEM.key(),
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "coating_potions")
	);

	// Loaded from config/bully.json in register(); defaults match the original values.
	public static int HITS_PER_POTION = 10;
	public static float DURATION_SCALE = 0.25F;
	public static int ANVIL_COST = 0;
	// Instant potions bypass armor duration scaling, so they get fewer hits at half strength.
	public static int INSTANT_HITS_PER_POTION = 3;
	public static double INSTANT_EFFECT_SCALE = 0.5D;

	public static int hitsFor(PotionContents contents) {
		for (MobEffectInstance effect : contents.getAllEffects()) {
			if (effect.getEffect().value().isInstantaneous()) {
				return INSTANT_HITS_PER_POTION;
			}
		}
		return HITS_PER_POTION;
	}

	public static final Codec<SwordCoating> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PotionContents.CODEC.fieldOf("contents").forGetter(SwordCoating::contents),
			ExtraCodecs.POSITIVE_INT.fieldOf("hits").forGetter(SwordCoating::hits)
	).apply(instance, SwordCoating::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SwordCoating> STREAM_CODEC = StreamCodec.composite(
			PotionContents.STREAM_CODEC, SwordCoating::contents,
			ByteBufCodecs.VAR_INT, SwordCoating::hits,
			SwordCoating::new
	);

	public static final DataComponentType<SwordCoating> TYPE = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "sword_coating"),
			DataComponentType.<SwordCoating>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC).build()
	);

	public static void register() {
		reload();
		Bully.LOGGER.info("Registering sword coating component for {}", Bully.MOD_ID);
	}

	/** Copies config/bully.json into the fields above; called on load and after the config screen saves. */
	public static void reload() {
		var config = net.kraftattacke.bully.config.BullyConfig.get().swordCoating;
		HITS_PER_POTION = config.hitsPerPotion;
		DURATION_SCALE = config.durationScale;
		ANVIL_COST = config.anvilCost;
		INSTANT_HITS_PER_POTION = config.instantHitsPerPotion;
		INSTANT_EFFECT_SCALE = config.instantEffectScale;
	}

	public static void onHit(ServerLevel level, ItemStack weapon, LivingEntity target, LivingEntity attacker) {
		if (weapon == null) {
			return;
		}
		SwordCoating coating = weapon.get(TYPE);
		if (coating == null) {
			return;
		}

		coating.contents.forEachEffect(effect -> {
			MobEffect type = effect.getEffect().value();
			if (type.isInstantaneous()) {
				// The sword hit just started invulnerability frames, which would swallow most of Instant Damage.
				((EntityAccessor) target).bully$setInvulnerableTime(0);
				type.applyInstantaneousEffect(level, attacker, attacker, target, effect.getAmplifier(), INSTANT_EFFECT_SCALE);
			} else {
				target.addEffect(effect, attacker);
			}
		}, DURATION_SCALE);
		if (coating.hits <= 1) {
			weapon.remove(TYPE);
		} else {
			weapon.set(TYPE, new SwordCoating(coating.contents, coating.hits - 1));
		}
	}
}
