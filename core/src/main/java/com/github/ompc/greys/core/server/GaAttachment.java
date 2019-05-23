package com.github.ompc.greys.core.server;

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static com.github.ompc.greys.core.server.LineDecodeState.READ_CHAR;

/**
 * GaServer操作的附件
 *
 * @author vlinux
 */
public class GaAttachment {

    private final int bufferSize;
    private ByteBuffer lineByteBuffer;
    @Getter
    private final Session session;
    @Getter
    @Setter
    private LineDecodeState lineDecodeState;

    GaAttachment(int bufferSize, Session session) {
        this.lineByteBuffer = ByteBuffer.allocate(bufferSize);
        this.bufferSize = bufferSize;
        this.lineDecodeState = READ_CHAR;
        this.session = session;
    }

    public void put(byte data) {
        if (lineByteBuffer.hasRemaining()) {
            lineByteBuffer.put(data);
        } else {
            final ByteBuffer newLineByteBuffer = ByteBuffer.allocate(lineByteBuffer.capacity() + bufferSize);
            lineByteBuffer.flip();
            newLineByteBuffer.put(lineByteBuffer);
            newLineByteBuffer.put(data);
            this.lineByteBuffer = newLineByteBuffer;
        }
    }

    public String clearAndGetLine(Charset charset) {
        lineByteBuffer.flip();
        final byte[] dataArray = new byte[lineByteBuffer.limit()];
        lineByteBuffer.get(dataArray);
        final String line = new String(dataArray, charset);
        lineByteBuffer.clear();
        return line;
    }


}
