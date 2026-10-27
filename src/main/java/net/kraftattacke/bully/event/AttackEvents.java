package net.kraftattacke.bully.event;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.kraftattacke.bully.enchantment.ModEnchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.InteractionResult;

public class AttackEvents {

    public static void register() {

        AttackEntityCallback.EVENT.register(
                (player, world, hand, entity, hitResult) -> {

                    if (!(entity instanceof LivingEntity target)) {
                        return InteractionResult.PASS;
                    }

                    ItemStack stack = player.getMainHandItem();

                    // Pull is handled in PlayerPullMixin (after vanilla knockback), not here.

                    if (!world.isClientSide() && target instanceof Player victim
                            && player.getAttackStrengthScale(0.5F) >= 0.9F) {
                        int leechLevel = EnchantmentHelper.getItemEnchantmentLevel(
                                world.registryAccess()
                                        .lookupOrThrow(Registries.ENCHANTMENT)
                                        .getOrThrow(ModEnchantments.HUNGER_LEECH_KEY),
                                stack
                        );
                        if (leechLevel > 0) {
                            leechHunger(player, victim, leechLevel);
                        }
                    }

                    return InteractionResult.PASS;
                }
        );
    }


    // A fully charged hit drains up to 1 shank and 2 saturation; the attacker gets 50% / 75% / 100% of it.
    static void leechHunger(Player attacker, Player victim, int level) {
        FoodData from = victim.getFoodData();
        FoodData to = attacker.getFoodData();
        float share = Math.min(1.0F, 0.25F + 0.25F * level);

        int food = Math.min(2, from.getFoodLevel());
        float saturation = Math.min(2.0F, from.getSaturationLevel());
        from.setFoodLevel(from.getFoodLevel() - food);
        from.setSaturation(from.getSaturationLevel() - saturation);

        float gained = food * share;
        int wholeFood = (int) gained;
        // Round the fraction randomly so the average transfer matches the share exactly.
        if (attacker.getRandom().nextFloat() < gained - wholeFood) {
            wholeFood++;
        }
        int newFood = Math.min(20, to.getFoodLevel() + wholeFood);
        to.setFoodLevel(newFood);
        to.setSaturation(Math.min(newFood, to.getSaturationLevel() + saturation * share));
    }

}