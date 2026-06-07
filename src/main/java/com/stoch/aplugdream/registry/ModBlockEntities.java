package com.stoch.aplugdream.registry;

import com.stoch.aplugdream.APlugDreamCore;
import com.stoch.aplugdream.block.entity.CashRegisterBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, APlugDreamCore.MODID);

    public static final RegistryObject<BlockEntityType<CashRegisterBlockEntity>> CASH_REGISTER_BE =
            BLOCK_ENTITIES.register("cash_register_be", () ->
                    BlockEntityType.Builder.of(CashRegisterBlockEntity::new,
                            ModBlocks.CASH_REGISTER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
