package com.elthisboy.calurosanavidad.item;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import com.elthisboy.calurosanavidad.block.ModBlock;
import com.elthisboy.calurosanavidad.item.custom.WaterBalloonEmptyItem;
import com.elthisboy.calurosanavidad.item.custom.WaterBalloonFilledItem;
import com.elthisboy.calurosanavidad.item.custom.WaterGunItem;
import com.elthisboy.calurosanavidad.item.custom.WaterPistolItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CalursaNavidad.MOD_ID);

    public static final DeferredItem<Item> WATER_PISTOL = ITEMS.register("water_pistol",
            () -> new WaterPistolItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WATER_GUN = ITEMS.register("water_gun",
            () -> new WaterGunItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WATER_BALLOON_EMPTY = ITEMS.register("water_balloon_empty",
            () -> new WaterBalloonEmptyItem(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> WATER_BALLOON_FILLED = ITEMS.register("water_balloon_filled",
            () -> new WaterBalloonFilledItem(new Item.Properties().stacksTo(16)));


    public static final DeferredItem<Item> EASTER_BREAD = ITEMS.register("easter_bread",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> CHRISTMAS_DINNER = ITEMS.register("christmas_dinner",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> INFLATABLE_POOL_ITEM = ITEMS.register(
            "inflatable_pool",
            () -> new BlockItem(ModBlock.INFLATABLE_POOL.get(), new Item.Properties())
    );


    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

}
