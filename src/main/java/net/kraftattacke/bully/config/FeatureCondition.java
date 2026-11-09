package net.kraftattacke.bully.config;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.kraftattacke.bully.Bully;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import org.jspecify.annotations.Nullable;

/** Data-file condition: {"condition": "bully:feature", "feature": "scythes"} loads the file only if that config toggle is on. */
public record FeatureCondition(String feature) implements ResourceCondition {
	public static final MapCodec<FeatureCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			com.mojang.serialization.Codec.STRING.fieldOf("feature").forGetter(FeatureCondition::feature)
	).apply(instance, FeatureCondition::new));

	public static final ResourceConditionType<FeatureCondition> TYPE =
			ResourceConditionType.create(Identifier.fromNamespaceAndPath(Bully.MOD_ID, "feature"), CODEC);

	public static void register() {
		ResourceConditions.register(TYPE);
	}

	@Override
	public ResourceConditionType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryInfo) {
		return BullyConfig.get().features.isEnabled(feature);
	}
}
