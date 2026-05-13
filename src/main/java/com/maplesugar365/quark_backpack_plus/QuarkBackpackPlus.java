package com.maplesugar365.quark_backpack_plus;

import com.maplesugar365.quark_backpack_plus.curios.QuarkBackpackCuriosRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.violetmoon.quark.addons.oddities.inventory.slot.BackpackSlot;
import org.violetmoon.quark.addons.oddities.module.BackpackModule;
import org.violetmoon.quark.base.handler.SimilarBlockTypeHandler;
import org.violetmoon.quark.content.management.module.ExpandedItemInteractionsModule;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@Mod(QuarkBackpackPlus.MOD_ID)
public class QuarkBackpackPlus {
    public static final String MOD_ID = "quark_backpack_plus";

    public QuarkBackpackPlus(IEventBus bus) {
        bus.addListener(this::clientSetup);
    }

    public void clientSetup(FMLClientSetupEvent event) {
        CuriosRendererRegistry.register(BackpackModule.backpack, QuarkBackpackCuriosRenderer::new);
    }

    @EventBusSubscriber(modid = MOD_ID)
    public static class QuarkBackpackPlusEvents {

        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            if (!ExpandedItemInteractionsModule.allowOpeningShulkerBoxes) return;
            ItemStack stack = event.getItemStack();

            if (!SimilarBlockTypeHandler.isShulkerBox(stack)) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
            if (!screen.getMenu().getCarried().isEmpty()) return;

            Slot slotUnder = screen.getSlotUnderMouse();
            if (!(slotUnder instanceof BackpackSlot)) return;

            event.getToolTip().add(Component.translatable("quark.misc.open_shulker").withStyle(ChatFormatting.YELLOW));
        }

        @SubscribeEvent
        public static void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player) || player.isDeadOrDying() || player.isRemoved() || event.getSlot() != EquipmentSlot.CHEST)
                return;

            CuriosApi.getCuriosInventory(player).ifPresent(inventory -> {
                if (event.getTo().getItem() == BackpackModule.backpack && !inventory.findCurios(BackpackModule.backpack).isEmpty()) {
                    player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                    if (!player.addItem(event.getTo())) player.drop(event.getTo(), false, true);
                }
            });
        }
    }

}
