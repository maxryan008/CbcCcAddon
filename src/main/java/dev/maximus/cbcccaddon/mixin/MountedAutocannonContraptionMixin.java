package dev.maximus.cbcccaddon.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.maximus.cbcccaddon.ducks.CannonComputerFireDuck;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.cannon_control.contraption.MountedAutocannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

@Mixin(MountedAutocannonContraption.class)
public abstract class MountedAutocannonContraptionMixin implements CannonComputerFireDuck {

    @Unique
    private boolean cbcccaddon$calledByComputer = false;

    @ModifyExpressionValue(
            method = "fireShot",
            at = @At(
                    value = "INVOKE",
                    target = "Lrbasamoyai/createbigcannons/cannons/autocannon/breech/AbstractAutocannonBreechBlockEntity;canFire()Z"
            )
    )
    private boolean cbcccaddon$injectCanFire(boolean original) {
        return original || cbcccaddon$calledByComputer;
    }

    @Inject(
            method = "fireShot",
            at = @At("RETURN")
    )
    private void cbcccaddon$reset(ServerLevel level, PitchOrientedContraptionEntity entity, CallbackInfo ci) {
        this.cbcccaddon$calledByComputer = false;
    }

    @Override
    public void cbcccaddon$setCalledByComputer() {
        this.cbcccaddon$calledByComputer = true;
    }
}