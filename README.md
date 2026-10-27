# Bully

Bully is a Fabric mod that picks up where vanilla stops. Once you've got netherite, there isn't much left to chase, so Bully adds a long stretch of endgame progression on top of it: eight new material tiers, full tool and armor sets for each, a spell-casting wand, custom enchantments, brewable status effects, and armor trims.

It runs on **Fabric** for **Minecraft 1.21.11, 26.2 and 26.3**.

## What's in it

### Material tiers past netherite
Eight tiers sit above netherite, each with its own armor set and a full tool line (sword, pickaxe, axe, shovel, hoe, plus a scythe and a spear):

Antimatter, Aetherald, Auronyx, Feralyth, Fluxite, Mystralite, Nebulazul and Voidcrist.

You start by upgrading netherite gear to Antimatter with the Antimatter Upgrade Template, then branch into the other variants using the Tool and Armor Upgrade Templates together with ore and gem blocks. There's also an Antimatter Elytra.

### The Magic Wand
A three-mode wand you charge up and release. Hold to charge, sneak-use to switch mode:

- **Ember Lance** (red) — a ranged bolt that burns the target and breaks through raised shields.
- **Frost Burst** (blue) — an area hit that slows and knocks enemies back.
- **Life Bloom** (green) — heals you and your allies in range, and hits enemies with a stack of damage-over-time effects.

Charge level scales range, damage, radius and effect duration, so a full charge hits a lot harder than a tap.

### Enchantments
- **Hunger Leech** — a sword enchantment that drains hunger and saturation from whoever you hit, more per level.
- **Mace Guard** — armor enchantment that cuts the mace's fall-damage bonus. It counts all four armor pieces, so a full set holds up against a mace slam from a long fall.
- **Pull** — drags the target toward you on hit.

### Grievous Wounds and sword coatings
**Grievous Wounds** is a status effect that shuts off healing and regeneration, golden apples included. You brew it with a Rosarium Crystal and it comes in the usual forms: normal, long and strong, as a drinking, splash and lingering potion, and on tipped arrows. A honey bottle clears it if you get hit with it.

**Sword coatings** let you apply any potion to a sword in the anvil. The sword then passes that potion's effects to whatever you hit, at a shorter duration. Instant potions like Instant Damage are toned down so a coated weapon stays strong without being an instant kill.

### Armor trims
Five custom trim patterns — Echo, Rift, Crown, Fang and Thorn — crafted from their smithing templates, plus Rosarium Crystal as a trim material.

### Music and the Discographer
Eight collectible music discs, and a new villager profession — the **Discographer** — who trades them. Track one down and you can buy your way to the full set instead of hoping they drop.

### Music and the Discographer
Eight new music discs to collect, plus a new villager profession, the **Discographer**, who trades all of them. If you'd rather not go hunting, you can complete the set through trading.

### Utility menu (portable workbenches)
Press the **Utility Menu** keybind and it opens the workbench of a block you're carrying — no need to place it down. Works for crafting table, anvil (chipped/damaged too), smithing table, grindstone, stonecutter, loom and cartography table. If you carry several, the arrows on the left and right of the window switch between them, in inventory-slot order. Carry none and nothing happens.

A couple of balance things: the portable crafting table has no recipe book, and recipe mods like JEI or REI can't push recipes into these menus (that only works at placed blocks). The portable anvil wears down too — it degrades off the carried item (anvil → chipped → damaged → gone), and a bit faster than a placed one (about 75% of the uses, tunable).

It's **on by default** (`utilityMenuEnabled`) and its keybind sits under the new **Bully** category in Options → Controls, **unbound by default**.

### Bully menu
A separate in-game menu with three pages. The middle page has an item **Dupe** grid; the left arrow opens an **effects** page (infinite max-level buffs — haste, saturation, night vision, invisibility, strength, a water set, a **Semi-God** combo, and a clear-all button), and the right arrow opens a **Command** page with a text field (vanilla-style command suggestions and Tab completion; run the command with the Execute button or by pressing Enter). It is **off by default** and has to be switched on in the config (`bullyMenuEnabled`), so it never opens unless you turn it on. On a server, only the **server's** config decides this. Its keybind is called **Debugger** (also under the Bully category) and is **unbound by default**.

## Config

All the tuning values live in `config/bully.json`, created the first time the mod loads. You can toggle the two menus (`bullyMenuEnabled`, `utilityMenuEnabled`), set the portable anvil's degrade rate (`portable.anvilDegradeUseFraction`), change sword-coating numbers (hits per potion, duration scale, anvil cost, the instant-potion nerf) and every Magic Wand value (damage, range, radius, healing, effect durations, cooldowns, charge time).

- Edit the file and restart, or run **`/bully reload`** (op level 2) to apply changes live without a restart. On a server, edit the server's copy of the file.
- If you have **Mod Menu** and **Cloth Config** installed, there's also an in-game config screen: open Mod Menu, find Bully, click the gear. Both mods are optional — without them the mod runs fine, you just edit the JSON directly.

## Requirements

- Minecraft 1.21.11, 26.2 or 26.3
- Fabric Loader 0.20.1 or newer
- Fabric API
- Java 25

Mod Menu and Cloth Config are optional, and only used for the config screen.
