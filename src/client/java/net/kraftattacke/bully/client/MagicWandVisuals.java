package net.kraftattacke.bully.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.kraftattacke.bully.Bully;
import net.kraftattacke.bully.item.MagicWandItem;
import net.kraftattacke.bully.item.ModItems;
import net.kraftattacke.bully.network.MagicWandBeamPayload;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

public final class MagicWandVisuals {
	private static final int FULL_BRIGHT_LIGHT = 0xF000F0;
	private static final int CHARGE_TEXTURE_ALPHA = 205;
	private static final int CHARGED_TEXTURE_ALPHA = 230;
	private static final int BEAM_LIFETIME_TICKS = 20;
	private static final double BEAM_RADIUS = 0.20;
	private static final double CHARGED_TEXTURE_BACK_OFFSET = 0.20;
	private static final double CIRCLE_FORWARD_OFFSET = 2.0;
	private static final double CIRCLE_VERTICAL_OFFSET = 0.0;
	private static final double CIRCLE_HAND_OFFSET = 0.0;
	private static final double CIRCLE_TEXTURE_PIXELS = 32.0;
	private static final double CIRCLE_DEPTH_PIXELS = 1.0;
	private static final double CIRCLE_GROUND_MARGIN = 0.035;
	private static final double MIN_CAMERA_RAY_DISTANCE = 0.35;
	private static final double CHARGED_TEXTURE_SIZE_MULTIPLIER = 1.33;
	private static final List<BeamEffect> BEAMS = new ArrayList<>();

	private MagicWandVisuals() {
	}

	public static void register() {
		MagicWandTargeting.register();
		ClientPlayNetworking.registerGlobalReceiver(MagicWandBeamPayload.TYPE, (payload, context) -> addBeam(context.client(), payload));
		LevelRenderEvents.COLLECT_SUBMITS.register(MagicWandVisuals::render);
	}

	private static void addBeam(Minecraft client, MagicWandBeamPayload payload) {
		if (client.level == null) {
			return;
		}

		BEAMS.add(new BeamEffect(payload.start(), payload.end(), normalizeMode(payload.modeId()), client.level.getGameTime()));
		if (BEAMS.size() > 64) {
			BEAMS.removeFirst();
		}
	}

