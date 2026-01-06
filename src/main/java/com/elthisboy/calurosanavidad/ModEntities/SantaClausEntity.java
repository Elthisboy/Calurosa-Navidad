package com.elthisboy.calurosanavidad.ModEntities;

import com.elthisboy.calurosanavidad.attachment.ModAttachments;
import com.elthisboy.calurosanavidad.attachment.SantaQuestData;
import com.elthisboy.calurosanavidad.menu.SantaMenu;
import com.elthisboy.calurosanavidad.quest.SantaDailyLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;

public class SantaClausEntity extends PathfinderMob implements MenuProvider {

    protected SantaClausEntity(EntityType<? extends PathfinderMob> type, net.minecraft.world.level.Level level) {
        super(type, level);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer sp) {
            // Normaliza reset diario al abrir el menú para que el GUI muestre todo actualizado
            long nowMs = System.currentTimeMillis();
            SantaQuestData data0 = sp.getData(ModAttachments.SANTA_DATA.get());
            SantaQuestData data = SantaDailyLogic.normalizeQuestReset(data0, nowMs);
            if (data != data0) {
                sp.setData(ModAttachments.SANTA_DATA.get(), data);
            }

            ((IPlayerExtension) sp).openMenu(this, buf -> buf.writeVarInt(this.getId()));
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.calurosanavidad.santa_claus");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new SantaMenu(containerId, playerInv, this.getId());
    }
}
