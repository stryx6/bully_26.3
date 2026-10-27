package net.kraftattacke.bully.portable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Hotkey trigger: open the first carried workbench. */
public record OpenPortablePayload() implements CustomPacketPayload {
	public static final Type<OpenPortablePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("bully", "portable_open"));
	public static final StreamCodec<RegistryFriendlyByteBuf, OpenPortablePayload> CODEC = StreamCodec.unit(new OpenPortablePayload());

	@Override
	public Type<OpenPortablePayload> type() { return TYPE; }
}
