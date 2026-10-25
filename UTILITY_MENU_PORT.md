# Utility Menu v4: Integration in Bully

## Ausgangszustand und Wiederaufnahme

Geprüft wurde das vorhandene Projekt mit Minecraft `26.3`, Fabric Loader `0.19.5`,
Fabric API `0.160.7+26.3`, Loom `1.17.20` und Java 25.
Beim Beginn dieser Wiederaufnahme zeigte Git ausschließlich bestehende Änderungen
an `CHANGELOG_1.4.1_to_1.4.2.2.md` und `LICENSE`. Unter `src` war keine
Utility-Menu-Portierung vorhanden. Der unveränderte Ausgangsstand bestand
`gradlew.bat build --offline`, einschließlich `validateLang`.

Die bestehende Bully-Implementierung, die Versionsnummer `1.4.2.2`, der
Änderungsverlauf und die bereits bearbeitete Lizenz wurden erhalten.
Der genaue alte Fünf-Schritte-Plan ist nicht vorhanden; eine Zuordnung zum
früheren „Schritt 2“ wäre deshalb unbelegt.

Die Ausgangsdatei `hackermenu-4.0.jar` wurde anhand ihrer Metadaten und des
dekompilierten Codes untersucht: NeoForge, Minecraft `1.21.1` bis vor `1.22`,
Autor `Benny Smith`, Version `4.0`.
SHA-256: `74e42a44b4415eeec3b9b1d3e63bcf18b0c1ddab82350e8659307fb3f38db34e`.
Analyseausgaben liegen ausschließlich unter dem ignorierten Verzeichnis
`build/utilitymenu-analysis`; die Downloads-JAR wurde nicht verändert.

## Umgesetzte Funktionen

| Originalfunktion | Übernommenes Verhalten |
| --- | --- |
| Debugger-Taste | Standardmäßig **Ende**, in den Steuerungsoptionen umbelegbar; öffnet das Hauptmenü. |
| Drei Seiten | Hauptmenü, Command page, Potion page, einschließlich Hin-/Zurücknavigation. |
| Survival / Creative / Spectator | Setzt den Spielmodus des anfragenden Spielers serverseitig. |
| 5 / 10 / 50 Levels | Addiert exakt die jeweilige Levelzahl. |
| Max Level | Ruft wie das Original `giveExperienceLevels(Integer.MAX_VALUE)` auf; kein eigener Ersatzwert. |
| Dupe | Verdoppelt die Mengen in allen neun eigenen Menüslots durch `grow(count)`, auch oberhalb der normalen Stackgröße. Item-Komponenten bleiben erhalten. |
| Dupe-Inventar | Neun Slots, Spielerinventar, Hotbar und Shift-Transfer; beim Verlassen werden die ungebundenen Items zurückgegeben, bei fehlendem Platz gemäß Vanilla abgelegt. |
| Truhenbindung | Die Menüslots verwenden einen Containerblock an der Öffnungsposition, wenn dieser mindestens neun Slots besitzt; beim Schließen bleiben dessen Items im Container. |
| Semi-god ON | Feuerresistenz, Regeneration und Resistenz. |
| Semi-god OFF | Entfernt sämtliche Effekte des anfragenden Spielers, wie `RemoveallProcedure`. |
| Quick Mine | Eile. |
| Inf Saturation | Exakt der ursprüngliche `effect give @p saturation 1000000 255 true`-Befehl. Sättigung ist ein Soforteffekt; der Button erzeugt keinen eigenständigen dauerhaften Hunger-Schutz. |
| Night Vision | Nachtsicht. |
| Invisibility | Unsichtbarkeit. |
| Insta Kill | Stärke; kein eigener Kill-Mechanismus. |
| Poseidon | Meereskraft, Gunst des Delfins und Wasseratmung. |
| Turn all off | Entfernt sämtliche Effekte des anfragenden Spielers. |
| Execute command | Textfeld mit maximal 32767 Zeichen; überträgt den Text tatsächlich an den Server und führt ihn mit oder ohne führenden Slash aus. |
| Escape | Schließt das Menü. |
| Ressourcen | Originale Texte, Menütexturen, Pfeile und Layoutabmessungen übernommen. |

Die Effektbefehle behalten die Originalparameter bei: `1000000` Sekunden,
Verstärker `255`, versteckte Partikel, Ziel `@p` an der beim Öffnen gespeicherten
Blockposition. `@p` ist somit der nächstgelegene Spieler an dieser Position und
nicht zwingend derjenige, der den Button betätigt. Befehle haben wie im Original
keine ausführende Entität: `@s` erhält dadurch keinen Spieler als Kontext.

