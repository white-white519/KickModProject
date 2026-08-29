package com.hite.kickmod;

import com.hite.kickmod.registry.ModItems;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(KickMod.MODID)
public class KickMod {
    public static final String MODID = "kickmod";
    public static final Logger LOGGER = LogManager.getLogger();

    public KickMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        GeckoLib.initialize();
        NetworkHandler.register();

        ModItems.ITEMS.register(modBus);

        modBus.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("KickMod client setup completed!");
    }
}