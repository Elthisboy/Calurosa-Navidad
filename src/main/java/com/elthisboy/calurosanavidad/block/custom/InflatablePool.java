package com.elthisboy.calurosanavidad.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InflatablePool extends Block {

    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);
    public static final EnumProperty<PoolVariant> VARIANT = EnumProperty.create("variant", PoolVariant.class);

    /**
     * TRUE solo en el BLOQUE "codo" cuando el cluster es una L (3 bloques).
     * Esto permite usar el modelo inflatable_pool_2_walls_corner solo cuando corresponde,
     * sin cambiar tu enum PoolVariant.
     */
    public static final BooleanProperty ELBOW = BooleanProperty.create("elbow");

    public InflatablePool(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LEVEL, 0)
                .setValue(VARIANT, PoolVariant.SINGLE)
                .setValue(ELBOW, false));
    }

    // ===== Hitbox dinámica (calza con tus modelos) =====
    private static final VoxelShape HB_BASE = Block.box(0, 0, 0, 16, 1, 16);

    // N/S siempre van completos (x=0..16)
    private static final VoxelShape HB_WALL_N = Block.box(0, 1, 0, 16, 5, 1);
    private static final VoxelShape HB_WALL_S = Block.box(0, 1, 15, 16, 5, 16);

    // E/W cambian su largo si falta N o S (para coincidir con 2_walls que “toca” la esquina)
    private static final VoxelShape HB_WALL_E_Z1_15 = Block.box(15, 1, 1, 16, 5, 15);
    private static final VoxelShape HB_WALL_E_Z0_15 = Block.box(15, 1, 0, 16, 5, 15);
    private static final VoxelShape HB_WALL_E_Z1_16 = Block.box(15, 1, 1, 16, 5, 16);
    private static final VoxelShape HB_WALL_E_Z0_16 = Block.box(15, 1, 0, 16, 5, 16);

    private static final VoxelShape HB_WALL_W_Z1_15 = Block.box(0, 1, 1, 1, 5, 15);
    private static final VoxelShape HB_WALL_W_Z0_15 = Block.box(0, 1, 0, 1, 5, 15);
    private static final VoxelShape HB_WALL_W_Z1_16 = Block.box(0, 1, 1, 1, 5, 16);
    private static final VoxelShape HB_WALL_W_Z0_16 = Block.box(0, 1, 0, 1, 5, 16);

    // Tapón 1x1 (para el modelo 2_walls_corner cuando ELBOW=true)
    private static final VoxelShape HB_ELBOW_NW = Block.box(0, 1, 0, 1, 5, 1);
    private static final VoxelShape HB_ELBOW_NE = Block.box(15, 1, 0, 16, 5, 1);
    private static final VoxelShape HB_ELBOW_SE = Block.box(15, 1, 15, 16, 5, 16);
    private static final VoxelShape HB_ELBOW_SW = Block.box(0, 1, 15, 1, 5, 16);

    private boolean isPool(BlockState s) {
        return s.getBlock() instanceof InflatablePool;
    }

    private static boolean isPool(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof InflatablePool;
    }

    private static VoxelShape eastWall(boolean wallN, boolean wallS) {
        if (wallN && wallS) return HB_WALL_E_Z1_15;
        if (!wallN && wallS) return HB_WALL_E_Z0_15;
        if (wallN && !wallS) return HB_WALL_E_Z1_16;
        return HB_WALL_E_Z0_16;
    }

    private static VoxelShape westWall(boolean wallN, boolean wallS) {
        if (wallN && wallS) return HB_WALL_W_Z1_15;
        if (!wallN && wallS) return HB_WALL_W_Z0_15;
        if (wallN && !wallS) return HB_WALL_W_Z1_16;
        return HB_WALL_W_Z0_16;
    }

    private VoxelShape elbowCap(BlockGetter level, BlockPos pos, boolean n, boolean e, boolean s, boolean w) {
        BlockState self = level.getBlockState(pos);
        if (!isPool(self) || !self.getValue(ELBOW)) return Shapes.empty();

        // El codo se define por dos vecinos ADYACENTES.
        // El tapón va en la esquina "faltante" (diagonal donde NO hay bloque en la L).
        if (n && w) return HB_ELBOW_SE; // vecinos N+W -> falta SE
        if (n && e) return HB_ELBOW_SW; // vecinos N+E -> falta SW
        if (s && e) return HB_ELBOW_NW; // vecinos S+E -> falta NW
        if (s && w) return HB_ELBOW_NE; // vecinos S+W -> falta NE
        return Shapes.empty();
    }

    private VoxelShape dynamicShape(BlockGetter level, BlockPos pos) {
        boolean n = isPool(level.getBlockState(pos.north()));
        boolean e = isPool(level.getBlockState(pos.east()));
        boolean s = isPool(level.getBlockState(pos.south()));
        boolean w = isPool(level.getBlockState(pos.west()));

        boolean wallN = !n;
        boolean wallS = !s;
        boolean wallE = !e;
        boolean wallW = !w;

        VoxelShape shape = HB_BASE;

        if (wallN) shape = Shapes.or(shape, HB_WALL_N);
        if (wallS) shape = Shapes.or(shape, HB_WALL_S);

        if (wallE) shape = Shapes.or(shape, eastWall(wallN, wallS));
        if (wallW) shape = Shapes.or(shape, westWall(wallN, wallS));

        VoxelShape cap = elbowCap(level, pos, n, e, s, w);
        if (!cap.isEmpty()) shape = Shapes.or(shape, cap);

        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return dynamicShape(level, pos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return dynamicShape(level, pos);
    }

    // ===== Estado =====
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL, VARIANT, ELBOW);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();

        // no permitir clusters > 2x2 (máx 4 bloques)
        if (!fitsMax2x2(ctx.getLevel(), pos)) return null;

        PoolVariant v = computeVariant(ctx.getLevel(), pos);
        return this.defaultBlockState()
                .setValue(LEVEL, 0)
                .setValue(VARIANT, v)
                .setValue(ELBOW, false);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction dir, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (dir.getAxis().isHorizontal()) {
            PoolVariant v = computeVariant(level, pos);
            boolean elbow = computeElbow(level, pos);

            if (state.getValue(VARIANT) != v || state.getValue(ELBOW) != elbow) {
                return state.setValue(VARIANT, v).setValue(ELBOW, elbow);
            }
        }
        return state;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) refreshAround(level, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!level.isClientSide) refreshAround(level, pos);
    }

    private void refreshAround(Level level, BlockPos center) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = center.offset(dx, 0, dz);
                BlockState s = level.getBlockState(p);
                if (!isPool(s)) continue;

                PoolVariant v = computeVariant(level, p);
                boolean elbow = computeElbow(level, p);

                if (s.getValue(VARIANT) != v || s.getValue(ELBOW) != elbow) {
                    level.setBlock(p, s.setValue(VARIANT, v).setValue(ELBOW, elbow), 2);
                }
            }
        }
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

        // con el límite 2x2, 3 o 4 vecinos no debería pasar; fallback:
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
                if (maxX - minX > 1) return false;
                if (maxZ - minZ > 1) return false;
            }
        }

        return true;
    }

    // ===== Cluster helpers (agua compartida) =====
    private static List<BlockPos> getConnectedCluster(LevelAccessor level, BlockPos origin) {
        List<BlockPos> out = new ArrayList<>();
        if (!isPool(level, origin)) return out;

        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> q = new ArrayDeque<>();

        visited.add(origin);
        q.add(origin);

        int minX = origin.getX(), maxX = origin.getX();
        int minZ = origin.getZ(), maxZ = origin.getZ();

        while (!q.isEmpty() && visited.size() <= 4) {
            BlockPos p = q.removeFirst();
            out.add(p);

            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos n = p.relative(d);
                if (visited.contains(n)) continue;
                if (!isPool(level, n)) continue;

                // mantener bounding 2x2
                int nMinX = Math.min(minX, n.getX());
                int nMaxX = Math.max(maxX, n.getX());
                int nMinZ = Math.min(minZ, n.getZ());
                int nMaxZ = Math.max(maxZ, n.getZ());
                if (nMaxX - nMinX > 1) continue;
                if (nMaxZ - nMinZ > 1) continue;

                visited.add(n);
                q.add(n);

                minX = nMinX;
                maxX = nMaxX;
                minZ = nMinZ;
                maxZ = nMaxZ;
            }
        }

        // orden estable (para que el reparto sea consistente)
        out.sort(Comparator.<BlockPos>comparingInt(p -> p.getX())
                .thenComparingInt(p -> p.getZ())
                .thenComparingInt(p -> p.getY()));
        return out;
    }

    private static int totalPoints(LevelAccessor level, List<BlockPos> cluster) {
        int sum = 0;
        for (BlockPos p : cluster) {
            BlockState s = level.getBlockState(p);
            if (!(s.getBlock() instanceof InflatablePool)) continue;
            sum += s.getValue(LEVEL);
        }
        return sum;
    }

    /**
     * Reparte "total" puntos de agua de la forma más pareja posible entre los bloques del cluster.
     * (diferencia máxima de 1 entre bloques).
     */
    private static void applyTotal(LevelAccessor level, List<BlockPos> cluster, int total) {
        int size = cluster.size();
        if (size <= 0) return;

        int cap = size * 3;
        if (total < 0) total = 0;
        if (total > cap) total = cap;

        int base = total / size;
        int rem = total % size;

        for (int i = 0; i < size; i++) {
            BlockPos p = cluster.get(i);
            BlockState s = level.getBlockState(p);
            if (!(s.getBlock() instanceof InflatablePool)) continue;

            int newLevel = base + (i < rem ? 1 : 0);
            int old = s.getValue(LEVEL);
            if (newLevel != old) {
                level.setBlock(p, s.setValue(LEVEL, newLevel), 3);
            }
        }
    }

    private boolean computeElbow(LevelAccessor level, BlockPos pos) {
        BlockState self = level.getBlockState(pos);
        if (!isPool(self)) return false;

        boolean n = isPool(level.getBlockState(pos.north()));
        boolean e = isPool(level.getBlockState(pos.east()));
        boolean s = isPool(level.getBlockState(pos.south()));
        boolean w = isPool(level.getBlockState(pos.west()));

        int c = (n ? 1 : 0) + (e ? 1 : 0) + (s ? 1 : 0) + (w ? 1 : 0);
        if (c != 2) return false;

        // si son opuestos, no es codo
        if ((n && s) || (e && w)) return false;

        // solo cuando el cluster es de 3 (L)
        List<BlockPos> cluster = getConnectedCluster(level, pos);
        return cluster.size() == 3;
    }

    /**
     * ✅ Para tus PISTOLAS DE AGUA:
     * Baja el nivel "de toda la piscina" (cluster) exactamente 1 "capa":
     * - 1x1: -1 punto
     * - 1x2: -2 puntos (1 por bloque)
     * - L (3): -3 puntos
     * - 2x2 (4): -4 puntos
     *
     * Retorna true si pudo (hay suficiente agua para bajar 1 capa completa).
     */
    public static boolean tryDrainOneLayerForGun(LevelAccessor level, BlockPos origin) {
        List<BlockPos> cluster = getConnectedCluster(level, origin);
        if (cluster.isEmpty()) return false;

        int size = cluster.size();
        int total = totalPoints(level, cluster);

        if (total < size) return false; // no hay suficiente para bajar una capa completa

        applyTotal(level, cluster, total - size);
        return true;
    }

    // ===== Interacción con BALDES (mantiene tu lógica de "4 baldes para 2x2") =====
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {

        // Water Bucket: +3 puntos repartidos en el cluster (equilibrado)
        if (stack.is(Items.WATER_BUCKET)) {
            List<BlockPos> cluster = getConnectedCluster(level, pos);
            if (cluster.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            int size = cluster.size();
            int total = totalPoints(level, cluster);
            int cap = size * 3;

            // ✅ si ya está lleno, NO permitir “poner agua encima”
            if (total >= cap) {
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }

            if (!level.isClientSide) {
                applyTotal(level, cluster, Math.min(cap, total + 3));

                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }

                player.awardStat(Stats.ITEM_USED.get(Items.WATER_BUCKET));
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Bucket vacío: -3 puntos del cluster -> entrega water bucket
        if (stack.is(Items.BUCKET)) {
            List<BlockPos> cluster = getConnectedCluster(level, pos);
            if (cluster.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            int total = totalPoints(level, cluster);
            if (total < 3) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            if (!level.isClientSide) {
                applyTotal(level, cluster, total - 3);

                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
                }

                player.awardStat(Stats.ITEM_USED.get(Items.BUCKET));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
