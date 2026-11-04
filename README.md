# Advancement Skill Points

A modern Fabric RPG progression mod for Minecraft **26.1.2** that converts your completed advancements into skill points to upgrade core player attributes, enhance combat abilities, and augment your survival capabilities.

---

## ✦ Features

### 1. Dynamic Advancement Reward System
- Automatically calculates and awards Skill Points (SP) whenever you unlock an advancement.
- Point rewards scale dynamically based on advancement difficulty:
  - **Task / Normal**: 1 Skill Point
  - **Goal**: 2 Skill Points
  - **Challenge**: 3–5 Skill Points
  - **Root / Milestone**: 1 Skill Point
- **Datapack & Mod Compatibility**: Any custom advancement added by datapacks or other mods is automatically analyzed and rewarded.

### 2. Four Core RPG Attributes (Max Rank 20)
Invest your earned Skill Points into four attributes directly from your inventory:

| Attribute | Icon | Rank Cap | Core Benefits |
|---|:---:|:---:|---|
| **Strength** | 🗡️ Sword | 20 | Increases universal player base damage, unarmed combat damage, and grants heavy scaling damage multipliers for registered weapons (Swords, Bows, Crossbows, Tridents, Spears). |
| **Vitality** | ❤️ Heart | 20 | Increases maximum health (+2 HP / 1 full heart per rank, up to 60 HP / 30 hearts) and grants knockback resistance. |
| **Endurance** | 🥾 Boot | 20 | Increases movement speed (+1.5% per rank), grants bonus natural armor toughness, and reduces damage taken. |
| **Stamina** | 🍗 Food | 20 | Increases maximum hunger pool and saturation capacity (up to 30 hunger points / 15 shanks) and boosts attack speed (+2% per rank). |

### 3. Universal Base Combat Damage Pipeline
- **Registered Weapons** (Swords, Bows, Crossbows, Tridents, Spears): Receive base damage plus dedicated Strength multiplier scaling.
- **Unarmed & Non-Weapon Items** (Empty hand, dirt blocks, pickaxes, shovels, etc.): Scale dynamically using the player's universal Strength base damage:
  $$\text{PlayerBaseDamage} = 1.0 + (0.30 \times \text{StrengthRank})$$
  *(Reaches 7.0 base unarmed attack damage at Strength Rank 20).*
- **Maces**: Vanilla smash attack behavior and mechanics are fully preserved.

### 4. Seamless Inventory GUI Integration
- Opens directly inside the player inventory (`E`) screen where the Recipe Book usually sits.
- Toggle between the Recipe Book and the Skill Points panel using the dedicated **Golden Sword button** in the crafting area.
- Includes:
  - Summary box displaying **Available SP** and **Spent SP** (with dynamic text scaling for any amount).
  - Four attribute rows with progress bars and interactive `[ + ]` upgrade buttons.
  - Hover tooltips detailing current rank bonuses, next rank effects, and upgrade costs.
  - In Creative Mode: A `[ Reset ]` button allowing instant attribute respeccing for testing.

### 5. Enhanced Custom HUD
- Clean, segmented HUD bars replacing vanilla hearts and hunger:
  - **Health Bar**: Scaled with maximum vitality.
  - **Armor Bar**: Dynamic display (only appears when wearing armor).
  - **Hunger Bar**: Reflects expanded hunger capacity.
  - **Oxygen Bar**: Dynamic display (only appears when underwater or holding breath).
  - **XP Bar**: Includes numeric level counter and XP progress.

### 6. Vanilla Advancements Integration (`L`)
- Retains Minecraft's **100% authentic vanilla Advancements screen** layout and navigation.
- **Node Reward Badges**: Displays a subtle, clean SP badge on the corner of each advancement node:
  - **Collected / Completed**: Emerald green badge (`✔ +X`).
  - **Available / Incomplete**: Gold badge (`✦ +X`).
- **Native Tooltip Integration**: Hovering over any advancement in the tree shows its description followed by its exact Skill Point reward (`✦ Reward: +X Skill Points`).

---

## ❓ Frequently Asked Questions

### 1. Is this mod compatible with other mods?
**Yes!**
- **Fabric API Standard**: Built purely on standard Fabric APIs and Mixins without destructive overrides.
- **Entity Attribute Compatibility**: Uses standard vanilla attribute modifiers (`generic.max_health`, `generic.movement_speed`, `generic.attack_speed`, etc.) tagged with unique mod identifiers, ensuring it stacks properly with other mods, potions, and equipment.
- **Custom Mod Advancements**: Fully compatible! When other mods register new advancements, Advancement Skill Points automatically detects them and assigns appropriate point rewards.
- **Combat & Weapons**: Mixins wrap standard attack hurt mechanics cleanly, ensuring custom weapons from other mods still function properly.

### 2. How do I get Skill Points for already completed advancements in an existing world?
**You do not need to edit or move any files manually! It happens automatically.**

#### Automatic Retroactive Claiming
When you load into an existing singleplayer world, the mod's retroactive sync engine immediately reads your world's advancement file and awards Skill Points for **all previously completed advancements** that have not yet been claimed. You will see a notification in chat upon joining:
```
[✦] Retroactively awarded X Skill Points for Y completed advancements!
```

#### Where Singleplayer Saves & Advancements Live
If you ever need to inspect or backup your data, Minecraft singleplayer files are located in your `.minecraft` folder:
- **Advancement Progress**:
  ```
  .minecraft/saves/<WorldName>/advancements/<PlayerUUID>.json
  ```
  *(This JSON file is managed by Minecraft and records all advancements you have achieved).*
- **Skill Points & Attribute Progression**:
  ```
  .minecraft/saves/<WorldName>/playerdata/<PlayerUUID>.dat
  ```
  *(Saved automatically using Fabric Data Attachments alongside your player data).*

#### Manual Resynchronization Command
If you ever want to force a re-check of your completed advancements at any time, run:
```
/skillpoints sync
```
*(Requires OP/cheats enabled in singleplayer).*

---

## 🛠️ Commands (Cheats / OP Enabled)

| Command | Description |
|---|---|
| `/skillpoints sync` | Retroactively scans and awards points for any unclaimed completed advancements. |
| `/skillpoints sync <targets>` | Syncs completed advancements for specified players. |
| `/skillpoints get <targets>` | View a player's available and total earned Skill Points. |
| `/skillpoints add <targets> <amount>` | Add Skill Points to a player. |
| `/skillpoints remove <targets> <amount>` | Deduct Skill Points from a player. |
| `/skillpoints set <targets> <amount>` | Set a player's available Skill Points to an exact value. |

---

## 📦 Requirements & Installation

1. Install **Minecraft 26.1.2**.
2. Install **Fabric Loader** (`0.20.1` or newer).
3. Place **Fabric API** (`0.155.3+26.1.2` or newer) into your `.minecraft/mods` folder.
4. Place `advancement-skill-points-1.0.0.jar` into your `.minecraft/mods` folder.
5. Launch the game!

---

## 📄 License
This project is licensed under the CC0 License.
