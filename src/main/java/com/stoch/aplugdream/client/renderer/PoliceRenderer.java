package com.stoch.aplugdream.client.renderer;

import com.stoch.aplugdream.APlugDreamCore;
import com.stoch.aplugdream.entity.PoliceEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PoliceRenderer extends HumanoidMobRenderer<PoliceEntity, HumanoidModel<PoliceEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(APlugDreamCore.MODID, "textures/entity/police.png");

    public PoliceRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new HumanoidModel<>(pContext.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(PoliceEntity pEntity) {
        return TEXTURE;
    }
}
