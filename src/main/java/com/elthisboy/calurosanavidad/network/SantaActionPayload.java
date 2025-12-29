package com.elthisboy.calurosanavidad.network;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SantaActionPayload(int santaEntityId, Action action, int questId) implements CustomPacketPayload {

    public enum Action { ACCEPT, CLAIM, GIFT }

    public static final Type<SantaActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CalursaNavidad.MOD_ID, "santa_action"));

    private static Action actionFromId(int id) {
        Action[] vals = Action.values();
        return (id >= 0 && id < vals.length) ? vals[id] : Action.ACCEPT;
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, Action> ACTION_CODEC = new StreamCodec<>() {
        @Override
        public Action decode(RegistryFriendlyByteBuf buf) {
            return actionFromId(buf.readVarInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, Action value) {
            buf.writeVarInt(value.ordinal());
        }
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, SantaActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SantaActionPayload::santaEntityId,
                    ACTION_CODEC, SantaActionPayload::action,
                    ByteBufCodecs.VAR_INT, SantaActionPayload::questId,
                    SantaActionPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
