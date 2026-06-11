package com.stoch.aplugdream.network.packet;

import com.stoch.aplugdream.capability.PlayerBankProvider;
import com.stoch.aplugdream.network.ModMessages;
import com.stoch.aplugdream.registry.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class DepositMoneyC2SPacket {
    public DepositMoneyC2SPacket() {}

    public DepositMoneyC2SPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // HERE WE ARE ON THE SERVER
            ServerPlayer player = context.getSender();
            if (player != null) {
                Inventory inv = player.getInventory();
                int totalCleanMoney = 0;
                
                // Count and remove clean money
                for(int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if(stack.getItem() == ModItems.CLEAN_MONEY.get()) {
                        totalCleanMoney += stack.getCount();
                        inv.setItem(i, ItemStack.EMPTY);
                    }
                }
                
                if(totalCleanMoney > 0) {
                    int finalTotalCleanMoney = totalCleanMoney;
                    player.getCapability(PlayerBankProvider.PLAYER_BANK).ifPresent(bank -> {
                        bank.addBalance(finalTotalCleanMoney);
                        // Sync to client
                        ModMessages.sendToPlayer(new SyncBankBalanceS2CPacket(bank.getBalance()), player);
                    });
                }
            }
        });
        return true;
    }
}