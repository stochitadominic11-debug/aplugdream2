package com.stoch.aplugdream.client.renderer;

import com.stoch.aplugdream.APlugDreamCore;
import com.stoch.aplugdream.entity.ClientNpcEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ClientNpcRenderer extends HumanoidMobRenderer<ClientNpcEntity, HumanoidModel<ClientNpcEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(APlugDreamCore.MODID, "textures/entity/client_npc.png");

    public ClientNpcRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new HumanoidModel<>(pContext.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ClientNpcEntity pEntity) {
        return TEXTURE;
    }
}
