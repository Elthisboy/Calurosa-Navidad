package com.elthisboy.calurosanavidad;

import com.elthisboy.calurosanavidad.ModEntities.ModEntities;
import com.elthisboy.calurosanavidad.attachment.ModAttachments;
import com.elthisboy.calurosanavidad.block.ModBlock;
import com.elthisboy.calurosanavidad.client.ClientKeybinds;
import com.elthisboy.calurosanavidad.item.ModItems;
import com.elthisboy.calurosanavidad.menu.ModMenus;
import com.elthisboy.calurosanavidad.network.ModNetworking;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CalursaNavidad.MOD_ID)
public class CalursaNavidad {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "calurosanavidad";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CalursaNavidad(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);


        ModItems.register(modEventBus);
        ModBlock.register(modEventBus);
        ModCreativeModeTab.register(modEventBus);
        ModNetworking.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModAttachments.ATTACHMENTS.register(modEventBus);


        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientKeybinds.init(modEventBus);
        }

        modEventBus.addListener(this::registerEntityAttributes);

        // Register the item to a creative tab
        //modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
}

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SANTA_CLAUS.get(), ModEntities.createAttributes());
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
