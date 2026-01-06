package com.elthisboy.calurosanavidad.attachment;

import com.elthisboy.calurosanavidad.CalursaNavidad;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.function.Supplier;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CalursaNavidad.MOD_ID);

    public static final Supplier<AttachmentType<SantaQuestData>> SANTA_DATA =
            ATTACHMENTS.register("santa_data", () ->
                    AttachmentType.builder(() -> new SantaQuestData(-1, 0, 0L, 0L))
                            // Persistencia (NBT)
                            .serialize(SantaQuestData.CODEC)
                            // Copiar al morir
                            .copyOnDeath()
                            // Sync solo al propio jugador
                            .sync((holder, to) -> holder == to, SantaQuestData.STREAM_CODEC)
                            .build()
            );

    private ModAttachments() {}
}
