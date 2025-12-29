package com.elthisboy.calurosanavidad.event;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import com.elthisboy.calurosanavidad.ModEntities.ModEntities;
import com.elthisboy.calurosanavidad.block.ModBlock;
import com.elthisboy.calurosanavidad.item.ModItems;
import com.elthisboy.calurosanavidad.menu.ModMenus;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.elthisboy.calurosanavidad.client.screen.SantaScreen;
import com.elthisboy.calurosanavidad.client.renderer.SantaRenderer;
import com.elthisboy.calurosanavidad.ModEntities.SantaClausEntity;




@EventBusSubscriber(modid = CalursaNavidad.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)


public final class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                ItemBlockRenderTypes.setRenderLayer(ModBlock.INFLATABLE_POOL.get(), RenderType.translucent())
        );
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.WATER_GUN.get(),
                    ResourceLocation.fromNamespaceAndPath("calurosanavidad", "filled"),
                    (stack, level, entity, seed) -> isFilled(stack) ? 1.0F : 0.0F
            );
        });
    }
    // Ajusta esto a cómo guardas el agua en tu ItemStack
    private static boolean isFilled(ItemStack stack) {
        CustomData custom = stack.get(DataComponents.CUSTOM_DATA);
        if (custom == null || custom.isEmpty()) return false;
        return custom.copyTag().getInt("Water") > 0; // <- cambia "Water" si tu key es otra
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

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.WATER_BALLOON_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.SANTA_CLAUS.get(), SantaRenderer::new);

    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.SANTA_MENU.get(), SantaScreen::new);
    }
}
