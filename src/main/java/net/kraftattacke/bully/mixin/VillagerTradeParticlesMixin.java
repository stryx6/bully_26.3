package net.kraftattacke.bully.mixin;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.kraftattacke.bully.Bully;
import net.kraftattacke.bully.villager.ModVillagers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.trading.ItemCost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public abstract class VillagerTradeParticlesMixin {
	@Unique
	private static final int[] BULLY$LEVEL_XP = {1, 5, 10, 15, 30};

	@Unique
	private static final int BULLY$MIN_DISC_PRICE = 5;

	@Unique
	private static final int BULLY$MAX_DISC_PRICE = 28;

	@Unique
	private static final int BULLY$LEVEL_5_DISC_COUNT = 2;

	@Unique
	private static final int BULLY$LEVEL_4_DISC_COUNT = 1;

	@Unique
	private static final int BULLY$LEVEL_3_DISC_COUNT = 1;

	@Unique
	private static final int BULLY$LEVEL_2_DISC_COUNT = 1;

	@Unique
	private static final int BULLY$LEVEL_1_DISC_COUNT = 1;

	@Unique
	private static final int BULLY$DISC_TRADE_MAX_USES = 12;

	@Unique
	private static final int BULLY$PLACEHOLDER_TRADE_MAX_USES = 20;

	@Unique
	private static final int[] BULLY$PLACEHOLDER_SELL_COUNTS = {24, 8, 14, 1};

	@Unique
	private static final int[] BULLY$PLACEHOLDER_EMERALD_PAYOUTS = {1, 2, 2, 3};

	@Unique
	private static final Item[] BULLY$PLACEHOLDER_SELL_ITEMS = {
			Items.PAPER,
			Items.AMETHYST_SHARD,
			Items.INK_SAC,
			Items.JUKEBOX
	};

	@Unique
	private static final TagKey<Item> BULLY$DISCOGRAPHER_MUSIC_DISCS_TAG = TagKey.create(
			Registries.ITEM,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "discographer_music_discs")
	);

	@Unique
	private int bully$offersBeforeUpdate = Integer.MIN_VALUE;

	@Inject(method = "updateTrades", at = @At("HEAD"))
	private void bully$rememberOffersBeforeUpdate(ServerLevel serverLevel, CallbackInfo ci) {
		Villager self = (Villager)(Object)this;
		this.bully$offersBeforeUpdate = bully$offerSignature(self.getOffers());
	}

	@Inject(method = "mobInteract", at = @At("HEAD"))
	private void bully$refreshDiscographerOffersOnInteract(Player player, InteractionHand hand, CallbackInfoReturnable<?> cir) {
		Villager self = (Villager)(Object)this;
		if (self.level().isClientSide()) {
			return;
		}
		if (!self.getVillagerData().profession().is(ModVillagers.DISCOGRAPHER_PROFESSION)) {
			return;
		}
		if (!self.getOffers().isEmpty()) {
			return;
		}

		self.setOffers(bully$buildDiscographerOffers(self));
	}

	@Inject(method = "updateTrades", at = @At("TAIL"))
	private void bully$spawnTradeChangeParticles(ServerLevel serverLevel, CallbackInfo ci) {
		Villager self = (Villager)(Object)this;
		if (!self.getVillagerData().profession().is(ModVillagers.DISCOGRAPHER_PROFESSION)) {
			return;
		}

		self.setOffers(bully$buildDiscographerOffers(self));

		int offersAfterUpdate = bully$offerSignature(self.getOffers());
		if (offersAfterUpdate == this.bully$offersBeforeUpdate) {
			return;
		}

		serverLevel.sendParticles(
				ParticleTypes.HAPPY_VILLAGER,
				self.getX(),
				self.getY() + 1.0D,
				self.getZ(),
				10,
				0.4D,
				0.3D,
				0.4D,
				0.01D
		);
	}

	@Unique
	private static MerchantOffers bully$buildDiscographerOffers(Villager villager) {
		MerchantOffers offers = new MerchantOffers();
		int villagerLevel = villager.getVillagerData().level();
		int maxLevel = Math.max(1, Math.min(villagerLevel, BULLY$LEVEL_XP.length));
		long seed = bully$discographerSeed(villager);
		List<Item> musicDiscs = bully$getMusicDiscs(seed);

		for (int level = 1; level <= maxLevel; level++) {
			int index = level - 1;
			int xp = BULLY$LEVEL_XP[index];

			if (index < BULLY$PLACEHOLDER_SELL_ITEMS.length) {
				offers.add(new MerchantOffer(
						new ItemCost(BULLY$PLACEHOLDER_SELL_ITEMS[index], BULLY$PLACEHOLDER_SELL_COUNTS[index]),
						new ItemStack(Items.EMERALD, BULLY$PLACEHOLDER_EMERALD_PAYOUTS[index]),
						BULLY$PLACEHOLDER_TRADE_MAX_USES,
						xp,
						0.05F
				));
			}

			if (level == 1) {
				int discCount = Math.min(BULLY$LEVEL_1_DISC_COUNT, musicDiscs.size());
				bully$addDiscOffers(offers, seed, level, musicDiscs, discCount, xp);
			}

			if (level == 2) {
				int discCount = Math.min(BULLY$LEVEL_2_DISC_COUNT, musicDiscs.size());
				bully$addDiscOffers(offers, seed, level, musicDiscs, discCount, xp);
			}

			if (level == 3) {
				int discCount = Math.min(BULLY$LEVEL_3_DISC_COUNT, musicDiscs.size());
				bully$addDiscOffers(offers, seed, level, musicDiscs, discCount, xp);
			}

			if (level == 4) {
				int discCount = Math.min(BULLY$LEVEL_4_DISC_COUNT, musicDiscs.size());
				bully$addDiscOffers(offers, seed, level, musicDiscs, discCount, xp);
			}

			if (level == 5) {
				int discCount = Math.min(BULLY$LEVEL_5_DISC_COUNT, musicDiscs.size());
				bully$addDiscOffers(offers, seed, level, musicDiscs, discCount, xp);
			}
		}

		return offers;
	}

	@Unique
	private static void bully$addDiscOffers(
			MerchantOffers offers,
			long seed,
			int level,
			List<Item> musicDiscs,
			int discCount,
			int villagerXp
	) {
		for (int discIndex = 0; discIndex < discCount; discIndex++) {
			int shuffledIndex = ((level - 1) + discIndex) % musicDiscs.size();
			offers.add(bully$randomDiscBuyOffer(
					seed,
					level,
					discIndex,
					musicDiscs.get(shuffledIndex),
					BULLY$MIN_DISC_PRICE,
					BULLY$MAX_DISC_PRICE,
					BULLY$DISC_TRADE_MAX_USES,
					villagerXp
			));
		}
	}

	@Unique
	private static MerchantOffer bully$randomDiscBuyOffer(
			long seed,
			int level,
			int discIndex,
			Item disc,
			int minPrice,
			int maxPrice,
			int maxUses,
			int villagerXp
	) {
		Random random = new Random(seed ^ (level * 341873128712L) ^ (discIndex * 132897987541L));
		int emeraldCost = minPrice + random.nextInt(maxPrice - minPrice + 1);
		return new MerchantOffer(
				new ItemCost(Items.EMERALD, emeraldCost),
				new ItemStack(disc),
				maxUses,
				villagerXp,
				0.05F
		);
	}

	@Unique
	private static List<Item> bully$getMusicDiscs(long seed) {
		List<Item> combined = new ArrayList<>();
		for (TagKey<Item> tag : List.of(BULLY$DISCOGRAPHER_MUSIC_DISCS_TAG)) {
			for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
				Item item = holder.value();
				if (!combined.contains(item)) {
					combined.add(item);
				}
			}
		}

		Collections.shuffle(combined, new Random(seed));
		return combined;
	}

	@Unique
	private static long bully$discographerSeed(Villager villager) {
		return villager.getUUID().getMostSignificantBits() ^ villager.getUUID().getLeastSignificantBits();
	}

	@Unique
	private static int bully$offerSignature(MerchantOffers offers) {
		int hash = 1;
		for (MerchantOffer offer : offers) {
			hash = 31 * hash + offer.getResult().getItem().hashCode();
			hash = 31 * hash + offer.getItemCostA().count();
		}
		return 31 * hash + offers.size();
	}
}
