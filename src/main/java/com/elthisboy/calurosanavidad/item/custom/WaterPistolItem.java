package com.elthisboy.calurosanavidad.item.custom;

import com.elthisboy.calurosanavidad.block.custom.InflatablePool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WaterPistolItem extends Item {

    // ===== Traducciones (EN_US) =====
    private static final String MSG_FULL       = "message.calurosanavidad.water_shooter.full";
    private static final String MSG_EMPTY      = "message.calurosanavidad.water_shooter.empty";
    private static final String MSG_NEEDS_PUMP = "message.calurosanavidad.water_shooter.needs_pump";
    private static final String MSG_READY      = "message.calurosanavidad.water_shooter.ready";
    private static final String MSG_MODE_STREAM= "message.calurosanavidad.water_shooter.mode_stream";
    private static final String MSG_MODE_BURST = "message.calurosanavidad.water_shooter.mode_burst";

    private static final String TT_WATER   = "tooltip.calurosanavidad.water_shooter.water";
    private static final String TT_PRESS   = "tooltip.calurosanavidad.water_shooter.pressure";
    private static final String TT_MODE    = "tooltip.calurosanavidad.water_shooter.mode";
    private static final String TT_HINT1   = "tooltip.calurosanavidad.water_shooter.hint_pump";
    private static final String TT_HINT2   = "tooltip.calurosanavidad.water_shooter.hint_fire";
    private static final String MODE_STREAM_KEY = "mode.calurosanavidad.stream";
    private static final String MODE_BURST_KEY  = "mode.calurosanavidad.burst";

    // ===== NBT keys (CustomData) =====
    private static final String TAG_WATER = "Water";
    private static final String TAG_PRESSURE = "Pressure";
    private static final String TAG_MODE = "Mode";

    // Anti-spam (solo para “está lleno”)
    private static final String TAG_LAST_FULL_MSG_TICK = "LastFullMsgTick";
    private static final int FULL_MSG_COOLDOWN_TICKS = 25;

    // Modo: 0 = stream, 1 = burst
    private static final int MODE_STREAM = 0;
    private static final int MODE_BURST  = 1;

    public WaterPistolItem(Properties props) {
        super(props);
    }

    // ===== Stats (WaterGun hereda y overridea esto) =====
    protected int maxWater() { return 16; }
    protected int maxPressure() { return 4; }
    protected double range() { return 8.0; }
    protected int streamIntervalTicks() { return 2; }
    protected int burstIntervalTicks() { return 6; }
    protected double pushStrength() { return 0.35; }
    protected double pushY() { return 0.08; }
    protected int fillAmountPerUse() { return 4; } // cuánto recarga por click (agua/cauldron/piscina)

    // ===== CustomData helpers =====
    protected static CompoundTag readData(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd != null ? cd.copyTag() : new CompoundTag();
    }

    protected static void writeData(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    protected int getWater(ItemStack stack) {
        CompoundTag tag = readData(stack);
        return Mth.clamp(tag.getInt(TAG_WATER), 0, maxWater());
    }

    protected void setWater(ItemStack stack, int value) {
        CompoundTag tag = readData(stack);
        tag.putInt(TAG_WATER, Mth.clamp(value, 0, maxWater()));
        writeData(stack, tag);
    }

    protected int getPressure(ItemStack stack) {
        CompoundTag tag = readData(stack);
        return Mth.clamp(tag.getInt(TAG_PRESSURE), 0, maxPressure());
    }

    protected void setPressure(ItemStack stack, int value) {
        CompoundTag tag = readData(stack);
        tag.putInt(TAG_PRESSURE, Mth.clamp(value, 0, maxPressure()));
        writeData(stack, tag);
    }

    protected int getMode(ItemStack stack) {
        CompoundTag tag = readData(stack);
        int m = tag.getInt(TAG_MODE);
        return (m == MODE_BURST) ? MODE_BURST : MODE_STREAM;
    }

    /** Lo usa el packet del keybind (sirve también para WaterGun porque hereda). */
    public Component toggleModeAndGetMessage(ItemStack stack) {
        CompoundTag tag = readData(stack);
        int mode = tag.getInt(TAG_MODE);
        int next = (mode == MODE_STREAM) ? MODE_BURST : MODE_STREAM;
        tag.putInt(TAG_MODE, next);
        writeData(stack, tag);

        return Component.translatable(next == MODE_STREAM ? MSG_MODE_STREAM : MSG_MODE_BURST);
    }

    // ===== Durability bar estilo “agua” =====
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0f * getWater(stack) / (float) maxWater());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x3F76E4; // azul
    }

    // ===== Tooltip traducible =====
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int w = getWater(stack);
        int p = getPressure(stack);
        int mode = getMode(stack);

        tooltip.add(Component.translatable(TT_WATER, w, maxWater()));
        tooltip.add(Component.translatable(TT_PRESS, p, maxPressure()));
        tooltip.add(Component.translatable(TT_MODE, Component.translatable(mode == MODE_STREAM ? MODE_STREAM_KEY : MODE_BURST_KEY)));
        tooltip.add(Component.translatable(TT_HINT1));
        tooltip.add(Component.translatable(TT_HINT2));
    }

    // ===== Animación de uso =====
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    // ===== Rellenar desde agua / caldero / piscina =====
    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack = ctx.getItemInHand();

        BlockPos clicked = ctx.getClickedPos();
        BlockPos target = clicked;

        // similar al bucket: si el bloque clickeado no es agua, intenta el bloque “adelante”
        if (!level.getFluidState(target).is(Fluids.WATER)) {
            BlockPos forward = clicked.relative(ctx.getClickedFace());
            if (level.getFluidState(forward).is(Fluids.WATER)) target = forward;
        }

        BlockState state = level.getBlockState(clicked);

        // Si está lleno y el jugador intenta rellenar desde una fuente válida -> mensaje corto con cooldown
        if (isValidFillSource(level, clicked, target, state) && getWater(stack) >= maxWater()) {
            if (!level.isClientSide) trySendFullMessage(level, player, stack);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (getWater(stack) >= maxWater()) return InteractionResult.PASS;

        boolean filled = false;

        // 1) Piscina (tu bloque)
        if (state.getBlock() instanceof InflatablePool) {
            int poolLevel = state.getValue(InflatablePool.LEVEL);
            if (poolLevel > 0) {
                int add = Math.min(fillAmountPerUse(), maxWater() - getWater(stack));

                if (!level.isClientSide) {
                    setWater(stack, getWater(stack) + add);

                    int newLevel = Math.max(0, poolLevel - 1);
                    level.setBlock(clicked, state.setValue(InflatablePool.LEVEL, newLevel), 3);

                    level.playSound(null, clicked, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                filled = true;
            }
        }
        // 2) Caldero con agua
        else if (state.is(Blocks.WATER_CAULDRON)) {
            int cauldronLevel = state.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL);
            if (cauldronLevel > 0) {
                int add = Math.min(fillAmountPerUse(), maxWater() - getWater(stack));

                if (!level.isClientSide) {
                    setWater(stack, getWater(stack) + add);

                    int newLevel = cauldronLevel - 1;
                    if (newLevel <= 0) {
                        level.setBlock(clicked, Blocks.CAULDRON.defaultBlockState(), 3);
                    } else {
                        level.setBlock(clicked, state.setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, newLevel), 3);
                    }

                    level.playSound(null, clicked, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                filled = true;
            }
        }
        // 3) Cualquier agua
        else if (level.getFluidState(target).is(Fluids.WATER)) {
            int add = Math.min(fillAmountPerUse(), maxWater() - getWater(stack));

            if (!level.isClientSide) {
                setWater(stack, getWater(stack) + add);
                level.playSound(null, target, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            filled = true;
        }

        return filled ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }

    private boolean isValidFillSource(Level level, BlockPos clicked, BlockPos target, BlockState state) {
        if (state.getBlock() instanceof InflatablePool) return state.getValue(InflatablePool.LEVEL) > 0;
        if (state.is(Blocks.WATER_CAULDRON)) return state.getValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL) > 0;
        return level.getFluidState(target).is(Fluids.WATER);
    }

    private void trySendFullMessage(Level level, Player player, ItemStack stack) {
        CompoundTag tag = readData(stack);
        long last = tag.getLong(TAG_LAST_FULL_MSG_TICK);
        long now = level.getGameTime();
        if (now - last < FULL_MSG_COOLDOWN_TICKS) return;

        tag.putLong(TAG_LAST_FULL_MSG_TICK, now);
        writeData(stack, tag);

        player.displayClientMessage(Component.translatable(MSG_FULL, stack.getHoverName()), true);
    }

    // ===== Click derecho en aire: SHIFT = bombear, sin SHIFT = disparar =====
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        int water = getWater(stack);
        int pressure = getPressure(stack);

        // SHIFT: bombear
        if (player.isShiftKeyDown()) {
            if (water <= 0) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable(MSG_EMPTY, stack.getHoverName()), true);
                }
                return InteractionResultHolder.fail(stack);
            }

            if (pressure >= maxPressure()) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable(MSG_READY), true);
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }

            if (!level.isClientSide) {
                int newP = pressure + 1;
                setPressure(stack, newP);

                level.playSound(null, player.blockPosition(), SoundEvents.PISTON_EXTEND, SoundSource.PLAYERS, 0.6f, 1.6f);

                if (newP >= maxPressure()) {
                    player.displayClientMessage(Component.translatable(MSG_READY), true);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        // Sin SHIFT: disparar (requiere agua + presión)
        if (water <= 0) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable(MSG_EMPTY, stack.getHoverName()), true);
            return InteractionResultHolder.fail(stack);
        }
        if (pressure <= 0) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable(MSG_NEEDS_PUMP), true);
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    // ===== Mientras mantienes click: dispara por ticks =====
    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (!(living instanceof Player player)) return;

        int water = getWater(stack);
        int pressure = getPressure(stack);
        if (water <= 0) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable(MSG_EMPTY, stack.getHoverName()), true);
            player.stopUsingItem();
            return;
        }
        if (pressure <= 0) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable(MSG_NEEDS_PUMP), true);
            player.stopUsingItem();
            return;
        }

        int mode = getMode(stack);
        int ticksUsed = getUseDuration(stack, player) - remainingUseDuration;

        int interval = (mode == MODE_STREAM) ? streamIntervalTicks() : burstIntervalTicks();
        if (ticksUsed % interval != 0) return;

        boolean creative = player.getAbilities().instabuild;
        if (!creative) {
            setWater(stack, water - 1);
            setPressure(stack, pressure - 1);
        }

        shoot(level, player);
    }

    protected void shoot(Level level, Player player) {
        Vec3 eye = player.getEyePosition(1.0f);
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(range()));

        BlockHitResult blockHit = level.clip(new ClipContext(
                eye, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY,
                player
        ));

        double maxDist = blockHit.getLocation().distanceTo(eye);
        Vec3 realEnd = eye.add(look.scale(maxDist));

        AABB aabb = player.getBoundingBox().expandTowards(look.scale(maxDist)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level, player, eye, realEnd, aabb,
                e -> e.isPickable() && !e.isSpectator() && e != player
        );

        Vec3 hitPoint = (entityHit != null) ? entityHit.getLocation() : blockHit.getLocation();

        if (!level.isClientSide && level instanceof ServerLevel server) {
            int steps = 8;
            for (int i = 1; i <= steps; i++) {
                double t = i / (double) steps;
                Vec3 p = eye.add(hitPoint.subtract(eye).scale(t));
                server.sendParticles(ParticleTypes.SPLASH, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
            }
        }

        if (!level.isClientSide && entityHit != null) {
            Entity target = entityHit.getEntity();
            target.clearFire();

            Vec3 push = look.scale(pushStrength());
            target.push(push.x, pushY(), push.z);
            target.hurtMarked = true;
        }

        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.25f, 1.7f);
        }
    }
}
