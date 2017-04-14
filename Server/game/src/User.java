import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.util.*;

import java.sql.*;

public class User {
  public int m_id;
  public String m_name;
  public boolean m_human;
  public int m_health;
  public int m_attack;
  public int m_defense;
  public int m_resources;
  public double m_locX;
  public double m_locY;

  // Manually construct User
  public User(int id, String name, boolean human, int health,
      int attack, int defense, int resources, double locX, double locY){
    m_id = id;
    m_name = name;
    m_human = human;
    m_health = health;
    m_attack = attack;
    m_defense = defense;
    m_resources = resources;
    m_locX = locX;
    m_locY = locY;
  }
  // Gets existing user from database
  public User(int id){
    try {
     Config.loadMySQLDriver(0);
      Connection con = DriverManager.getConnection(
          Config.MYSQL_ADDR, Config.MYSQL_USER, Config.MYSQL_PASS);
      PreparedStatement stmt = con.prepareStatement("SELECT * FROM userInfo WHERE UserID=?");
      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()){
        m_id = id;
        m_name = rs.getString("UserName");
        m_human = rs.getBoolean("Human");
        m_health = rs.getInt("Health");
        m_attack = rs.getInt("Attack");
        m_defense = rs.getInt("Defense");
        m_resources = rs.getInt("Resources");
        m_locX = rs.getDouble("LocationX");
        m_locY = rs.getDouble("LocationY");
      } else {
        m_id = -1;
      }
      con.close();
    } catch (Exception e){
      m_id = -1;
    }
  }

  // Creates user object with default 
  public User(String name, double locX, double locY, String type){
    // handled by auto-increment
    m_id = -1;
    // should be fine for multiple users to have same name, no check done
    m_name = name;
    if (type.equals("human")){
      m_human = true;
      m_health = Config.HUMAN_HEALTH;
      m_attack = Config.HUMAN_ATTACK;
      m_defense = Config.HUMAN_DEFENSE;
      m_resources = Config.HUMAN_RESOURCES;
    } else if (type.equals("zombie")){
      m_human = false;
      m_health = Config.ZOMBIE_HEALTH;
      m_attack = Config.ZOMBIE_ATTACK;
      m_defense = Config.ZOMBIE_DEFENSE;
      m_resources = Config.ZOMBIE_RESOURCES;
    } else if (type.equals("human_npc")){
      m_human = true;
      m_health = Config.HUMAN_NPC_HEALTH;
      m_attack = Config.HUMAN_NPC_ATTACK;
      m_defense = Config.HUMAN_NPC_DEFENSE;
      m_resources = Config.HUMAN_NPC_RESOURCES;
    } else if (type.equals("zombie_npc")){
      m_human = false;
      m_health = Config.ZOMBIE_NPC_HEALTH;
      m_attack = Config.ZOMBIE_NPC_ATTACK;
      m_defense = Config.ZOMBIE_NPC_DEFENSE;
      m_resources = Config.ZOMBIE_NPC_RESOURCES;
    } else {
      m_human = true;
      m_health = Config.HUMAN_HEALTH;
      m_attack = Config.HUMAN_ATTACK;
      m_defense = Config.HUMAN_DEFENSE;
      m_resources = Config.HUMAN_RESOURCES;
    }
    m_locX = locX;
    m_locY = locY;
  }

  public int add(){
    try {
      Config.loadMySQLDriver(0);
      Connection con = DriverManager.getConnection(
          Config.MYSQL_ADDR, Config.MYSQL_USER, Config.MYSQL_PASS);
      PreparedStatement insStmt = con.prepareStatement("INSERT INTO userInfo (UserName, Human, Health, Attack, Defense, Resources, LocationX, LocationY) VALUES (?,?,?,?,?,?,?,?)");
      insStmt.setString(1, m_name);
      insStmt.setBoolean(2, m_human);
      insStmt.setInt(3, m_health);
      insStmt.setInt(4, m_attack);
      insStmt.setInt(5, m_defense);
      insStmt.setInt(6, m_resources);
      insStmt.setDouble(7, m_locX);
      insStmt.setDouble(8, m_locY);
      insStmt.executeUpdate();
      ResultSet rs = con.createStatement().executeQuery("SELECT MAX(UserID) FROM userInfo");
      rs.next();
      String idString = rs.getString(1);
      con.close();
      return Integer.valueOf(idString);
    } catch (Exception e){
//      System.out.println(e);
      return -1;
    }
  }

  public int update(){
    try {
      Config.loadMySQLDriver(0);
      Connection con = DriverManager.getConnection(
          Config.MYSQL_ADDR, Config.MYSQL_USER, Config.MYSQL_PASS);
      PreparedStatement insStmt = con.prepareStatement("UPDATE userInfo SET UserName = ?, Human = ?, Health = ?, Attack = ?, Defense = ?, Resources = ?, LocationX = ?, LocationY = ? WHERE UserID = ?");
      insStmt.setString(1, m_name);
      insStmt.setBoolean(2, m_human);
      insStmt.setInt(3, m_health);
      insStmt.setInt(4, m_attack);
      insStmt.setInt(5, m_defense);
      insStmt.setInt(6, m_resources);
      insStmt.setDouble(7, m_locX);
      insStmt.setDouble(8, m_locY);
      insStmt.setInt(9, m_id);
      insStmt.executeUpdate();
      con.close();
      return m_id;
    } catch (Exception e){
      System.out.println(e);
      return -1;
    }
  }

  public boolean isWithinRangeOf(double x, double y, double radius){
    return (Math.sqrt(Math.pow(m_locX-x,2) + Math.pow(m_locY-y,2)) < radius);
  }

  public static List<User> usersWithinRange(double x, double y, double radius){
    return usersWithinRange(x,y,radius,false);
  }
  public static List<User> usersWithinRange(double x, double y, double radius, 
      boolean playersOnly){
    try {
     Config.loadMySQLDriver(0);
      Connection con = DriverManager.getConnection(
          Config.MYSQL_ADDR, Config.MYSQL_USER, Config.MYSQL_PASS);
      PreparedStatement inRange = con.prepareStatement(
          "SELECT * FROM userInfo WHERE LocationX > ? AND LocationX < ? AND LocationY > ? and LocationY < ?" + ((playersOnly)?(" AND UserName NOT LIKE \"NPC%\""):("")));
      inRange.setDouble(1, x-radius);
      inRange.setDouble(2, x+radius);
      inRange.setDouble(3, y-radius);
      inRange.setDouble(4, y+radius);
      ResultSet rs = inRange.executeQuery();
      List<User> users = new ArrayList<User>();
      while (rs.next()){
        User u = new User(
            rs.getInt("UserID"),
            rs.getString("UserName"),
            rs.getBoolean("Human"),
            rs.getInt("Health"),
            rs.getInt("Attack"),
            rs.getInt("Defense"),
            rs.getInt("Resources"),
            rs.getDouble("LocationX"),
            rs.getDouble("LocationY"));
        if (u.isWithinRangeOf(x,y,radius)){
          users.add(u);
        }
      }
      con.close();
      return users;
    } catch (Exception e){
      return null;
    }
  }
}
