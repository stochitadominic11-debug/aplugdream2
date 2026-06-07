package com.stoch.aplugdream.network.packet;

import com.stoch.aplugdream.capability.PlayerWantedProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncWantedLevelS2CPacket {
    private final int wantedLevel;

    public SyncWantedLevelS2CPacket(int wantedLevel) {
        this.wantedLevel = wantedLevel;
    }

    public SyncWantedLevelS2CPacket(FriendlyByteBuf buf) {
        this.wantedLevel = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(wantedLevel);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            net.minecraft.client.Minecraft.getInstance().player
                    .getCapability(PlayerWantedProvider.PLAYER_WANTED).ifPresent(wanted -> {
                        wanted.setWantedLevel(this.wantedLevel);
                    });
        });
        return true;
    }
}
