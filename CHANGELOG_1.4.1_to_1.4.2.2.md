# Changelog seit dem ersten 1.4.1 Build

Vergleichsbasis: erster lokaler Build `bully-1.4.1`.
Aktueller Stand: `1.4.2.2`.

## 1.4.2.2

### Neu
- Neuer Custom-Partikel `bully:burning_shield` inklusive Client-Factory und Texturen.
- Burning-Shield-Effekt fuer den Ember-Lance-Schildbruch.

### Geaendert
- Magic-Wand-Ladezeit neu gebalanced:
  - minimale Ladezeit von 8 auf 50 Ticks erhoeht.
  - volle Ladezeit von 80 auf 180 Ticks erhoeht.
- Ember Lance entfernt beim Schildbruch nicht mehr zusaetzlich Slowness/Weakness, sondern fokussiert auf Schildschaden, Schild-Cooldown, Feuer und Partikel.
- Magic-Circle-Rendering ueberarbeitet:
  - Kreis folgt weiterhin dem Fadenkreuz.
  - Front-Third-Person rendert den Kreis nicht mehr hinter dem Spieler.
  - Kreis wird knapp ueber dem Boden gehalten, damit die Textur nicht in Gras/Bloecke einsinkt.
  - Kreisgeometrie/Depth/Charged-Offset angepasst.

## 1.4.2.1

### Neu
- Separate In-Hand-Modelle und Texturen fuer alle Speere:
  - Aetherald
  - Antimatter
  - Auronyx
  - Feralyth
  - Fluxite
  - Mystralite
  - Nebulazul
  - Voidcrist
- Eigene `ScytheItem`-Klasse für Scythes mit magischem Damage-Type.

### Geaendert
- Speer-Item-Definitionen nutzen jetzt `minecraft:display_context`:
  - GUI/Ground/Fixed/Shelf zeigen das normale Speer-Icon.
  - In der Hand wird das neue `*_spear_in_hand`-Modell genutzt.
- Speer-Swap-Animation skaliert.
- Magic-Circle-Texturen wurden ersetzt/verbessert.
- Magic-Wand-Visuals wurden weiter angepasst.
- Dark-Star- und Magic-Crystal-Rezepte wurden überarbeitet.
- Scythes wurden von `MaceItem` auf `ScytheItem` umgestellt und bekommen eine Weapon-Komponente.

### Geaendert am Gameplay
- Ember Lance kann blockierende Schilde beschädigen, in Cooldown setzen, die Block-Nutzung abbrechen und den Gegner anzünden.

## 1.4.2

### Neu
- Neues Item `Magic Wand`.
- Drei Zaubermodi für die Magic Wand:
  - `Ember Lance`: langer, direkter Feuer-/Schadenszauber.
  - `Frost Burst`: AoE-Schaden mit starker Slowness und Knockback.
  - `Life Bloom`: Heilung für den Spieler/Verbuedete und Debuffs/Schaden gegen Gegner.
- Moduswechsel per Sneak + Rechtsklick.
- Lade- und Full-Charge-System mit Sounds, Partikeln, Cooldowns und Haltbarkeitsverbrauch.
- Neues Client-Targeting:
  - Ziele werden beim Laden anhand des Fadenkreuzes gesucht.
  - Gültige Ziele bekommen einen farbigen Outline-Effekt passend zum Zaubermodus.
- Neues Beam-Rendering:
  - Server sendet `magic_wand_beam` an Spieler in Reichweite.
  - Client rendert farbige Beacon-Beam-Ribbons für abgefeuerte Zauber.
- Neues Magic-Circle-Ladevisual vor dem Spieler bzw. im Fadenkreuz.
- Dynamisches Licht für gehaltene/geladene Magic Wand:
  - Idle-Lichtlevel 8.
  - Charging-Lichtlevel 12.
  - Temporäre Light-Blocks werden automatisch entfernt und beim Speichern/Serverstop bereinigt.
- Neue Items:
  - `Large Stick`
  - `Magic Crystal`
  - `Dark Star`
- Neue Crafting-Kette:
  - Logs -> `Large Stick`
  - Antimatter/Nether Star/Ominous Trial Key/Enchanted Golden Apple/Crying Obsidian -> `Dark Star`
  - `Dark Star` + Amethyst/Echo Shard/Diamond/Redstone/Lapis/Emerald -> `Magic Crystal`
  - `Magic Crystal` + `Large Stick` -> `Magic Wand`
- Neue Magic-Wand-Modelle und Texturen:
  - Rot/Blau/Gruen
  - jeweils normale und Charging-Variante
  - separate Kristall-Innen-/Aussen-Texturen
  - Magic-Circle- und Charged-Magic-Circle-Texturen
- Neue Damage-Types:
  - `magic_wand_ember_lance`
  - `magic_wand_frost_burst`
  - `magic_wand_life_bloom`
- Neue Damage-Type-Tags für Magic-Wand-Schaden:
  - `bypasses_armor`
  - `bypasses_wolf_armor`
  - `no_knockback`
  - `panic_causes`
  - `witch_resistant_to`
  - `avoids_guardian_thorns`
  - `always_triggers_silverfish`
- Magic Wand ist jetzt im Durability-Enchantable-Tag.
- Neue Death-Messages und Tooltips für Magic Wand und ihre Zauber.

### Geaendert
- Neue Items wurden in den Bully-Creative-Tab aufgenommen.
- Client-Initialisierung registriert Magic-Wand-Visuals.
- Server-Initialisierung registriert Magic-Wand-Networking und Magic-Wand-Light-Events.
- Creative-Tab-Registrierung für Toolsets wurde aus `ModTools` entfernt und zentral über `ModItems`/Creative-Tab-Aufbau geführt.

## 1.4.1.1

### Neu
- Baby-Armor-Support für alle Custom-Armor-Sets:
  - Aetherald
  - Antimatter
  - Auronyx
  - Feralyth
  - Fluxite
  - Mystralite
  - Nebulazul
  - Voidcrist
- Neue `humanoid_baby`-Texturen und Equipment-JSON-Eintraege für die genannten Sets.

## Zusammenfassung

Seit dem ersten `1.4.1` Build kamen vor allem die komplette Magic-Wand-Feature-Linie, neue Crafting-Zwischenitems, neue Zauber-/Beam-/Targeting-/Licht-Visuals, Baby-Armor-Assets, separate Speer-In-Hand-Darstellung und Scythe-Damage-Anpassungen dazu. Insgesamt wurden im Vergleich zum ersten `1.4.1` Build ueber 120 Dateien geaendert oder neu hinzugefuegt.
