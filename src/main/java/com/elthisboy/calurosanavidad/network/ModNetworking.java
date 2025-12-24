package com.elthisboy.calurosanavidad.network;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class ModNetworking {
    private static boolean REGISTERED = false;

    public static void register(IEventBus modEventBus) {
        if (REGISTERED) return;
        REGISTERED = true;
        modEventBus.addListener(ModNetworking::onRegisterPayloads);
    }

    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(CalursaNavidad.MOD_ID).versioned("1");
        registrar.playToServer(
                ToggleWaterPistolModePayload.TYPE,
                ToggleWaterPistolModePayload.STREAM_CODEC,
                ToggleWaterPistolModePayload::handle
        );
    }

    public static void sendToggleModeToServer() {
        PacketDistributor.sendToServer(ToggleWaterPistolModePayload.INSTANCE);
    }
}
