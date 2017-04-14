import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.util.*;

import java.sql.*;

public class UserListServlet extends HttpServlet implements Servlet {

  public UserListServlet() {}

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String pageTitle = "User List";
    request.setAttribute("title", pageTitle);

    String content = new String();
    
    Map<String,String> getReq = Config.parseGET(request.getParameterMap());
    String idString = getReq.get("id");
    content = idString + "<br>\n";
    if (idString != null){
      int id = Integer.parseInt(getReq.get("id"));
      User u = new User(id);
//      content = "USER";
//      content += "_" + String.valueOf(u.m_id);
      content = String.valueOf(u.m_id);
      content += ", " + ((u.m_human)?"human":"zombie");
      content += ", " + u.m_name;
      content += ", " + String.valueOf(u.m_health);
      content += ", " + String.valueOf(u.m_attack);
      content += ", " + String.valueOf(u.m_defense);
      content += ", " + String.valueOf(u.m_resources);
      content += ", " + String.valueOf(u.m_locX);
      content += ", " + String.valueOf(u.m_locY) + "<br>\n";
    } else {
      int currID = 1;
      User u = new User(currID);
      content = "";
      while (u.m_id != -1){
//        content += "USER";
//        content += ": " + String.valueOf(u.m_id);
        content += String.valueOf(u.m_id);
        content += ", " + ((u.m_human)?"human":"zombie");
        content += ", " + u.m_name;
        content += ", " + String.valueOf(u.m_health);
        content += ", " + String.valueOf(u.m_attack);
        content += ", " + String.valueOf(u.m_defense);
        content += ", " + String.valueOf(u.m_resources);
        content += ", " + String.valueOf(u.m_locX);
        content += ", " + String.valueOf(u.m_locY) + "<br>\n";
        currID += 1;
        u = new User(currID);
      }
    }
    request.setAttribute("content", content);
	  request.setAttribute("script", "");
    System.out.println("Users Listed");
    if (getReq.get("app") != null){
      request.setAttribute("content", content.replaceAll("<br>", ""));
      request.getRequestDispatcher("/app.jsp").forward(request, response);
    } else {
      request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
  }
}
