# Armor-Trim-Arena

Die fertige Welt liegt nach einem erfolgreichen Export unter
`run/saves/Bully Armor Trim Arena`, die transportable ZIP unter
`build/exports/Bully Armor Trim Arena.zip`.

Benötigt Minecraft 26.3, Fabric, Fabric API und die Bully-JAR aus diesem Build.
Der Schutz vor Kreativ-Angriffen und physischer Bewegung gehört zur Mod und
gilt ausschließlich für Rüstungsständer mit dem Entity-Tag `bully:trim_arena`.
Die Ausrüstung bleibt über die normale Rechtsklick-Interaktion zugänglich.
Unmarkierte Ständer werden durch diese Änderung nicht geschützt.

## Inhalt

15 vollständige Rüstungssets, 23 Trim-Muster, 12 Trim-Materialien:
4.140 Ständer, 16.560 Rüstungsteile, 345 Musterschilder.
Keine separaten Schildkrötenhelme oder Leder-Farbvarianten.

Muster von links nach rechts, beim Blick von der Mitte zur jeweiligen Tribüne:
Bolt, Coast, Crown, Dune, Echo, Eye, Fang, Flow, Host, Raiser, Rib, Rift,
Sentry, Shaper, Silence, Snout, Spire, Thorn, Tide, Vex, Ward, Wayfinder, Wild.

Materialien von oben/hinten nach unten/vorne:
Eisen, Kupfer, Quarz, Rosarium, Redstone, Resin, Gold, Emerald, Diamant,
Lapis, Amethyst, Netherite.

| Seite | Rüstungen |
| --- | --- |
| Nord | Leder, Kupfer, Kettenrüstung, Eisen |
| Ost | Gold, Diamant, Netherite, Antimatter |
| Süd | Aetherald, Fluxite, Nebulazul, Mystralite |
| West | Voidcrist, Auronyx, Feralyth, Eingang |

Deepslate-Brick-Tribünen stehen innerhalb von etwa 307 × 307 Blöcken.
Die Superflat-Welt hat einen begrünten Innenhof mit beleuchteten Wegen.
Spawn: ungefähr `-110 -59 -83`, Blick nach Osten. Kreativmodus, festes
Tageslicht, klares Wetter und kein natürliches Mob-Spawning.
Seitliche Materialstreifen verwenden unter anderem den eigenen
Rosarium-Kristallblock und gewachstes Kupfer.

## Erneut erzeugen

```powershell
$env:GRADLE_USER_HOME = 'C:\Users\strep\.gradle'
.\gradlew.bat runClientGameTest -ParmorTrimArena --offline --console=plain
.\gradlew.bat build --offline --console=plain
```

Vor einem erneuten Export die beiden oben genannten Ausgabeziele selbst
umbenennen oder sichern. Der Generator überschreibt keine vorhandene
Ausstellungswelt und keine vorhandene ZIP. Der normale Client-Testlauf ohne
`-ParmorTrimArena` führt weiterhin die bisherigen Regressionstests aus.
Der Generator wird nicht in der veröffentlichten Mod-JAR ausgeliefert.

Der Export läuft in einer isolierten Testwelt. Er prüft die Registries, baut
alle Tribünen, prüft die Kombinationen und die Interaktionen in Überleben und
Kreativ und öffnet den Spielstand danach erneut. Alle 4.140 UUIDs, Positionen,
Rüstungssets, Trim-Komponenten und Schilder müssen nach dem Laden vorhanden
sein. Zur Prüfung der Bewegung werden sämtliche MoverTypes sowie ein Impuls
mit anschließenden Ticks verwendet; ein unmarkierter Kontrollständer muss
weiterhin durch Kreativ-Angriffe zerstörbar sein.

Temporär erzwungene Chunks werden vor dem finalen Speichern freigegeben.
Erst nach dem Wiederöffnen werden Welt und ZIP exportiert. Prüfbericht und
sieben Übersichts-/Detailaufnahmen liegen neben der ZIP; das vollständige
Protokoll des hier ausgeführten Exports liegt in
`build/armor-arena-generation.log`.
