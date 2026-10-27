package net.kraftattacke.bully.portable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server -> client: which inventory slots hold the currently open / previous / next carried workbench. */
public record PortableInfoPayload(int currentSlot, int prevSlot, int nextSlot) implements CustomPacketPayload {
	public static final Type<PortableInfoPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("bully", "portable_info"));
	public static final StreamCodec<RegistryFriendlyByteBuf, PortableInfoPayload> CODEC = CustomPacketPayload.codec(
			(payload, buffer) -> {
				buffer.writeVarInt(payload.currentSlot);
				buffer.writeVarInt(payload.prevSlot);
				buffer.writeVarInt(payload.nextSlot);
			},
			buffer -> new PortableInfoPayload(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt()));

	@Override
	public Type<PortableInfoPayload> type() { return TYPE; }
}
