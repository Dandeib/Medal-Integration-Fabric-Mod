# 🎬 Medal Integration — Fabric Mod

Automatically clip your Minecraft highlights with [Medal](https://medal.tv)! This mod detects when **you kill a player** — entirely client-side — and triggers Medal's AutoClip so you never miss a moment.

---

## ✨ Features

- **Fully client-side** kill detection — install it on your client only, no server mod needed
- **Two complementary detection methods** for reliability:
  - Vanilla death messages (works on any server that sends them)
  - Generic custom kill lines + action bar (works on public servers with their own kill messages)
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

1. **Vanilla death messages** — it reads translatable death messages (e.g. *"Steve was slain by you"*), checks whether **you** are the killer (not the victim), and reports a kill. Covers melee and ranged on any server that sends these messages.
2. **Generic kill lines** — for public servers that use their own custom kill messages, it parses plain chat and action-bar text. It uses the sentence voice to tell *"you killed X"* (kill) apart from *"X killed you"* (death): the killer follows `by` in passive lines and precedes the kill verb in active ones. Works across most public networks even when they don't send vanilla death messages.

Both paths feed a single, debounced handler that shows feedback and fires the Medal event:

```
POST http://localhost:12665/api/v1/event/invoke
```

Medal then saves a clip of the configured length to your library. Everything runs on your own machine — this mod sends no data to any external server.

---

## 🎮 Supported Events

| Event | Trigger | Default Clip Length |
|---|---|---------------------|
| `Player Kill` | You kill a player (melee or ranged) | 30 seconds           |

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

**What if a server doesn't send vanilla death messages?**
The generic detection parses the server's own custom kill messages (chat and action bar), so it keeps working on most public networks. If a server uses an unusual wording or shows kills only as a title, detection may miss it — open an issue with the exact text and it can be added.

**Are kills 100% accurate?**
The generic path is a text heuristic, so unusual kill-message wording can occasionally be missed (or, rarely, mis-detected). The vanilla death-message path is exact whenever the server sends those messages.

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

- `VanillaKillDetector` — kills from vanilla death messages
- `CustomKillDetector` — kills from servers' custom kill lines (chat + action bar)
- `KillNotifier` — shared, debounced kill handler (feedback + Medal trigger)
- `MedalClipTrigger` — Medal AutoClip HTTP call

Pull requests are welcome!

---

## 📄 License

PolyForm Shield License 1.0.0 — see [LICENSE](LICENSE) for details.

---

## 🔗 Links

- [Medal](https://medal.tv)
- [Medal Developer Docs](https://medal.tv/developer/auto-clipping)
- [Fabric](https://fabricmc.net)
- [Modrinth](https://modrinth.com) *(coming soon)*
