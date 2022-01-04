package bgu.spl.net.srv;

import java.io.IOException;
import java.util.Collection;
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

    public Collection<User> getActiveUsers () {return UserMap.values();}

    public void setActiveMap(Integer id,ConnectionHandler ch){activeMap.put(id,ch);}

    public boolean removeUserMap(Integer id){ return UserMap.remove(id) != null;}

    public void addConnection(int id, ConnectionHandler connection){
        activeMap.put(id, connection);
    }

    public ConnectionHandler getConnection(int id){
        return activeMap.get(id);
    }

    @Override
    public boolean send(int connectionId, Object msg) {
        activeMap.get(connectionId).send(msg);
        return false;
    }

    @Override
    public void broadcast(Object msg) {

    }

    @Override
    public void disconnect(int connectionId) {
        try {
            activeMap.get(connectionId).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        UserMap.remove(connectionId);
        activeMap.remove(connectionId);
    }
}
