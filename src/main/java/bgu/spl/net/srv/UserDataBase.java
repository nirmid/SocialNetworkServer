package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UserDataBase {
    private ConcurrentHashMap<String,User> userDB ;
    //private ConcurrentHashMap<User, ConcurrentLinkedQueue<String>> blockDB;
    private ConcurrentHashMap<User, ConcurrentLinkedQueue<String>> postDB;
    private ConcurrentLinkedQueue<String> postsAndPms;
    private ConcurrentLinkedQueue<String> wordsToFilter;
    private static boolean isDone = false;
    private static UserDataBase DB = null;



    private UserDataBase(){
        postsAndPms = new ConcurrentLinkedQueue<String>();
        userDB = new ConcurrentHashMap<String,User>();
        //blockDB = new ConcurrentHashMap<User , ConcurrentLinkedQueue<String>>();
        postDB = new ConcurrentHashMap<User, ConcurrentLinkedQueue<String>>();
        wordsToFilter = new ConcurrentLinkedQueue<String>();
    }

    public static UserDataBase getInstance() {
        if (isDone == false) {
            synchronized (UserDataBase.class) {
                if (isDone == false) {
                    DB = new UserDataBase();
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

    /*
    public void setBlockDB(User user , String username){
        if(blockDB.get(user) == null ){
            ConcurrentLinkedQueue list = new ConcurrentLinkedQueue<String>();
            list.add(username);
            blockDB.put(user,list);
        }
        else{
            blockDB.get(user).add(username);
        }
    }


    public boolean isUserBlocked(User user, String username){
        if(blockDB.get(user) == null)
            return false;
        return blockDB.get(user).contains(username);
    }
     */

    public void addPostOrPm(String string){
        postsAndPms.add(string);
    }
    public ConcurrentLinkedQueue<String> getWordsToFilter(){return wordsToFilter;}

    public void addWordToFilter(String word){wordsToFilter.add(word);}


}
