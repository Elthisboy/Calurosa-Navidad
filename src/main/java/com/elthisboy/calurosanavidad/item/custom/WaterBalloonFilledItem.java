package com.elthisboy.calurosanavidad.item.custom;

import com.elthisboy.calurosanavidad.ModEntities.ModEntities;
import com.elthisboy.calurosanavidad.ModEntities.WaterBalloonProjectile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Properties;

public class WaterBalloonFilledItem extends Item {
    public WaterBalloonFilledItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            WaterBalloonProjectile proj = new WaterBalloonProjectile(ModEntities.WATER_BALLOON_PROJECTILE.get(), level);
            proj.setOwner(player);
            proj.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            proj.setItem(stack.copyWithCount(1));

            proj.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.2F, 1.0F);
            level.addFreshEntity(proj);

            level.playSound(null, player.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.8F, 1.0F);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
