package net.kraftattacke.bully.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kraftattacke.bully.Bully;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public record MagicWandBeamPayload(
		double startX,
		double startY,
		double startZ,
		double endX,
		double endY,
		double endZ,
		String modeId
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<MagicWandBeamPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Bully.MOD_ID, "magic_wand_beam"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MagicWandBeamPayload> CODEC = CustomPacketPayload.codec(MagicWandBeamPayload::write, MagicWandBeamPayload::read);
	private static final double TRACKING_RANGE = 96.0;

	@Override
	public CustomPacketPayload.Type<MagicWandBeamPayload> type() {
		return TYPE;
	}

	public Vec3 start() {
		return new Vec3(startX, startY, startZ);
	}

	public Vec3 end() {
		return new Vec3(endX, endY, endZ);
	}

	public static void sendToTracking(ServerLevel level, Vec3 start, Vec3 end, String modeId) {
		MagicWandBeamPayload payload = new MagicWandBeamPayload(start.x, start.y, start.z, end.x, end.y, end.z, modeId);
		Vec3 center = start.add(end).scale(0.5);
		double maxDistanceSqr = TRACKING_RANGE * TRACKING_RANGE;

		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(center) > maxDistanceSqr || !ServerPlayNetworking.canSend(player, TYPE)) {
				continue;
			}

			ServerPlayNetworking.send(player, payload);
		}
	}

	private static MagicWandBeamPayload read(RegistryFriendlyByteBuf buffer) {
		return new MagicWandBeamPayload(
				buffer.readDouble(),
				buffer.readDouble(),
				buffer.readDouble(),
				buffer.readDouble(),
				buffer.readDouble(),
				buffer.readDouble(),
				buffer.readUtf(16)
		);
	}

	private void write(RegistryFriendlyByteBuf buffer) {
		buffer.writeDouble(startX);
		buffer.writeDouble(startY);
		buffer.writeDouble(startZ);
		buffer.writeDouble(endX);
		buffer.writeDouble(endY);
		buffer.writeDouble(endZ);
		buffer.writeUtf(modeId, 16);
	}
}
