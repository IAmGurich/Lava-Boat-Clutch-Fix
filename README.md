# Lava Boat Clutch Fix

> Restores the classic lava boat clutch mechanic which was removed in Minecraft 1.21.5.

[![Modrinth](https://img.shields.io/modrinth/v/lava-boat-clutch-fix?logo=modrinth&label=Modrinth&color=00AF5C)](https://modrinth.com/mod/lava-boat-clutch-fix)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Fabric](https://img.shields.io/badge/Mod_Loader-Fabric-dbb36e)](https://fabricmc.net)
[![MC](https://img.shields.io/badge/Minecraft-1.21.5%20–%201.21.9-62b47a)](https://minecraft.net)

---

## 📖 What is this?

In Minecraft 1.21.5, Mojang "fixed" the bug that allowed boats to briefly survive on lava — the mechanic behind the legendary **lava boat clutch** (famously used by Dream in Manhunt).

This mod restores it. When a boat first touches lava, it receives a short immunity window so the player can land on the hitbox and survive the fall — exactly like in older versions.

**Now you can do lava boat clutches in modern Minecraft.**

---

## ✨ Features

- 🔥 **Lava immunity** — boats survive on lava for a configurable number of ticks (default: 3)
- 🛡️ **Fire damage protection** — no fire damage to the boat during the immunity window
- 📦 **Item drop bounce** — the dropped boat item bounces upward out of the lava so it doesn't burn
- 🎨 **No fire flicker** — fire particles are suppressed on the client for the first 4 ticks, eliminating visual glitches
- ⚙️ **Fully configurable** — adjust everything via the in-game config screen (requires Cloth Config + Mod Menu)
- 🔌 **Toggle on/off** — disable the mod at any time without removing it

---

## ⚙️ Configuration

Open the config screen via **Mod Menu → Lava Boat Clutch Fix → Config**.

| Option | Default | Description |
|---|---|---|
| `Enable Mod` | `true` | Toggle the entire mod on/off |
| `Immunity Ticks` | `3` | How many ticks the boat is protected on first lava contact |
| `Bounce Mode` | `Vanilla` | `Vanilla` — random X/Z bounce; `Custom` — fixed X/Y/Z velocity |
| `Bounce Y` | `0.15` | Upward velocity of the dropped item (Custom mode) |
| `Bounce X/Z` | `0.0` | Horizontal velocity of the dropped item (Custom mode) |

---

## 📦 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/)
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Install [Cloth Config](https://modrinth.com/mod/cloth-config) *(required for config)*
4. Install [Mod Menu](https://modrinth.com/mod/modmenu) *(optional, for in-game config screen)*
5. Drop `lava-boat-clutch-fix-*.jar` into your `mods` folder

---

## 🗂️ Branches & Versions

Each branch corresponds to a range of supported Minecraft versions:

| Branch | Minecraft |
|---|---|
| `1.21.5-1.21.6` | 1.21.5, 1.21.6 |
| `1.21.7` | 1.21.7 |
| `1.21.9` | 1.21.9 |

---

## 🔧 Building from Source

```bash
git clone https://github.com/IAmGurich/Lava-Boat-Clutch-Fix.git
cd Lava-Boat-Clutch-Fix
git checkout <branch>
./gradlew build
```

The output JAR will be in `build/libs/`.

---

## 🤝 Contributing

Bug reports and pull requests are welcome! Please open an [Issue](https://github.com/IAmGurich/Lava-Boat-Clutch-Fix/issues) before submitting large changes.

---

## 📄 License

[MIT](LICENSE) © 2026 IAmGurich
