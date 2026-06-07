package com.stoch.aplugdream.registry;

import com.stoch.aplugdream.APlugDreamCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, APlugDreamCore.MODID);

    public static final RegistryObject<CreativeModeTab> APLUGDREAM_TAB = CREATIVE_MODE_TABS.register("aplugdream_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.DIRTY_MONEY.get()))
                    .title(Component.translatable("creativetab.aplugdream_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.DIRTY_MONEY.get());
                        pOutput.accept(ModItems.CLEAN_MONEY.get());
                        pOutput.accept(ModItems.SMARTPHONE.get());
                        pOutput.accept(ModItems.WHITE_POWDER.get());
                        pOutput.accept(ModItems.COCA_SEEDS.get());
                        pOutput.accept(ModBlocks.CASH_REGISTER.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
