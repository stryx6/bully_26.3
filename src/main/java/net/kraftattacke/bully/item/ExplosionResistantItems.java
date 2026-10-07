package net.kraftattacke.bully.item;

import net.kraftattacke.bully.Bully;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DamageResistant;

final class ExplosionResistantItems {
    private static final TagKey<DamageType> FIRE_AND_EXPLOSION = TagKey.create(
            Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(Bully.MOD_ID, "fire_and_explosion"));

    private ExplosionResistantItems() {
    }

    static Item.Properties protect(Item.Properties properties) {
        return properties.delayedComponent(DataComponents.DAMAGE_RESISTANT,
                registries -> new DamageResistant(registries.getOrThrow(FIRE_AND_EXPLOSION)));
    }
}
