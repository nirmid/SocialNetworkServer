package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImp implements Connections{

    private ConcurrentHashMap<Integer,BlockingConnectionHandler> ;
    private ConcurrentHashMap<BlockingConnectionHandler,User>;
    @Override
    public boolean send(int connectionId, Object msg) {
        return false;
    }

    @Override
    public void broadcast(Object msg) {

    }

    @Override
    public void disconnect(int connectionId) {

    }
}
