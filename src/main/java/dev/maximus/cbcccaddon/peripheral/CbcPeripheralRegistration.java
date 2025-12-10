package dev.maximus.cbcccaddon.peripheral;

import dan200.computercraft.api.peripheral.PeripheralLookup;
import dev.maximus.cbcccaddon.Cbcccaddon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import rbasamoyai.createbigcannons.cannon_control.cannon_mount.CannonMountBlockEntity;

public final class CbcPeripheralRegistration {

    private CbcPeripheralRegistration() {}

    public static void registerPeripherals() {
        Cbcccaddon.LOGGER.info("[{}] Registering cannon mount peripheral (fallback)", Cbcccaddon.MOD_ID);

        PeripheralLookup.get().registerFallback(
                (Level world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, Direction context) -> {
                    if (blockEntity instanceof CannonMountBlockEntity mount) {
                        return new CannonMountPeripheral(mount);
                    }
                    return null;
                }
        );

        Cbcccaddon.LOGGER.info("[{}] Cannon mount fallback peripheral registration complete", Cbcccaddon.MOD_ID);
    }
}