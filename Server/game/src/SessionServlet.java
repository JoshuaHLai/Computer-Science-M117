import java.io.IOException;

import java.util.*;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.security.SecureRandom;
import java.math.BigInteger;

import java.sql.*;

public class SessionServlet extends HttpServlet implements Servlet {
  private SecureRandom random;
  static private long start;
  public String sessionID;

  public SessionServlet() {}

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String pageTitle = "Session Info";
    request.setAttribute("title", pageTitle);

    String content = new String();

    Map<String,String> getReq = Config.parseGET(request.getParameterMap());
    if (getReq.get("restart") != null){
        endSession();
        initializeSession();
        start = System.currentTimeMillis();
    }
    
    long elapsed = timeElapsedMillis();
    int sessionMinutes = (int)Math.ceil(elapsed/(60*1000));
    int sessionSeconds = ((int)Math.ceil(elapsed/1000))%60;
    int sessionMillis = (int)elapsed%1000;

    content = "Session ID: " + sessionID + "<br>\n";
    if (getReq.get("dynamic") != null){
	    request.setAttribute("script", "<script>" + "var start = new Date().getTime();"
          + "var x = setInterval(function(){"
//          + "var m = "+String.valueOf(sessionMinutes)+";var s ="+String.valueOf(sessionSeconds)
          + "var ms ="+String.valueOf(elapsed)+";var now = new Date().getTime();"
          + "var distance = now - (start - ms);"
          + "var minutes = Math.floor(distance/(1000*60));"
          + "var seconds = (distance/1000)%60;"
//          + "var millis = distance % (1000*60);"
          + "document.getElementById(\"timer\").innerHTML = "
          + "\"Session Length: \"+minutes+\":\"+Math.floor(seconds*10)/10;"
          + "}, 100);</script>");
      content += "<span id=\"timer\">"+"Session Length: " + String.valueOf(sessionMinutes) + ":"
        + String.valueOf(sessionSeconds) + "." + String.valueOf(sessionMillis) + "</span>" + "<br>\n";
    } else {
      content += "Session Length: " + String.valueOf(sessionMinutes) + ":"
        + String.valueOf(sessionSeconds) + "." + String.valueOf(sessionMillis) + "<br>\n";
    }

    content += "<br>\n";
    content += "Scoreboard:<br>\n";
    try {
      Config.loadMySQLDriver(0);
      Connection con = DriverManager.getConnection(
          Config.MYSQL_ADDR, Config.MYSQL_USER, Config.MYSQL_PASS);
      ResultSet rs = con.createStatement().executeQuery("SELECT * FROM userInfo ORDER BY Resources DESC LIMIT 15");
      while (rs.next()){
        content += String.valueOf(rs.getInt("Resources")) + ": "
          + rs.getString("UserName") + "<br>\n";
      }
      con.close();
    } catch (Exception e){
      System.out.println("doGet()");
//      System.out.println(e);
    }
    request.setAttribute("content", content);
    if (getReq.get("app") != null){
      request.setAttribute("content", content.replaceAll("<br>", ""));
      request.getRequestDispatcher("/app.jsp").forward(request, response);
    } else {
      request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
  }

  public void init() throws ServletException{
    Config.loadMySQLDriver(0);
    random = new SecureRandom();

    endSession();
    initializeSession();

    Timer timer = new Timer();
    timer.schedule(new TimerTask(){
      @Override
      public void run(){
        endSession();
        initializeSession();
        start = System.currentTimeMillis();
      }
    },(long)(1000*60*Config.SESSION_LENGTH),(long)(1000*60*Config.SESSION_LENGTH));
    start = System.currentTimeMillis();
    System.out.println("Session Initialized");
  }

  private void endSession(){
    try {
      Config.loadMySQLDriver(0);
      Connection con = DriverManager.getConnection(
          Config.MYSQL_ADDR, Config.MYSQL_USER, Config.MYSQL_PASS);
      con.createStatement().executeUpdate("DROP TABLE IF EXISTS userInfo");
      con.createStatement().executeUpdate("DROP TABLE IF EXISTS resources");
      con.createStatement().executeUpdate("DROP TABLE IF EXISTS session");
      con.createStatement().executeUpdate("CREATE TABLE userInfo (UserID INT PRIMARY KEY AUTO_INCREMENT, Human BOOLEAN, UserName text, Health INT, Attack INT, Defense INT, Resources INT, LocationX DOUBLE, LocationY DOUBLE)");
      con.createStatement().executeUpdate("CREATE TABLE session (SessionID text)");
      System.out.println("Session ended");
      con.close();
    } catch (Exception e){
      System.out.println("endSession()");
//      System.out.println(e);
    }
  }

  private void initializeSession(){
    sessionID = new BigInteger(64, random).toString(32);
    try {
      Config.loadMySQLDriver(0);
      Connection con = DriverManager.getConnection(
          Config.MYSQL_ADDR, Config.MYSQL_USER, Config.MYSQL_PASS);
      PreparedStatement ps = con.prepareStatement("INSERT INTO session VALUES (?)");
      ps.setString(1, sessionID);
      ps.executeUpdate();
      con.close();
    } catch (Exception e){
      System.out.println("initializeSession()");
//      System.out.println(e);
    }
    System.out.println("New Session Started");
  }

  public static long timeRemainingMillis(){
    long sessionLength = (long)(Config.SESSION_LENGTH * 60*1000);
    return sessionLength - (System.currentTimeMillis() - start);
  }

  public static long timeElapsedMillis(){
    return System.currentTimeMillis() - start;
  }

  public static String getSessionID(){
    try {
     Config.loadMySQLDriver(0);
      Connection con = DriverManager.getConnection(
          Config.MYSQL_ADDR, Config.MYSQL_USER, Config.MYSQL_PASS);
      ResultSet rs = con.createStatement().executeQuery("SELECT * FROM session");
      rs.next();
      String sID = rs.getString("SessionID");
      con.close();
      return sID;
    } catch (Exception e){
      System.out.println("getSessionID()");
      e.printStackTrace();
      //      System.out.println(e);
    }
    return null;
  }
}
