package bgu.spl.net.api;

import bgu.spl.net.srv.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BidiMessagingProtocolImp implements BidiMessagingProtocol {
    private DataBase userDB;
    private int connectionID;
    private Connections connections;


    @Override
    public void start(int connectionId, Connections connections) {
        userDB = DataBase.getInstance();
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
                    connections.send(connectionID, "111");
                } else {
                    User user = new User(username, password, birthday);
                    userDB.setUserDB(user);
                    connections.send(connectionID, "101");
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
                    connections.send(connectionID, "102");
                    message = user.removeMessage();
                    while (message != null){
                        connections.send(connectionID, message);
                        message = user.removeMessage();
                    }
                } else
                    connections.send(connectionID, "112");
                break;
            }
            case 3: // logout
            {
                if (((ConnectionsImp) connections).getUserMap(connectionID) != null) {
                    User user = ((ConnectionsImp) connections).getUserMap(connectionID);
                    user.setCurClient(-1);
                    ((ConnectionsImp) connections).removeUserMap(connectionID);
                    connections.send(connectionID, "103");
                } else
                    connections.send(connectionID, "113");
                break;
            }
            case 4: // follow / unfollow
            {
                int command = Integer.parseInt(string.substring(0, 1));
                String[] tokens = string.substring(2).split("\0");
                String username = tokens[0];
                User clientUser = ((ConnectionsImp) connections).getUserMap(connectionID);
                User targetUser = userDB.getUser(username);
                if (targetUser == null | clientUser == null || clientUser.isBlocked(targetUser)) { // if user is not registered or the CH is not logged in to a user or blocked
                    connections.send(connectionID, "114");
                } else {
                    if (command == 0) {
                        if (clientUser.isFollowing(targetUser)) {
                            connections.send(connectionID, "114");
                        } else {
                            clientUser.follow(targetUser);
                            targetUser.newFollower(clientUser);
                            connections.send(connectionID, "104" + username);
                        }
                    } else {
                        if (!clientUser.isFollowing(targetUser)) {
                            connections.send(connectionID, "114");
                        } else {
                            clientUser.unFollow(targetUser);
                            userDB.getUser(username).removeFollower(clientUser);
                            connections.send(connectionID, "104" + username);
                        }

                    }
                }
                break;
            }

            case 5: { // post
                User clientUser = ((ConnectionsImp) connections).getUserMap(connectionID);
                if (clientUser != null) {
                    String post = ((String) string).substring(0, string.length() - 1);
                    userDB.addPostOrPm(post);
                    String[] tokens = post.split(" ");
                    String output ="091"+clientUser.getUserName()+"\0"+post+"\0";
                    for (User follower : clientUser.getFollowers()) {
                        if (follower.getCurClient() != -1)
                            connections.send(follower.getCurClient(), output);
                        else
                            follower.addMessage(output);
                    }
                    for (String word : tokens) {
                        if (((Character) word.charAt(0)).equals('@')) {
                            if (userDB.getUser(word.substring(1)) == null)
                                connections.send(connectionID, "114"); ///????????? should it send an error?
                            else if (userDB.getUser(word.substring(1)).getCurClient() != -1)
                                connections.send(userDB.getUser(word.substring(1)).getCurClient(), post);
                            else {
                                userDB.getUser(word.substring(1)).addMessage(post);
                            }
                        }
                    }
                    clientUser.addPost(); // adding user's post counter
                } else
                    connections.send(connectionID, "115");
                break;
            }

            case 6: // PM
                User clientUser = ((ConnectionsImp) connections).getUserMap(connectionID);
                if (clientUser != null) { // if error accured should I save the PM??
                    String[] tokens = string.split("\0");
                    if (userDB.getUser(tokens[0]) != null) {
                        String filteredMessage = "";
                        String[] pm = string.split(" ");
                        for (String word : pm) {
                            if (userDB.getWordsToFilter().contains(word))
                                filteredMessage = filteredMessage + "<filtered> ";
                            else
                                filteredMessage = filteredMessage + word + " ";
                        }
                        String output ="090"+clientUser.getUserName()+"\0"+filteredMessage+"\0";
                        if (userDB.getUser(tokens[0]).getCurClient() != -1)
                            connections.send(userDB.getUser(tokens[0]).getCurClient(),output);
                            //connections.send(userDB.getUser(tokens[0]).getCurClient(), filteredMessage + "\0" + tokens[2]);
                        else
                            userDB.getUser(tokens[0]).addMessage(output);
                            //userDB.getUser(tokens[0]).addMessage(filteredMessage + "\0" + tokens[2]);
                        userDB.addPostOrPm(filteredMessage + "\0" + tokens[2]);///////// w\o date and time
                    } else
                        connections.send(connectionID, "116");
                } else
                    connections.send(connectionID, "116");
                break;

            case 7: // logstat
            {
                if (((ConnectionsImp) connections).getUserMap(connectionID) != null) {
                    LocalDate date = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    String[] today = date.format(formatter).split("-");
                    int year = Integer.parseInt(today[2]);
                    int month = Integer.parseInt(today[1]);
                    int day = Integer.parseInt(today[0]);
                    for (User user : userDB.getUserDB().values()) {
                        String stat = "10 7 ";
                        String[] birthDay = user.getBirthDate().split("-");
                        int age = year - Integer.parseInt(birthDay[2]);
                        //  if (month > )


                    }
                } else
                    connections.send(connectionID, "117");
                break;
            }

            case 8: // STAT
            {
                if(((ConnectionsImp) connections).getUserMap(connectionID) != null){
                    String[] tokens = string.split("(//|)|(\0)");
                    for(String username : tokens){
                        if(username != "//|" | username !="\0"){
                            if(userDB.getUser(username) == null)
                                connections.send(connectionID, "118");
                            else
                                connections.send(connectionID,userDB.getUser(username).getStat());
                        }
                    }
                }
                else
                    connections.send(connectionID, "118");
                break;
            }
            case 12: // Block
            {
                String[] tokens = string.split("\0");
                User blockedUser = userDB.getUser(tokens[0]);
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