	private static void render(LevelRenderContext context) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			BEAMS.clear();
			return;
		}

		Camera camera = client.gameRenderer.mainCamera();
		Vec3 cameraPosition = camera.position();
		renderChargingTextures(context, client, camera, cameraPosition);
		renderBeams(context, client, cameraPosition);
	}

	private static void renderChargingTextures(LevelRenderContext context, Minecraft client, Camera camera, Vec3 cameraPosition) {
		for (AbstractClientPlayer player : client.level.players()) {
			if (!player.isUsingItem()) {
				continue;
			}

			ItemStack stack = player.getUseItem();
			if (stack.isEmpty() || stack.getItem() != ModItems.MAGIC_WAND) {
				continue;
			}

			int chargeTicks = player.getTicksUsingItem();
			float charge = Math.min(1.0F, chargeTicks / (float) MagicWandItem.fullChargeTicks());
			boolean charged = charge >= 1.0F;
			String modeId = normalizeMode(MagicWandItem.getModeId(stack));
			InteractionHand hand = player.getUsedItemHand();
			double handSide = hand == InteractionHand.OFF_HAND ? -CIRCLE_HAND_OFFSET : CIRCLE_HAND_OFFSET;
			boolean localPlayer = player == client.player;
			boolean firstPerson = localPlayer && client.options.getCameraType() == CameraType.FIRST_PERSON;
			Vec3 cameraForward = toVec3(camera.forwardVector()).normalize();
			Vec3 playerForward = player.getLookAngle().normalize();
			Vec3 forward = localPlayer ? cameraForward : playerForward;
			Vec3 right = localPlayer ? toVec3(camera.leftVector()).scale(-1.0).normalize() : perpendicularTo(forward);
			Vec3 up = localPlayer ? toVec3(camera.upVector()).normalize() : right.cross(forward).normalize();
			Vec3 center = localPlayer
					? localCircleCenter(cameraPosition, player.getEyePosition(), cameraForward, playerForward, right, up, handSide, firstPerson)
					: player.getEyePosition()
							.add(playerForward.scale(CIRCLE_FORWARD_OFFSET))
							.add(up.scale(CIRCLE_VERTICAL_OFFSET))
							.add(right.scale(handSide));
			double halfSize = 0.48 + charge * 0.32;

			Vec3 liftedCenter = liftCenterAboveGround(client, center, forward, right, up, halfSize);
			submitTexturedVolume(context, magicCircleTexture(modeId, false), liftedCenter, forward, right, up, halfSize, CHARGE_TEXTURE_ALPHA, cameraPosition);
			if (charged) {
				Vec3 chargedCenter = center.add(forward.scale(CHARGED_TEXTURE_BACK_OFFSET));
				double chargedHalfSize = halfSize * CHARGED_TEXTURE_SIZE_MULTIPLIER;
				Vec3 liftedChargedCenter = liftCenterAboveGround(client, chargedCenter, forward, right, up, chargedHalfSize);
				submitTexturedVolume(context, magicCircleTexture(modeId, true), liftedChargedCenter, forward, right, up, chargedHalfSize, CHARGED_TEXTURE_ALPHA, cameraPosition);
			}
		}
	}

	private static Vec3 localCircleCenter(Vec3 cameraPosition, Vec3 eyePosition, Vec3 cameraForward, Vec3 playerForward, Vec3 right, Vec3 up, double handSide, boolean firstPerson) {
		double rayDistance = CIRCLE_FORWARD_OFFSET;
		if (!firstPerson) {
			double cameraToEye = cameraPosition.distanceTo(eyePosition);
			double offsetFromEye = cameraForward.dot(playerForward) < 0.0 ? -CIRCLE_FORWARD_OFFSET : CIRCLE_FORWARD_OFFSET;
			rayDistance = Math.max(MIN_CAMERA_RAY_DISTANCE, cameraToEye + offsetFromEye);
		}

		return cameraPosition
				.add(cameraForward.scale(rayDistance))
				.add(up.scale(CIRCLE_VERTICAL_OFFSET))
				.add(right.scale(handSide));
	}

	private static Vec3 liftCenterAboveGround(Minecraft client, Vec3 center, Vec3 forward, Vec3 right, Vec3 up, double halfSize) {
		if (client.level == null || client.player == null) {
			return center;
		}

		double bottomY = center.y + lowestLocalYOffset(forward, right, up, halfSize);
		Vec3 rayStart = new Vec3(center.x, bottomY + 1.0, center.z);
		Vec3 rayEnd = new Vec3(center.x, bottomY - 0.25, center.z);
		BlockHitResult blockHit = client.level.clip(new ClipContext(rayStart, rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, client.player));
		if (blockHit.getType() == HitResult.Type.MISS) {
			return center;
		}

		double minimumBottomY = blockHit.getLocation().y + CIRCLE_GROUND_MARGIN;
		double lift = minimumBottomY - bottomY;
		return lift > 0.0 ? center.add(0.0, lift, 0.0) : center;
	}

	private static double lowestLocalYOffset(Vec3 forward, Vec3 right, Vec3 up, double halfSize) {
		double halfDepth = halfSize * CIRCLE_DEPTH_PIXELS / CIRCLE_TEXTURE_PIXELS;
		return -Math.abs(right.y) * halfSize - Math.abs(up.y) * halfSize - Math.abs(forward.normalize().y) * halfDepth;
	}

	private static void renderBeams(LevelRenderContext context, Minecraft client, Vec3 cameraPosition) {
		long gameTime = client.level.getGameTime();
		Iterator<BeamEffect> iterator = BEAMS.iterator();
		while (iterator.hasNext()) {
			BeamEffect beam = iterator.next();
			int age = (int) (gameTime - beam.startedAtGameTime());
			if (age > BEAM_LIFETIME_TICKS) {
				iterator.remove();
				continue;
			}

			float fade = 1.0F - age / (float) BEAM_LIFETIME_TICKS;
			submitBeam(context, beam, cameraPosition, gameTime, Mth.clamp((int) (210 * fade), 35, 210));
		}
	}

	private static void submitTexturedVolume(LevelRenderContext context, Identifier texture, Vec3 center, Vec3 forward, Vec3 right, Vec3 up, double halfSize, int alpha, Vec3 cameraPosition) {
		double halfDepth = halfSize * CIRCLE_DEPTH_PIXELS / CIRCLE_TEXTURE_PIXELS;
		Vec3 depth = forward.normalize().scale(halfDepth);
		Vec3 frontCenter = center.subtract(depth);
		Vec3 backCenter = center.add(depth);
		Vec3 frontLowerLeft = frontCenter.add(right.scale(-halfSize)).add(up.scale(-halfSize)).subtract(cameraPosition);
		Vec3 frontLowerRight = frontCenter.add(right.scale(halfSize)).add(up.scale(-halfSize)).subtract(cameraPosition);
		Vec3 frontUpperRight = frontCenter.add(right.scale(halfSize)).add(up.scale(halfSize)).subtract(cameraPosition);
		Vec3 frontUpperLeft = frontCenter.add(right.scale(-halfSize)).add(up.scale(halfSize)).subtract(cameraPosition);
		Vec3 backLowerLeft = backCenter.add(right.scale(-halfSize)).add(up.scale(-halfSize)).subtract(cameraPosition);
		Vec3 backLowerRight = backCenter.add(right.scale(halfSize)).add(up.scale(-halfSize)).subtract(cameraPosition);
		Vec3 backUpperRight = backCenter.add(right.scale(halfSize)).add(up.scale(halfSize)).subtract(cameraPosition);
		Vec3 backUpperLeft = backCenter.add(right.scale(-halfSize)).add(up.scale(halfSize)).subtract(cameraPosition);
		RenderType renderType = RenderTypes.entityCutout(texture);

		context.submitNodeCollector().submitCustomGeometry(new PoseStack(), renderType, (pose, vertices) -> {
			addQuad(vertices, pose, frontLowerLeft, frontLowerRight, frontUpperRight, frontUpperLeft, 255, 255, 255, alpha);
			addQuad(vertices, pose, backLowerRight, backLowerLeft, backUpperLeft, backUpperRight, 255, 255, 255, alpha);
			addTexturedQuad(vertices, pose, frontLowerLeft, backLowerLeft, backLowerRight, frontLowerRight, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 255, 255, 255, alpha);
			addTexturedQuad(vertices, pose, frontUpperRight, backUpperRight, backUpperLeft, frontUpperLeft, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 255, 255, 255, alpha);
			addTexturedQuad(vertices, pose, frontUpperLeft, backUpperLeft, backLowerLeft, frontLowerLeft, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 255, 255, 255, alpha);
			addTexturedQuad(vertices, pose, frontLowerRight, backLowerRight, backUpperRight, frontUpperRight, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 255, 255, 255, alpha);
		});
	}

	private static void submitBeam(LevelRenderContext context, BeamEffect beam, Vec3 cameraPosition, long gameTime, int alpha) {
		Vec3 start = beam.start();
		Vec3 end = beam.end();
		Vec3 axis = end.subtract(start);
		double length = axis.length();
		if (length < 0.01) {
			return;
		}

		Vec3 forward = axis.normalize();
		Vec3 right = perpendicularTo(forward).scale(BEAM_RADIUS);
		Vec3 up = forward.cross(right).normalize().scale(BEAM_RADIUS);
		Vec3 startRelative = start.subtract(cameraPosition);
		Vec3 endRelative = end.subtract(cameraPosition);
		int color = beamColor(beam.modeId());
		int red = color >> 16 & 0xFF;
		int green = color >> 8 & 0xFF;
		int blue = color & 0xFF;
		float vStart = -(gameTime % 80) * 0.025F;
		float vEnd = vStart + (float) length * 0.45F;
		RenderType renderType = RenderTypes.beaconBeam(BeaconRenderer.BEAM_LOCATION, true);

		context.submitNodeCollector().submitCustomGeometry(new PoseStack(), renderType, (pose, vertices) -> {
			addBeamRibbon(vertices, pose, startRelative, endRelative, right, red, green, blue, alpha, vStart, vEnd);
			addBeamRibbon(vertices, pose, startRelative, endRelative, up, red, green, blue, alpha, vStart, vEnd);
		});
	}

	private static void addBeamRibbon(VertexConsumer vertices, PoseStack.Pose pose, Vec3 start, Vec3 end, Vec3 offset, int red, int green, int blue, int alpha, float vStart, float vEnd) {
		Vec3 startA = start.add(offset);
		Vec3 startB = start.subtract(offset);
		Vec3 endB = end.subtract(offset);
		Vec3 endA = end.add(offset);
		addVertex(vertices, pose, startA, 0.0F, vStart, red, green, blue, alpha);
		addVertex(vertices, pose, startB, 1.0F, vStart, red, green, blue, alpha);
		addVertex(vertices, pose, endB, 1.0F, vEnd, red, green, blue, alpha);
		addVertex(vertices, pose, endA, 0.0F, vEnd, red, green, blue, alpha);
	}

	private static void addQuad(VertexConsumer vertices, PoseStack.Pose pose, Vec3 lowerLeft, Vec3 lowerRight, Vec3 upperRight, Vec3 upperLeft, int red, int green, int blue, int alpha) {
		addVertex(vertices, pose, lowerLeft, 0.0F, 1.0F, red, green, blue, alpha);
		addVertex(vertices, pose, lowerRight, 1.0F, 1.0F, red, green, blue, alpha);
		addVertex(vertices, pose, upperRight, 1.0F, 0.0F, red, green, blue, alpha);
		addVertex(vertices, pose, upperLeft, 0.0F, 0.0F, red, green, blue, alpha);
	}

	private static void addTexturedQuad(VertexConsumer vertices, PoseStack.Pose pose, Vec3 first, Vec3 second, Vec3 third, Vec3 fourth, float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4, int red, int green, int blue, int alpha) {
		addVertex(vertices, pose, first, u1, v1, red, green, blue, alpha);
		addVertex(vertices, pose, second, u2, v2, red, green, blue, alpha);
		addVertex(vertices, pose, third, u3, v3, red, green, blue, alpha);
		addVertex(vertices, pose, fourth, u4, v4, red, green, blue, alpha);
	}

	private static void addVertex(VertexConsumer vertices, PoseStack.Pose pose, Vec3 point, float u, float v, int red, int green, int blue, int alpha) {
		vertices.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
				.setColor(red, green, blue, alpha)
				.setUv(u, v)
				.setOverlay(OverlayTexture.NO_OVERLAY)
				.setLight(FULL_BRIGHT_LIGHT)
				.setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	private static Identifier magicCircleTexture(String modeId, boolean charged) {
		return Identifier.fromNamespaceAndPath(Bully.MOD_ID, "textures/effect/magic_circle_" + modeId + (charged ? "_charged.png" : ".png"));
	}

	private static String normalizeMode(String modeId) {
		return switch (modeId.toLowerCase(Locale.ROOT)) {
			case "blue" -> "blue";
			case "green" -> "green";
			default -> "red";
		};
	}

	private static int beamColor(String modeId) {
		return switch (normalizeMode(modeId)) {
			case "blue" -> 0x1582E8;
			case "green" -> 0x0DB53A;
			default -> 0xCC2D25;
		};
	}

	private static Vec3 perpendicularTo(Vec3 forward) {
		Vec3 right = forward.cross(new Vec3(0.0, 1.0, 0.0));
		if (right.lengthSqr() < 0.001) {
			return new Vec3(1.0, 0.0, 0.0);
		}
		return right.normalize();
	}

	private static Vec3 toVec3(Vector3fc vector) {
		return new Vec3(vector.x(), vector.y(), vector.z());
	}

	private record BeamEffect(Vec3 start, Vec3 end, String modeId, long startedAtGameTime) {
	}
}
