package com.stoch.aplugdream.capability;

import net.minecraft.nbt.CompoundTag;

public class PlayerWantedData {
    private int wantedLevel;
    private static final int MAX_WANTED = 5;

    public int getWantedLevel() {
        return wantedLevel;
    }

    public void setWantedLevel(int level) {
        this.wantedLevel = Math.max(0, Math.min(level, MAX_WANTED));
    }

    public void addWantedLevel(int amount) {
        setWantedLevel(this.wantedLevel + amount);
    }

    public void reduceWantedLevel(int amount) {
        setWantedLevel(this.wantedLevel - amount);
    }

    public boolean isWanted() {
        return this.wantedLevel > 0;
    }

    public void copyFrom(PlayerWantedData source) {
        // Wanted level resets on death (like GTA)
        this.wantedLevel = 0;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("wanted_level", wantedLevel);
    }

    public void loadNBTData(CompoundTag nbt) {
        wantedLevel = nbt.getInt("wanted_level");
    }
}
