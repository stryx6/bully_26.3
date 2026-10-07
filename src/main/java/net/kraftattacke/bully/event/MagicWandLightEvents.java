package net.kraftattacke.bully.event;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kraftattacke.bully.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class MagicWandLightEvents {
	private static final int IDLE_LIGHT_LEVEL = 8;
	private static final int CHARGING_LIGHT_LEVEL = 12;
	private static final Map<UUID, WandLight> ACTIVE_LIGHTS = new HashMap<>();

	private MagicWandLightEvents() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(MagicWandLightEvents::tick);
		ServerLifecycleEvents.SERVER_STOPPING.register(MagicWandLightEvents::clearAll);
		ServerLifecycleEvents.BEFORE_SAVE.register((server, flush, force) -> clearAll(server));
	}

	private static void tick(MinecraftServer server) {
		Set<LightKey> ownedBeforeTick = currentOwnedKeys();
		Map<UUID, WandLight> nextLights = new HashMap<>();
		Map<LightKey, Integer> desiredLevels = new HashMap<>();

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			int lightLevel = getRequestedLightLevel(player);
			if (lightLevel <= 0) {
				continue;
			}

			ServerLevel level = (ServerLevel) player.level();
			BlockPos pos = findLightPos(level, player, ownedBeforeTick);
			if (pos == null) {
				continue;
			}

			WandLight light = new WandLight(level.dimension(), pos, lightLevel);
			nextLights.put(player.getUUID(), light);
			desiredLevels.merge(light.key(), lightLevel, Math::max);
		}

		removeObsoleteLights(server, desiredLevels.keySet());
		placeDesiredLights(server, desiredLevels);

		ACTIVE_LIGHTS.clear();
		ACTIVE_LIGHTS.putAll(nextLights);
	}

	private static int getRequestedLightLevel(ServerPlayer player) {
		boolean holdingWand = isMagicWand(player.getMainHandItem()) || isMagicWand(player.getOffhandItem());
		if (!holdingWand) {
			return 0;
		}

		if (player.isUsingItem() && isMagicWand(player.getUseItem())) {
			return CHARGING_LIGHT_LEVEL;
		}

		return IDLE_LIGHT_LEVEL;
	}

	private static boolean isMagicWand(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() == ModItems.MAGIC_WAND;
	}

	private static BlockPos findLightPos(ServerLevel level, ServerPlayer player, Set<LightKey> ownedBeforeTick) {
		BlockPos[] candidates = {
				BlockPos.containing(player.getEyePosition()).immutable(),
				player.blockPosition().above(),
				player.blockPosition()
		};

		for (BlockPos candidate : candidates) {
			if (canUseLightPos(level, candidate, ownedBeforeTick)) {
				return candidate;
			}
		}

		return null;
	}

	private static boolean canUseLightPos(ServerLevel level, BlockPos pos, Set<LightKey> ownedBeforeTick) {
		BlockState state = level.getBlockState(pos);
		if (state.isAir()) {
			return true;
		}

		return state.getBlock() == Blocks.LIGHT && ownedBeforeTick.contains(new LightKey(level.dimension(), pos));
	}

	private static Set<LightKey> currentOwnedKeys() {
		Set<LightKey> keys = new HashSet<>();
		for (WandLight light : ACTIVE_LIGHTS.values()) {
			keys.add(light.key());
		}
		return keys;
	}

	private static void removeObsoleteLights(MinecraftServer server, Set<LightKey> desiredKeys) {
		Set<LightKey> removed = new HashSet<>();
		for (WandLight light : ACTIVE_LIGHTS.values()) {
			LightKey key = light.key();
			if (desiredKeys.contains(key) || !removed.add(key)) {
				continue;
			}

			ServerLevel level = server.getLevel(key.dimension());
			if (level != null && level.getBlockState(key.pos()).getBlock() == Blocks.LIGHT) {
				level.setBlock(key.pos(), Blocks.AIR.defaultBlockState(), 3);
			}
		}
	}

	private static void placeDesiredLights(MinecraftServer server, Map<LightKey, Integer> desiredLevels) {
		for (Map.Entry<LightKey, Integer> entry : desiredLevels.entrySet()) {
			LightKey key = entry.getKey();
			ServerLevel level = server.getLevel(key.dimension());
			if (level == null) {
				continue;
			}

			BlockState current = level.getBlockState(key.pos());
			if (!current.isAir() && current.getBlock() != Blocks.LIGHT) {
				continue;
			}

			BlockState lightState = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, entry.getValue());
			level.setBlock(key.pos(), lightState, 3);
		}
	}

	private static void clearAll(MinecraftServer server) {
		removeObsoleteLights(server, Set.of());
		ACTIVE_LIGHTS.clear();
	}

	private record WandLight(ResourceKey<Level> dimension, BlockPos pos, int level) {
		private LightKey key() {
			return new LightKey(dimension, pos);
		}
	}

	private record LightKey(ResourceKey<Level> dimension, BlockPos pos) {
	}
}
