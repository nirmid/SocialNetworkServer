package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataBase {
    private ConcurrentHashMap<String,User> userDB ;
    private ConcurrentHashMap<User, ConcurrentLinkedQueue<String>> postDB;
    private ConcurrentLinkedQueue<String> postsAndPms;
    private ConcurrentLinkedQueue<String> wordsToFilter;
    private static boolean isDone = false;
    private static DataBase DB = null;



    private DataBase(){
        postsAndPms = new ConcurrentLinkedQueue<String>();
        userDB = new ConcurrentHashMap<String,User>();
        postDB = new ConcurrentHashMap<User, ConcurrentLinkedQueue<String>>();
        wordsToFilter = new ConcurrentLinkedQueue<String>();
    }

    public static DataBase getInstance() {
        if (isDone == false) {
            synchronized (DataBase.class) {
                if (isDone == false) {
                    DB = new DataBase();
                    isDone = true;
                }
            }
        }
        return DB;
    }

    public void setUserDB(User user){
        userDB.put(user.getUserName(),user);
    }

    public User getUser(String username){ // can return null!
        return userDB.get(username);
    }

    public ConcurrentHashMap<String,User> getUserDB(){return userDB;}

    public void addPostOrPm(String string){
        postsAndPms.add(string);
    }
    public ConcurrentLinkedQueue<String> getWordsToFilter(){return wordsToFilter;}

    public void addWordToFilter(String word){wordsToFilter.add(word);}


}
