/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.sdk.msg.types;

import com.vecsight.dragonite.sdk.exception.IncorrectMessageException;
import com.vecsight.dragonite.sdk.misc.DragoniteGlobalConstants;
import com.vecsight.dragonite.sdk.msg.MessageType;
import com.vecsight.dragonite.sdk.msg.ReliableMessage;
import com.vecsight.dragonite.utils.binary.BinaryReader;
import com.vecsight.dragonite.utils.binary.BinaryWriter;

import java.nio.BufferUnderflowException;

public class HeartbeatMessage implements ReliableMessage {

    private static final byte VERSION = DragoniteGlobalConstants.PROTOCOL_VERSION;

    private static final MessageType TYPE = MessageType.HEARTBEAT;

    public static final int FIXED_LENGTH = 6;

    private int sequence;

    public HeartbeatMessage(final int sequence) {
        this.sequence = sequence;
    }

    public HeartbeatMessage(final byte[] msg) throws IncorrectMessageException {
        final BinaryReader reader = new BinaryReader(msg);

        try {

            final byte remoteVersion = reader.getSignedByte();
            final byte remoteType = reader.getSignedByte();

            if (remoteVersion != VERSION) {
                throw new IncorrectMessageException("Incorrect version (" + remoteVersion + ", should be " + VERSION + ")");
            }
            if (remoteType != TYPE.getValue()) {
                throw new IncorrectMessageException("Incorrect type (" + remoteType + ", should be " + TYPE + ")");
            }

            sequence = reader.getSignedInt();

        } catch (final BufferUnderflowException e) {
            throw new IncorrectMessageException("Incorrect message length");
        }
    }

    @Override
    public byte getVersion() {
        return VERSION;
    }

    @Override
    public MessageType getType() {
        return TYPE;
    }

    @Override
    public byte[] toBytes() {
        final BinaryWriter writer = new BinaryWriter(getFixedLength());

        writer.putSignedByte(VERSION)
                .putSignedByte(TYPE.getValue())
                .putSignedInt(sequence);

        return writer.toBytes();
    }

    @Override
    public int getFixedLength() {
        return FIXED_LENGTH;
    }

    @Override
    public int getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(final int sequence) {
        this.sequence = sequence;
    }
}
