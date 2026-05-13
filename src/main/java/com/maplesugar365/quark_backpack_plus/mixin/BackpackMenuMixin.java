package com.maplesugar365.quark_backpack_plus.mixin;

import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.addons.oddities.inventory.BackpackContainer;
import org.violetmoon.quark.addons.oddities.inventory.BackpackMenu;
import org.violetmoon.quark.addons.oddities.inventory.slot.BackpackSlot;
import org.violetmoon.quark.addons.oddities.module.BackpackModule;
import org.violetmoon.quark.base.handler.SimilarBlockTypeHandler;
import org.violetmoon.quark.content.management.module.ExpandedItemInteractionsModule;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

@Mixin(BackpackMenu.class)
public abstract class BackpackMenuMixin extends InventoryMenu {

    public BackpackMenuMixin(Inventory playerInventory, boolean active, Player owner) {
        super(playerInventory, active, owner);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void init(int windowId, Player player, CallbackInfo ci) {
        Optional<SlotResult> result = CuriosApi.getCuriosInventory(player).map(inv -> inv.findFirstCurio(BackpackModule.backpack)).orElse(Optional.empty());

        Slot anchor = slots.get(9);
        int left = anchor.x;
        int top = anchor.y - 58;

        if (player.getInventory().armor.get(2).getItem() != BackpackModule.backpack && result.isPresent()) {
            BackpackContainer backpackInv = new BackpackContainer(result.get().stack());

            for (int i = 0; i < 3; ++i)
                for (int j = 0; j < 9; ++j) {
                    int k = j + i * 9;
                    addSlot(new BackpackSlot(backpackInv, k, left + j * 18, top + i * 18));
                }
        }
    }

    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void onRightClickOpenShulkerBox(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (clickType != ClickType.PICKUP || button != 1) return;
        if (!ExpandedItemInteractionsModule.allowOpeningShulkerBoxes) return;

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (slotId < 0 || slotId >= menu.slots.size()) return;

        Slot slot = menu.slots.get(slotId);
        if (!(slot instanceof BackpackSlot)) return;
        Container backpackContainer = slot.container;

        ItemStack stack = slot.getItem();

        if (!SimilarBlockTypeHandler.isShulkerBox(stack)) return;

        if (!menu.getCarried().isEmpty()) return;

        if (player.level().isClientSide) return;

        ShulkerBoxContainer container = new ShulkerBoxContainer(stack, slot, backpackContainer);
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ShulkerBoxMenu(id, inv, container), stack.getHoverName()));
        player.awardStat(Stats.OPEN_SHULKER_BOX);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5F, 1.0F);
        PiglinAi.angerNearbyPiglins(player, true);
        ci.cancel();
    }

    private static class ShulkerBoxContainer extends SimpleContainer {
        private final ItemStack hostStack;
        private final Container backpackContainer;
        private final int slotIndex;

        public ShulkerBoxContainer(ItemStack hostStack, Slot slot, Container backpackContainer) {
            super(27);
            this.hostStack = hostStack;
            this.backpackContainer = backpackContainer;
            this.slotIndex = slot.getContainerSlot();
            ItemContainerContents contents = hostStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            contents.copyInto(this.getItems());
        }

        @Override
        public void stopOpen(Player player) {
            hostStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
            backpackContainer.setItem(slotIndex, hostStack);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5F, 1.0F);
            super.stopOpen(player);
            if (player instanceof ServerPlayer sp && !player.level().isClientSide) {
                sp.getServer().execute(() -> {
                    sp.openMenu(new SimpleMenuProvider((id, inv, p) -> new BackpackMenu(id, p), Component.empty()));
                });
            }
        }
    }
}