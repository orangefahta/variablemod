# VariableMod — Variable System for Commands and Chat

A Fabric mod for Minecraft 1.21 - 1.21.8 that lets you define variables and use them anywhere — in commands, chat messages, and command blocks. Just type `v:myvar` and it gets replaced with the stored value automatically.

---

## Requirements

- Minecraft 1.21 – 1.21.5
- Fabric Loader 0.16+
- Fabric API

---

## Installation

1. Drop `variablemod.jar` into your `.minecraft/mods/` folder
2. Launch the game
3. Variables are stored per-world at `<world>/variable/commands/v/`

---

## How Variables Work

There are two types of variables:

- `v:name` — your custom variables, defined via `/variable new`
- `v!:name` — built-in variables resolved from the player's state (position, health, etc.)

Both are replaced in-line wherever you use them — in commands, chat, or `/say`.

Example — define a variable and use it in a command:
```
/variable new target Steve
/tp v:target
```
The command above becomes `/tp Steve` when executed.

---

## Commands

All commands require operator permission (level 2).

### `/variable new <name> <value>`
Creates a new variable with a text value.
```
/variable new spawn 100 64 100
/variable new playername Alex
```

### `/variable edit <name> <value>`
Changes the value of an existing variable.
- If the variable type is `number`, the new value must also be a valid number.
```
/variable edit spawn 0 70 0
```

### `/variable delete <name>`
Permanently deletes a variable.

### `/variable type <name> text|number`
Sets the type of a variable.
- `text` — accepts any string value (default)
- `number` — only accepts numeric values; useful for math-based use cases
```
/variable type lives number
```

### `/variable cfg <name>`
Shows the current value and type of a variable.

### `/variable list`
Lists all defined variables with their values and types.

### `/variable builtins`
Lists all built-in variables (`v!:`) with descriptions.

---

## Built-in Variables (`v!:`)

Built-in variables are resolved automatically from the executing player's context.

| Variable | Description |
|---|---|
| `v!:nickname` | Player's display name (with formatting) |
| `v!:name` | Player's raw username |
| `v!:gamemode` | Current game mode (`survival`, `creative`, etc.) |
| `v!:world` | Current dimension (`minecraft:overworld`, etc.) |
| `v!:x` | Block X coordinate |
| `v!:y` | Block Y coordinate |
| `v!:z` | Block Z coordinate |
| `v!:health` | Current health (integer) |
| `v!:food` | Food level (integer) |
| `v!:xp` | Total experience points |
| `v!:level` | Experience level |
| `v!:time` | In-game time (`HH:mm`) |
| `v!:date` | Real-world date (`yyyy-MM-dd`) |
| `v!:day` | Current in-game day number |
| `v!:onlinecnt` | Number of players currently online |

---

## Example Workflows

### Teleport shortcut
```
/variable new hub 0 64 0
/tp @s v:hub
```

### Announce player position in chat
```
I'm at v!:x v!:y v!:z in v!:world
```
Sends something like: `I'm at 142 63 -88 in minecraft:overworld`

### Dynamic kill command
```
/variable new target Notch
/kill v:target
```

### Reward based on day
```
/variable new day_msg Day v!:day reward unlocked!
/say v:day_msg
```

---

## Storage

Variables are saved as JSON files per world:
```
<world save>/variable/commands/v/<name>.json
```
Each file contains the name, value, and type. Files can be edited manually while the server is stopped.

---

## Notes

- Variable names support letters, digits, and hyphens (`[\w\-]+`)
- If a variable is not found, `v:name` is left as-is without errors
- Built-in variables (`v!:`) require a valid player context — they return `?` for non-player sources
- The mod supports English and Russian languages
