package com.elthisboy.calurosanavidad.quest;

import com.elthisboy.calurosanavidad.attachment.SantaQuestData;


public final class SantaDailyLogic {

    public static final long DAY_MS = 86_400_000L;

    public static SantaQuestData normalizeQuestReset(SantaQuestData data, long nowMs) {
        long last = data.lastQuestResetMs();
        if (last <= 0L || nowMs - last >= DAY_MS) {
            return new SantaQuestData(-1, 0, nowMs, data.lastGiftMs());
        }
        return data;
    }

    public static boolean isGiftReady(SantaQuestData data, long nowMs) {
        long last = data.lastGiftMs();
        return last <= 0L || nowMs - last >= DAY_MS;
    }

    public static long giftRemainingMs(SantaQuestData data, long nowMs) {
        long last = data.lastGiftMs();
        if (last <= 0L) return 0L;
        long remaining = DAY_MS - (nowMs - last);
        return Math.max(0L, remaining);
    }

    public static SantaQuestData setGiftClaimedNow(SantaQuestData data, long nowMs) {
        return new SantaQuestData(data.activeQuestId(), data.completedMask(), data.lastQuestResetMs(), nowMs);
    }

    private SantaDailyLogic() {}
}
