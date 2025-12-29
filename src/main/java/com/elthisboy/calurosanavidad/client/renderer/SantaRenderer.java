package com.elthisboy.calurosanavidad.client.renderer;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import com.elthisboy.calurosanavidad.ModEntities.SantaClausEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.client.renderer.entity.MobRenderer;
import com.elthisboy.calurosanavidad.ModEntities.SantaClausEntity;


public class SantaRenderer extends MobRenderer<SantaClausEntity, VillagerModel<SantaClausEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CalursaNavidad.MOD_ID, "textures/entity/santa_claus.png");

    public SantaRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(SantaClausEntity entity) {
        return TEXTURE;
    }
}