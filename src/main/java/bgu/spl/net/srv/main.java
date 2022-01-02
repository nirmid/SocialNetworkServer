package bgu.spl.net.srv;

import bgu.spl.net.api.messageEncoderDecoderImp;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class main {
    public static void main(String[] args){
        messageEncoderDecoderImp ed = new messageEncoderDecoderImp();
        byte[] test = ed.encode("1008 21 15 10 11 50\0");
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
}

