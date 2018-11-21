/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.socket;

public class ResendItem {

    private final int sequence;

    private final long startTime;

    private volatile int sendCount = 0;

    private volatile long nextSendTime;

    private volatile boolean resended = false;

    public ResendItem(final int sequence, final long startTime, final long nextSendTime) {
        this.sequence = sequence;
        this.startTime = startTime;
        this.nextSendTime = nextSendTime;
    }

    public int getSequence() {
        return sequence;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getSendCount() {
        return sendCount;
    }

    public void addSendCount() {
        sendCount++;
    }

    public long getNextSendTime() {
        return nextSendTime;
    }

    public void setNextSendTime(final long nextSendTime) {
        this.nextSendTime = nextSendTime;
    }

    public boolean isResended() {
        return resended;
    }

    public void setResended() {
        this.resended = true;
    }
}
