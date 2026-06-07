package com.stoch.aplugdream.registry;

import com.stoch.aplugdream.APlugDreamCore;
import com.stoch.aplugdream.registry.ModBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, APlugDreamCore.MODID);

    public static final RegistryObject<Item> DIRTY_MONEY = ITEMS.register("dirty_money",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> CLEAN_MONEY = ITEMS.register("clean_money",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SMARTPHONE = ITEMS.register("smartphone",
            () -> new com.stoch.aplugdream.item.SmartphoneItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> WHITE_POWDER = ITEMS.register("white_powder",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> COCA_SEEDS = ITEMS.register("coca_seeds",
            () -> new ItemNameBlockItem(ModBlocks.COCA_CROP.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
