package net.kraftattacke.bully.client;

import com.mojang.serialization.MapCodec;
import kraftattacke.bully.net.mixin.client.ConditionalItemModelPropertiesAccessor;
import net.kraftattacke.bully.Bully;
import net.kraftattacke.bully.config.BullyConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Item model condition "bully:scythe_3d": true when this client's config wants 3D scythes. Read live, so toggling needs no reload. */
public record Scythe3dProperty() implements ConditionalItemModelProperty {
	public static final MapCodec<Scythe3dProperty> MAP_CODEC = MapCodec.unit(new Scythe3dProperty());

	public static void register() {
		ConditionalItemModelPropertiesAccessor.bully$idMapper().put(Identifier.fromNamespaceAndPath(Bully.MOD_ID, "scythe_3d"), MAP_CODEC);
	}

	@Override
	public boolean get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed, ItemDisplayContext context) {
		return BullyConfig.get().visuals.scythe3d;
	}

	@Override
	public MapCodec<Scythe3dProperty> type() {
		return MAP_CODEC;
	}
}
