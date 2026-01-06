package com.elthisboy.calurosanavidad.network;

import java.util.concurrent.atomic.AtomicReference;


public final class SantaClientState {
    private static final AtomicReference<SantaAckPayload> LAST_ACK = new AtomicReference<>();

    public static void pushAck(SantaAckPayload ack) {
        LAST_ACK.set(ack);
    }


    public static SantaAckPayload consumeAckIfMatches(int santaEntityId, int questId) {
        SantaAckPayload cur = LAST_ACK.get();
        if (cur == null) return null;
        if (cur.santaEntityId() == santaEntityId && cur.questId() == questId) {
            // CAS: solo consumimos si sigue siendo el mismo
            LAST_ACK.compareAndSet(cur, null);
            return cur;
        }
        return null;
    }

    private SantaClientState() {}
}
