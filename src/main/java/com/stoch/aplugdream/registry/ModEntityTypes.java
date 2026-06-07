package com.stoch.aplugdream.registry;

import com.stoch.aplugdream.APlugDreamCore;
import com.stoch.aplugdream.entity.ClientNpcEntity;
import com.stoch.aplugdream.entity.PoliceEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, APlugDreamCore.MODID);

    public static final RegistryObject<EntityType<ClientNpcEntity>> CLIENT_NPC =
            ENTITY_TYPES.register("client_npc",
                    () -> EntityType.Builder.of(ClientNpcEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .build("client_npc"));

    public static final RegistryObject<EntityType<PoliceEntity>> POLICE =
            ENTITY_TYPES.register("police",
                    () -> EntityType.Builder.of(PoliceEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .build("police"));
}
