package bgu.spl.net.srv;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

public class User {
    private String userName;
    private String password;
    private String birthDate;
    private int age;
    private ConcurrentLinkedQueue<User> followers;
    private ConcurrentLinkedQueue<User> following;
    private ConcurrentLinkedQueue<Object> messages;
    private ConcurrentLinkedQueue<User> blocked;
    private int numOfPost;
    private int curClient;

    public User(String _userName,String _password, String _birthDate){
        userName = _userName;
        password = _password;
        birthDate = _birthDate;
        followers = new ConcurrentLinkedQueue<User>();
        following = new ConcurrentLinkedQueue<User>();
        messages = new ConcurrentLinkedQueue<Object>();
        blocked= new ConcurrentLinkedQueue<User>();
        curClient = -1;
        numOfPost = 0;
        age = calculateAge();
    }

    private int calculateAge(){
        LocalDate date = LocalDate.now();
        LocalDate birthday = LocalDate.parse(birthDate);
        return Period.between(birthday,date).getYears();
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public String getBirthDate(){ return birthDate;}

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

    public void addPost(){ numOfPost = numOfPost +1;}

    public void addBlocked(User user){ blocked.add(user); }

    public boolean isBlocked(User user){ return blocked.contains(user); }

    public int getNumOfPost() { return numOfPost;}

    public void addMessage(Object msg){
        messages.add(msg);
    }

    public Object removeMessage(){
        return messages.poll();
    }

    public ConcurrentLinkedQueue<User>  getFollowers(){return followers;}

    public String getStat(){
        String output = "ACK 8 "+age+" "+numOfPost+" "+followers.size()+" "+following.size();
        return output;
    }




}
