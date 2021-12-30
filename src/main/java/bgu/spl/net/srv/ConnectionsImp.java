package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImp implements Connections{

    private ConcurrentHashMap<Integer,ConnectionHandler> activeMap;
    private ConcurrentHashMap<Integer,User> UserMap;

    public ConnectionsImp(){
        activeMap = new ConcurrentHashMap<Integer,ConnectionHandler>();
        UserMap = new ConcurrentHashMap<Integer,User>();
    }

    public void setUserMap(Integer id,User user) {
        UserMap.put(id,user);
    }

    public User getUserMap(Integer id){
        return UserMap.get(id);
    }

    public void addConnection(int id, ConnectionHandler connection){
        activeMap.put(id, connection);
    }

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
