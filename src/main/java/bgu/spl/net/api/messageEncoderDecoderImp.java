package bgu.spl.net.api;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class messageEncoderDecoderImp implements MessageEncoderDecoder {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;


    @Override
    public Object decodeNextByte(byte nextByte) {
        if (nextByte == ';') {
            return popString();
        }

        pushByte(nextByte);
        return null;
    }

    @Override
    public byte[] encode(Object message) {
        return (message + ";").getBytes();
    }


    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private String popString() {
        String result = new String(bytes, 2, len , StandardCharsets.UTF_8);
        len = 0;
        return  result;
    }

}
