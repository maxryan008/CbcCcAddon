package dev.maximus.cbcccaddon.peripheral;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.maximus.cbcccaddon.Cbcccaddon;
import dev.maximus.cbcccaddon.ducks.CannonComputerFireDuck;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannons.autocannon.breech.AbstractAutocannonBreechBlockEntity;
import rbasamoyai.createbigcannons.munitions.autocannon.AutocannonAmmoItem;

import java.util.HashMap;
import java.util.Map;

public final class CannonMountPeripheral implements IPeripheral {

    private final CannonMountBlockEntity mount;

    public CannonMountPeripheral(CannonMountBlockEntity mount) {
        this.mount = mount;
    }

    @NotNull
    @Override
    public String getType() {
        return "cbc_cannon_mount";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        if (this == other) return true;
        if (!(other instanceof CannonMountPeripheral that)) return false;
        return that.mount == this.mount;
    }

    private Level level() {
        return mount.getLevel();
    }

    private BlockPos pos() {
        return mount.getBlockPos();
    }

    private Map<String, Object> baseResult(boolean success, String message) {
        Map<String, Object> out = new HashMap<>();
        out.put("success", success);
        out.put("message", message);
        return out;
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> getPos() {
        Map<String, Object> out = baseResult(true, "ok");
        BlockPos pos = pos();
        out.put("x", pos.getX() + 0.5);
        out.put("y", pos.getY() + 0.5);
        out.put("z", pos.getZ() + 0.5);
        return out;
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> getState() {
        float yaw = getCurrentYaw();
        float pitch = getCurrentPitch();
        boolean running = isRunningInternal();

        Map<String, Object> out = baseResult(true, "ok");
        out.put("yaw", yaw);
        out.put("pitch", pitch);
        out.put("running", running);
        return out;
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> getAngles() {
        float yaw = getCurrentYaw();
        float pitch = getCurrentPitch();

        Map<String, Object> out = baseResult(true, "ok");
        out.put("yaw", yaw);
        out.put("pitch", pitch);
        return out;
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> setAngles(double yaw, double pitch) throws LuaException {
        Level level = level();
        if (level.isClientSide) {
            return baseResult(false, "cannot set angles on client side");
        }

        boolean yawOk = setYawInternal((float) yaw);
        boolean pitchOk = setPitchInternal((float) pitch);

        Map<String, Object> out;
        if (yawOk && pitchOk) {
            out = baseResult(true, "angles updated");
        } else if (!yawOk && !pitchOk) {
            out = baseResult(false, "failed to set yaw and pitch");
        } else if (!yawOk) {
            out = baseResult(false, "failed to set yaw");
        } else {
            out = baseResult(false, "failed to set pitch");
        }

        out.put("yaw", getCurrentYaw());
        out.put("pitch", getCurrentPitch());
        return out;
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> isRunning() {
        boolean running = isRunningInternal();
        Map<String, Object> out = baseResult(true, "ok");
        out.put("running", running);
        return out;
    }

    @LuaFunction(mainThread = true)
    public final Map<String, Object> fire() throws LuaException {
        Map<String, Object> out;

        Level level = level();
        if (level.isClientSide) {
            out = baseResult(false, "cannot fire from client side");
            out.put("reason", "client_side");
            return out;
        }

        var contraptionEntity = mount.getContraption();
        if (contraptionEntity == null) {
            out = baseResult(false, "cannon mount has no contraption attached");
            out.put("reason", "no_contraption");
            return out;
        }

        if (!(contraptionEntity.level() instanceof ServerLevel slevel)) {
            out = baseResult(false, "contraption is not in a server level");
            out.put("reason", "not_server_level");
            return out;
        }

        var contraption = contraptionEntity.getContraption();
        if (!(contraption instanceof AbstractMountedCannonContraption mounted)) {
            out = baseResult(false, "attached contraption is not a mounted cannon");
            out.put("reason", "not_mounted_cannon");
            return out;
        }

        Object var4 = mounted.presentBlockEntities.get(mounted.getStartPos());
        if (var4 instanceof AbstractAutocannonBreechBlockEntity) {
            AbstractAutocannonBreechBlockEntity breech = (AbstractAutocannonBreechBlockEntity)var4;
            ItemStack foundProjectile = breech.extractNextInput();
            Item var6 = foundProjectile.getItem();
            if (!(var6 instanceof AutocannonAmmoItem)) {
                out = baseResult(false, "cannon has no ammo");
                out.put("reason", "no_ammo");
                return out;
            }
        }

        if (mounted instanceof CannonComputerFireDuck duck) {
            duck.cbcccaddon$setCalledByComputer();
        }

        try {
            mounted.fireShot(slevel, contraptionEntity);
            out = baseResult(true, "fired");
            out.put("reason", "ok");
        } catch (Throwable t) {
            out = baseResult(false, "exception while firing: " + t.getClass().getSimpleName());
            out.put("reason", "exception");
            if (t.getMessage() != null) {
                out.put("error", t.getMessage());
            }
        }

        return out;
    }

    private float getCurrentYaw() {
        try {
            return mount.getYawOffset(0.0f);
        } catch (Throwable t) {
            return 0.0f;
        }
    }

    private float getCurrentPitch() {
        try {
            return mount.getPitchOffset(0.0f);
        } catch (Throwable t) {
            return 0.0f;
        }
    }

    private boolean setYawInternal(float yaw) {
        try {
            mount.setYaw(yaw);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean setPitchInternal(float pitch) {
        try {
            mount.setPitch(pitch);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private boolean isRunningInternal() {
        try {
            return mount.isRunning();
        } catch (Throwable t) {
            return false;
        }
    }
}