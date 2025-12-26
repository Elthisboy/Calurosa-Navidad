package com.elthisboy.calurosanavidad.ModEntities;

import com.elthisboy.calurosanavidad.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class WaterBalloonProjectile extends ThrowableItemProjectile {

    public WaterBalloonProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    private void spawnWaterImpactParticles(Vec3 pos) {
        if (!(level() instanceof ServerLevel server)) return;

        server.sendParticles(ParticleTypes.SPLASH,
                pos.x, pos.y, pos.z,
                25, 0.25, 0.25, 0.25, 0.02);

        server.sendParticles(ParticleTypes.FALLING_WATER,
                pos.x, pos.y, pos.z,
                12, 0.20, 0.20, 0.20, 0.01);

        level().playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS,
                0.7F, 1.1F);
    }

    private void tryPlaceSingleWater(BlockPos pos) {
        if (!(level() instanceof ServerLevel server)) return;

        if (server.dimensionType().ultraWarm()) {
            server.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                    10, 0.2, 0.1, 0.2, 0.02);
            server.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.2F);
            return;
        }

        BlockState state = server.getBlockState(pos);

        if (state.canBeReplaced() && server.getFluidState(pos).isEmpty()) {
            if (!server.getBlockState(pos.below()).isAir()) {
                server.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            }
            return;
        }

        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
            server.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.WATERLOGGED, true));
        }
    }


    private static final float WATER_CHANCE = 0.20F; // 20% (0.0 = nunca, 1.0 = siempre)

    @Override
    protected Item getDefaultItem() {
        return ModItems.WATER_BALLOON_FILLED.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);

        if (level().isClientSide) return;

        Entity target = hit.getEntity();
        Entity owner = getOwner();

        target.hurt(level().damageSources().thrown(this, owner), 0.2F);

        // empuje suave en la dirección del proyectil
        Vec3 push = this.getDeltaMovement().normalize().scale(0.35);
        target.setDeltaMovement(target.getDeltaMovement().add(push.x, 0.15, push.z));
        target.hurtMarked = true;

        spawnWaterImpactParticles(hit.getLocation());

        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);

        if (level().isClientSide) return;

        spawnWaterImpactParticles(hit.getLocation());

        ServerLevel server = (ServerLevel) level();
        if (server.random.nextFloat() < WATER_CHANCE) {
            BlockPos placePos = hit.getBlockPos().relative(hit.getDirection());
            tryPlaceSingleWater(placePos); // máximo 1 bloque
        }

        discard();
    }


    private void tryPlaceWater(Level level, BlockPos pos) {
        if (level.dimensionType().ultraWarm()) {
            ((ServerLevel) level).sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                    10, 0.2, 0.1, 0.2, 0.02);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.2F);
            return;
        }

        BlockState state = level.getBlockState(pos);

        if (state.canBeReplaced() && level.getFluidState(pos).isEmpty()) {
            BlockState below = level.getBlockState(pos.below());
            if (!below.isAir()) {
                level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            }
            return;
        }

        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.WATERLOGGED, true));
        }
    }
}