Das Original vergibt diese Aktionen ohne OP-Prüfung. Auch die Portierung erhält
dieses Verhalten; freie Befehle laufen mit den maximalen Serverberechtigungen.
Client und Server müssen Bully mit dieser Integration installiert haben.
Die Mod verleiht keine solchen Funktionen auf fremden Servern ohne die Mod.

„Fly“ kommt nur als ungenutzter Eintrag in der Original-Sprachdatei vor. In der
gelieferten JAR existiert dazu keine implementierte Menüaktion.

## Technische Änderungen

- Gemeinsamer Code in `src/main/java/net/kraftattacke/bully/utilitymenu`:
  Menütypen, Container, Aktionen und zwei Fabric-Payloads.
- Clientcode in `UtilityMenuClient` und `UtilityMenuScreen`: Registrierung,
  Taste und Rendering über die Minecraft-26.3-API.
- Registrierung in den bestehenden `Bully`-/`BullyClient`-Entrypoints.
- Command-Text wird im Payload übertragen. Das Original verwendet stattdessen
  eine statische GUI-Map mit einem Client-`EditBox`, was keine korrekte
  Übertragung zu einem getrennten Server darstellt.
- Aktionen müssen zur aktuell geöffneten serverseitigen Menü-ID gehören.
  Position und Seite stammen vom Servermenü; veraltete Menüpakete werden ignoriert.
- Testmodul unter `src/gametest` mit eigenen, automatisch erzeugten Spielwelten;
  Testklassen werden nicht in die Release-JAR aufgenommen.

Die verwendeten Fabric-Verfahren sind in der offiziellen Dokumentation zu
[Networking](https://docs.fabricmc.net/develop/networking) und
[automatisierten Tests](https://docs.fabricmc.net/develop/automatic-testing)
beschrieben. Maßgeblich für die konkrete Implementierung waren die lokal
vorhandenen APIs der tatsächlichen Projektversion.

## Prüfungen

Ausführen mit Java 25:

```powershell
.\gradlew.bat build --offline
.\gradlew.bat runClientGameTest --offline
powershell -ExecutionPolicy Bypass -File tools/verify_utilitymenu.ps1 -OriginalJar C:\Users\strep\Downloads\hackermenu-4.0.jar
```

Der Client-Gametest verwendet echte Menübuttons und Fabric-Netzwerkpakete.
Er prüft in einer Einzelspieler-Testwelt die Spielmodi, XP-Zugaben,
alle neun Dupe-Slots einschließlich eines 80er-Stacks, Rückgabe von 152 Items
beim Seitenwechsel, sämtliche Effekte, Effektentfernung, Navigation,
getippte Befehlseingabe und Zurückweisung einer veralteten Menü-ID.

Zusätzlich verwendet er einen separaten lokalen Dedicated Server: Menüöffnung,
Dupe mit Erhaltung eines benutzerdefinierten Itemnamens, Befehlseingabe eines
Spielers ohne OP, Truhenbindung und Erhaltung der Truhenitems nach dem Schließen.
Die Testwelt ist jeweils isoliert von `run/saves`.
Screenshots der drei Seiten liegen unter
`build/run/clientGameTest/screenshots`, der Prüfbericht unter
`build/utilitymenu-client-test.log`.

Beide Gradle-Aufrufe bestanden bei dieser Wiederaufnahme. Die Paketprüfung
vergleicht die Originalresources bytegenau und prüft Release-Metadaten,
Vorhandensein der Portklassen sowie Abwesenheit von Testklassen,
NeoForge-Abhängigkeiten und Clientreferenzen im gemeinsamen Portcode.

Diese Prüfungen belegen die geprüften Abläufe auf 26.3. Sie sind kein Beweis für
jede Kombination mit anderen Mods oder jeden durch freie Befehle auslösbaren
Vanilla-Spielzustand.

## Rechtehinweis

Die Original-JAR deklariert `All Rights Reserved`. Deshalb enthält die
Integration einen eigenen Hinweis in `META-INF/HACKER_MENU_NOTICE.txt` und
einen entsprechenden Lizenzverweis in `fabric.mod.json`. Die vorhandene
MIT-Lizenz von Bully erteilt keine zusätzlichen Rechte an den Originalassets.
Eine Veröffentlichung dieser integrierten Fassung setzt entsprechende Rechte
an Utility Menu voraus; aus der gelieferten JAR lässt sich eine Erlaubnis zur
Weiterverbreitung nicht nachweisen.
