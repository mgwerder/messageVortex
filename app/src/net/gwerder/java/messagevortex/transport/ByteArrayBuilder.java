package net.gwerder.java.messagevortex.transport;

import java.util.Arrays;

public class ByteArrayBuilder {

    byte[] values=new byte[0];

    ByteArrayBuilder append(byte[] bytes,int length) {
        int offset = values.length;
        values = Arrays.copyOf(values, offset+length);
        System.arraycopy(bytes, 0, values, offset,length);
        return this;
    }

    byte[] toBytes() {
        return values;
    }

}
