package com.stoch.aplugdream.event;

import com.stoch.aplugdream.APlugDreamCore;
import com.stoch.aplugdream.capability.PlayerBankProvider;
import com.stoch.aplugdream.capability.PlayerWantedProvider;
import com.stoch.aplugdream.capability.PlayerWantedData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = APlugDreamCore.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerBankProvider.PLAYER_BANK).isPresent()) {
                event.addCapability(new ResourceLocation(APlugDreamCore.MODID, "bank"), new PlayerBankProvider());
            }
            if (!event.getObject().getCapability(PlayerWantedProvider.PLAYER_WANTED).isPresent()) {
                event.addCapability(new ResourceLocation(APlugDreamCore.MODID, "wanted"), new PlayerWantedProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            // Copy bank balance on death
            event.getOriginal().getCapability(PlayerBankProvider.PLAYER_BANK).ifPresent(oldBank -> {
                event.getEntity().getCapability(PlayerBankProvider.PLAYER_BANK).ifPresent(newBank -> {
                    newBank.copyFrom(oldBank);
                });
            });
            // Wanted level resets on death (handled in copyFrom)
            event.getOriginal().getCapability(PlayerWantedProvider.PLAYER_WANTED).ifPresent(oldWanted -> {
                event.getEntity().getCapability(PlayerWantedProvider.PLAYER_WANTED).ifPresent(newWanted -> {
                    newWanted.copyFrom(oldWanted);
                });
            });
        }
    }

    @Mod.EventBusSubscriber(modid = APlugDreamCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {
        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(com.stoch.aplugdream.capability.PlayerBankData.class);
            event.register(PlayerWantedData.class);
        }
    }
}
