package net.kraftattacke.bully.enchantment;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Keybind press: ask the server for an Elytra Boost. */
public record ElytraBoostPayload() implements CustomPacketPayload {
	public static final Type<ElytraBoostPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("bully", "elytra_boost"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ElytraBoostPayload> CODEC = StreamCodec.unit(new ElytraBoostPayload());

	@Override
	public Type<ElytraBoostPayload> type() { return TYPE; }
}
