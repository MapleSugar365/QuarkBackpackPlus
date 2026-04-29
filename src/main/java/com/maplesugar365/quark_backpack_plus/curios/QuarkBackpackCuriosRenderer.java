package com.maplesugar365.quark_backpack_plus.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.violetmoon.quark.addons.oddities.item.BackpackItem;
import org.violetmoon.quark.base.client.handler.ModelHandler;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@SuppressWarnings({"unchecked", "rawtypes"})
public class QuarkBackpackCuriosRenderer implements ICurioRenderer {

    private final HumanoidModel model = ModelHandler.armorModel(ModelHandler.backpack, EquipmentSlot.CHEST);

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (stack.getItem() instanceof BackpackItem backpack) {
            EntityModel<T> entityModel = renderLayerParent.getModel();
            if (entityModel instanceof HumanoidModel<T> humanoidModel) humanoidModel.copyPropertiesTo(model);
            else entityModel.copyPropertiesTo(model);
            boolean hasFoil = stack.hasFoil();
            int color = backpack.getBarColor(stack);
            render(poseStack, multiBufferSource, light, hasFoil, model, color, getArmorResource(stack, null));
            render(poseStack, multiBufferSource, light, hasFoil, model, 0xffffffff, getArmorResource(stack, "overlay"));
        }
    }

    private void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, boolean glint, Model backpackModel, int color, ResourceLocation armorResource) {
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(multiBufferSource, RenderType.armorCutoutNoCull(armorResource), glint);
        backpackModel.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, color);
    }

    private ResourceLocation getArmorResource(ItemStack backpack, String type) {
        return backpack.getItem().getArmorTexture(backpack, Minecraft.getInstance().player, EquipmentSlot.CHEST, null, false);
    }
}
