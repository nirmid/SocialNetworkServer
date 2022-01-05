package bgu.spl.net.srv;

import bgu.spl.net.api.BidiMessagingProtocolImp;
import bgu.spl.net.api.messageEncoderDecoderImp;
import com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class main {
    public static void main(String[] args) {
        Server server = TPCServer.TPCServer(7777,
                ()-> new BidiMessagingProtocolImp(),
                ()-> new messageEncoderDecoderImp()
                );
        server.serve();
        }
    }








