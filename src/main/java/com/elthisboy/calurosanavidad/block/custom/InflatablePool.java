package com.elthisboy.calurosanavidad.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ItemInteractionResult;


public class InflatablePool extends Block {

    public InflatablePool(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);


    private static final VoxelShape SHAPE = Shapes.or(
            // base: [0,0,0] -> [16,1,16]
            Block.box(0, 0, 0, 16, 1, 16),

            // pared norte: [0,1,0] -> [16,5,1]
            Block.box(0, 1, 0, 16, 5, 1),

            // pared sur: [0,1,15] -> [16,5,16]
            Block.box(0, 1, 15, 16, 5, 16),

            // pared este: [15,1,1] -> [16,5,15]
            Block.box(15, 1, 1, 16, 5, 15),

            // pared oeste: [0,1,1] -> [1,5,15]
            Block.box(0, 1, 1, 1, 5, 15)
    );


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(LEVEL, 0);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        int current = state.getValue(LEVEL);

        if (stack.is(Items.WATER_BUCKET) && current < 3) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LEVEL, 3), 3);

                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }

                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        if (stack.is(Items.BUCKET) && current == 3) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LEVEL, 0), 3);

                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
                }

                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
