# Changelog Since the First 1.4.1 Build

Comparison baseline: first local build `bully-1.4.1`.
Current state: `1.4.2.2`.

Note: `1.4.2.1` and `1.4.2.2` belong to the same version here. The `1.4.2.2` build only exists because `gradle.properties` was accidentally not updated before the `1.4.2.1` build.

## 1.4.2.1 / 1.4.2.2

### Added
- New custom particle "bully:burning_shield", including client factory and textures.
- Burning shield effect for the Ember Lance shield break.
- Separate in-hand models and textures for all spears:
  - Aetherald
  - Antimatter
  - Auronyx
  - Feralyth
  - Fluxite
  - Mystralite
  - Nebulazul
  - Voidcrist
- Dedicated "ScytheItem" class for scythes with a magic damage type.

### Changed
- Rebalanced the Magic Wand charge timing:
  - Minimum charge time increased from 8 to 50 ticks.
  - Full charge time increased from 80 to 180 ticks.
- Ember Lance no longer additionally removes Slowness/Weakness on shield break. It now focuses on shield damage, shield cooldown, fire, and particles.
- Reworked Magic Circle rendering:
  - The circle still follows the crosshair.
  - Front third-person view no longer renders the circle behind the player.
  - The circle is kept just above the ground so the texture does not sink into grass or blocks.
  - Circle geometry, depth handling, and charged offset were adjusted.
- Spear item definitions now use "minecraft:display_context":
  - GUI, ground, fixed, and shelf views show the regular spear icon.
  - The new "*_spear_in_hand" model is used while held in hand.
- Scaled the spear swap animation.
- Replaced and improved Magic Circle textures.
- Further adjusted Magic Wand visuals.
- Reworked Dark Star and Magic Crystal recipes.
- Scythes were changed from "MaceItem" to "ScytheItem" and now receive a weapon component.

### Gameplay Changed
- Ember Lance can damage blocking shields, put them on cooldown, interrupt active blocking, and set the opponent on fire.

## 1.4.2

### Added
- New item: `Magic Wand`.
- Three spell modes for the Magic Wand:
  - `Ember Lance`: long, direct fire/damage spell.
  - `Frost Burst`: AoE damage with strong Slowness and knockback.
  - `Life Bloom`: healing for the player/allies and debuffs/damage against enemies.
- Mode switching via Sneak + right-click.
- Charge and full-charge system with sounds, particles, cooldowns, and durability usage.
- New client targeting:
  - Targets are searched while charging based on the crosshair.
  - Valid targets receive a colored outline effect matching the spell mode.
- New beam rendering:
  - The server sends `magic_wand_beam` to players in range.
  - The client renders colored beacon beam ribbons for fired spells.
- New Magic Circle charge visual in front of the player or at the crosshair.
- Dynamic light for held/charged Magic Wand:
  - Idle light level 8.
  - Charging light level 12.
  - Temporary light blocks are removed automatically and cleaned up on save/server stop.
- New items:
  - `Large Stick`
  - `Magic Crystal`
  - `Dark Star`
- New crafting chain:
  - Logs -> `Large Stick`
  - Antimatter/Nether Star/Ominous Trial Key/Enchanted Golden Apple/Crying Obsidian -> `Dark Star`
  - `Dark Star` + Amethyst/Echo Shard/Diamond/Redstone/Lapis/Emerald -> `Magic Crystal`
  - `Magic Crystal` + `Large Stick` -> `Magic Wand`
- New Magic Wand models and textures:
  - Red/blue/green
  - Regular and charging variants for each color
  - Separate crystal inner/outer textures
  - Magic Circle and charged Magic Circle textures
- New damage types:
  - `magic_wand_ember_lance`
  - `magic_wand_frost_burst`
  - `magic_wand_life_bloom`
- New damage type tags for Magic Wand damage:
  - `bypasses_armor`
  - `bypasses_wolf_armor`
  - `no_knockback`
  - `panic_causes`
  - `witch_resistant_to`
  - `avoids_guardian_thorns`
  - `always_triggers_silverfish`
- Magic Wand is now part of the durability enchantable tag.
- New death messages and tooltips for Magic Wand and its spells.

### Changed
- New items were added to the Bully creative tab.
- Client initialization now registers Magic Wand visuals.
- Server initialization now registers Magic Wand networking and Magic Wand light events.
- Creative tab registration for toolsets was removed from `ModTools` and is now handled centrally through `ModItems`/creative tab setup.

## 1.4.1.1

### Added
- Baby armor support for all custom armor sets:
  - Aetherald
  - Antimatter
  - Auronyx
  - Feralyth
  - Fluxite
  - Mystralite
  - Nebulazul
  - Voidcrist
- New `humanoid_baby` textures and equipment JSON entries for the listed sets.

## Summary

Since the first `1.4.1` build, the main additions were the full Magic Wand feature line, new intermediate crafting items, new spell/beam/targeting/light visuals, baby armor assets, separate spear in-hand rendering, and scythe damage adjustments. In total, more than 120 files were changed or added compared to the first `1.4.1` build.
