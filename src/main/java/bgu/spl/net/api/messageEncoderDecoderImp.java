package bgu.spl.net.api;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
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
        String smessage = message.toString();
        if (Integer.parseInt(smessage.substring(0,2)) == 10 &&
                (Integer.parseInt(smessage.substring(2, 4)) == 7 || (Integer.parseInt(smessage.substring(2, 4)) == 8 ))){
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String statsPerUser = smessage.substring(0, smessage.length()-1);
            String[] tokens = statsPerUser.split("\0");
            for (String userStat : tokens){
                Short opcode = Short.parseShort(userStat.substring(0,2));
                out.write(shortToBytes(opcode), 0, 2);
                String[] stats = userStat.substring(3).split(" ");
                for (String stat : stats){
                    Short statVal = Short.parseShort(stat);
                    out.write(shortToBytes(statVal), 0, 2);
                }
                out.write(("\0").getBytes(StandardCharsets.UTF_8), 0, 1);
            }
            out.write((";").getBytes(StandardCharsets.UTF_8), 0, 1);
            return out.toByteArray();
        }
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

    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }


}
