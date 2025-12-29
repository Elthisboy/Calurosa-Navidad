package com.elthisboy.calurosanavidad.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SantaQuestData(int activeQuestId, int completedMask, long lastGiftDay) {

    public static final Codec<SantaQuestData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("activeQuestId").forGetter(SantaQuestData::activeQuestId),
            Codec.INT.fieldOf("completedMask").forGetter(SantaQuestData::completedMask),
            Codec.LONG.fieldOf("lastGiftDay").forGetter(SantaQuestData::lastGiftDay)
    ).apply(inst, SantaQuestData::new));

    public static final StreamCodec<ByteBuf, SantaQuestData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SantaQuestData::activeQuestId,
            ByteBufCodecs.VAR_INT, SantaQuestData::completedMask,
            ByteBufCodecs.VAR_LONG, SantaQuestData::lastGiftDay,
            SantaQuestData::new
    );

    public boolean isCompleted(int questId) {
        if (questId < 0 || questId > 30) return false;
        return (completedMask & (1 << questId)) != 0;
    }

    public SantaQuestData withActive(int questId) {
        return new SantaQuestData(questId, completedMask, lastGiftDay);
    }

    public SantaQuestData markCompleted(int questId) {
        return new SantaQuestData(activeQuestId, completedMask | (1 << questId), lastGiftDay);
    }

    public SantaQuestData withGiftDay(long day) {
        return new SantaQuestData(activeQuestId, completedMask, day);
    }
}