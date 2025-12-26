package com.elthisboy.calurosanavidad.ModEntities;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

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
}
