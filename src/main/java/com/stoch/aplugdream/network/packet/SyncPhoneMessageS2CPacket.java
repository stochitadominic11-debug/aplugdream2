package com.stoch.aplugdream.network.packet;

import com.stoch.aplugdream.client.ClientPhoneData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncPhoneMessageS2CPacket {
    private final String message;

    public SyncPhoneMessageS2CPacket(String message) {
        this.message = message;
    }

    public SyncPhoneMessageS2CPacket(FriendlyByteBuf buf) {
        this.message = buf.readUtf(512);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(message, 512);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // Add to client-side phone data
            ClientPhoneData.addMessage(message);
        });
        return true;
    }
}
