package com.stoch.aplugdream.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerBankProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerBankData> PLAYER_BANK = CapabilityManager.get(new CapabilityToken<PlayerBankData>() { });

    private PlayerBankData bankData = null;
    private final LazyOptional<PlayerBankData> optional = LazyOptional.of(this::createPlayerBank);

    private PlayerBankData createPlayerBank() {
        if(this.bankData == null) {
            this.bankData = new PlayerBankData();
        }

        return this.bankData;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == PLAYER_BANK) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerBank().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerBank().loadNBTData(nbt);
    }
}