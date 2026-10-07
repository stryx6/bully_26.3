package net.kraftattacke.bully.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.kraftattacke.bully.item.MagicWandItem;
import net.kraftattacke.bully.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class MagicWandTargeting {
	private static final float TARGET_PICK_MARGIN = 0.35F;
	private static int highlightedEntityId = -1;
	private static int highlightedOutlineColor = EntityRenderState.NO_OUTLINE;

	private MagicWandTargeting() {
	}

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(MagicWandTargeting::tick);
	}

	public static int outlineColorFor(Entity entity) {
		return entity.getId() == highlightedEntityId ? highlightedOutlineColor : EntityRenderState.NO_OUTLINE;
	}

	private static void tick(Minecraft client) {
		clearHighlight();

		if (client.level == null || client.player == null || !client.player.isUsingItem()) {
			return;
		}

		ItemStack stack = client.player.getUseItem();
		if (stack.isEmpty() || stack.getItem() != ModItems.MAGIC_WAND) {
			return;
		}

		float charge = Math.min(1.0F, client.player.getTicksUsingItem() / (float) MagicWandItem.fullChargeTicks());
		LivingEntity target = findTarget(client.level, client.player, MagicWandItem.getTargetRange(stack, charge));
		if (target == null) {
			return;
		}

		highlightedEntityId = target.getId();
		highlightedOutlineColor = MagicWandItem.getModeOutlineColor(stack);
	}

	private static void clearHighlight() {
		highlightedEntityId = -1;
		highlightedOutlineColor = EntityRenderState.NO_OUTLINE;
	}

	private static LivingEntity findTarget(Level level, Player player, double range) {
		Vec3 start = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		Vec3 end = start.add(look.scale(range));
		BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		Vec3 hitLocation = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
		double entityRange = start.distanceTo(hitLocation);
		AABB searchBox = player.getBoundingBox().expandTowards(look.scale(entityRange)).inflate(1.0);

		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
				level,
				player,
				start,
				hitLocation,
				searchBox,
				entity -> isValidTarget(player, entity),
				TARGET_PICK_MARGIN
		);

		if (entityHit != null && entityHit.getEntity() instanceof LivingEntity livingTarget) {
			return livingTarget;
		}

		return null;
	}

	private static boolean isValidTarget(Player player, Entity entity) {
		return entity != player && entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator() && entity.canBeHitByProjectile();
	}
}
