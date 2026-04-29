package com.maplesugar365.quark_backpack_plus.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.violetmoon.quark.addons.oddities.inventory.BackpackMenu;
import top.theillusivec4.curios.common.network.client.CPacketOpenVanilla;
import top.theillusivec4.curios.common.network.server.CuriosServerPayloadHandler;
import top.theillusivec4.curios.common.network.server.SPacketGrabbedItem;

@Mixin(CuriosServerPayloadHandler.class)
public class CuriosServerPayloadHandlerMixin {

    @Overwrite(remap = false)
    public void handleOpenVanilla(final CPacketOpenVanilla data, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();

            if (player instanceof ServerPlayer serverPlayer) {
                ItemStack stack = player.isCreative() ? data.carried() : player.containerMenu.getCarried();
                player.containerMenu.setCarried(ItemStack.EMPTY);

                if (!(serverPlayer.containerMenu instanceof BackpackMenu)) serverPlayer.doCloseContainer();

                if (!stack.isEmpty()) {
                    player.inventoryMenu.setCarried(stack);
                    PacketDistributor.sendToPlayer(serverPlayer, new SPacketGrabbedItem(stack));
                }
            }
        });
    }
}
