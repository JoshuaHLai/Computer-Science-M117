import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.util.*;

import java.sql.*;

public class AttackServlet extends HttpServlet implements Servlet {

    public AttackServlet() {}

    public static String performAttack(String currSession, int id1,
        int id2, String session){
      String content = new String();
      User u1 = new User(id1);
      User u2 = new User(id2);

      if (u1.m_id != -1 && u2.m_id != -1
          && currSession.equals(session)){
        int health_lost = u1.m_attack - u2.m_defense;
        if (health_lost > 0) {
          int new_health = u2.m_health - health_lost;
          if (new_health > 0){
            content = "OLD HEALTH: " + String.valueOf(u2.m_health) + "<br>\n";
            u2.m_health = new_health;
            u2.update();
            content += "NEW HEALTH: " + String.valueOf(u2.m_health);
          } else {
            //u2.m_health = 0;
            // TODO: human to zombie, notify u2
            // give u1 resources, etc.
            u1.m_resources += u2.m_resources;
            
            // No handling for NPCs and recycling of ids
            // only a problem for prolonged game
            u2.m_name = "DEAD";
            u2.m_human = false;
            u2.m_health = Config.ZOMBIE_NPC_HEALTH;
            u2.m_attack = Config.ZOMBIE_NPC_ATTACK;
            u2.m_defense = Config.ZOMBIE_NPC_DEFENSE;
            u2.m_resources = Config.ZOMBIE_NPC_RESOURCES;

            // Sent to North Korea until app decides death period is over
            u2.m_locX = 40.3399f;
            u2.m_locY = 127.5101f;
            
            u1.update();
            u2.update();

            content = "KILL: " + String.valueOf(u2.m_resources);
          }
        }
      } else if (!currSession.equals(session)){
        content = "SESSION ENDED: " + currSession;
      } else if (u1.m_id == -1 || u2.m_id == -1){
        content = "NONEXISTENT USER";
      }
      return content;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      String pageTitle = "Attack";
      request.setAttribute("title", pageTitle);

      String content = new String();

      Map<String,String> getReq = Config.parseGET(request.getParameterMap());

      String currSession = SessionServlet.getSessionID();
      String id1String = getReq.get("id1");
      String id2String = getReq.get("id2");
      int id1 = Integer.valueOf(id1String);
      int id2 = Integer.valueOf(id2String);
      String sessionString = getReq.get("session");

      content = performAttack(currSession, id1, id2, sessionString);

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
