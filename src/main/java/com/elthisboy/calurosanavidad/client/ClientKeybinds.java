package com.elthisboy.calurosanavidad.client;

import com.elthisboy.calurosanavidad.network.ModNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

public class ClientKeybinds {
    public static final String CATEGORY = "key.categories.calurosanavidad";
    public static final String KEY_TOGGLE_MODE = "key.calurosanavidad.toggle_water_mode";

    public static final KeyMapping TOGGLE_MODE = new KeyMapping(
            KEY_TOGGLE_MODE,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );

    public static void init(net.neoforged.bus.api.IEventBus modEventBus) {
        if (!FMLEnvironment.dist.isClient()) return;

        modEventBus.addListener(ClientKeybinds::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(ClientKeybinds::onClientTick);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_MODE);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (TOGGLE_MODE.consumeClick()) {
            ModNetworking.sendToggleModeToServer();
        }
    }
}
