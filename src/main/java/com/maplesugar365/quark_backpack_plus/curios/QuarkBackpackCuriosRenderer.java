package com.maplesugar365.quark_backpack_plus.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.violetmoon.quark.addons.oddities.item.BackpackItem;
import org.violetmoon.quark.base.client.handler.ModelHandler;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

@SuppressWarnings("unchecked")
public class QuarkBackpackCuriosRenderer implements ICurioRenderer {

    private final HumanoidModel<?> model = ModelHandler.armorModel(ModelHandler.backpack, EquipmentSlot.CHEST);

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(stack.getItem() instanceof BackpackItem backpack)) return;

        HumanoidModel<T> armorModel = (HumanoidModel<T>) model;
        EntityModel<T> entityModel = renderLayerParent.getModel();
        if (entityModel instanceof HumanoidModel<T> humanoidModel) {
            humanoidModel.copyPropertiesTo(armorModel);
        } else {
            entityModel.copyPropertiesTo(armorModel);
        }

        boolean hasFoil = stack.hasFoil();
        int fallbackColor = stack.is(ItemTags.DYEABLE)
            ? FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(stack, DyedItemColor.LEATHER_COLOR))
            : 0xFFFFFFFF;

        ArmorMaterial armorMaterial = ((ArmorItem) stack.getItem()).getMaterial().value();
        for (int i = 0; i < armorMaterial.layers().size(); i++) {
            ArmorMaterial.Layer layer = armorMaterial.layers().get(i);
            ResourceLocation texture = backpack.getArmorTexture(stack, slotContext.entity(), EquipmentSlot.CHEST, layer, false);
            int color = layer.dyeable() ? fallbackColor : 0xFFFFFFFF;
            renderLayer(poseStack, multiBufferSource, light, hasFoil, armorModel, color, texture);
        }
    }

    private void renderLayer(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, boolean glint, Model backpackModel, int color, ResourceLocation armorResource) {
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(multiBufferSource, RenderType.armorCutoutNoCull(armorResource), glint);
        backpackModel.renderToBuffer(poseStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, color);
    }
}