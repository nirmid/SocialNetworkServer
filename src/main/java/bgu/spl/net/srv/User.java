package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentLinkedQueue;

public class User {
    private String userName;
    private String password;
    private String birthDate;
    private ConcurrentLinkedQueue<User> followers;
    private ConcurrentLinkedQueue<User> following;
    private ConcurrentLinkedQueue<Object> meseeges;
    private int curClient;

    public User(String _userName,String _password, String _birthDate){
        userName = _userName;
        password = _password;
        birthDate = _birthDate;
        followers = new ConcurrentLinkedQueue<User>();
        following = new ConcurrentLinkedQueue<User>();
        meseeges = new ConcurrentLinkedQueue<Object>();
        curClient = -1;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public int getCurClient(){
        return curClient;
    }

    public void setCurClient(int clientId){
        curClient = clientId;
    }

    public boolean follow(User user){
        return following.add(user);
    }

    public boolean newFollower(User user){
        return followers.add(user);
    }

    public boolean isFollowing(User user){return following.contains(user);}

    public boolean unFollow(User user){
        return following.remove(user);
    }

    public boolean removeFollower(User user){
        return followers.remove(user);
    }

    public void addMessage(Object msg){
        meseeges.add(msg);
    }

    public Object removeMessage(){
        return meseeges.poll();
    }




}
