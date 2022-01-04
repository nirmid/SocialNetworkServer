package bgu.spl.net.srv;

import bgu.spl.net.api.messageEncoderDecoderImp;
import com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class main {
    public static void main(String[] args){
        messageEncoderDecoderImp ed = new messageEncoderDecoderImp();
        byte[] test = ed.encode("1008"+"10"+"50"+"10"+"5020");
   //     String result ="";
//        for(int i = 0; i< test.length; i=i+2){
//            byte[] cur = {test[i], test[i+1]};
//            short temp = bytesToShort(cur);
//            result = result + " " + temp;
//
//            System.out.println(temp);
//        }
//        System.out.println(result);
        InputStream input = new BufferedInputStream(new ByteArrayInputStream(test));

        try { //just for automatic closing
            int read;
            while ((read = input.read()) >= 0) {
                Object nextMessage = ed.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    System.out.println(nextMessage.toString());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }





    }
    public static short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }


}



