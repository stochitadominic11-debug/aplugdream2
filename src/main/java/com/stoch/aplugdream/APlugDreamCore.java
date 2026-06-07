package com.stoch.aplugdream;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.stoch.aplugdream.registry.ModItems;
import com.stoch.aplugdream.registry.ModBlocks;
import com.stoch.aplugdream.registry.ModBlockEntities;
import com.stoch.aplugdream.registry.ModMenuTypes;
import com.stoch.aplugdream.registry.ModEntityTypes;
import com.stoch.aplugdream.screen.CashRegisterScreen;
import com.stoch.aplugdream.registry.ModCreativeModeTabs;
import com.stoch.aplugdream.network.ModMessages;
import com.stoch.aplugdream.entity.ClientNpcEntity;
import com.stoch.aplugdream.entity.PoliceEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

@Mod(APlugDreamCore.MODID)
public class APlugDreamCore {
    public static final String MODID = "aplugdream";
    private static final Logger LOGGER = LogUtils.getLogger();

    public APlugDreamCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onAttributeCreate);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("A Plug Dream Core setup started.");
        ModMessages.register();
    }

    private void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.CLIENT_NPC.get(), ClientNpcEntity.createAttributes().build());
        event.put(ModEntityTypes.POLICE.get(), PoliceEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("A Plug Dream Core server starting.");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("A Plug Dream Core client setup started.");
            MenuScreens.register(ModMenuTypes.CASH_REGISTER_MENU.get(), CashRegisterScreen::new);
        }
    }
}

