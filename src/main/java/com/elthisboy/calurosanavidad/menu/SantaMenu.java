package com.elthisboy.calurosanavidad.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class SantaMenu extends AbstractContainerMenu {
    private final int santaEntityId;

    // CLIENT ctor (lo llama IForgeMenuType.create con buf)
    public SantaMenu(int containerId, Inventory inv, FriendlyByteBuf buf) {
        this(containerId, inv, buf.readVarInt());
    }

    // SERVER ctor (lo usas tú al abrir)
    public SantaMenu(int containerId, Inventory inv, int santaEntityId) {
        super(ModMenus.SANTA_MENU.get(), containerId);
        this.santaEntityId = santaEntityId;
    }

    public int getSantaEntityId() {
        return santaEntityId;
    }

    @Override
    public boolean stillValid(Player player) {
        // Validación suave (la validación fuerte la hacemos en el handler del packet)
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public Entity getSantaClientSide(Player player) {
        return player.level().getEntity(santaEntityId);
    }
}