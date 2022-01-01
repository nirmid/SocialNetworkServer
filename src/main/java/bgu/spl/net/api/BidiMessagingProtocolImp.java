package bgu.spl.net.api;

import bgu.spl.net.srv.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BidiMessagingProtocolImp implements BidiMessagingProtocol {
    private UserDataBase userDB;
    private int connectionID;
    private Connections connections;


    @Override
    public void start(int connectionId, Connections connections) {
        userDB = UserDataBase.getInstance();
        this.connectionID = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(Object message) {
        int opcode = Integer.parseInt(((String) message).substring(0, 2));
        String string = ((String) message).substring(2);
        switch (opcode) {
            case 1: // register
            {
                String[] tokens = string.split("\0");
                String username = tokens[0];
                String password = tokens[1];
                String birthday = tokens[2];
                if (userDB.getUser(username) != null) {
                    connections.send(connectionID, "ERROR 1");
                } else {
                    User user = new User(username, password, birthday);
                    userDB.setUserDB(user);
                    connections.send(connectionID, "ACK 1");
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
                if (user != null && user.getPassword().equals(password) && user.getCurClient() == -1 && capatcha == 1 &&
                        ((ConnectionsImp) connections).getUserMap(connectionID) == null) {
                    user.setCurClient(connectionID);
                    ((ConnectionsImp) connections).setUserMap(connectionID, user); // in order to know if certain connectionID has already logged in to some user
                    connections.send(connectionID, "ACK 2");
                } else
                    connections.send(connectionID, "ERROR 2");
                break;
            }
            case 3: // logout
            {
                if (((ConnectionsImp) connections).getUserMap(connectionID) != null) {
                    User user = ((ConnectionsImp) connections).getUserMap(connectionID);
                    user.setCurClient(-1);
                    ((ConnectionsImp) connections).removeUserMap(connectionID);
                    connections.send(connectionID, "ACK 3");
                } else
                    connections.send(connectionID, "ERROR 3");
                break;
            }
            case 4: // follow / unfollow
            {
                int command = Integer.parseInt(string.substring(0, 1));
                String[] tokens = string.substring(2).split("\0");
                String username = tokens[0];
                if (userDB.getUser(username) == null | ((ConnectionsImp) connections).getUserMap(connectionID) == null
                        || userDB.isUserBlocked(userDB.getUser(username), ((ConnectionsImp) connections).getUserMap(connectionID).getUserName())) { // if user is not registered or the CH is not logged in to a user or blocked
                    connections.send(connectionID, "ERROR 4");
                } else {
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
                if (((ConnectionsImp) connections).getUserMap(connectionID) != null) {
                    String post = ((String) string).substring(0, string.length() - 1);
                    userDB.addPostOrPm(post);
                    String[] tokens = post.split(" ");
                    for (User follower : ((ConnectionsImp) connections).getUserMap(connectionID).getFollowers()) {
                        if (follower.getCurClient() != -1)
                            connections.send(follower.getCurClient(), post);
                        else
                            follower.addMessage(post);
                    }
                    for (String word : tokens) {
                        if (((Character) word.charAt(0)).equals('@')) {
                            if (userDB.getUser(word.substring(1)) == null)
                                connections.send(connectionID, "ERROR 4"); ///????????? should it send an error?
                            else if (userDB.getUser(word.substring(1)).getCurClient() != -1)
                                connections.send(userDB.getUser(word.substring(1)).getCurClient(), post);
                            else {
                                userDB.getUser(word.substring(1)).addMessage(post);
                            }
                        }
                    }
                }
                else
                    connections.send(connectionID, "ERROR 5");
                break;
            }

            case 6: // PM
                if (((ConnectionsImp) connections).getUserMap(connectionID) != null) { // if error accured should I save the PM??
                    String[] tokens = string.split("\0");
                    if (userDB.getUser(tokens[0]) != null){
                        String filteredMessage = "";
                        String[] pm = string.split(" ");
                        for (String word: pm){
                            if (userDB.getWordsToFilter().contains(word))
                                filteredMessage = filteredMessage + "<filtered> ";
                            else
                                filteredMessage = filteredMessage + word + " " ;
                        }
                        if (userDB.getUser(tokens[0]).getCurClient() != -1)
                            connections.send(userDB.getUser(tokens[0]).getCurClient(), filteredMessage + "\0" + tokens[2]);
                        else
                            userDB.getUser(tokens[0]).addMessage(filteredMessage + "\0" + tokens[2]);
                        userDB.addPostOrPm(filteredMessage + "\0" + tokens[2]);///////// w\o date and time
                    }
                    else
                        connections.send(connectionID, "ERROR 6");
                }
                else
                    connections.send(connectionID, "ERROR 6");
                break;

            case 7: // logstat
                if (((ConnectionsImp) connections).getUserMap(connectionID) != null) {
                    LocalDate date = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    String[] today = date.format(formatter).split("-");
                    int year = Integer.parseInt(today[2]);
                    int month = Integer.parseInt(today[1]);
                    int day = Integer.parseInt(today[0]);
                    for (User user: userDB.getUserDB().values()){
                        String stat = "10 7 ";
                        String[] birthDay = user.getBirthDate().split("-");
                        int age = year - Integer.parseInt(birthDay[2]);
                      //  if (month > )



                    }
                }
                else
                    connections.send(connectionID, "ERROR 7");




                }
    }


    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
