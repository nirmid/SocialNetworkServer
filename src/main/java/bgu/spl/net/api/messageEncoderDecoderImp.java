package bgu.spl.net.api;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class messageEncoderDecoderImp implements MessageEncoderDecoder<String> {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;


    @Override
    public String decodeNextByte(byte nextByte) {
        if (nextByte == ';') {
            return popString();
        }

        pushByte(nextByte);
        return null;
    }

    public String decodeNextShort(byte nextByte) {

        return null;
    }

    @Override
    public byte[] encode(String message) {
        byte[] output;
        short command = Short.parseShort(message.substring(0,2));
        byte[] commandB = shortToBytes(command);
        short opcode = Short.parseShort(message.substring(2,4));
        byte[] opcodeB = shortToBytes(opcode);
        int index = 0;
        if (command== 10 && (opcode == 7 || opcode == 8 )){
            String statsPerUser = message.substring(0, message.length()-1);
            String[] tokens = statsPerUser.split("\0");
            output = new byte[tokens.length*(6+1)];

            for(int i=0; i< tokens.length; i= i+1){
                String[] stats = tokens[i].split(" ");
                for (String stat : stats){
                    byte[] temp = shortToBytes(Short.parseShort(stat));
                    output[index++] = temp[0];
                    output[index++] = temp[1];
                }
                if(i<tokens.length-1){
                    byte[] zero = ("\0").getBytes(StandardCharsets.UTF_8);
                    output[index++] = zero[0];
                }
            }
            byte[] temp = (";").getBytes(StandardCharsets.UTF_8);
            output[index++] =temp[0];
        }
        else{
            if(message.length()>4){
                byte[] content = message.substring(5).getBytes(StandardCharsets.UTF_8);
                output = new byte[4+content.length+1];
                output[index++] = commandB[0];
                output[index++] = commandB[1];
                output[index++] = opcodeB[0];
                output[index++] = opcodeB[1];
                for(int i=0; i<content.length; i=i+1){
                    output[index++] = content[i];
                }
                byte[] temp = (";").getBytes(StandardCharsets.UTF_8);
                output[index++] =temp[0];
            }
            else{
                output = new byte[4+1];
                output[index++] = commandB[0];
                output[index++] = commandB[1];
                output[index++] = opcodeB[0];
                output[index++] = opcodeB[1];
                byte[] temp = (";").getBytes(StandardCharsets.UTF_8);
                output[index++] =temp[0];
            }
        }
        return output;
    }




    public byte[] encode2(String message) {
        byte[] output;
        if (Integer.parseInt(message.substring(0,2)) == 10 &&
                (Integer.parseInt(message.substring(2, 4)) == 7 || (Integer.parseInt(message.substring(2, 4)) == 8 ))){
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String statsPerUser = message.substring(0, message.length()-1);

            String[] tokens = statsPerUser.split("\0");
            for(int i=0; i< tokens.length; i= i+1){
                Short opcode = Short.parseShort(tokens[i].substring(0,2));
                out.write(shortToBytes(opcode), 0, 2);
                String[] stats = tokens[i].substring(2).split(" ");
                for (String stat : stats){
                    Short statVal = Short.parseShort(stat);
                    out.write(shortToBytes(statVal), 0, 2);
                }
                if(i<tokens.length-1)
                    out.write(("\0").getBytes(StandardCharsets.UTF_8), 0, 1);
            }
            /*
            for (String userStat : tokens){
                Short opcode = Short.parseShort(userStat.substring(0,2));
                out.write(shortToBytes(opcode), 0, 2);
                String[] stats = userStat.substring(2).split(" ");
                for (String stat : stats){
                    Short statVal = Short.parseShort(stat);
                    out.write(shortToBytes(statVal), 0, 2);
                }
                out.write(("\0").getBytes(StandardCharsets.UTF_8), 0, 1);
            }
             */

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


    private String popString() { // decoder
        String result = "";
        byte[] opcodeB ={bytes[0],bytes[1]};
        short opcode = bytesToShort(opcodeB);
        result = new String(bytes, 2, len, StandardCharsets.UTF_8);
        result = opcode+result;
        if(opcode < 10)
            result = '0' +result;
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
