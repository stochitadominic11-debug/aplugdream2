package com.stoch.aplugdream.network.packet;

import com.stoch.aplugdream.capability.PlayerBankProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SyncBankBalanceS2CPacket {
    private final int balance;

    public SyncBankBalanceS2CPacket(int balance) {
        this.balance = balance;
    }

    public SyncBankBalanceS2CPacket(FriendlyByteBuf buf) {
        this.balance = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(balance);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // HERE WE ARE ON THE CLIENT
            net.minecraft.client.Minecraft.getInstance().player.getCapability(PlayerBankProvider.PLAYER_BANK).ifPresent(bank -> {
                bank.setBalance(this.balance);
            });
        });
        return true;
    }
}