package bgu.spl.net.srv;

public class main {
    public static void main(String[] args) {
        Server server = TPCServer.TPCServer(Integer.parseInt(args[0]),
                ()-> new BidiMessagingProtocolImp(),
                ()-> new messageEncoderDecoderImp()
        );
        server.serve();



        /*Server server = ReactorServer.reactor(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
                ()-> new BidiMessagingProtocolImp(),
                ()-> new messageEncoderDecoderImp()
                );
        server.serve();

         */
        }
    }








