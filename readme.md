# Projektplanung & Entwickler-Dokumentation: Medal.tv Fabric Integration

## 1. Projektübersicht
Dieses Projekt ist ein Client-Side Minecraft Fabric Mod, der als Schnittstelle zwischen Minecraft und der Medal.tv Desktop-App fungiert. Da Medal keine native Game-Hook für Minecraft-Events besitzt, nutzt dieser Mod die offizielle Medal Log-Scanning-API (ICYMI). 

Ziel ist es, wichtige Ingame-Ereignisse zu erfassen und diese an Medal weiterzuleiten, um wahlweise **vollautomatische Clips** zu erstellen oder **dynamische Titel (Bookmarks)** für manuell erstellte Clips zu generieren.

## 2. Kernfunktionen (Features)
*   **Auto-Clipping:** Bei vordefinierten High-Priority-Events (z. B. eigenen Kills) erzwingt der Mod eine sofortige Hintergrundaufnahme durch Medal für eine bestimmte Dauer.
*   **Auto-Titling (Bookmarks):** Bei fortlaufenden Events (Kampfbeginn, Tode von Gegnern) setzt der Mod unsichtbare Lesezeichen in der Medal-Timeline. Wird die manuelle Clip-Taste gedrückt, übernimmt Medal automatisch den passenden Titel.
*   **Client-Side Only:** Keine Server-Installation erforderlich. Funktioniert auf allen Server-Netzwerken durch clientseitiges Auslesen von Chat-Paketen und Render-Events.

## 3. Architektur & Datenfluss

Die Kommunikation erfolgt unidirektional (Minecraft -> Medal) über den Standard-Output (`latest.log`).

1.  **Event-Trigger:** Fabric API lauscht auf `ClientReceiveMessageEvents` (Chat-Nachrichten) oder Entity-Status.
2.  **Mod-Verarbeitung:** Die Nachricht wird geparst (z. B. Entfernung von Farbcodes, Regex-Matching für Kill-Messages).
3.  **JSON-Generierung:** Die `MedalAPI`-Klasse baut ein JSON-Objekt mit den Event-Daten.
4.  **Log-Output:** Der Mod schreibt das JSON mit dem zwingenden Präfix `[_MAPIEvent][v1/event/invoke]` in das Log.
5.  **Medal-Verarbeitung:** Medal scannt das Log im Hintergrund und führt die Aktion (Clip oder Bookmark) aus.

## 4. Geplante Use-Cases (Beispiele)

*   **Sword PvP Tracking (HT4 / LT3):** Spezifische Erkennung von 1v1 Kills, um saubere Clips für VOD-Reviews oder Montages zu generieren.
*   **Awonia Server Integration:** Anpassung des Regex-Parsers an die spezifischen Chat-Formate (z. B. Gilden-Kriege, PvP-Zonen), um Events präzise abzufangen.

## 5. Implementierungs-Schritte (Roadmap)

### Phase 1: Setup & Grundgerüst
- [ ] Fabric Mod Template initialisieren (Environment: Client).
- [ ] Logger-Klasse (`MedalIntegration`) für den konsistenten Konsolen-Output aufsetzen.
- [ ] GSON-Abhängigkeit prüfen (in Minecraft integriert).

### Phase 2: Die Medal API Wrapper-Klasse
- [ ] Implementierung der Methode `triggerAutoClip(String eventId, String title, int duration)`.
- [ ] Implementierung der Methode `registerBookmark(String eventId, String title)`.

### Phase 3: Event-Listener (Die Logik)
- [ ] Registrierung von `ClientReceiveMessageEvents.GAME`.
- [ ] Regex-Parser schreiben, der Spielernamen dynamisch ausliest (`MinecraftClient.getInstance().player.getName().getString()`).
- [ ] Erkennungslogik für "Kill" (Auto-Clip) und "Death/Assist" (Auto-Title) implementieren.

### Phase 4: Konfiguration & Polish
- [ ] (Optional) Integration von YACL (Yet Another Config Lib) für ein Ingame-Menü.
- [ ] Toggle-Optionen für Auto-Clip vs. Nur-Bookmark.
- [ ] Anpassbare Clip-Dauer in der Config.

## 6. Technische Spezifikation (Medal Payload)

### 6.1 Bookmark (Nur Titel)
Wird verwendet, um die Timeline für manuelle Clips zu markieren.
```json
// Log-Output-Format:
[_MAPIEvent][v1/event/invoke] {"eventId": "sword_pvp_kill", "eventName": "Sword PvP - LT3 Kill"}
