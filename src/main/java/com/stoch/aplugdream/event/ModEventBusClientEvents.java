package com.stoch.aplugdream.event;

import com.stoch.aplugdream.APlugDreamCore;
import com.stoch.aplugdream.client.renderer.ClientNpcRenderer;
import com.stoch.aplugdream.client.renderer.PoliceRenderer;
import com.stoch.aplugdream.registry.ModEntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = APlugDreamCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.CLIENT_NPC.get(), ClientNpcRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.POLICE.get(), PoliceRenderer::new);
    }
}
