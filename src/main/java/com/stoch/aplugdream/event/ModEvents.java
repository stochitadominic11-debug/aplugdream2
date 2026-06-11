package com.stoch.aplugdream.event;

import com.stoch.aplugdream.APlugDreamCore;
import com.stoch.aplugdream.capability.PlayerBankProvider;
import com.stoch.aplugdream.capability.PlayerWantedProvider;
import com.stoch.aplugdream.capability.PlayerWantedData;
import com.stoch.aplugdream.network.ModMessages;
import com.stoch.aplugdream.network.packet.SyncBankBalanceS2CPacket;
import com.stoch.aplugdream.network.packet.SyncWantedLevelS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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

    /**
     * FIX: Synca bank balance e wanted level al client appena il player entra nel mondo.
     * Senza questo, il client mostra sempre 0 finché non viene mandato un aggiornamento.
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getCapability(PlayerBankProvider.PLAYER_BANK).ifPresent(bank ->
                ModMessages.sendToPlayer(new SyncBankBalanceS2CPacket(bank.getBalance()), serverPlayer)
            );
            serverPlayer.getCapability(PlayerWantedProvider.PLAYER_WANTED).ifPresent(wanted ->
                ModMessages.sendToPlayer(new SyncWantedLevelS2CPacket(wanted.getWantedLevel()), serverPlayer)
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(PlayerBankProvider.PLAYER_BANK).ifPresent(oldBank ->
                event.getEntity().getCapability(PlayerBankProvider.PLAYER_BANK).ifPresent(newBank ->
                    newBank.copyFrom(oldBank)
                )
            );
            event.getOriginal().getCapability(PlayerWantedProvider.PLAYER_WANTED).ifPresent(oldWanted ->
                event.getEntity().getCapability(PlayerWantedProvider.PLAYER_WANTED).ifPresent(newWanted ->
                    newWanted.copyFrom(oldWanted)
                )
            );
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