package com.elthisboy.calurosanavidad.event;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import com.elthisboy.calurosanavidad.block.ModBlock;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = CalursaNavidad.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)


public final class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                ItemBlockRenderTypes.setRenderLayer(ModBlock.INFLATABLE_POOL.get(), RenderType.translucent())
        );
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (level == null || pos == null) return 0x3F76E4; // fallback (item/GUI)
            return (tintIndex == 0) ? net.minecraft.client.renderer.BiomeColors.getAverageWaterColor(level, pos) : 0xFFFFFFFF;
        }, ModBlock.INFLATABLE_POOL.get());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        final int DEFAULT_WATER = 0x3F76E4;
        event.register((stack, tintIndex) -> tintIndex == 0 ? DEFAULT_WATER : -1,
                ModBlock.INFLATABLE_POOL.get().asItem());
    }
}
