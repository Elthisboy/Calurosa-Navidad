package com.elthisboy.calurosanavidad.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;


public record SantaQuestData(int activeQuestId, int completedMask, long lastQuestResetMs, long lastGiftMs) {

    // Campos opcionales para no romper NBT viejo si existía (defaults 0).
    public static final Codec<SantaQuestData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("activeQuestId", -1).forGetter(SantaQuestData::activeQuestId),
            Codec.INT.optionalFieldOf("completedMask", 0).forGetter(SantaQuestData::completedMask),
            Codec.LONG.optionalFieldOf("lastQuestResetMs", 0L).forGetter(SantaQuestData::lastQuestResetMs),
            Codec.LONG.optionalFieldOf("lastGiftMs", 0L).forGetter(SantaQuestData::lastGiftMs)
    ).apply(inst, SantaQuestData::new));

    public static final StreamCodec<ByteBuf, SantaQuestData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SantaQuestData::activeQuestId,
            ByteBufCodecs.VAR_INT, SantaQuestData::completedMask,
            ByteBufCodecs.VAR_LONG, SantaQuestData::lastQuestResetMs,
            ByteBufCodecs.VAR_LONG, SantaQuestData::lastGiftMs,
            SantaQuestData::new
    );

    public boolean isCompleted(int questId) {
        if (questId < 0 || questId > 30) return false;
        return (completedMask & (1 << questId)) != 0;
    }

    public SantaQuestData withActive(int questId) {
        return new SantaQuestData(questId, completedMask, lastQuestResetMs, lastGiftMs);
    }

    public SantaQuestData clearActive() {
        return new SantaQuestData(-1, completedMask, lastQuestResetMs, lastGiftMs);
    }

    public SantaQuestData markCompleted(int questId) {
        return new SantaQuestData(activeQuestId, completedMask | (1 << questId), lastQuestResetMs, lastGiftMs);
    }

    public SantaQuestData withQuestResetNow(long nowMs) {
        return new SantaQuestData(-1, 0, nowMs, lastGiftMs);
    }

    public SantaQuestData withGiftNow(long nowMs) {
        return new SantaQuestData(activeQuestId, completedMask, lastQuestResetMs, nowMs);
    }
}
