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
        int opcode = Integer.parseInt(((String)message).substring(0,2));
        String string = ((String)message).substring(3);
        switch (opcode){
            case 1: // register
            {
                String[] tokens = string.split("\0");
                String username = tokens[0];
                String password = tokens[1];
                String birthday = tokens[2];
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
                String[] tokens = string.split("\0");
                String username = tokens[0];
                String password = tokens[1];
                int capatcha = Integer.parseInt(tokens[2]);
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
                int command = Integer.parseInt(string.substring(0,1));
                String[] tokens = string.substring(2).split("\0");
                String username = tokens[0];
                if(userDB.getUser(username) == null | ((ConnectionsImp)connections).getUserMap(connectionID) == null
                        || userDB.isUserBlocked(userDB.getUser(username),((ConnectionsImp)connections).getUserMap(connectionID).getUserName() )) { // if user is not registered or the CH is not logged in to a user or blocked
                    connections.send(connectionID,"ERROR 4");
                }
                else {
                    if (command == 1) {
                        if (((ConnectionsImp) connections).getUserMap(connectionID).isFollowing(userDB.getUser(username))) {
                            connections.send(connectionID, "ERROR 4");
                        } else {
                            ((ConnectionsImp) connections).getUserMap(connectionID).follow(userDB.getUser(username));
                            connections.send(connectionID, "ACK 4 " + username);
                        }
                    } else {
                        if (!((ConnectionsImp) connections).getUserMap(connectionID).isFollowing(userDB.getUser(username))) {
                            connections.send(connectionID, "ERROR 4");
                        } else {
                            ((ConnectionsImp) connections).getUserMap(connectionID).unFollow(userDB.getUser(username));
                            connections.send(connectionID, "ACK 4 " + username);
                        }

                    }
                }
                break;
            }

            case 5: { // post
                String[] tokens = string.split("\0");
                String post = tokens[0];

            }
        }
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
