package bgu.spl.net.api;

import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImp;
import bgu.spl.net.srv.User;
import bgu.spl.net.srv.UserDataBase;

public class BidiMessagingProtocolImp implements BidiMessagingProtocol {
    private UserDataBase userDB;
    private int connectionID;
    private Connections connections;


    @Override
    public void start(int connectionId, Connections connections) {
        userDB = UserDataBase.getInstace();
        this.connectionID = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(Object message) {
        String[] tokens = ((String)message).split(" ");
        int i=1;
        int opcode = Integer.parseInt(tokens[0]);
        switch (opcode){
            case 1: // register
            {
                String username = tokens[1];
                String password = tokens[2];
                String birthday = tokens[3];
                if(userDB.getUser(username) != null){
                    connections.send(connectionID,"ERROR 1");
                }
                else{
                    User user = new User(username,password,birthday);
                    userDB.setUserDB(user);
                    connections.send(connectionID,"ACK 1");
                }
                break;
            }
            case 2: // login
            {
                String username = tokens[1];
                String password = tokens[2];
                int capatcha = Integer.parseInt(tokens[3]);
                User user = userDB.getUser(username);
                if(user != null && user.getPassword().equals(password) && user.getCurClient() != -1){
                    if(capatcha == 1){
                        user.setCurClient(connectionID);
                        connections.send(connectionID,"ACK 2");
                    }
                }
                else
                    connections.send(connectionID,"ERROR 2");
                break;
            }
            case 3: // logout
            {
                if(((ConnectionsImp)connections).getUserMap(connectionID) != null){
                    User user = ((ConnectionsImp)connections).getUserMap(connectionID);
                    user.setCurClient(-1);
                    connections.send(connectionID,"ACK 3");
                }
                else
                    connections.send(connectionID,"ERROR 3");
                break;
            }
            case 4: // follow / unfollow
        }
    }

    private void register(String[] tokens){

    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
