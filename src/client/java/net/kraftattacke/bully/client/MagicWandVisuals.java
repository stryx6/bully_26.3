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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
	private static final int CIRCLE_ALPHA = 235;
	private static final java.util.Map<java.util.UUID, Float> LAST_CHARGE = new java.util.HashMap<>();
	private static final java.util.Set<java.util.UUID> FULLY_CHARGED = new java.util.HashSet<>();
	private static long lastCueTick = -1;
	private static final int BEAM_LIFETIME_TICKS = 20;
	private static final int CIRCLE_LIFETIME_TICKS = 55;
	private static final int CIRCLE_FADE_TICKS = 30;
	private static final java.util.Map<java.util.UUID, Double> PHASE = new java.util.HashMap<>();
	private static final java.util.Map<java.util.UUID, Double> LAST_ANIM = new java.util.HashMap<>();
	private static final java.util.Map<java.util.UUID, Double> FULL_AT = new java.util.HashMap<>();
	/** Ground circle left behind after the cast: player -> {mode index, radius, game tick it ends}. */
	private static final java.util.Map<java.util.UUID, Object[]> GROUND_GHOSTS = new java.util.HashMap<>();
	private static final double BEAM_RADIUS = 0.09;
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

	/** Per-element look so the three circles do not move alike: degrees per tick for each layer, pulse strength/speed, size. */
	private record ModeStyle(double innerSpin, double outerSpin, double groundScale) {
	}

	private static ModeStyle style(String modeId) {
		return switch (modeId) {
			case "blue" -> new ModeStyle(-2.0, 1.2, 1.1);
			case "green" -> new ModeStyle(3.5, -3.5, 1.0);
			default -> new ModeStyle(6.0, -3.0, 0.95);
		};
	}

	/** Smooth animation clock in ticks, independent of the 20 TPS game time. */
	private static double animTime() {
		return (System.nanoTime() / 1_000_000L % 100_000_000L) / 50.0;
	}

	private static Vec3[] rotate(Vec3 right, Vec3 up, double degrees) {
		double radians = Math.toRadians(degrees);
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		return new Vec3[] {right.scale(cos).add(up.scale(sin)), right.scale(-sin).add(up.scale(cos))};
	}

	/** Four concentric circle layers spinning against each other. Flat ones lie on the ground, top face at center. */
	private static void submitCircleStack(LevelRenderContext context, String ringSuffix, String variant, String modeId, Vec3 center, Vec3 normal, Vec3 right, Vec3 up,
			double radius, double time, int alpha, boolean flat, Vec3 cameraPosition) {
		ModeStyle style = style(modeId);
		String secondRing = ringSuffix.equals("_star") ? "_charged" : "_star";
		Object[][] layers = {
				{ringSuffix, 1.0, style.outerSpin()},
				{secondRing, 0.70, -style.outerSpin() * 0.8},
				{"_runes", 0.40, style.innerSpin() * 0.6},
				{"", 0.30, style.innerSpin()}
		};
		for (int i = 0; i < layers.length; i++) {
			double layerRadius = radius * (double) layers[i][1];
			Vec3[] basis = rotate(right, up, (double) layers[i][2] * time);
			Vec3 layerCenter = center.add(normal.scale(0.012 * i));
			if (flat) {
				layerCenter = layerCenter.subtract(normal.scale(layerRadius * CIRCLE_DEPTH_PIXELS / CIRCLE_TEXTURE_PIXELS));
			}
			Identifier texture = Identifier.fromNamespaceAndPath(Bully.MOD_ID, "textures/effect/magic_circle_" + modeId + variant + layers[i][0] + ".png");
			submitTexturedVolume(context, texture, layerCenter, normal, basis[0], basis[1], layerRadius, alpha, cameraPosition);
		}
	}

	/** Where one hovering circle sits relative to the caster: side/up/back in the aim frame, plus the beam it fires (0 = none).
	 *  ring = big first layer, variant = texture set ("" main, "_sat" satellites, "_gnd" ground/accent). */
	private record Emitter(double side, double up, double back, double radius, double beamRadius, String ring, String variant) {
	}

	/** Every spell gets its own arrangement of circles above and behind the caster (kept high so the third-person camera is not inside them). */
	private static List<Emitter> emitters(String modeId, double time) {
		List<Emitter> list = new ArrayList<>();
		switch (modeId) {
			// Ice: a wide fan of seven lances, with a second tier of smaller circles above.
			case "blue" -> {
				double[] side = {-4.8, -3.8, -2.1, 0.0, 2.1, 3.8, 4.8};
				double[] height = {1.6, 2.2, 2.7, 2.9, 2.7, 2.2, 1.6};
				double[] radius = {0.75, 0.9, 1.0, 1.15, 1.0, 0.9, 0.75};
				double[] beam = {0.04, 0.05, 0.06, 0.08, 0.06, 0.05, 0.04};
				for (int i = 0; i < side.length; i++) {
					list.add(new Emitter(side[i], height[i], 1.5, radius[i], beam[i], "_star", i == 3 ? "" : "_sat"));
				}
				list.add(new Emitter(-1.1, 4.3, 1.9, 0.55, 0.0, "_charged", "_gnd"));
				list.add(new Emitter(1.1, 4.3, 1.9, 0.55, 0.0, "_charged", "_gnd"));
				list.add(new Emitter(-3.0, 3.9, 1.8, 0.50, 0.0, "_charged", "_gnd"));
				list.add(new Emitter(3.0, 3.9, 1.8, 0.50, 0.0, "_charged", "_gnd"));
			}
			// Life: a big rune circle, four small ones orbiting it, four tiny ones orbiting the other way.
			case "green" -> {
				list.add(new Emitter(0.0, 2.1, 1.8, 1.20, 0.08, "_charged", ""));
				for (int i = 0; i < 4; i++) {
					double angle = Math.toRadians(time * 3.0 + 90.0 * i);
					list.add(new Emitter(Math.cos(angle) * 1.8, 2.1 + Math.sin(angle) * 1.8, 1.5, 0.75, 0.04, "_star", "_sat"));
				}
				for (int i = 0; i < 4; i++) {
					double angle = Math.toRadians(-time * 2.0 + 45.0 + 90.0 * i);
					list.add(new Emitter(Math.cos(angle) * 2.8, 2.1 + Math.sin(angle) * 1.6, 1.3, 0.50, 0.0, "_charged", "_gnd"));
				}
			}
			// Fire: three rings behind each other growing toward the back (the beam leaves the rear one), and a crown of six small rings around it.
			default -> {
				list.add(new Emitter(0.0, 1.7, 0.9, 0.80, 0.0, "_charged", ""));
				list.add(new Emitter(0.0, 1.9, 1.8, 1.15, 0.0, "_charged", ""));
				list.add(new Emitter(0.0, 2.2, 2.9, 1.60, 0.15, "_charged", ""));
				for (int i = 0; i < 6; i++) {
					double angle = Math.toRadians(time * 4.0 + 60.0 * i);
					list.add(new Emitter(Math.cos(angle) * 2.2, 2.2 + Math.sin(angle) * 2.2, 2.9, 0.40, 0.0, "_star", "_sat"));
				}
			}
		}
		return list;
	}

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

		BeamEffect effect = new BeamEffect(payload.start(), payload.end(), normalizeMode(payload.modeId()), client.level.getGameTime());
		BEAMS.add(effect);
		spawnMuzzleBlast(client, effect);
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
		float partialTick = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		long gameTime = client.level.getGameTime();
		boolean newTick = gameTime != lastCueTick;
		lastCueTick = gameTime;
		for (AbstractClientPlayer player : client.level.players()) {
			ItemStack stack = player.getUseItem();
			if (!player.isUsingItem() || stack.isEmpty() || stack.getItem() != ModItems.MAGIC_WAND) {
				FULLY_CHARGED.remove(player.getUUID());
				FULL_AT.remove(player.getUUID());
				renderGroundGhost(context, player, partialTick, gameTime, cameraPosition);
				continue;
			}
			GROUND_GHOSTS.remove(player.getUUID());

			float charge = Math.min(1.0F, player.getTicksUsingItem() / (float) MagicWandItem.fullChargeTicks());
			boolean full = charge >= 1.0F;
			LAST_CHARGE.put(player.getUUID(), charge);
			String modeId = normalizeMode(MagicWandItem.getModeId(stack));
			Vec3 feet = player.getPosition(partialTick);
			double groundRadius = (1.2 + charge * 1.6) * style(modeId).groundScale();
			// Spin is doubled when the charge completes and eases back to normal speed over two seconds.
			double now = animTime();
			double sinceFull = full ? now - FULL_AT.computeIfAbsent(player.getUUID(), id -> now) : 1000.0;
			double time = advancePhase(player.getUUID(), 1.0 + Math.max(0.0, 1.0 - sinceFull / 40.0));
			GROUND_GHOSTS.put(player.getUUID(), new Object[] {modeId, groundRadius, 0L});
			submitCircleStack(context, "_star", "_gnd", modeId, feet.add(0.0, 0.03, 0.0), new Vec3(0.0, 1.0, 0.0),
					new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), groundRadius, time, CIRCLE_ALPHA, true, cameraPosition);
			submitEmitterCircles(context, modeId, player.getEyePosition(partialTick), player.getViewVector(partialTick),
					emitterScale(charge), time, CIRCLE_ALPHA, cameraPosition, null, gameTime, 0);

			if (full) {
				// Unmistakable "ready" cue: faster spin, a one-time burst, and sparks rising from the circle.
				ParticleOptions cue = switch (modeId) {
					case "blue" -> ParticleTypes.END_ROD;
					case "green" -> ParticleTypes.HAPPY_VILLAGER;
					default -> ParticleTypes.FLAME;
				};
					boolean ice = modeId.equals("blue");
					int burst = ice ? 16 : 48;
					if (FULLY_CHARGED.add(player.getUUID())) {
						for (int i = 0; i < burst; i++) {
							double angle = Math.PI * 2.0 * i / burst;
						client.level.addParticle(cue, feet.x + Math.cos(angle) * groundRadius, feet.y + 0.1, feet.z + Math.sin(angle) * groundRadius,
								Math.cos(angle) * 0.1, 0.25, Math.sin(angle) * 0.1);
					}
					} else if (newTick && (!ice || gameTime % 3 == 0)) {
						for (int i = 0; i < (ice ? 1 : 4); i++) {
						double angle = Math.random() * Math.PI * 2.0;
						double radius = groundRadius * (0.5 + Math.random() * 0.5);
						client.level.addParticle(cue, feet.x + Math.cos(angle) * radius, feet.y + 0.1, feet.z + Math.sin(angle) * radius, 0.0, 0.12, 0.0);
					}
				}
			}
		}
	}

	/** Spin clock per player that can change speed without the circles jumping: it adds up elapsed time times the current multiplier. */
	private static double advancePhase(java.util.UUID id, double multiplier) {
		double now = animTime();
		Double last = LAST_ANIM.put(id, now);
		double phase = PHASE.getOrDefault(id, 0.0) + (last == null ? 0.0 : Math.min(5.0, now - last)) * multiplier;
		PHASE.put(id, phase);
		return phase;
	}

	/** After the cast the ground circle fades out slowly instead of vanishing at once. */
	private static void renderGroundGhost(LevelRenderContext context, AbstractClientPlayer player, float partialTick, long gameTime, Vec3 cameraPosition) {
		Object[] ghost = GROUND_GHOSTS.get(player.getUUID());
		if (ghost == null) {
			return;
		}
		long end = (long) ghost[2];
		if (end == 0L) {
			end = gameTime + CIRCLE_FADE_TICKS;
			ghost[2] = end;
		}
		double remaining = end - gameTime - partialTick;
		if (remaining <= 0.0) {
			GROUND_GHOSTS.remove(player.getUUID());
			return;
		}
		int alpha = (int) (CIRCLE_ALPHA * Mth.clamp(remaining / CIRCLE_FADE_TICKS, 0.0, 1.0));
		String modeId = (String) ghost[0];
		submitCircleStack(context, "_star", "_gnd", modeId, player.getPosition(partialTick).add(0.0, 0.03, 0.0), new Vec3(0.0, 1.0, 0.0),
				new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), (double) ghost[1], advancePhase(player.getUUID(), 1.0), alpha, true, cameraPosition);
	}

	private static double emitterScale(float charge) {
		return 0.35 + 0.65 * charge;
	}

	private static void renderBeams(LevelRenderContext context, Minecraft client, Vec3 cameraPosition) {
		Camera camera = client.gameRenderer.mainCamera();
		long gameTime = client.level.getGameTime();
		Iterator<BeamEffect> iterator = BEAMS.iterator();
		while (iterator.hasNext()) {
			BeamEffect beam = iterator.next();
			int age = (int) (gameTime - beam.startedAtGameTime());
			if (age > CIRCLE_LIFETIME_TICKS) {
				iterator.remove();
				continue;
			}

			boolean beamLive = age <= BEAM_LIFETIME_TICKS;
			float fade = 1.0F - Math.min(age, BEAM_LIFETIME_TICKS) / (float) BEAM_LIFETIME_TICKS;
			int alpha = Mth.clamp((int) (230 * fade), 40, 230);
			AbstractClientPlayer shooter = shooterNear(client, beam.start());
			boolean firstPersonShot = shooter != null && shooter == client.player && client.options.getCameraType() == CameraType.FIRST_PERSON;
			if (beamLive && (shooter == null || firstPersonShot)) {
				// First person: the beam leaves the wand crystal. Everyone else sees it leave the circles.
				Vec3 start = firstPersonShot ? firstPersonCrystalPosition(client, camera, cameraPosition) : beam.start();
				submitBeam(context, beam, start, cameraPosition, gameTime, alpha, BEAM_RADIUS);
			}
			if (shooter != null) {
				submitEmitters(context, beam, shooter, cameraPosition, gameTime, age, alpha, !firstPersonShot && beamLive);
			}
		}
	}

	private static AbstractClientPlayer shooterNear(Minecraft client, Vec3 start) {
		AbstractClientPlayer best = null;
		double bestDistance = 16.0;
		for (AbstractClientPlayer player : client.level.players()) {
			double distance = player.getEyePosition().distanceToSqr(start);
			if (distance < bestDistance) {
				bestDistance = distance;
				best = player;
			}
		}
		return best;
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
		RenderType renderType = RenderTypes.entityTranslucent(texture);

		context.submitNodeCollector().submitCustomGeometry(new PoseStack(), renderType, (pose, vertices) -> {
			addQuad(vertices, pose, frontLowerLeft, frontLowerRight, frontUpperRight, frontUpperLeft, 255, 255, 255, alpha);
			addQuad(vertices, pose, backLowerRight, backLowerLeft, backUpperLeft, backUpperRight, 255, 255, 255, alpha);
			addTexturedQuad(vertices, pose, frontLowerLeft, backLowerLeft, backLowerRight, frontLowerRight, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 255, 255, 255, alpha);
			addTexturedQuad(vertices, pose, frontUpperRight, backUpperRight, backUpperLeft, frontUpperLeft, 1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 255, 255, 255, alpha);
			addTexturedQuad(vertices, pose, frontUpperLeft, backUpperLeft, backLowerLeft, frontLowerLeft, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 255, 255, 255, alpha);
			addTexturedQuad(vertices, pose, frontLowerRight, backLowerRight, backUpperRight, frontUpperRight, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, 1.0F, 0.0F, 255, 255, 255, alpha);
		});
	}

	private static void submitBeam(LevelRenderContext context, BeamEffect beam, Vec3 start, Vec3 cameraPosition, long gameTime, int alpha, double radius) {
		Vec3 end = beam.end();
		Vec3 axis = end.subtract(start);
		double length = axis.length();
		if (length < 0.01) {
			return;
		}

		Vec3 forward = axis.normalize();
		Vec3 right = perpendicularTo(forward).scale(radius);
		Vec3 up = forward.cross(right).normalize().scale(radius);
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

	/**
	 * Where the wand's floating crystal is on screen in first person. Computed from the wand model's
	 * firstperson_righthand display transform (rotation [0,90,-28], translation [13.75,-8.25,-12.75]) and the crystal cube
	 * (center 1/23.5/1) plus vanilla's arm offset: about 0.98 right, 0.03 up, 1.59 ahead of the camera.
	 * The hand is drawn with a fixed 70 degree FOV, so the offset is rescaled to the player's real FOV.
	 */
	private static Vec3 firstPersonCrystalPosition(Minecraft client, Camera camera, Vec3 cameraPosition) {
		double scale = Math.tan(Math.toRadians(client.options.fov().get() / 2.0)) / Math.tan(Math.toRadians(35.0));
		Vec3 right = toVec3(camera.leftVector()).scale(-1.0);
		return cameraPosition
				.add(right.scale(0.98 * scale))
				.add(toVec3(camera.upVector()).scale(0.03 * scale))
				.add(toVec3(camera.forwardVector()).scale(1.59));
	}

	/** After the cast the circles keep their charge size, fire the beams, then fade. */
	private static void submitEmitters(LevelRenderContext context, BeamEffect beam, AbstractClientPlayer shooter, Vec3 cameraPosition, long gameTime, int age, int beamAlpha, boolean withBeams) {
		Vec3 eye = shooter.getEyePosition();
		Vec3 aim = beam.end().subtract(eye);
		if (aim.length() < 0.5) {
			return;
		}
		int circleAlpha = (int) (CIRCLE_ALPHA * Mth.clamp((CIRCLE_LIFETIME_TICKS - age) / (double) CIRCLE_FADE_TICKS, 0.0, 1.0));
		if (circleAlpha <= 0) {
			return;
		}
		double scale = emitterScale(LAST_CHARGE.getOrDefault(shooter.getUUID(), 1.0F));
		submitEmitterCircles(context, beam.modeId(), eye, aim.normalize(), scale, advancePhase(shooter.getUUID(), 1.0), circleAlpha, cameraPosition, withBeams ? beam : null, gameTime, beamAlpha);
	}

	/** The hovering circles behind the caster; with a beam given they also fire it. */
	private static void submitEmitterCircles(LevelRenderContext context, String modeId, Vec3 eye, Vec3 dir, double scale, double time, int alpha,
			Vec3 cameraPosition, BeamEffect beam, long gameTime, int beamAlpha) {
		Vec3 right = perpendicularTo(dir);
		Vec3 up = right.cross(dir).normalize();
		for (Emitter emitter : emitters(modeId, time)) {
			Vec3 center = eye.add(right.scale(emitter.side())).add(up.scale(emitter.up())).subtract(dir.scale(emitter.back()));
			// Circles around the third-person camera would hide everything: fade them out when close.
			int visible = (int) (alpha * Mth.clamp((center.distanceTo(cameraPosition) - 1.5) / 2.0, 0.0, 1.0));
			if (visible > 3) {
				submitCircleStack(context, emitter.ring(), emitter.variant(), modeId, center, dir, right, up, emitter.radius() * scale, time, visible, false, cameraPosition);
			}
			if (beam != null && emitter.beamRadius() > 0.0) {
				submitBeam(context, beam, center, cameraPosition, gameTime, beamAlpha, emitter.beamRadius());
			}
		}
	}

	/** Muzzle blast through every circle at the moment of the cast; a different one per spell. */
	private static void spawnMuzzleBlast(Minecraft client, BeamEffect beam) {
		AbstractClientPlayer shooter = shooterNear(client, beam.start());
		if (shooter == null) {
			return;
		}
		Vec3 eye = shooter.getEyePosition();
		Vec3 aim = beam.end().subtract(eye);
		if (aim.length() < 0.5) {
			return;
		}
		Vec3 dir = aim.normalize();
		Vec3 right = perpendicularTo(dir);
		Vec3 up = right.cross(dir).normalize();
		ParticleOptions main = switch (beam.modeId()) {
			case "blue" -> ParticleTypes.SNOWFLAKE;
			case "green" -> ParticleTypes.HAPPY_VILLAGER;
			default -> ParticleTypes.FLAME;
		};
		ParticleOptions accent = switch (beam.modeId()) {
			case "blue" -> ParticleTypes.END_ROD;
			case "green" -> ParticleTypes.COMPOSTER;
			default -> ParticleTypes.LARGE_SMOKE;
		};
		for (Emitter emitter : emitters(beam.modeId(), animTime())) {
			Vec3 center = eye.add(right.scale(emitter.side())).add(up.scale(emitter.up())).subtract(dir.scale(emitter.back()));
			for (int i = 0; i < 28; i++) {
				double angle = Math.PI * 2.0 * i / 28.0;
				Vec3 radial = right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
				Vec3 position = center.add(radial.scale(emitter.radius() * 0.85));
				Vec3 velocity = dir.scale(0.45).add(radial.scale(0.12));
				client.level.addParticle(i % 3 == 0 ? accent : main, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
			}
		}
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
