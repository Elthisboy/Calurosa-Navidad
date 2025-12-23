package com.elthisboy.calurosanavidad.block;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import com.elthisboy.calurosanavidad.block.custom.InflatablePool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlock {


    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CalursaNavidad.MOD_ID);

    public static final DeferredBlock<Block> INFLATABLE_POOL = BLOCKS.register("inflatable_pool",
            () -> new InflatablePool(BlockBehaviour.Properties.of().strength(0.75F, 0.75F).noOcclusion()));

    public static void register(IEventBus iEventBus) {
        BLOCKS.register(iEventBus);
    }

}
