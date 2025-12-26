package com.elthisboy.calurosanavidad.item.custom;

import com.elthisboy.calurosanavidad.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Properties;

public class WaterBalloonEmptyItem extends Item {
    public WaterBalloonEmptyItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);

        boolean canFill = false;
        boolean tookFromCauldron = false;


        if (level.getFluidState(pos).is(FluidTags.WATER)) {
            canFill = true;
        }

        if (state.is(Blocks.WATER_CAULDRON)) {
            canFill = true;
            tookFromCauldron = true;
        }

        if (!canFill) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("Necesitas una fuente de agua."), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            if (tookFromCauldron) {
                int lvl = state.getValue(LayeredCauldronBlock.LEVEL);
                if (lvl > 1) {
                    level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL, lvl - 1));
                } else {
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                }
            }

            ItemStack filled = new ItemStack(ModItems.WATER_BALLOON_FILLED.get());

            // Manejo de stacks: si era 1, reemplaza; si eran varios, consume 1 y da 1 lleno.
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            if (stack.isEmpty()) {
                player.setItemInHand(hand, filled);
            } else {
                if (!player.getInventory().add(filled)) {
                    player.drop(filled, false);
                }
            }

            level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.8F, 1.0F);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
