package net.kraftattacke.bully.utilitymenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** A button belongs to the currently open server menu, never to client-supplied coordinates. */
public record UtilityMenuPayload(int containerId, int button, String command) implements CustomPacketPayload {
    public static final Type<UtilityMenuPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("bully", "utility_menu_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UtilityMenuPayload> CODEC = CustomPacketPayload.codec(
            (payload, buffer) -> {
                buffer.writeVarInt(payload.containerId);
                buffer.writeVarInt(payload.button);
                buffer.writeUtf(payload.command, 32767);
            }, buffer -> new UtilityMenuPayload(buffer.readVarInt(), buffer.readVarInt(), buffer.readUtf(32767)));

    @Override
    public Type<UtilityMenuPayload> type() { return TYPE; }
}
