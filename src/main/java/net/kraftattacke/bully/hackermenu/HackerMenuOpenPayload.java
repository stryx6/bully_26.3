package net.kraftattacke.bully.hackermenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HackerMenuOpenPayload() implements CustomPacketPayload {
    public static final Type<HackerMenuOpenPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("bully", "hacker_menu_open"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HackerMenuOpenPayload> CODEC = StreamCodec.unit(new HackerMenuOpenPayload());

    @Override
    public Type<HackerMenuOpenPayload> type() { return TYPE; }
}
