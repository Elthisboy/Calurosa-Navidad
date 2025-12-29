package com.elthisboy.calurosanavidad.menu;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;


public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, CalursaNavidad.MOD_ID);

    // Si tu SantaMenu lee extraData (entityId, etc.), usa IMenuTypeExtension.create
    public static final Supplier<MenuType<SantaMenu>> SANTA_MENU =
            MENUS.register("santa_menu", () -> IMenuTypeExtension.create(SantaMenu::new));
}