package net.kraftattacke.bully.portable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Arrow click: switch to the next (+1) / previous (-1) carried workbench. */
public record CyclePortablePayload(int direction) implements CustomPacketPayload {
	public static final Type<CyclePortablePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("bully", "portable_cycle"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CyclePortablePayload> CODEC = CustomPacketPayload.codec(
			(payload, buffer) -> buffer.writeVarInt(payload.direction),
			buffer -> new CyclePortablePayload(buffer.readVarInt()));

	@Override
	public Type<CyclePortablePayload> type() { return TYPE; }
}
