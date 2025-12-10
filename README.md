# CBC-CC  
### Create Big Cannons × ComputerCraft Integration

CBC-CC is a lightweight integration mod that connects **Create: Big Cannons** cannon mounts with **ComputerCraft / CC:Tweaked**, enabling programmable artillery, automated aiming, and advanced firing logic through Lua scripts.

This allows full automation of cannon systems, including aiming, firing, safety checks and diagnostics.

---

## Features

- Cannon mounts automatically register as ComputerCraft peripherals (`cbc_cannon_mount`)
- Fully structured API for:
  - Reading mount position
  - Reading/setting yaw & pitch
  - Reading general mount state
  - Safely firing the cannon
- All Lua functions return structured result tables
- Ideal for automated turrets, testing rigs, AI‑driven artillery, and scripting.

---

# Installation

1. Install **Minecraft 1.20.1**
2. Install dependencies:
   - **Create Big Cannons**
   - **ComputerCraft / CC:Tweaked**
3. Install **CBC‑CC**
4. Launch the game — cannon mounts will be discoverable with `peripheral.find`.

---

# Finding the Peripheral

```lua
local p = peripheral.find("cbc_cannon_mount")
```

If `p == nil`, no cannon mount is adjacent or connected to the computer.

---

# API Reference (Lua)

Every function returns a structured table:

```lua
{
  success = true/false,
  message = "human readable string",
  reason = "ok | no_ammo | exception | ...",
  ... additional fields
}
```

### Possible `reason` values

| Reason               | Description                                  |
|----------------------|----------------------------------------------|
| `ok`                 | Successful                                   |
| `client_side`        | Must be called from server side              |
| `no_contraption`     | Cannon mount has no contraption              |
| `not_server_level`   | Contraption not in a ServerLevel             |
| `not_mounted_cannon` | Attached contraption is not a mounted cannon |
| `no_ammo`            | CBC rejected firing due to no ammo           |
| `exception`          | Internal error occurred                      |

---

# Functions

---

## `p.getPos()`

Returns the cannon mount’s world coordinates (centered).

```lua
local pos = p.getPos()
```

Returns:

```lua
{
  success = true,
  message = "ok",
  x = <float>,
  y = <float>,
  z = <float>
}
```

---

## `p.getState()`

Returns yaw, pitch, and running state.

```lua
local state = p.getState()
```

Returns:

```lua
{
  success = true,
  message = "ok",
  yaw = <float>,
  pitch = <float>,
  running = true/false
}
```

---

## `p.getAngles()`

Returns only yaw and pitch.

```lua
local ang = p.getAngles()
```

Returns:

```lua
{
  success = true,
  message = "ok",
  yaw = <float>,
  pitch = <float>
}
```

---

## `p.setAngles(yaw, pitch)`

Sets cannon yaw & pitch.

```lua
local r = p.setAngles(45, -10)
```

Returns:

```lua
{
  success = true/false,
  message = "angles updated" or error message,
  yaw = <current>,
  pitch = <current>
}
```

Fails if called client‑side or if CBC rejects changes.

---

## `p.isRunning()`

Checks if the mount has an assembled cannon contraption.

```lua
local r = p.isRunning()
```

Returns:

```lua
{
  success = true,
  message = "ok",
  running = true/false
}
```

---

## `p.fire()`

Attempts to fire the cannon.

Internally calls CBC’s `fireShot()` and validates `canFire()` via mixin hooks.

```lua
local r = p.fire()
```

### Success:

```lua
{
  success = true,
  message = "fired",
  reason = "ok"
}
```

### Failure examples:

```lua
{ success=false, message="cannon has no ammo", reason="no_ammo" }
{ success=false, message="cannon mount has no contraption attached", reason="no_contraption" }
{ success=false, message="attached contraption is not a mounted cannon", reason="not_mounted_cannon" }
{ success=false, message="cannot fire from client side", reason="client_side" }
{ success=false, message="exception while firing: IllegalStateException", reason="exception" }
```

---

# Example Lua Programs

---

## Basic firing loop

```lua
local p = peripheral.find("cbc_cannon_mount")
if not p then error("No cannon found") end

while true do
    local r = p.fire()
    print(textutils.serialize(r))
    sleep(1)
end
```

---

# Return Table Structure

Every function follows:

```lua
{
  success = boolean,
  message = string,
  reason = string,
  ... extra fields
}
```

---

# Contributing

Pull requests and suggestions are welcome.

---

# License

MIT License
