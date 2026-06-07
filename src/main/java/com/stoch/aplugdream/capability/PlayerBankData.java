package com.stoch.aplugdream.capability;

import net.minecraft.nbt.CompoundTag;

public class PlayerBankData {
    private int balance;

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void addBalance(int amount) {
        this.balance += amount;
    }

    public void subBalance(int amount) {
        this.balance -= amount;
        if(this.balance < 0) this.balance = 0;
    }

    public void copyFrom(PlayerBankData source) {
        this.balance = source.balance;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("bank_balance", balance);
    }

    public void loadNBTData(CompoundTag nbt) {
        balance = nbt.getInt("bank_balance");
    }
}
