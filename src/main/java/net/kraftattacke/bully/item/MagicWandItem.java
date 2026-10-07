package net.kraftattacke.bully.item;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import net.kraftattacke.bully.Bully;
import net.kraftattacke.bully.ModParticles;
import net.kraftattacke.bully.network.MagicWandBeamPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MagicWandItem extends Item {
	private static final String MODE_TAG = "BullyMagicWandMode";
	private static final int TICKS_PER_SECOND = 20;
	private static final int USE_DURATION_TICKS = 72000;
	private static final int MIN_CHARGE_TICKS = 10;
	private static final int FULL_CHARGE_TICKS = 200;
	private static final int CHARGE_STEP_TICKS = 20;
	private static final int CHARGE_SOUND_INTERVAL_TICKS = 16;
	private static final ResourceKey<DamageType> EMBER_LANCE_DAMAGE = damageType("magic_wand_ember_lance");
	private static final ResourceKey<DamageType> FROST_BURST_DAMAGE = damageType("magic_wand_frost_burst");
	private static final ResourceKey<DamageType> LIFE_BLOOM_DAMAGE = damageType("magic_wand_life_bloom");

	// Spell tuning: change damage, range, radius, healing, effects and cooldowns here.
	private static final double EMBER_LANCE_BASE_RANGE = 40.0;
	private static final double EMBER_LANCE_CHARGED_RANGE_BONUS = 20.0;
	private static final float EMBER_LANCE_BASE_DAMAGE = 10.0F;
	private static final float EMBER_LANCE_CHARGED_DAMAGE_BONUS = 50.0F;
	private static final float EMBER_LANCE_BASE_FIRE_SECONDS = 5.0F;
	private static final float EMBER_LANCE_CHARGED_FIRE_SECONDS_BONUS = 5.0F;
	private static final int EMBER_LANCE_COOLDOWN_TICKS = 10;
	private static final int EMBER_LANCE_SHIELD_BURN_BASE_DAMAGE = 10;
	private static final int EMBER_LANCE_SHIELD_BURN_CHARGED_DAMAGE_BONUS = 14;
	private static final int EMBER_LANCE_SHIELD_STUN_BASE_TICKS = 50;
	private static final int EMBER_LANCE_SHIELD_STUN_CHARGED_TICKS_BONUS = 50;

	private static final double FROST_BURST_BASE_RANGE = 30.0;
	private static final double FROST_BURST_CHARGED_RANGE_BONUS = 15.0;
	private static final double FROST_BURST_BASE_RADIUS = 3.0;
	private static final double FROST_BURST_CHARGED_RADIUS_BONUS = 3.0;
	private static final float FROST_BURST_BASE_DAMAGE = 6.0F;
	private static final float FROST_BURST_CHARGED_DAMAGE_BONUS = 6.0F;
	private static final int FROST_BURST_COOLDOWN_TICKS = 30;

	private static final double LIFE_BLOOM_BASE_RANGE = 20.0;
	private static final double LIFE_BLOOM_CHARGED_RANGE_BONUS = 10.0;
	private static final double LIFE_BLOOM_BASE_RADIUS = 4.0;
	private static final double LIFE_BLOOM_CHARGED_RADIUS_BONUS = 4.0;
	private static final float LIFE_BLOOM_SELF_HEAL = 3.0F;
	private static final float LIFE_BLOOM_CHARGED_SELF_HEAL_BONUS = 5.0F;
	private static final float LIFE_BLOOM_ALLY_HEAL = 3.0F;
	private static final float LIFE_BLOOM_CHARGED_ALLY_HEAL_BONUS = 3.0F;
	private static final float LIFE_BLOOM_HOSTILE_DAMAGE = 3.0F;
	private static final float LIFE_BLOOM_CHARGED_HOSTILE_DAMAGE_BONUS = 3.0F;
	private static final int LIFE_BLOOM_COOLDOWN_TICKS = 50;

	// Effect duration tuning in seconds. Bonuses scale with charge; negative effects use 20-tick steps.
	private static final float FROST_BURST_SLOWNESS_SECONDS = 5.0F;
	private static final float FROST_BURST_CHARGED_SLOWNESS_SECONDS_BONUS = 25.0F;
	private static final float LIFE_BLOOM_SATURATION_SECONDS = 5F;
	private static final float LIFE_BLOOM_CHARGED_SATURATION_SECONDS_BONUS = 5F;
	private static final float LIFE_BLOOM_REGEN_SECONDS = 5F;
	private static final float LIFE_BLOOM_CHARGED_REGEN_SECONDS_BONUS = 5F;
	private static final float LIFE_BLOOM_ABSORPTION_SECONDS = 6.0F;
	private static final float LIFE_BLOOM_CHARGED_ABSORPTION_SECONDS_BONUS = 4.0F;
	private static final float LIFE_BLOOM_POISON_SECONDS = 5.0F;
	private static final float LIFE_BLOOM_CHARGED_POISON_SECONDS_BONUS = 25.0F;
	private static final float LIFE_BLOOM_WITHER_SECONDS = 5.0F;
	private static final float LIFE_BLOOM_CHARGED_WITHER_SECONDS_BONUS = 25.0F;
	private static final float LIFE_BLOOM_DARKNESS_SECONDS = 5.0F;
	private static final float LIFE_BLOOM_CHARGED_DARKNESS_SECONDS_BONUS = 25.0F;
	private static final float LIFE_BLOOM_WEAKNESS_SECONDS = 5.0F;
	private static final float LIFE_BLOOM_CHARGED_WEAKNESS_SECONDS_BONUS = 25.0F;

	public MagicWandItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (player.isSecondaryUseActive()) {
			SpellMode nextMode = getMode(stack).next();
			setMode(stack, nextMode);
			player.getCooldowns().addCooldown(stack, 6);

			if (!level.isClientSide()) {
				player.sendOverlayMessage(Component.translatable(nextMode.translationKey()).withStyle(nextMode.formatting));
				playModeSwitchSound(level, player, nextMode);
			}

			return InteractionResult.SUCCESS;
		}

		player.startUsingItem(hand);
		if (!level.isClientSide()) {
			playChargeStartSound(level, player, getMode(stack));
		}
		return InteractionResult.CONSUME;
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
		if (!(livingEntity instanceof Player player)) {
			return false;
		}

		int chargeTicks = getUseDuration(stack, livingEntity) - timeLeft;
		if (chargeTicks < MIN_CHARGE_TICKS) {
			if (!level.isClientSide()) {
				playFizzleSound(level, player);
			}
			return false;
		}

		if (!(level instanceof ServerLevel serverLevel)) {
			return true;
		}

		SpellMode mode = getMode(stack);
		InteractionHand hand = livingEntity.getUsedItemHand();
		float charge = Math.min(1.0F, chargeTicks / (float) FULL_CHARGE_TICKS);
		// Integer division keeps the selected values constant between 20-tick boundaries.
		float steppedCharge = Math.max(0, Math.min(chargeTicks, FULL_CHARGE_TICKS) / CHARGE_STEP_TICKS)
				/ (float) (FULL_CHARGE_TICKS / CHARGE_STEP_TICKS);
		boolean cast = switch (mode) {
			case RED -> castEmberLance(serverLevel, player, hand, charge, steppedCharge);
			case BLUE -> castFrostBurst(serverLevel, player, hand, charge, steppedCharge);
			case GREEN -> castLifeBloom(serverLevel, player, hand, charge, steppedCharge);
		};

		if (cast) {
			stack.hurtAndBreak(1, player, livingEntity.getUsedItemHand());
			player.getCooldowns().addCooldown(stack, mode.cooldownTicks);
		}

		return cast;
	}

	@Override
	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseTicks) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		int chargeTicks = getUseDuration(stack, livingEntity) - remainingUseTicks;
		if (chargeTicks > 0 && chargeTicks % CHARGE_SOUND_INTERVAL_TICKS == 0) {
			float charge = Math.min(1.0F, chargeTicks / (float) FULL_CHARGE_TICKS);
			playChargePulseSound(serverLevel, livingEntity, getMode(stack), charge);
		}
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity livingEntity) {
		return USE_DURATION_TICKS;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.BOW;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
		SpellMode mode = getMode(stack);
		tooltip.accept(Component.translatable("tooltip.bully.magic_wand.mode", Component.translatable(mode.translationKey()).withStyle(mode.formatting)).withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("tooltip.bully.magic_wand.use").withStyle(ChatFormatting.DARK_GRAY));
	}

	public static int fullChargeTicks() {
		return FULL_CHARGE_TICKS;
	}

	public static String getModeId(ItemStack stack) {
		return getMode(stack).id;
	}

	public static double getTargetRange(ItemStack stack, float charge) {
		return getMode(stack).targetRange(clampCharge(charge));
	}

	public static int getModeOutlineColor(ItemStack stack) {
		return 0xFF000000 | getMode(stack).particleColor;
	}

	private static boolean castEmberLance(ServerLevel level, Player player, InteractionHand hand, float charge, float steppedCharge) {
		double range = EMBER_LANCE_BASE_RANGE + charge * EMBER_LANCE_CHARGED_RANGE_BONUS;
		SpellTarget target = findTarget(level, player, range);
		Vec3 origin = getWandParticleOrigin(player, hand);
		spawnBeam(level, origin, target.location(), SpellMode.RED);
		spawnBurst(level, target.location(), SpellMode.RED, 24 + (int) (charge * 24), 0.16 + charge * 0.14);
		playCastSound(level, origin, target.location(), SpellMode.RED, charge);

		if (target.entity() == null) {
			return true;
		}

		burnBlockingShield(level, target.entity(), charge);
		DamageSource source = createDamageSource(level, SpellMode.RED, player);
		target.entity().hurtServer(level, source, EMBER_LANCE_BASE_DAMAGE + steppedCharge * EMBER_LANCE_CHARGED_DAMAGE_BONUS);
		target.entity().igniteForSeconds(EMBER_LANCE_BASE_FIRE_SECONDS + charge * EMBER_LANCE_CHARGED_FIRE_SECONDS_BONUS);
		return true;
	}

	private static boolean castFrostBurst(ServerLevel level, Player player, InteractionHand hand, float charge, float steppedCharge) {
		SpellTarget target = findTarget(level, player, FROST_BURST_BASE_RANGE + charge * FROST_BURST_CHARGED_RANGE_BONUS);
		Vec3 center = target.location();
		double radius = FROST_BURST_BASE_RADIUS + charge * FROST_BURST_CHARGED_RADIUS_BONUS;
		List<LivingEntity> targets = getLivingEntitiesAround(level, player, center, radius);

		Vec3 origin = getWandParticleOrigin(player, hand);
		spawnBeam(level, origin, center, SpellMode.BLUE);
		spawnBurst(level, center, SpellMode.BLUE, 54 + (int) (charge * 42), radius * 0.18);
		spawnImpactRing(level, center, SpellMode.BLUE, radius, 42);
		level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 0.25, center.z, 46 + (int) (charge * 36), radius * 0.38, 0.45, radius * 0.38, 0.025);
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + 0.25, center.z, 12 + (int) (charge * 14), radius * 0.18, 0.18, radius * 0.18, 0.02);
		playCastSound(level, origin, center, SpellMode.BLUE, charge);

		DamageSource source = createDamageSource(level, SpellMode.BLUE, player);
		for (LivingEntity livingTarget : targets) {
			livingTarget.hurtServer(level, source, FROST_BURST_BASE_DAMAGE + charge * FROST_BURST_CHARGED_DAMAGE_BONUS);
			livingTarget.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, effectDurationTicks(FROST_BURST_SLOWNESS_SECONDS, FROST_BURST_CHARGED_SLOWNESS_SECONDS_BONUS, steppedCharge), 4, false, true, true));
			Vec3 push = livingTarget.position().subtract(center);
			if (push.lengthSqr() < 0.01) {
				push = player.getLookAngle();
			}
			livingTarget.setDeltaMovement(livingTarget.getDeltaMovement().add(push.normalize().scale(0.35 + charge * 0.45)).add(0.0, 0.12 + charge * 0.08, 0.0));
		}

		return true;
	}

	private static boolean castLifeBloom(ServerLevel level, Player player, InteractionHand hand, float charge, float steppedCharge) {
		SpellTarget target = findTarget(level, player, LIFE_BLOOM_BASE_RANGE + charge * LIFE_BLOOM_CHARGED_RANGE_BONUS);
		Vec3 center = target.location();
		double radius = LIFE_BLOOM_BASE_RADIUS + charge * LIFE_BLOOM_CHARGED_RADIUS_BONUS;
		List<LivingEntity> targets = getLifeBloomEntitiesAround(level, player, center, radius);

		Vec3 origin = getWandParticleOrigin(player, hand);
		spawnBeam(level, origin, center, SpellMode.GREEN);
		spawnLifeBloomField(level, center, radius, charge);
		boolean hitLivingTarget = !targets.isEmpty();
		applyLifeBloomHealing(level, player, LIFE_BLOOM_SELF_HEAL + charge * LIFE_BLOOM_CHARGED_SELF_HEAL_BONUS, charge, hitLivingTarget);
		playCastSound(level, origin, center, SpellMode.GREEN, charge);

		DamageSource source = createDamageSource(level, SpellMode.GREEN, player);
		for (LivingEntity livingTarget : targets) {
			if (isLifeBloomFriendly(player, livingTarget)) {
				applyLifeBloomHealing(level, livingTarget, LIFE_BLOOM_ALLY_HEAL + charge * LIFE_BLOOM_CHARGED_ALLY_HEAL_BONUS, charge, true);
				continue;
			}

			livingTarget.hurtServer(level, source, LIFE_BLOOM_HOSTILE_DAMAGE + charge * LIFE_BLOOM_CHARGED_HOSTILE_DAMAGE_BONUS);
			livingTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, effectDurationTicks(LIFE_BLOOM_DARKNESS_SECONDS, LIFE_BLOOM_CHARGED_DARKNESS_SECONDS_BONUS, steppedCharge), charge > 0.75F ? 1 : 0, false, true, true));
			livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, effectDurationTicks(LIFE_BLOOM_WITHER_SECONDS, LIFE_BLOOM_CHARGED_WITHER_SECONDS_BONUS, steppedCharge), charge > 0.75F ? 1 : 0, false, true, true));
			livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, effectDurationTicks(LIFE_BLOOM_POISON_SECONDS, LIFE_BLOOM_CHARGED_POISON_SECONDS_BONUS, steppedCharge), charge > 0.75F ? 1 : 0, false, true, true));
			livingTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, effectDurationTicks(LIFE_BLOOM_WEAKNESS_SECONDS, LIFE_BLOOM_CHARGED_WEAKNESS_SECONDS_BONUS, steppedCharge), 0, false, true, true));
			spawnLifeBloomTargetParticles(level, livingTarget, false, charge);
		}

		return true;
	}

	private static ResourceKey<DamageType> damageType(String name) {
		return ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(Bully.MOD_ID, name));
	}

	private static DamageSource createDamageSource(ServerLevel level, SpellMode mode, Player player) {
		return level.damageSources().source(mode.damageType, player);
	}

	private static void playModeSwitchSound(Level level, LivingEntity caster, SpellMode mode) {
		Vec3 origin = soundOrigin(caster);
		playSound(level, origin, SoundEvents.AMETHYST_BLOCK_CHIME, 0.65F, mode.pitch + 0.2F);
		playSound(level, origin, SoundEvents.ENCHANTMENT_TABLE_USE, 0.25F, 0.85F + mode.ordinal() * 0.12F);
	}

	private static void playChargeStartSound(Level level, LivingEntity caster, SpellMode mode) {
		Vec3 origin = soundOrigin(caster);
		playSound(level, origin, SoundEvents.AMETHYST_BLOCK_RESONATE, 0.32F, 0.75F + mode.pitch * 0.25F);
		playSound(level, origin, SoundEvents.BREEZE_INHALE, 0.20F, 1.25F);
	}

	private static void playChargePulseSound(Level level, LivingEntity caster, SpellMode mode, float charge) {
		Vec3 origin = soundOrigin(caster);
		playSound(level, origin, SoundEvents.AMETHYST_BLOCK_CHIME, 0.14F + charge * 0.20F, mode.pitch + charge * 0.45F);
		if (charge >= 1.0F) {
			playSound(level, origin, SoundEvents.BEACON_POWER_SELECT, 0.24F, 1.45F);
		} else if (charge > 0.55F) {
			playSound(level, origin, SoundEvents.BREEZE_WHIRL, 0.10F + charge * 0.08F, 1.25F + charge * 0.2F);
		}
	}

	private static void playFizzleSound(Level level, LivingEntity caster) {
		Vec3 origin = soundOrigin(caster);
		playSound(level, origin, SoundEvents.AMETHYST_BLOCK_HIT, 0.45F, 0.55F);
		playSound(level, origin, SoundEvents.FIRE_EXTINGUISH, 0.24F, 1.35F);
	}

	private static void playCastSound(ServerLevel level, Vec3 origin, Vec3 impact, SpellMode mode, float charge) {
		playSound(level, origin, SoundEvents.ENCHANTMENT_TABLE_USE, 0.26F + charge * 0.10F, 0.95F + charge * 0.30F);

		switch (mode) {
			case RED -> {
				playSound(level, origin, SoundEvents.FIRECHARGE_USE, 0.46F + charge * 0.18F, 1.05F + charge * 0.18F);
				playSound(level, impact, SoundEvents.BLAZE_SHOOT, 0.88F + charge * 0.22F, 0.90F + charge * 0.35F);
				playSound(level, impact, SoundEvents.DRAGON_FIREBALL_EXPLODE, 0.30F + charge * 0.22F, 1.45F + charge * 0.18F);
			}
			case BLUE -> {
				playSound(level, origin, SoundEvents.BREEZE_CHARGE, 0.34F + charge * 0.12F, 1.20F + charge * 0.15F);
				playSound(level, impact, SoundEvents.BREEZE_SHOOT, 0.62F + charge * 0.20F, 1.05F + charge * 0.20F);
				playSound(level, impact, SoundEvents.SNOW_GOLEM_SHOOT, 0.78F + charge * 0.12F, 0.68F + charge * 0.24F);
				playSound(level, impact, SoundEvents.GLASS_BREAK, 0.18F + charge * 0.16F, 1.55F + charge * 0.20F);
			}
			case GREEN -> {
				playSound(level, impact, SoundEvents.EVOKER_CAST_SPELL, 0.72F + charge * 0.16F, 1.00F + charge * 0.25F);
				playSound(level, impact, SoundEvents.ALLAY_ITEM_GIVEN, 0.42F + charge * 0.12F, 1.20F + charge * 0.18F);
				playSound(level, impact, SoundEvents.AMETHYST_BLOCK_CHIME, 0.46F + charge * 0.14F, 1.35F + charge * 0.24F);
			}
		}
	}

	private static void playSound(Level level, Vec3 location, SoundEvent sound, float volume, float pitch) {
		level.playSound(null, location.x, location.y, location.z, sound, SoundSource.PLAYERS, volume, pitch);
	}

	private static Vec3 soundOrigin(LivingEntity entity) {
		return entity.getEyePosition().add(0.0, -0.2, 0.0);
	}

	private static SpellTarget findTarget(ServerLevel level, Player player, double range) {
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
				0.35F
		);

		if (entityHit != null && entityHit.getEntity() instanceof LivingEntity livingTarget) {
			return new SpellTarget(entityHit.getLocation(), livingTarget);
		}

		return new SpellTarget(hitLocation, null);
	}

	private static List<LivingEntity> getLivingEntitiesAround(ServerLevel level, Player player, Vec3 center, double radius) {
		AABB area = AABB.ofSize(center, radius * 2.0, radius * 2.0, radius * 2.0);
		return level.getEntities(
				EntityTypeTest.forClass(LivingEntity.class),
				area,
				entity -> isValidTarget(player, entity) && entity.distanceToSqr(center) <= radius * radius
		);
	}

	private static List<LivingEntity> getLifeBloomEntitiesAround(ServerLevel level, Player player, Vec3 center, double radius) {
		AABB area = AABB.ofSize(center, radius * 2.0, radius * 2.0, radius * 2.0);
		return level.getEntities(
				EntityTypeTest.forClass(LivingEntity.class),
				area,
				entity -> entity != player && entity.isAlive() && !entity.isSpectator() && entity.distanceToSqr(center) <= radius * radius
		);
	}

	private static boolean isValidTarget(Player player, Entity entity) {
		return entity != player && entity instanceof LivingEntity && entity.isAlive() && !entity.isSpectator() && entity.canBeHitByProjectile();
	}

	private static boolean isLifeBloomFriendly(Player player, LivingEntity entity) {
		return !(entity instanceof Player) && entity.isAlliedTo(player);
	}

	private static int effectDurationTicks(float baseSeconds, float chargedBonusSeconds, float charge) {
		return Math.max(1, Math.round((baseSeconds + chargedBonusSeconds * charge) * TICKS_PER_SECOND));
	}

	private static float clampCharge(float charge) {
		return Math.max(0.0F, Math.min(1.0F, charge));
	}

	private static void applyLifeBloomHealing(ServerLevel level, LivingEntity target, float healAmount, float charge, boolean grantStatusEffects) {
		if (grantStatusEffects) {
			cleanseLifeBloomTarget(target);
		}
		target.heal(healAmount);
		if (grantStatusEffects) {
			target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, effectDurationTicks(LIFE_BLOOM_REGEN_SECONDS, LIFE_BLOOM_CHARGED_REGEN_SECONDS_BONUS, charge), charge > 0.8F ? 1 : 0, false, true, true));
			target.addEffect(new MobEffectInstance(MobEffects.SATURATION, effectDurationTicks(LIFE_BLOOM_SATURATION_SECONDS, LIFE_BLOOM_CHARGED_SATURATION_SECONDS_BONUS, charge), charge > 0.8F ? 1 : 0, false, true, true));
			if (charge > 0.55F) {
				target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, effectDurationTicks(LIFE_BLOOM_ABSORPTION_SECONDS, LIFE_BLOOM_CHARGED_ABSORPTION_SECONDS_BONUS, charge), 0, false, true, true));
			}
		}
		spawnLifeBloomTargetParticles(level, target, true, charge);
	}

	private static void burnBlockingShield(ServerLevel level, LivingEntity target, float charge) {
		ItemStack blockingItem = target.getItemBlockingWith();
		if (blockingItem == null || blockingItem.isEmpty()) {
			return;
		}

		int burnDamage = EMBER_LANCE_SHIELD_BURN_BASE_DAMAGE + Math.round(EMBER_LANCE_SHIELD_BURN_CHARGED_DAMAGE_BONUS * charge);
		int stunTicks = EMBER_LANCE_SHIELD_STUN_BASE_TICKS + Math.round(EMBER_LANCE_SHIELD_STUN_CHARGED_TICKS_BONUS * charge);
		if (target instanceof Player shieldUser) {
			shieldUser.getCooldowns().addCooldown(blockingItem, stunTicks);
		}
		blockingItem.hurtAndBreak(burnDamage, target, target.getUsedItemHand());
		target.stopUsingItem();

		target.igniteForSeconds(2.0F + charge * 3.0F);
		spawnShieldBurnParticles(level, target, charge);
		playSound(level, target.getEyePosition(), SoundEvents.FIRECHARGE_USE, 0.7F + charge * 0.25F, 0.7F + charge * 0.25F);
	}

	private static void spawnShieldBurnParticles(ServerLevel level, LivingEntity target, float charge) {
		Vec3 center = target.getEyePosition().add(0.0, -0.2, 0.0);
		level.sendParticles(ModParticles.BURNING_SHIELD, center.x, center.y, center.z, 24 + (int) (charge * 20), 0.34, 0.42, 0.34, 0.05);
		level.sendParticles(SpellMode.RED.secondaryDust(0.95F), center.x, center.y, center.z, 8 + (int) (charge * 8), 0.28, 0.34, 0.28, 0.025);
	}

	private static void cleanseLifeBloomTarget(LivingEntity target) {
		target.removeEffect(MobEffects.POISON);
		target.removeEffect(MobEffects.WITHER);
		target.removeEffect(MobEffects.WEAKNESS);
		target.removeEffect(MobEffects.SLOWNESS);
	}

	public static Vec3 getWandParticleOrigin(LivingEntity caster, InteractionHand hand) {
		Vec3 forward = caster.getLookAngle().normalize();
		Vec3 right = perpendicularTo(forward);
		Vec3 up = right.cross(forward).normalize();
		double handSide = hand == InteractionHand.OFF_HAND ? -0.48 : 0.48;
		return caster.getEyePosition()
				.add(forward.scale(0.55))
				.add(right.scale(handSide))
				.add(up.scale(-0.38));
	}

	private static void spawnBeam(ServerLevel level, Vec3 start, Vec3 end, SpellMode mode) {
		double distance = start.distanceTo(end);
		if (distance < 0.01) {
			return;
		}

		MagicWandBeamPayload.sendToTracking(level, start, end, mode.id);
	}

	private static void spawnBurst(ServerLevel level, Vec3 center, SpellMode mode, int count, double speed) {
		level.sendParticles(mode.dust(1.35F), center.x, center.y, center.z, count, 0.32, 0.32, 0.32, speed);
		level.sendParticles(mode.secondaryDust(0.95F), center.x, center.y, center.z, Math.max(10, count / 3), 0.46, 0.34, 0.46, speed * 0.6);
		level.sendParticles(mode.burstParticle, center.x, center.y, center.z, Math.max(8, count / 5), 0.42, 0.34, 0.42, speed * 0.35);
		level.sendParticles(mode.accentParticle, center.x, center.y, center.z, Math.max(4, count / 10), 0.24, 0.24, 0.24, speed * 0.18);
		spawnImpactRing(level, center, mode, 0.6 + speed * 2.4, 22);
	}

	private static void spawnImpactRing(ServerLevel level, Vec3 center, SpellMode mode, double radius, int points) {
		for (int i = 0; i < points; i++) {
			double angle = Math.PI * 2.0 * i / points;
			Vec3 point = center.add(Math.cos(angle) * radius, 0.08, Math.sin(angle) * radius);
			level.sendParticles(i % 2 == 0 ? mode.dust(1.0F) : mode.secondaryDust(0.8F), point.x, point.y, point.z, 1, 0.01, 0.01, 0.01, 0.0);
		}
	}

	private static void spawnLifeBloomField(ServerLevel level, Vec3 center, double radius, float charge) {
		spawnBurst(level, center, SpellMode.GREEN, 56 + (int) (charge * 42), radius * 0.14);
		spawnImpactRing(level, center, SpellMode.GREEN, radius * 0.65, 34);
		spawnImpactRing(level, center, SpellMode.GREEN, radius, 46);
		level.sendParticles(ParticleTypes.COMPOSTER, center.x, center.y + 0.2, center.z, 26 + (int) (charge * 22), radius * 0.3, 0.35, radius * 0.3, 0.02);
		level.sendParticles(ParticleTypes.HAPPY_VILLAGER, center.x, center.y + 0.45, center.z, 18 + (int) (charge * 20), radius * 0.2, 0.45, radius * 0.2, 0.02);
	}

	private static void spawnLifeBloomTargetParticles(ServerLevel level, LivingEntity target, boolean healing, float charge) {
		Vec3 center = target.getEyePosition().add(0.0, -0.25, 0.0);
		if (healing) {
			level.sendParticles(ParticleTypes.HEART, center.x, center.y + 0.25, center.z, 2 + (int) (charge * 3), 0.28, 0.26, 0.28, 0.02);
			level.sendParticles(ParticleTypes.HAPPY_VILLAGER, center.x, center.y, center.z, 8 + (int) (charge * 8), 0.32, 0.28, 0.32, 0.035);
			level.sendParticles(SpellMode.GREEN.secondaryDust(0.8F), center.x, center.y, center.z, 10 + (int) (charge * 10), 0.3, 0.32, 0.3, 0.02);
			return;
		}

		level.sendParticles(ParticleTypes.WITCH, center.x, center.y, center.z, 12 + (int) (charge * 10), 0.35, 0.32, 0.35, 0.04);
		level.sendParticles(SpellMode.GREEN.dust(0.75F), center.x, center.y, center.z, 8 + (int) (charge * 8), 0.24, 0.25, 0.24, 0.02);
	}

	private static Vec3 perpendicularTo(Vec3 forward) {
		Vec3 right = forward.cross(new Vec3(0.0, 1.0, 0.0));
		if (right.lengthSqr() < 0.001) {
			return new Vec3(1.0, 0.0, 0.0);
		}
		return right.normalize();
	}

	private static SpellMode getMode(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if (data == null || data.isEmpty()) {
			return SpellMode.RED;
		}

		CompoundTag tag = data.copyTag();
		return SpellMode.byId(tag.getStringOr(MODE_TAG, SpellMode.RED.id));
	}

	private static void setMode(ItemStack stack, SpellMode mode) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(MODE_TAG, mode.id));
		stack.set(DataComponents.CUSTOM_MODEL_DATA, mode.customModelData());
	}

	private record SpellTarget(Vec3 location, LivingEntity entity) {
	}

	private enum SpellMode {
		RED("red", 0.0F, 0xFF2A1D, 0xFFD36A, ParticleTypes.FLAME, ParticleTypes.END_ROD, ChatFormatting.RED, "tooltip.bully.magic_wand.mode.red", EMBER_LANCE_COOLDOWN_TICKS, 0.8F, EMBER_LANCE_DAMAGE),
		BLUE("blue", 1.0F, 0x49A7FF, 0xD7F7FF, ParticleTypes.SNOWFLAKE, ParticleTypes.ELECTRIC_SPARK, ChatFormatting.AQUA, "tooltip.bully.magic_wand.mode.blue", FROST_BURST_COOLDOWN_TICKS, 1.15F, FROST_BURST_DAMAGE),
		GREEN("green", 2.0F, 0x45E06F, 0xD8FF7A, ParticleTypes.HAPPY_VILLAGER, ParticleTypes.GLOW, ChatFormatting.GREEN, "tooltip.bully.magic_wand.mode.green", LIFE_BLOOM_COOLDOWN_TICKS, 1.35F, LIFE_BLOOM_DAMAGE);

		private final String id;
		private final float customModelData;
		private final int particleColor;
		private final int secondaryParticleColor;
		private final ParticleOptions burstParticle;
		private final ParticleOptions accentParticle;
		private final ChatFormatting formatting;
		private final String translationKey;
		private final int cooldownTicks;
		private final float pitch;
		private final ResourceKey<DamageType> damageType;

		SpellMode(String id, float customModelData, int particleColor, int secondaryParticleColor, ParticleOptions burstParticle, ParticleOptions accentParticle, ChatFormatting formatting, String translationKey, int cooldownTicks, float pitch, ResourceKey<DamageType> damageType) {
			this.id = id;
			this.customModelData = customModelData;
			this.particleColor = particleColor;
			this.secondaryParticleColor = secondaryParticleColor;
			this.burstParticle = burstParticle;
			this.accentParticle = accentParticle;
			this.formatting = formatting;
			this.translationKey = translationKey;
			this.cooldownTicks = cooldownTicks;
			this.pitch = pitch;
			this.damageType = damageType;
		}

		private static SpellMode byId(String id) {
			for (SpellMode mode : values()) {
				if (mode.id.equals(id.toLowerCase(Locale.ROOT))) {
					return mode;
				}
			}
			return RED;
		}

		private SpellMode next() {
			SpellMode[] modes = values();
			return modes[(ordinal() + 1) % modes.length];
		}

		private String translationKey() {
			return translationKey;
		}

		private double targetRange(float charge) {
			return switch (this) {
				case RED -> EMBER_LANCE_BASE_RANGE + charge * EMBER_LANCE_CHARGED_RANGE_BONUS;
				case BLUE -> FROST_BURST_BASE_RANGE + charge * FROST_BURST_CHARGED_RANGE_BONUS;
				case GREEN -> LIFE_BLOOM_BASE_RANGE + charge * LIFE_BLOOM_CHARGED_RANGE_BONUS;
			};
		}

		private CustomModelData customModelData() {
			return new CustomModelData(List.of(customModelData), List.of(), List.of(), List.of());
		}

		private DustParticleOptions dust(float scale) {
			return new DustParticleOptions(particleColor, scale);
		}

		private DustParticleOptions secondaryDust(float scale) {
			return new DustParticleOptions(secondaryParticleColor, scale);
		}
	}
}
