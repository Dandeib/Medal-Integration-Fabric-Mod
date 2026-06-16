# 🎬 Medal Integration — Fabric Mod

Automatically clip your Minecraft highlights with [Medal](https://medal.tv)! This mod detects when **you kill a player** — entirely client-side — and triggers Medal's AutoClip so you never miss a moment.

---

## ✨ Features

- **Fully client-side** kill detection — install it on your client only, no server mod needed
- **Two complementary detection methods** for reliability:
  - Vanilla death messages (works on any server that sends them)
  - Entity death state (melee hits + your own projectiles, works even without death messages)
- **Melee *and* ranged** kills (sword, bow, crossbow, trident, …)
- On-screen feedback: action bar message + sound on every detected kill
- Saves a clip via Medal's local AutoClip HTTP API
- Built-in debounce so a single kill never triggers twice
- Lightweight — one async HTTP call per kill, no impact on tick rate

---

## 📋 Requirements

- Minecraft **1.21.11** (Fabric)
- [Fabric Loader](https://fabricmc.net/use/installer/) `>= 0.19.3`
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 21
- [Medal](https://medal.tv) installed and running in the background

---

## 🚀 Installation

1. Download the latest `.jar` from the [Releases](../../releases) page
2. Drop it into your `.minecraft/mods/` folder
3. Launch Minecraft
4. Done!

> **Note:** Medal must be open and running in the background for clips to be saved.

---

## ⚙️ How It Works

The mod detects your kills client-side and asks Medal to save a clip:

1. **Death messages** — it reads vanilla death messages (e.g. *"Steve was slain by you"*), checks whether **you** are the killer (not the victim), and reports a kill. Covers melee and ranged on any server that sends these messages.
2. **Entity state** — independently, it tracks players you hit in melee ([`AttackEntityCallback`](https://maven.fabricmc.net/docs/fabric-api/net/fabricmc/fabric/api/event/player/AttackEntityCallback.html)) and the projectiles you fire, then watches their health. When a tracked player's health hits `0` (the same signal that drives the death animation), it's counted as your kill. Works even when a server disables death messages.

Both paths feed a single, debounced handler that shows feedback and fires the Medal event:

```
POST http://localhost:12665/api/v1/event/invoke
```

Medal then saves a clip of the configured length to your library. Everything runs on your own machine — this mod sends no data to any external server.

---

## 🎮 Supported Events

| Event | Trigger | Default Clip Length |
|---|---|---|
| `Player Kill` | You kill a player (melee or ranged) | 60 seconds |

---

## 🔧 Configuration

Medal settings live as constants in [`MedalClipTrigger.java`](src/client/java/com/dandeib/client/MedalClipTrigger.java). Set them to match your Medal account (Medal → **Settings → Hotkeys / Developer**) and rebuild:

| Constant | Meaning |
|---|---|
| `ENABLED` | Master switch for the Medal trigger |
| `PUBLIC_KEY` | Your Medal public key |
| `GAME_ID` | Your Medal game ID |
| `EVENT_ID` / `EVENT_NAME` | Must match the event configured in Medal |
| `CLIP_DURATION` | Clip length in seconds |
| `CLIP_DELAY` | Capture delay sent to Medal |

> Kill detection itself runs even if the Medal trigger is disabled, so you can test it via the on-screen feedback and the log.

---

## ❓ FAQ

**Does this work on servers?**
Yes — the mod runs entirely client-side and detects kills locally. You only install it on your own client.

**What if a server hides death messages?**
The entity-based detection still catches melee and own-projectile kills, so it keeps working.

**Are ranged kills 100% accurate?**
The entity-based ranged detection is a proximity heuristic (a projectile of yours vanishing right next to a player = a hit), so rare edge cases are possible. The death-message path covers ranged kills precisely whenever the server sends those messages.

**What Minecraft versions are supported?**
Built for **1.21.11**. Check the [Releases](../../releases) page for other versions.

---

## 🛠️ For Developers

Build from source (requires JDK 21):

```bash
git clone https://github.com/Dandeib/Medal-Integration-Fabric-Mod.git
cd Medal-Integration-Fabric-Mod
./gradlew build
```

The built `.jar` will be in `build/libs/`.

Key classes:

- `KillDetector` — death-message detection
- `EntityKillTracker` — entity/projectile detection
- `KillNotifier` — shared, debounced kill handler (feedback + Medal trigger)
- `MedalClipTrigger` — Medal AutoClip HTTP call

Pull requests are welcome!

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

---

## 🔗 Links

- [Medal](https://medal.tv)
- [Medal Developer Docs](https://medal.tv/developer/auto-clipping)
- [Fabric](https://fabricmc.net)
- [Modrinth](https://modrinth.com) *(coming soon)*
