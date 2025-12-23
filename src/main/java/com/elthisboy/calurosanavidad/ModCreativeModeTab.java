package com.elthisboy.calurosanavidad;

import com.elthisboy.calurosanavidad.block.ModBlock;
import com.elthisboy.calurosanavidad.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CalursaNavidad.MOD_ID);


    public static final Supplier<CreativeModeTab> CALUROSA_NAVIDAD_ITEMS_TAB = CREATIVE_MODE_TABS.register("titlegroup",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.WATER_PISTOL.get()))
                    .title(Component.translatable("creativetab.elthisboy.titlegroup"))
                    .displayItems((itemDisplayParameters, output) -> {


                        output.accept(ModItems.WATER_PISTOL.get());
                        output.accept(ModItems.WATER_BALLON.get());
                        output.accept(ModItems.CHRISTMAS_DINNER.get());
                        output.accept(ModItems.EASTER_BREAD.get());
                        output.accept(ModBlock.INFLATABLE_POOL.get());



                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
