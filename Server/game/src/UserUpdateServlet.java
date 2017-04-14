import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.SecureRandom;
import java.math.BigInteger;
import java.io.BufferedReader;
import java.util.*;

import java.sql.*;

public class UserUpdateServlet extends HttpServlet implements Servlet {
  private SecureRandom random;

  public UserUpdateServlet(){
    random = new SecureRandom();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String pageTitle = "User Update";
    request.setAttribute("title", pageTitle);

    String content = new String();
    
    Map<String,String> getReq = Config.parseGET(request.getParameterMap());
    int id = Integer.valueOf(getReq.get("id"));
    User u = new User(id);
    String currSession = SessionServlet.getSessionID();
    if (u.m_id != -1 && currSession.equals(getReq.get("session"))){
      String[] getVals = {"name", "human", "h", "a", "d" ,"r", "x", "y"};
      for (String s : getVals){
        String val = getReq.get(s);
        if (val == null) continue;
        switch (s){
          case "name":
            u.m_name = val;
            break;
          case "human":
            u.m_human = !(val.equals("false"));
            break;
          case "h":
            u.m_health = Integer.valueOf(val);
            break;
          case "a":
            u.m_attack = Integer.valueOf(val);
            break;
          case "d":
            u.m_defense = Integer.valueOf(val);
            break;
          case "r":
            u.m_resources = Integer.valueOf(val);
            break;
          case "x":
            u.m_locX = Double.valueOf(val);
            break;
          case "y":
            u.m_locY = Double.valueOf(val);
            break;
        }
      }
      u.update();
      List<User> nearbyUsers = User.usersWithinRange(u.m_locX, u.m_locY, Config.LoS);
      content = new String();
      for (User neighbor : nearbyUsers){
        if (neighbor.m_id == u.m_id){
          content += "SELF";
        } else if (neighbor.m_human == u.m_human) {
          content += "ALLY";
        } else {
          content += "ENEMY";
          if (neighbor.m_name.startsWith("NPC") && 
              neighbor.isWithinRangeOf(u.m_locX, u.m_locY,
                Config.NPC_ATTACK_RANGE) &&
              random.nextDouble() < Config.NPC_ATTACK_CHANCE){
            AttackServlet.performAttack(currSession, neighbor.m_id, u.m_id,
                getReq.get("session"));
            // TODO: make npcs buy upgrades?
          }
        }
        content += ": " + String.valueOf(neighbor.m_id);
        content += ", " + ((neighbor.m_human)?"human":"zombie");
        content += ", " + neighbor.m_name;
        content += ", " + String.valueOf(neighbor.m_health);
        content += ", " + String.valueOf(neighbor.m_attack);
        content += ", " + String.valueOf(neighbor.m_defense);
        content += ", " + String.valueOf(neighbor.m_resources);
        content += ", " + String.valueOf(neighbor.m_locX);
        content += ", " + String.valueOf(neighbor.m_locY) + "<br>\n";
      }

      if (getReq.get("bot") != null &&
          random.nextDouble() < Config.NPC_SPAWN_CHANCE &&
          getReq.get("x") != null && getReq.get("y") != null){
        for (int attempts = 0; attempts < Config.NPC_SPAWN_ATTEMPTS;
            attempts += 1){
          double x = Double.valueOf(getReq.get("x"));
          double y = Double.valueOf(getReq.get("y"));
          List<User> prohibitedSpawn = User.usersWithinRange(x,y,Config.LoS*3,true);

          boolean npcHuman = (random.nextDouble() > 0.5);
          double angle = random.nextDouble()*(double)Math.PI*2;
          double magnitude = random.nextDouble()*Config.LoS + Config.LoS;
          double npcX = x + magnitude*(double)Math.cos(angle);
          double npcY = y + magnitude*(double)Math.sin(angle);
          
          boolean allowedSpawn = true;
          for (User inRange : prohibitedSpawn){
            if (inRange.isWithinRangeOf(npcX,npcY,Config.LoS)){
              allowedSpawn = false;
              break;
            }
          }
          if (allowedSpawn){
            User npc = new User("NPC " + new BigInteger(15,random).toString(32),
                npcX, npcY, (npcHuman)?"human_npc":"zombie_npc");
            npc.add();
            break;
          } else {
            // do nothing, attempts increments
          }
        }
      }
    } else if (!currSession.equals(getReq.get("session"))){
      content = "NEW SESSION: " + currSession;
    } else if (u.m_id == -1){
      content = "NONEXISTENT USER";
    }
    request.setAttribute("content", content);
	  request.setAttribute("script", "");
    if (getReq.get("app") != null){
      request.setAttribute("content", content.replaceAll("<br>", ""));
      request.getRequestDispatcher("/app.jsp").forward(request, response);
    } else {
      request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
  }
}
