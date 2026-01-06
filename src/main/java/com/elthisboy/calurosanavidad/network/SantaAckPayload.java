package com.elthisboy.calurosanavidad.network;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public record SantaAckPayload(int santaEntityId, int questId, boolean success) implements CustomPacketPayload {

    public static final Type<SantaAckPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CalursaNavidad.MOD_ID, "santa_ack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SantaAckPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SantaAckPayload::santaEntityId,
                    ByteBufCodecs.VAR_INT, SantaAckPayload::questId,
                    ByteBufCodecs.BOOL, SantaAckPayload::success,
                    SantaAckPayload::new
            );

    public static void handle(SantaAckPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> SantaClientState.pushAck(payload));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
