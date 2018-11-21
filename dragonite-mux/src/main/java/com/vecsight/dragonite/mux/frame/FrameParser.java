/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.mux.frame;


import com.vecsight.dragonite.mux.exception.DataLengthMismatchException;
import com.vecsight.dragonite.mux.exception.IncorrectFrameException;
import com.vecsight.dragonite.mux.frame.types.*;

public class FrameParser {

    private final FrameBuffer frameBuffer;

    private boolean needMore = false;

    private int expectedLength = 0;

    public FrameParser(final int maxFrameSize) {
        frameBuffer = new FrameBuffer(maxFrameSize);
    }

    public Frame feed(final byte[] rawBytes) {

        frameBuffer.add(rawBytes);

        if (!needMore || frameBuffer.getSize() >= expectedLength) {

            Frame frame = null;
            try {
                frame = parseFrameRaw(frameBuffer.get());
                frameBuffer.reset();
                needMore = false;
            } catch (final IncorrectFrameException e) {
                frameBuffer.reset();
                needMore = false;
            } catch (final DataLengthMismatchException e) {
                expectedLength = e.getExpectedLength();
                needMore = true;
            }
            return frame;

        } else {
            return null;
        }

    }

    private static Frame parseFrameRaw(final byte[] rawBytes) throws IncorrectFrameException, DataLengthMismatchException {
        if (rawBytes.length >= 2) {
            try {
                switch (FrameType.fromByte(rawBytes[1])) {
                    case CREATE:
                        return new CreateConnectionFrame(rawBytes);
                    case CLOSE:
                        return new CloseConnectionFrame(rawBytes);
                    case DATA:
                        return new DataFrame(rawBytes);
                    case PAUSE:
                        return new PauseConnectionFrame(rawBytes);
                    case CONTINUE:
                        return new ContinueConnectionFrame(rawBytes);
                    default:
                        throw new IncorrectFrameException("Unknown Frame Type");
                }
            } catch (final IllegalArgumentException e) {
                throw new IncorrectFrameException("Unknown Frame Type");
            }
        } else {
            throw new IncorrectFrameException("Packet is too short");
        }
    }

}
