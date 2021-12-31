package bgu.spl.net.api;

import bgu.spl.net.srv.*;

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
                if(user != null && user.getPassword().equals(password) && user.getCurClient() == -1 && capatcha == 1 &&
                        ((ConnectionsImp)connections).getUserMap(connectionID) == null){
                    user.setCurClient(connectionID);
                    ((ConnectionsImp)connections).setUserMap(connectionID,user); // in order to know if certain connectionID has already logged in to some user
                    connections.send(connectionID,"ACK 2");
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
                    ((ConnectionsImp)connections).removeUserMap(connectionID);
                    connections.send(connectionID,"ACK 3");
                }
                else
                    connections.send(connectionID,"ERROR 3");
                break;
            }
            case 4: // follow / unfollow
            {
                int command = Integer.parseInt(tokens[1]);
                String username = tokens[2];
                if(userDB.getUser(username) == null | ((ConnectionsImp)connections).getUserMap(connectionID) == null) { // if user is not registered or the CH is not logged in to a user
                    connections.send(connectionID,"ERROR 4");
                }
                else{

                }
            }
        }
    }

    private void register(String[] tokens){

    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
