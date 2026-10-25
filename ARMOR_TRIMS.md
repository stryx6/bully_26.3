# Rosarium und neue Armor Trims

Zielversion: Minecraft 26.3 / Fabric. Die Projektversion bleibt `1.4.3`.

## Umsetzung

- 88 Materialwechsel-Rezepte schließen ihr jeweiliges Ergebnisitem als Basis
  aus. Beispielsweise ergeben Aetherald-Brustplatte + Emerald Block und
  Aetherald-Speer + Emerald Block kein Ergebnis mehr. Die Wechsel zwischen
  unterschiedlichen Materialien bleiben möglich; Name und Item-Komponenten
  werden weiterhin übernommen.
- Antimatter bleibt ein normales Bully-Material für Ausrüstung und Rezepte,
  ist aber kein Trim-Material mehr. Der dynamische Materialeintrag
  `bully:antimatter`, seine Trim-Palette und die zugehörigen Equipment-Overrides
  wurden entfernt.
- Neues Item `bully:rosarium_crystal`, Anzeigename **Rosarium Crystal**.
  Seine Standardkomponente `minecraft:provides_trim_material` verweist auf
  `bully:rosarium`. Das Item ist im Trim-Material-Tag und im Bully-Kreativtab.
- Rosarium verwendet eine eigene pinke Palette. Im Tooltip erhalten sowohl
  Mustername als auch Materialname die Farbe `#F472B6`.
- Alle 32 Mod-Rüstungsteile besitzen Inventarmodelle mit Overlays für die
  elf Vanilla-Trim-Materialien und Rosarium. Die 29 Vanilla-Rüstungsteile
  erhalten zusätzlich einen Rosarium-Zweig. Die Lederfärbung bleibt erhalten.
- Die fünf neuen Muster benutzen normale `minecraft:smithing_trim`-Rezepte
  mit `#minecraft:trimmable_armor` und `#minecraft:trim_materials`. Dadurch sind
  sie auf Vanilla- und Bully-Rüstungen mit allen vorhandenen Trim-Materialien
  verwendbar. Vorhandene Vanilla-Muster akzeptieren ebenfalls Rosarium.

Die importierten PNGs für Rosarium Crystal, das Echo-Template und beide
Echo-Rüstungsmuster liegen bereits an den referenzierten Pfaden. Für noch
fehlende Bilder der übrigen Muster erscheinen Missing-Texture-Platzhalter.
Die Rezepte, Komponenten, Inventar-Trim-Overlays und Rosarium-Farbpalette sind
vorhanden.

## Deine Texturdateien

Alle folgenden Pfade sind relativ zu `src/main/resources/assets/bully`.
Für Rosarium brauchst du nur diese Itemdatei:

```text
textures/item/rosarium_crystal.png
```

Für jedes Muster brauchst du drei PNGs. Ersetze `<muster>` durch einen der
fünf IDs aus der Tabelle:

```text
textures/item/<muster>_armor_trim_smithing_template.png
textures/trims/entity/humanoid/<muster>.png
textures/trims/entity/humanoid_leggings/<muster>.png
```

| Anzeigename | Muster-ID |
| --- | --- |
| Echo | `echo` |
| Rift | `rift` |
| Crown | `crown` |
| Fang | `fang` |
| Thorn | `thorn` |

Die beiden Rüstungs-Musterbilder verwenden das Vanilla-Trim-UV-Layout,
standardmäßig **64 × 32 Pixel**, transparent außerhalb des Musters.
`humanoid` enthält die Verzierungen für Helm, Brustplatte und Stiefel;
`humanoid_leggings` enthält die Verzierungen für die Hose.
Die Muster werden nicht direkt pink gemalt. Verwende die acht exakten Farben
der Minecraft-Basispalette, damit die Materialfarben zuverlässig angewendet
werden:

```text
#E0E0E0 #C0C0C0 #A0A0A0 #808080 #606060 #404040 #202020 #000000
```

Die bereits vorhandene Rosarium-Palette liegt unter
`textures/palettes/trim/rosarium.png` und ersetzt diese Graustufen durch pinke
Farbtöne. Die Inventar-Overlays verwenden automatisch dieselbe Palette.
Für sie brauchst du keine weiteren PNGs. Neue Muster verändern das
Inventarsymbol wie bei Vanilla durch ein generisches Trim-Overlay in der
Materialfarbe; das individuelle Muster wird auf der getragenen Rüstung gezeigt.

