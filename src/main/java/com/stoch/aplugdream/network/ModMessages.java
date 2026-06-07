package com.stoch.aplugdream.network;

import com.stoch.aplugdream.APlugDreamCore;
import com.stoch.aplugdream.network.packet.DepositMoneyC2SPacket;
import com.stoch.aplugdream.network.packet.SyncBankBalanceS2CPacket;
import com.stoch.aplugdream.network.packet.SyncWantedLevelS2CPacket;
import com.stoch.aplugdream.network.packet.SyncPhoneMessageS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(APlugDreamCore.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(SyncBankBalanceS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncBankBalanceS2CPacket::new)
                .encoder(SyncBankBalanceS2CPacket::toBytes)
                .consumerMainThread(SyncBankBalanceS2CPacket::handle)
                .add();

        net.messageBuilder(DepositMoneyC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(DepositMoneyC2SPacket::new)
                .encoder(DepositMoneyC2SPacket::toBytes)
                .consumerMainThread(DepositMoneyC2SPacket::handle)
                .add();

        net.messageBuilder(SyncWantedLevelS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncWantedLevelS2CPacket::new)
                .encoder(SyncWantedLevelS2CPacket::toBytes)
                .consumerMainThread(SyncWantedLevelS2CPacket::handle)
                .add();

        net.messageBuilder(SyncPhoneMessageS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncPhoneMessageS2CPacket::new)
                .encoder(SyncPhoneMessageS2CPacket::toBytes)
                .consumerMainThread(SyncPhoneMessageS2CPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
