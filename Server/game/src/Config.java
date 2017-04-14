import java.util.*;

public final class Config{
  public static final double SESSION_LENGTH = 10;
//  public static final double SESSION_BREAK = 0.5;

  public static final String MYSQL_ADDR = "jdbc:mysql://localhost:3306/test?autoReconnect=true&useSSL=false";
  public static final String MYSQL_USER = "root";

  public static final double LoS = 0.00025;
  public static final int NPC_SPAWN_COUNT = 10;
  public static final double NPC_SPAWN_CHANCE = 0.01;
  public static final int NPC_SPAWN_ATTEMPTS = 5;
  public static final double NPC_ATTACK_RANGE = 0.0001;
  public static final double NPC_ATTACK_CHANCE = 0.05;

  public static final int HUMAN_HEALTH = 40;
  public static final int HUMAN_ATTACK = 10;
  public static final int HUMAN_DEFENSE = 2;
  public static final int HUMAN_RESOURCES = 500;
  
  public static final int HUMAN_NPC_HEALTH = 10;
  public static final int HUMAN_NPC_ATTACK = 0;
  public static final int HUMAN_NPC_DEFENSE = 0;
  public static final int HUMAN_NPC_RESOURCES = 100;
  
  public static final int ZOMBIE_HEALTH = 20;
  public static final int ZOMBIE_ATTACK = 10;
  public static final int ZOMBIE_DEFENSE = 0;
  public static final int ZOMBIE_RESOURCES = 300;
  
  public static final int ZOMBIE_NPC_HEALTH = 20;
  public static final int ZOMBIE_NPC_ATTACK = 5;
  public static final int ZOMBIE_NPC_DEFENSE = 0;
  public static final int ZOMBIE_NPC_RESOURCES = 50;
 
  public static void loadMySQLDriver(int attempts){
    if (attempts > 100) {return;}
    try{
      Class.forName("com.mysql.jdbc.Driver");
    } catch (Exception e){
      try {
        Thread.sleep(1000);
        loadMySQLDriver(attempts+1);
      } catch (InterruptedException ie){
        loadMySQLDriver(attempts+1);
      }
    }
  }

  public static Map<String, String> parseGET(Map map){
    Map<String, String> getReq = new HashMap<String, String>();

    for (Object key: map.keySet()){
      String keyStr = (String)key;
      String[] value = (String[])map.get(keyStr);
      String concatValue = new String();
      for (String s : value){
        concatValue += s;
      }
      getReq.put((String)key, concatValue);
    }
    return getReq;
  }

  public static final String MYSQL_PASS = "mysql";

  private Config(){
    throw new AssertionError();
  }
}
