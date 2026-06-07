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

public class PlayerWantedProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerWantedData> PLAYER_WANTED = CapabilityManager.get(new CapabilityToken<PlayerWantedData>() { });

    private PlayerWantedData wantedData = null;
    private final LazyOptional<PlayerWantedData> optional = LazyOptional.of(this::createPlayerWanted);

    private PlayerWantedData createPlayerWanted() {
        if (this.wantedData == null) {
            this.wantedData = new PlayerWantedData();
        }
        return this.wantedData;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_WANTED) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerWanted().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerWanted().loadNBTData(nbt);
    }
}
