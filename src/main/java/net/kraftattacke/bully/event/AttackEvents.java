package net.kraftattacke.bully.event;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.kraftattacke.bully.enchantment.ModEnchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionResult;

public class AttackEvents {

    public static void register() {

        AttackEntityCallback.EVENT.register(
                (player, world, hand, entity, hitResult) -> {

                    if (!(entity instanceof LivingEntity target)) {
                        return InteractionResult.PASS;
                    }

                    ItemStack stack = player.getMainHandItem();

                    int level = EnchantmentHelper.getItemEnchantmentLevel(
                            player.level().registryAccess()
                                    .lookupOrThrow(Registries.ENCHANTMENT)
                                    .getOrThrow(ModEnchantments.PULL_KEY),
                            stack
                    );

                    if (level > 0) {
                        pull(player, target, level);
                    }

                    return InteractionResult.PASS;
                }
        );
    }


    private static void pull(Player player, LivingEntity target, int level) {
        Vec3 direction = player.position()
                .subtract(target.position())
                .normalize();

        double strength = 0.5 + level * 0.3;

        Vec3 motion = target.getDeltaMovement().add(direction.scale(strength));

        target.setDeltaMovement(
                motion.x,
                Math.min(0.4, motion.y),
                motion.z
        );
    }
}