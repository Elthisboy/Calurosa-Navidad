package com.elthisboy.calurosanavidad.ModEntities;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, CalursaNavidad.MOD_ID);



    public static final DeferredHolder<EntityType<?>, EntityType<WaterBalloonProjectile>> WATER_BALLOON_PROJECTILE =
            ENTITIES.register("water_balloon_projectile", () ->
                    EntityType.Builder.<WaterBalloonProjectile>of(WaterBalloonProjectile::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("water_balloon_projectile"));


    public static final Supplier<EntityType<SantaClausEntity>> SANTA_CLAUS =
            ENTITIES.register("santa_claus",
                    () -> EntityType.Builder.of(SantaClausEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.9F)
                            .build(ResourceLocation.fromNamespaceAndPath(CalursaNavidad.MOD_ID, "santa_claus").toString())
            );

    public static AttributeSupplier createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .build();
    }


}