Die Itemmodelle und Trim-Pattern-JSONs referenzieren bereits die genannten
Dateinamen. Nach dem Einfügen brauchst du keine Java-Änderung; im
Entwicklungsclient genügt ein Ressourcen-Neuladen. Für eine installierte Mod
musst du die JAR neu bauen.

## Bearbeitbare Crafting-Rezepte

Alle Rezepte liegen unter `src/main/resources/data/bully/recipe`.
Die sechs neuen Crafting-Dateien sind:

```text
rosarium_crystal.json
echo_armor_trim_smithing_template.json
rift_armor_trim_smithing_template.json
crown_armor_trim_smithing_template.json
fang_armor_trim_smithing_template.json
thorn_armor_trim_smithing_template.json
```

Die Template-Herstellungsrezepte verwenden einen Dark Star, sechs umgebende
Blöcke und zwei weitere Zutaten; sie ergeben jeweils vier Templates:

| Template | Umgebende Blöcke | Weitere Zutat |
| --- | --- | --- |
| Echo | Sculk | Echo Shard |
| Rift | Obsidian | Amethyst Shard |
| Crown | Gold Block | Redstone Block |
| Fang | Packed Ice | Pointed Dripstone |
| Thorn | Cactus | Dead Bush |

Die fünf zusätzlichen Dateien mit dem Suffix `_dupe.json` verwenden sieben
Diamanten, ein vorhandenes Template und einen Block: Chiseled Deepslate für
Echo, Obsidian für Rift, Gold Block für Crown, Packed Ice für Fang und Cactus
für Thorn. Sie ergeben jeweils zwei Templates.

Rosarium Crystal ist ein positionsunabhängiges Rezept aus jeweils einem
Ghast Tear, Pink Petals und Amethyst Shard; die Ausgabe beträgt eins.

Bei geformten Rezepten kannst du `pattern`, `key` und `result.count` direkt
bearbeiten; jedes Musterzeichen außer Leerzeichen muss in `key` definiert
sein. Rosarium verwendet stattdessen `ingredients`. Die separaten
Dateien mit dem Suffix `_smithing_trim.json` regeln die Anwendung am
Schmiedetisch und müssen für eine Änderung des Crafting-Rezepts nicht angefasst
werden.

## Prüfungen

`gradlew.bat build --offline` und `gradlew.bat runClientGameTest --offline`
bestanden. Der Trim-Test prüft in einer isolierten Spielwelt:

- Ablehnung aller 88 Upgrades auf dasselbe Material und 616 weiterhin gültige
  Materialwechsel mit Erhaltung benutzerdefinierter Itemnamen.
- 3660 Schmiedetisch-Kombinationen: fünf Muster × 61 Rüstungsteile × zwölf
  Trim-Materialien, einschließlich Slotfilter und Ergebnis-Komponenten.
- Ablehnung der nochmaligen Anwendung desselben Rosarium-Trims.
- Rosarium mit einem bestehenden Vanilla-Muster auf Mod-Rüstung.
- Alle elf Crafting-Rezepte einschließlich Duplizierung, sechs freie
  Rosarium-Zutatenanordnungen und die pinken Tooltipfarben.
- Entfernung des Flame-Musters einschließlich des Template-Items.
- Entfernung des alten Antimatter-Trim-Materials.

Die bestehenden Utility-Menu-Laufzeittests bestanden ebenfalls.
Die Screenshots der Inventar-Farbmatrix und getragener Aetherald-Rüstung mit
Rosarium wurden visuell geprüft. Sie befinden sich unter
`build/run/clientGameTest/screenshots`.
Das Laufzeitprotokoll der Rezeptkorrektur liegt unter
`build/datapack-fix-test.log`.

Die Anwendung und Materialzuordnung aller fünf Muster wurden mit dem echten
Schmiedetisch-Menü geprüft. Eine visuelle Prüfung der individuellen Muster
setzt deren jeweilige PNGs voraus; die bisherigen Render-Screenshots verwenden
das Vanilla-Muster Sentry.
