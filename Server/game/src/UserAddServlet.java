import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.util.*;

import java.sql.*;
import java.security.SecureRandom;
import java.math.BigInteger;

public class UserAddServlet extends HttpServlet implements Servlet {
  private SecureRandom random;
  private int serverCounter;

  public UserAddServlet(){
    random = new SecureRandom();
    serverCounter = 0;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String pageTitle = "Add User";
    request.setAttribute("title", pageTitle);

    String content = new String();
    
    Map<String,String> getReq = Config.parseGET(request.getParameterMap());

    String getName = getReq.get("name");
    String getX = getReq.get("x");
    String getY = getReq.get("y");
    String getHuman = getReq.get("type");
    String name = new BigInteger(64, random).toString(32);
    if (getName != null){
      name = getName;
    }
    double x = 0.0f;
    if (getX != null){
      x = Double.valueOf(getX);
    }
    double y = 0.0f;
    if (getY != null){
      y = Double.valueOf(getY);
    }
    String type = "human";
    if (getHuman != null){
      type = getHuman;
    }

    User u = new User(name, x, y, type);
    int userID = u.add();
    content = "ID: " + Integer.toString(userID) + "<br>\n";
    content += "SESSION: " + SessionServlet.getSessionID() + "<br>\n";

    long remain = SessionServlet.timeRemainingMillis();
    int sessionMinutes = (int)Math.ceil(remain/(60*1000));
    int sessionSeconds = ((int)Math.ceil(remain/1000))%60;
    int sessionMillis = (int)remain%1000;
    content += "TIME: " + String.valueOf(sessionMinutes) +
      ":" + String.valueOf(sessionSeconds);
      
    if (getReq.get("bot") != null){
      List<User> prohibitedSpawn = User.usersWithinRange(x,y,Config.LoS*3,true);
      for (int i = 0; i < Config.NPC_SPAWN_COUNT; i++){
        boolean npcHuman = (random.nextDouble() > 0.5);
        double angle = random.nextDouble()*(double)Math.PI*2;
        double magnitude = random.nextDouble()*2*Config.LoS;
        double npcX = x + magnitude*(double)Math.cos(angle);
        double npcY = y + magnitude*(double)Math.sin(angle);
        
        boolean allowedSpawn = true;
        for (User inRange : prohibitedSpawn){
          if (inRange.m_id != userID && inRange.isWithinRangeOf(npcX,npcY,Config.LoS)){
            allowedSpawn = false;
            break;
          }
        }
        if (allowedSpawn){
          User npc = new User("NPC "
              + new BigInteger(15, random).toString(32),
              npcX, npcY, (npcHuman)?"human_npc":"zombie_npc");
          npc.add();
        } else {
          i -= 1;
        }
      }
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
