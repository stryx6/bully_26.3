# Magic Wand Assets

The code registers one item: `bully:magic_wand`.

Put your item textures here:

- `src/main/resources/assets/bully/textures/item/magic_wand_red.png`
- `src/main/resources/assets/bully/textures/item/magic_wand_blue.png`
- `src/main/resources/assets/bully/textures/item/magic_wand_green.png`
- `src/main/resources/assets/bully/textures/item/magic_wand_red_charging.png`
- `src/main/resources/assets/bully/textures/item/magic_wand_blue_charging.png`
- `src/main/resources/assets/bully/textures/item/magic_wand_green_charging.png`

The wand models also define separate texture slots for your floating crystal. Your current Blockbench export uses `#3` for the wooden wand, `#5` for the inner crystal, and `#6` for the outer crystal. Those slots are already mapped in all six wand model variants.

Because the 3D wand also uses `minecraft:block/spruce_log`, the cuboid model must keep its crystal textures on the block atlas too. The model JSON files therefore reference the crystal textures as `bully:block/...`.

Create these PNGs:

- `src/main/resources/assets/bully/textures/block/magic_wand_crystal_inner_red.png`
- `src/main/resources/assets/bully/textures/block/magic_wand_crystal_outer_red.png`
- `src/main/resources/assets/bully/textures/block/magic_wand_crystal_inner_blue.png`
- `src/main/resources/assets/bully/textures/block/magic_wand_crystal_outer_blue.png`
- `src/main/resources/assets/bully/textures/block/magic_wand_crystal_inner_green.png`
- `src/main/resources/assets/bully/textures/block/magic_wand_crystal_outer_green.png`

Optional charging glow textures:

- `src/main/resources/assets/bully/textures/block/magic_wand_crystal_inner_red_charging.png`
- `src/main/resources/assets/bully/textures/block/magic_wand_crystal_outer_red_charging.png`
- `src/main/resources/assets/bully/textures/block/magic_wand_crystal_inner_blue_charging.png`
- `src/main/resources/assets/bully/textures/block/magic_wand_crystal_outer_blue_charging.png`
- `src/main/resources/assets/bully/textures/block/magic_wand_crystal_inner_green_charging.png`
- `src/main/resources/assets/bully/textures/block/magic_wand_crystal_outer_green_charging.png`

Floating charge effect textures:

- `src/main/resources/assets/bully/textures/effect/magic_circle_red.png`
- `src/main/resources/assets/bully/textures/effect/magic_circle_red_charged.png`
- `src/main/resources/assets/bully/textures/effect/magic_circle_blue.png`
- `src/main/resources/assets/bully/textures/effect/magic_circle_blue_charged.png`
- `src/main/resources/assets/bully/textures/effect/magic_circle_green.png`
- `src/main/resources/assets/bully/textures/effect/magic_circle_green_charged.png`

The base `magic_circle_<color>.png` is rendered as a slightly transparent square floating in front of the wand while it charges. At full charge, `magic_circle_<color>_charged.png` is rendered slightly behind it. If a file is missing, Minecraft will show the missing-texture pattern for that floating effect.

The fired beam uses Minecraft's vanilla `minecraft:textures/entity/beacon/beacon_beam.png` and is tinted red, blue or green by code.

The wooden wand texture slot is named `#wand` and currently points to `minecraft:block/spruce_log`.

If you already have a Blockbench 3D model, export it as Minecraft item model JSON, not as a texture.
Replace these model files with your exported models:

- `src/main/resources/assets/bully/models/item/magic_wand_red.json`
- `src/main/resources/assets/bully/models/item/magic_wand_blue.json`
- `src/main/resources/assets/bully/models/item/magic_wand_green.json`
- `src/main/resources/assets/bully/models/item/magic_wand_red_charging.json`
- `src/main/resources/assets/bully/models/item/magic_wand_blue_charging.json`
- `src/main/resources/assets/bully/models/item/magic_wand_green_charging.json`

Keep `src/main/resources/assets/bully/items/magic_wand.json` unless you rename those model files. It switches the visible model with `minecraft:custom_model_data`:

- `0.0`: red crystal, Ember Lance
- `1.0`: blue crystal, Frost Burst
- `2.0`: green crystal, Life Bloom

Add your recipe later as:

- `src/main/resources/data/bully/recipe/magic_wand.json`

The recipe now exists and can be edited there. Supporting recipes/items:

- `src/main/resources/data/bully/recipe/large_stick.json`
- `src/main/resources/data/bully/recipe/magic_crystal.json`
- `src/main/resources/data/bully/tags/item/logs.json`

`large_stick` and `magic_crystal` now use custom texture paths. Create these PNG files:

- `src/main/resources/assets/bully/textures/item/large_stick.png`
- `src/main/resources/assets/bully/textures/item/magic_crystal.png`

Until those PNG files exist, Minecraft will show the missing-texture pattern for those two items.
