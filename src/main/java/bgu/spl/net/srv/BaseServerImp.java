package bgu.spl.net.srv;

import java.util.function.Supplier;

public class BaseServerImp extends BaseServer{

    public BaseServerImp(int port, Supplier protocolFactory, Supplier encdecFactory) {
        super(port, protocolFactory, encdecFactory);
    }

    @Override
    protected void execute(BlockingConnectionHandler handler) {
        Thread thread = new Thread(handler);
        thread.start();

    }
}
