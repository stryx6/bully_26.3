package net.kraftattacke.bully.item;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;

/** Measures real spell outcomes in a disposable integrated-server world. */
public final class MagicWandGameTest implements FabricClientGameTest {
    // Explicit expectations, independent of the production charge formula.
    private static final int[][] CASES = {
        {9, 0, 0, 0}, {10, 10, 100, 100}, {19, 10, 100, 100},
        {20, 15, 150, 150}, {39, 15, 150, 150},
        {40, 20, 200, 200}, {59, 20, 200, 200},
        {60, 25, 250, 250}, {79, 25, 250, 250},
        {80, 30, 300, 300}, {99, 30, 300, 300},
        {100, 35, 350, 350}, {119, 35, 350, 350},
        {120, 40, 400, 400}, {139, 40, 400, 400},
        {140, 45, 450, 450}, {159, 45, 450, 450},
        {160, 50, 500, 500}, {179, 50, 500, 500},
        {180, 55, 550, 550}, {199, 55, 550, 550},
        {200, 60, 600, 600}, {240, 60, 600, 600}
    };

    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                var level = player.level();
                player.setGameMode(GameType.SURVIVAL);
                player.setPos(0.5, 200, 0.5);
                player.setYRot(0);
                player.setXRot(0);
                for (String mode : new String[]{"red", "blue", "green"}) {
                    for (int[] expected : CASES) {
                        var target = (Mob) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("villager"))
                                .create(level, EntitySpawnReason.COMMAND);
                        require(target != null, "create target");
                        target.setNoAi(true);
                        target.setNoGravity(true);
                        target.setPos(0.5, 200.5, 5.5);
                        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
                        target.setHealth(200);
                        require(level.addFreshEntity(target), "spawn target");
                        try {
                            ItemStack stack = new ItemStack(ModItems.MAGIC_WAND);
                            CustomData.update(DataComponents.CUSTOM_DATA, stack,
                                    tag -> tag.putString("BullyMagicWandMode", mode));
                            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                            player.removeAllEffects();
                            player.startUsingItem(InteractionHand.MAIN_HAND);
                            var wand = (MagicWandItem) stack.getItem();
                            boolean cast = wand.releaseUsing(stack, level, player,
                                    wand.getUseDuration(stack, player) - expected[0]);
                            String label = mode + " at " + expected[0] + " ticks";
                            require(cast == (expected[0] >= 10), label + " cast result");
                            if (!cast) {
                                require(target.getHealth() == 200 && target.getActiveEffects().isEmpty(),
                                        label + " must have no effect");
                            } else if (mode.equals("red")) {
                                require(Math.abs((200 - target.getHealth()) - expected[1]) < 0.001F,
                                        label + " damage: " + (200 - target.getHealth()));
                            } else if (mode.equals("blue")) {
                                var effect = target.getEffect(MobEffects.SLOWNESS);
                                require(effect != null && effect.getDuration() == expected[2],
                                        label + " slowness duration: " + (effect == null ? "missing" : effect.getDuration()));
                            } else {
                                for (var type : java.util.List.of(MobEffects.DARKNESS, MobEffects.WITHER,
                                        MobEffects.POISON, MobEffects.WEAKNESS)) {
                                    var effect = target.getEffect(type);
                                    require(effect != null && effect.getDuration() == expected[3],
                                            label + " " + type + " duration: " + (effect == null ? "missing" : effect.getDuration()));
                                }
                            }
                            System.out.println("MAGIC_WAND_CHECK PASSED: " + label);
                        } finally {
                            player.stopUsingItem();
                            target.discard();
                        }
                    }
                }
                System.out.println("MAGIC_WAND_CHECK: all " + (CASES.length * 3) + " spell cases passed");
            });
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
