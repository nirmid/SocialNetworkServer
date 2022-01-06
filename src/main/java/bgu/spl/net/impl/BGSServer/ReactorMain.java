package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.ReactorServer;
import bgu.spl.net.api.Server;
import bgu.spl.net.srv.messageEncoderDecoderImp;

public class ReactorMain {
    public static void main(String[] args) {

        Server reactor = ReactorServer.reactor(Integer.parseInt(args[1]), Integer.parseInt(args[0]),
                ()-> new BidiMessagingProtocolImp(),
                ()-> new messageEncoderDecoderImp()
                );
        reactor.serve();


        }
    }








