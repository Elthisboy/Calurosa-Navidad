package com.elthisboy.calurosanavidad.network;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import com.elthisboy.calurosanavidad.item.custom.WaterPistolItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleWaterPistolModePayload() implements CustomPacketPayload {
    public static final ToggleWaterPistolModePayload INSTANCE = new ToggleWaterPistolModePayload();

    public static final Type<ToggleWaterPistolModePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CalursaNavidad.MOD_ID, "toggle_water_pistol_mode"));

    public static final StreamCodec<FriendlyByteBuf, ToggleWaterPistolModePayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ToggleWaterPistolModePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = (Player) ctx.player();
            if (player == null) return;

            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof WaterPistolItem pistol)) {
                stack = player.getOffhandItem();
                if (!(stack.getItem() instanceof WaterPistolItem pistol2)) return;
                player.displayClientMessage(pistol2.toggleModeAndGetMessage(stack), true);
                return;
            }

            player.displayClientMessage(pistol.toggleModeAndGetMessage(stack), true);
        });
    }
}
