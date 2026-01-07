package com.elthisboy.calurosanavidad.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class InflatablePool extends Block {

    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);
    public static final EnumProperty<PoolVariant> VARIANT = EnumProperty.create("variant", PoolVariant.class);


    // Hitbox dinámica: se quitan paredes internas cuando hay otra piscina al lado
    private static final VoxelShape HITBOX_BASE = Block.box(0, 0, 0, 16, 1, 16);
    private static final VoxelShape HITBOX_WALL_N = Block.box(0, 1, 0, 16, 5, 1);
    private static final VoxelShape HITBOX_WALL_S = Block.box(0, 1, 15, 16, 5, 16);
    private static final VoxelShape HITBOX_WALL_E = Block.box(15, 1, 1, 16, 5, 15);
    private static final VoxelShape HITBOX_WALL_W = Block.box(0, 1, 1, 1, 5, 15);

    private VoxelShape hitboxFor(BlockGetter level, BlockPos pos) {
        boolean n = isPool(level.getBlockState(pos.north()));
        boolean e = isPool(level.getBlockState(pos.east()));
        boolean s = isPool(level.getBlockState(pos.south()));
        boolean w = isPool(level.getBlockState(pos.west()));

        VoxelShape shape = HITBOX_BASE;
        if (!n) shape = Shapes.or(shape, HITBOX_WALL_N);
        if (!s) shape = Shapes.or(shape, HITBOX_WALL_S);
        if (!e) shape = Shapes.or(shape, HITBOX_WALL_E);
        if (!w) shape = Shapes.or(shape, HITBOX_WALL_W);

        return shape;
    }


    // base + 4 paredes (tu modelo "inflatable_pool" completo)
    private static final VoxelShape SHAPE_SINGLE = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),   // base
            Block.box(0, 1, 0, 16, 5, 1),    // norte
            Block.box(0, 1, 15, 16, 5, 16),  // sur
            Block.box(15, 1, 1, 16, 5, 15),  // este
            Block.box(0, 1, 1, 1, 5, 15)     // oeste
    );

    // 3 paredes (abierto hacia X)
    private static final VoxelShape SHAPE_END_N = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 1, 15, 16, 5, 16),
            Block.box(15, 1, 1, 16, 5, 15),
            Block.box(0, 1, 1, 1, 5, 15)
    );

    private static final VoxelShape SHAPE_END_E = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 1, 0, 16, 5, 1),
            Block.box(0, 1, 15, 16, 5, 16),
            Block.box(0, 1, 1, 1, 5, 15)
    );

    private static final VoxelShape SHAPE_END_S = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 1, 0, 16, 5, 1),
            Block.box(15, 1, 1, 16, 5, 15),
            Block.box(0, 1, 1, 1, 5, 15)
    );

    private static final VoxelShape SHAPE_END_W = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 1, 0, 16, 5, 1),
            Block.box(0, 1, 15, 16, 5, 16),
            Block.box(15, 1, 1, 16, 5, 15)
    );

    // 2 paredes en esquina (abierto hacia vecinos)
    // CORNER_NE = vecinos al norte+este => faltan paredes N y E, quedan S+W
    private static final VoxelShape SHAPE_CORNER_NE = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 1, 15, 16, 5, 16),
            Block.box(0, 1, 1, 1, 5, 15)
    );

    // CORNER_NW = vecinos al norte+oeste => faltan N y W, quedan S+E
    private static final VoxelShape SHAPE_CORNER_NW = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 1, 15, 16, 5, 16),
            Block.box(15, 1, 1, 16, 5, 15)
    );

    // CORNER_SE = vecinos al sur+este => faltan S y E, quedan N+W
    private static final VoxelShape SHAPE_CORNER_SE = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 1, 0, 16, 5, 1),
            Block.box(0, 1, 1, 1, 5, 15)
    );

    // CORNER_SW = vecinos al sur+oeste => faltan S y W, quedan N+E
    private static final VoxelShape SHAPE_CORNER_SW = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 1, 0, 16, 5, 1),
            Block.box(15, 1, 1, 16, 5, 15)
    );

    // 2 paredes opuestas (por si algún día lo usas)
    // STRAIGHT_NS = vecinos N+S => faltan N y S, quedan E+W
    private static final VoxelShape SHAPE_STRAIGHT_NS = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(15, 1, 1, 16, 5, 15),
            Block.box(0, 1, 1, 1, 5, 15)
    );

    // STRAIGHT_EW = vecinos E+W => faltan E y W, quedan N+S
    private static final VoxelShape SHAPE_STRAIGHT_EW = Shapes.or(
            Block.box(0, 0, 0, 16, 1, 16),
            Block.box(0, 1, 0, 16, 5, 1),
            Block.box(0, 1, 15, 16, 5, 16)
    );

    public InflatablePool(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LEVEL, 0)
                .setValue(VARIANT, PoolVariant.SINGLE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, VARIANT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return hitboxFor(level, pos);

    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return hitboxFor(level, pos);

    }

    private static VoxelShape shapeFor(PoolVariant v) {
        return switch (v) {
            case SINGLE -> SHAPE_SINGLE;

            case END_N -> SHAPE_END_N;
            case END_E -> SHAPE_END_E;
            case END_S -> SHAPE_END_S;
            case END_W -> SHAPE_END_W;

            case CORNER_NE -> SHAPE_CORNER_NE;
            case CORNER_NW -> SHAPE_CORNER_NW;
            case CORNER_SE -> SHAPE_CORNER_SE;
            case CORNER_SW -> SHAPE_CORNER_SW;

            case STRAIGHT_NS -> SHAPE_STRAIGHT_NS;
            case STRAIGHT_EW -> SHAPE_STRAIGHT_EW;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();

        // límite: máximo 2x2 y máximo 4 bloques conectados
        if (!fitsMax2x2(ctx.getLevel(), pos)) return null;

        return this.defaultBlockState()
                .setValue(LEVEL, 0)
                .setValue(VARIANT, computeVariant(ctx.getLevel(), pos));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (dir.getAxis().isHorizontal()) {
            return state.setValue(VARIANT, computeVariant(level, pos));
        }
        return state;
    }

    private boolean isPool(BlockState s) {
        return s.getBlock() instanceof InflatablePool;
    }

    private PoolVariant computeVariant(LevelAccessor level, BlockPos pos) {
        boolean n = isPool(level.getBlockState(pos.north()));
        boolean e = isPool(level.getBlockState(pos.east()));
        boolean s = isPool(level.getBlockState(pos.south()));
        boolean w = isPool(level.getBlockState(pos.west()));

        int c = (n ? 1 : 0) + (e ? 1 : 0) + (s ? 1 : 0) + (w ? 1 : 0);

        if (c == 0) return PoolVariant.SINGLE;

        if (c == 1) {
            if (n) return PoolVariant.END_N;
            if (e) return PoolVariant.END_E;
            if (s) return PoolVariant.END_S;
            return PoolVariant.END_W;
        }

        if (c == 2) {
            if (n && s) return PoolVariant.STRAIGHT_NS;
            if (e && w) return PoolVariant.STRAIGHT_EW;

            if (n && e) return PoolVariant.CORNER_NE;
            if (n && w) return PoolVariant.CORNER_NW;
            if (s && e) return PoolVariant.CORNER_SE;
            return PoolVariant.CORNER_SW;
        }

        // con el límite 2x2 esto no debería ocurrir
        return PoolVariant.SINGLE;
    }

    private boolean fitsMax2x2(LevelAccessor level, BlockPos placingPos) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();

        visited.add(placingPos);
        q.add(placingPos);

        int minX = placingPos.getX(), maxX = placingPos.getX();
        int minZ = placingPos.getZ(), maxZ = placingPos.getZ();

        while (!q.isEmpty()) {
            BlockPos p = q.removeFirst();

            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos n = p.relative(d);
                if (visited.contains(n)) continue;

                if (!isPool(level.getBlockState(n))) continue;

                visited.add(n);
                q.add(n);

                minX = Math.min(minX, n.getX());
                maxX = Math.max(maxX, n.getX());
                minZ = Math.min(minZ, n.getZ());
                maxZ = Math.max(maxZ, n.getZ());

                if (visited.size() > 4) return false;
                if (maxX - minX > 1) return false; // ancho > 2
                if (maxZ - minZ > 1) return false; // largo > 2
            }
        }

        return true;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        int current = state.getValue(LEVEL);

        // llenar con balde de agua -> nivel 3 (mantiene VARIANT)
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

        // sacar agua solo si está lleno (nivel 3) -> vuelve a 0 (mantiene VARIANT)
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
