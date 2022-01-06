package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.Server;
import bgu.spl.net.srv.TPCServer;
import bgu.spl.net.srv.messageEncoderDecoderImp;

public class TPCMain {
    public static void main(String[] args) {

        Server tpc = TPCServer.TPCServer(Integer.parseInt(args[0]),
                () -> new BidiMessagingProtocolImp(),
                () -> new messageEncoderDecoderImp()
        );
        tpc.serve();
    }
}
