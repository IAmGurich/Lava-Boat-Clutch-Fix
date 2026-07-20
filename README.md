# <img src="assets/icon.png" width="64"> Lava Boat Clutch Fix
> Restores the "lava boat clutch" mechanic which was removed in Minecraft 1.21.5

[![CurseForge](https://cf.way2muchnoise.eu/1532732.svg?badge_style=for_the_badge)](https://www.curseforge.com/minecraft/mc-mods/lava-boat-clutch-fix)
[![Modrinth](https://img.shields.io/modrinth/dt/lava-boat-clutch-fix?style=for-the-badge&logo=modrinth&label=modrinth&color=35d07f)](https://modrinth.com/mod/lava-boat-clutch-fix)

---

In Minecraft 1.21.5, Mojang fixed the bug that allowed boats to briefly survive on lava — the mechanic behind the legendary **lava boat clutch** (famously used by **Dream** in **Manhunt**).

This mod restores it. When a boat first touches lava, it receives a short immunity window so the player can land on the hitbox and survive the fall — exactly like in older versions.

**Now you can do lava boat clutches in modern Minecraft!**

## Supported Platforms

Lava Boat Clutch Fix is currently available for Fabric, Quilt or NeoForge on Minecraft: Java Edition 1.21.5 or newer.

## Two Editions

This mod comes in three editions, so you can pick the one that fits your setup:

## Regular edition

The full experience with an in-game config screen.
- <img src="assets/lava_bucket.png" width="22"> **Lava immunity** — boats survive on lava for a configurable number of ticks (default: 3)
- <img src="assets/shield.png" width="15"> **Fire damage protection** — no fire damage to the boat during the immunity window
- <img src="assets/oak_boat.png" width="22"> **Item drop bounce** — the dropped boat item bounces upward out of the lava so it doesn't burn (default: vanilla)
- <img src="assets/Fire.gif" width="22"> **No fire flicker** — fire particles are suppressed on the client for the first 4 ticks
- <img src="assets/lever.png" width="22"> **Toggle on/off** — disable the mod at any time without removing it (default: enable mod)
- ⚙️ **Fully configurable** — adjust everything via the in-game config screen (requires Cloth Config + Mod Menu)
- 🌐 **Localized** — this mod version supports a lot of languages!
> Dependencies: on Fabric/Quilt the config screen requires Cloth Config + Mod Menu, on NeoForge the config screen is built in - no dependencies.

**<p>Also available: a "lightweight regular edition" — same mod, same config, just without the extra translations, so the file size is smaller.**

## Raw edition

The exact same mechanic with zero dependencies and zero config.
- <img src="assets/lava_bucket.png" width="22"> **Lava immunity** — boats survive on lava for a configurable number of ticks
- <img src="assets/shield.png" width="15"> **Fire damage protection** — no fire damage to the boat during the immunity window
- <img src="assets/oak_boat.png" width="22"> **Item drop bounce** — the dropped boat item bounces upward out of the lava so it doesn't burn
- <img src="assets/Fire.gif" width="22"> **No fire flicker** — fire particles are suppressed on the client for the first 4 ticks
- 🪶 **Zero dependencies** — no Cloth Config, no Mod Menu required
> If you want to change any of the config values, use the regular edition instead.
---

## Configuration (regular edition)

Open the config screen via Mod Menu → Lava Boat Clutch Fix → Config (Fabric/Quilt) or Mods → Lava Boat Clutch Fix → Config (NeoForge)

| Option | Default | Description |
|---|---|---|
| `Enable Mod` | `true` | Toggle the entire mod on/off |
| `Immunity Ticks` | `3` | How many ticks the boat is protected on first lava contact |
| `Bounce Mode` | `Vanilla` | `Vanilla` — vanilla behavior; `Custom` — custom X/Y/Z velocity; <p>`Random` - random  X/Y/Z velocity</p>|
| `Bounce Y` | `0.0` | Upward velocity of the dropped item (Custom mode) |
| `Bounce X/Z` | `0.0` | Horizontal velocity of the dropped item (Custom mode) |

---

## Gratitude

Special thanks to:

- [Minecraft Curios Animations](https://www.youtube.com/@MinecraftCuriosAnimations) for mod image.
- [ClaudeAI](https://claude.com) for helping me :)

---

## Support me

- Subscribe to my [youtube](https://www.youtube.com/@IAmGurich)
- Send [donation](https://dalink.to/iamgurich)
