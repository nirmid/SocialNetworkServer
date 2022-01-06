package bgu.spl.net.api;

import bgu.spl.net.srv.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BidiMessagingProtocolImp implements BidiMessagingProtocol<String> {
    private DataBase dataBase;
    private int connectionID;
    private Connections connections;


    @Override
    public void start(int connectionId, Connections connections) {
        dataBase = DataBase.getInstance();
        this.connectionID = connectionId;
        this.connections = connections;
        System.out.println("protocol started");
    }


    @Override
    public void process(String message) {
        System.out.println(message);
        int opcode = Integer.parseInt((message).substring(0, 2));
        String string = message.substring(2);
        switch (opcode) {
            case 1: // register
            {
                String[] tokens = string.split("\0");
                String username = tokens[0];
                String password = tokens[1];
                String birthday = tokens[2];
                if (dataBase.getUser(username) != null) {
                    connections.send(connectionID, "1101");
                } else {
                    User user = new User(username, password, birthday);
                    dataBase.setUserDB(user);
                    connections.send(connectionID, "1001");
                }
                break;
            }
            case 2: // login
            {
                String[] tokens = string.split("\0");
                String username = tokens[0];
                String password = tokens[1];
                int capatcha = Integer.parseInt(tokens[2]);
                User user = dataBase.getUser(username);
                if (user != null && user.getCurClient() == -1) {
                    synchronized (user) {

                        if (user.getPassword().equals(password) && user.getCurClient() == -1 && capatcha == 1 &&
                                ((ConnectionsImp) connections).getUserMap(connectionID) == null) {
                            user.setCurClient(connectionID);
                            ((ConnectionsImp) connections).setUserMap(connectionID, user); // in order to know if certain connectionID has already logged in to some user
                            connections.send(connectionID, "1002");
                            Object toSend  = user.removeMessage();
                            while (toSend != null) {
                                connections.send(connectionID, toSend);
                                toSend = user.removeMessage();
                            }
                        }
                        else
                            connections.send(connectionID, "1102");
                    }
                }
                else
                    connections.send(connectionID, "1102");
                break;
            }
            case 3: // logout
            {
                if (((ConnectionsImp) connections).getUserMap(connectionID) != null) {
                    User user = ((ConnectionsImp) connections).getUserMap(connectionID);
                    user.setCurClient(-1);
                    ((ConnectionsImp) connections).removeUserMap(connectionID);
                    connections.send(connectionID, "1003");
                    connections.disconnect(connectionID);

                } else
                    connections.send(connectionID, "1103");
                break;
            }
            case 4: // follow / unfollow
            {
                int command = Integer.parseInt(string.substring(0, 1));
                String username = string.substring(1);
                User clientUser = ((ConnectionsImp) connections).getUserMap(connectionID);
                User targetUser = dataBase.getUser(username);
                if (targetUser == null | clientUser == null || clientUser.isBlocked(targetUser)) { // if user is not registered or the CH is not logged in to a user or blocked
                    connections.send(connectionID, "1104");
                } else {
                    if (command == 0) {
                        if (clientUser.isFollowing(targetUser)) {
                            connections.send(connectionID, "1104");
                        } else {
                            clientUser.follow(targetUser);
                            targetUser.newFollower(clientUser);
                            connections.send(connectionID, "1004" + username +"\0");
                        }
                    } else if (command == 1) {
                        if (!clientUser.isFollowing(targetUser)) {
                            connections.send(connectionID, "1104");
                        } else {
                            clientUser.unFollow(targetUser);
                            dataBase.getUser(username).removeFollower(clientUser);
                            connections.send(connectionID, "1004" + username +"\0");
                        }
                    }
                    else
                        connections.send(connectionID, "1104");
                }
                break;
            }

            case 5: { // POST
                User clientUser = ((ConnectionsImp) connections).getUserMap(connectionID);
                if (clientUser != null) {
                    String post = ((String) string).substring(0, string.length() - 1);
                    dataBase.addPostOrPm(post);
                    String[] tokens = post.split(" ");
                    connections.send(connectionID,"1005");
                    String output ="091"+clientUser.getUserName()+"\0"+post+"\0";
                    for (User follower : clientUser.getFollowers()) {
                        if (follower.getCurClient() != -1)
                            connections.send(follower.getCurClient(), output);
                        else
                            follower.addMessage(output);
                    }
                    for (String word : tokens) {
                        if (((Character) word.charAt(0)).equals('@')) {
                            if (dataBase.getUser(word.substring(1)) != null && !clientUser.isBlocked(dataBase.getUser(word.substring(1)))) {
                                if (dataBase.getUser(word.substring(1)).getCurClient() != -1)
                                    connections.send(dataBase.getUser(word.substring(1)).getCurClient(), output);
                                else {
                                    dataBase.getUser(word.substring(1)).addMessage(output);
                                }
                            }
                        }
                    }
                    clientUser.addPost(); // adding user's post counter
                } else
                    connections.send(connectionID, "1105");
                break;
            }

            case 6: // PM
                User clientUser = ((ConnectionsImp) connections).getUserMap(connectionID);
                if (clientUser != null) {
                    String[] tokens = string.split("\0");
                    if (dataBase.getUser(tokens[0]) != null && clientUser.isFollowing(dataBase.getUser(tokens[0]))) {
                        String filteredMessage = "";
                        String[] pm = tokens[1].split(" ");
                        for (String word : pm) {
                            if (dataBase.getWordsToFilter().contains(word))
                                filteredMessage = filteredMessage + "<filtered> ";
                            else
                                filteredMessage = filteredMessage + word + " ";
                        }
                        String output ="090"+clientUser.getUserName()+"\0"+filteredMessage+" "+tokens[2]+"\0";
                        if (dataBase.getUser(tokens[0]).getCurClient() != -1)
                            connections.send(dataBase.getUser(tokens[0]).getCurClient(),output);
                        else
                            dataBase.getUser(tokens[0]).addMessage(output);
                        dataBase.addPostOrPm(filteredMessage);
                        connections.send(connectionID,"1006");
                    } else
                        connections.send(connectionID, "1106");
                } else
                    connections.send(connectionID, "1106");
                break;

            case 7: // logstat
            {
                if (((ConnectionsImp) connections).getUserMap(connectionID) != null) {
                    String output = "";
                    for (User user : ((ConnectionsImp) connections).getActiveUsers()) {
                        if(!user.isBlocked(((ConnectionsImp) connections).getUserMap(connectionID)))
                            output = output + "1007" + user.getStat();
                    }
                    connections.send(connectionID, output); // need to incode properly
                } else
                    connections.send(connectionID, "1107");
                break;
            }

            case 8: // STAT
            {
                if(((ConnectionsImp) connections).getUserMap(connectionID) != null){
                    //String[] temp = string.split("\0");
                    String[] tokens = string.substring(0,string.length()-1).split("\\|");
                    boolean proper = true;
                    String output = "";
                    for(int i = 0; i < tokens.length && proper ; i ++){
                        if(dataBase.getUser(tokens[i]) == null || dataBase.getUser(tokens[i]).isBlocked(((ConnectionsImp) connections).getUserMap(connectionID)))
                            proper = false;
                        else
                            output = output + "1008"+dataBase.getUser(tokens[i]).getStat();
                    }
                    if (proper)
                        connections.send(connectionID, output);
                    else
                        connections.send(connectionID, "1108");
                }
                else
                    connections.send(connectionID, "1108");
                break;
            }
            case 12: // Block
            {
                String check = string.substring(0,string.length()-1);
                User blockedUser =  dataBase.getUser(check);
                User blockingUser = ((ConnectionsImp) connections).getUserMap(connectionID);
                if(blockedUser != null && blockingUser != null ){
                    // remove blocking user from blocked user
                    blockedUser.removeFollower(blockingUser);
                    blockedUser.unFollow(blockingUser);
                    // remove blocked user from blocking user
                    blockingUser.removeFollower(blockedUser);
                    blockingUser.unFollow(blockedUser);
                    // adding to the blocked list at each user
                    blockingUser.addBlocked(blockedUser);
                    blockedUser.addBlocked(blockingUser);
                    connections.send(connectionID,"1012");
                }
                else
                    connections.send(connectionID,"1112");
            }
        }
    }



    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
