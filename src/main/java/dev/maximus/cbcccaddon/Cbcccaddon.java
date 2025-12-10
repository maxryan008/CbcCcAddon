package dev.maximus.cbcccaddon;

import dev.maximus.cbcccaddon.peripheral.CbcPeripheralRegistration;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cbcccaddon implements ModInitializer {

    public static final String MOD_ID = "cbcccaddon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[{}] Initialising CBC + CC:Tweaked addon", MOD_ID);

        if (FabricLoader.getInstance().isModLoaded("createbigcannons")) {
            CbcPeripheralRegistration.registerPeripherals();
        } else {
            LOGGER.warn("[{}] createbigcannons not present, skipping peripheral registration", MOD_ID);
        }
    }
}