package net.kraftattacke.bully.utilitymenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record UtilityMenuOpenPayload() implements CustomPacketPayload {
    public static final Type<UtilityMenuOpenPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("bully", "utility_menu_open"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UtilityMenuOpenPayload> CODEC = StreamCodec.unit(new UtilityMenuOpenPayload());

    @Override
    public Type<UtilityMenuOpenPayload> type() { return TYPE; }
}
