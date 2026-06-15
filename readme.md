# 🎬 Medal Integration — Fabric Mod
 
Automatically clip your Minecraft highlights with [Medal](https://medal.tv)! This mod detects in-game events (kills, etc.) and triggers Medal's AutoClip feature so you never miss a moment.
 
---
 
## ✨ Features
 
- Automatic clip recording on kills
- Works with Medal's AutoClip API
- Configurable via a simple config file
- No performance impact — lightweight HTTP call on event
---
 
## 📋 Requirements
 
- Minecraft (Fabric) — see [Releases](../../releases) for supported versions
- [Fabric Loader](https://fabricmc.net/use/installer/)
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Medal](https://medal.tv) installed and running in the background
---
 
## 🚀 Installation
 
1. Download the latest `.jar` from the [Releases](../../releases) page
2. Drop it into your `.minecraft/mods/` folder
3. Launch Minecraft
5. Done!
---
 
> **Note:** Medal must be open and running in the background for clips to be saved.
 
---
 
## ⚙️ How It Works
 
1. The mod listens for in-game events (e.g. killing a mob or player)
2. When an event fires, it sends a POST request to Medal's local API at `http://localhost:12665/api/v1/event/invoke`
3. Medal receives the event and saves a clip of the configured duration to your Medal library
4. You can find and share the clip directly in the Medal app
Medal runs entirely on your own machine — no data is sent to any external server by this mod.
 
---
 
## 🎮 Supported Events
 
| Event | Trigger | Default Clip Length |
|---|---|---|
| player_kill | Killing a player | 20 seconds |
 
More events coming soon!
 
---
 
## ❓ FAQ
 
**Does this work on servers?**
Yes! The mod runs client-side and detects kills locally.
 
**What Minecraft versions are supported?**
Check the [Releases](../../releases) page for the current supported versions.
 
---
 
## 🛠️ For Developers
 
Want to contribute or build from source?
 
```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git
cd YOUR_REPO
./gradlew build
```
 
The built `.jar` will be in `build/libs/`.
 
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
