package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class UserDataBase {
    private ConcurrentHashMap<String,User> userDB ;
    private static boolean isDone = false;
    private static UserDataBase DB = null;


    private UserDataBase(){
        userDB = new ConcurrentHashMap<String,User>();
    }

    public static UserDataBase getInstace() {
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


}
