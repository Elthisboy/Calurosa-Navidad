package com.elthisboy.calurosanavidad.network;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private static boolean REGISTERED = false;

    public static void register(IEventBus modEventBus) {
        if (REGISTERED) return;
        REGISTERED = true;

        modEventBus.addListener(ModNetworking::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar(CalursaNavidad.MOD_ID).versioned("2");

        r.playToServer(
                ToggleWaterPistolModePayload.TYPE,
                ToggleWaterPistolModePayload.STREAM_CODEC,
                ToggleWaterPistolModePayload::handle
        );

        r.playToServer(
                SantaActionPayload.TYPE,
                SantaActionPayload.STREAM_CODEC,
                SantaServerHandler::handle
        );

        // ACK server -> client para cerrar la pantalla solo si el server confirma.
        r.playToClient(
                SantaAckPayload.TYPE,
                SantaAckPayload.STREAM_CODEC,
                SantaAckPayload::handle
        );
    }

    public static void sendToggleModeToServer() {
        PacketDistributor.sendToServer(ToggleWaterPistolModePayload.INSTANCE);
    }

    private ModNetworking() {}
}
