import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.util.Enumeration;

import java.sql.*;

public class EchoServlet extends HttpServlet implements Servlet {

  public EchoServlet() {}

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    String pageTitle = "Echo";
    request.setAttribute("title", pageTitle);

    String content = new String();
    content = getRequestText(request);
    request.setAttribute("content", content);

	  request.setAttribute("script", "");
    request.getRequestDispatcher("/index.jsp").forward(request, response);
  }

  private String getRequestText(HttpServletRequest request) throws IOException{
    String req = request.getMethod() + " " +
      request.getRequestURI() + "?" + request.getQueryString() + " "
      + request.getProtocol() + "<br>\n";

    String headers = new String();
    Enumeration headerNames = request.getHeaderNames();
    while(headerNames.hasMoreElements()) {
        String headerName = (String)headerNames.nextElement();
          headers += headerName + ": " + request.getHeader(headerName) + "<br>\n";
    }

    BufferedReader br = request.getReader();
    String bodyLine = br.readLine();
    String body = new String();
    while (bodyLine != null){
      body += bodyLine + "<br>\n";
      bodyLine = br.readLine();
    }
    return req+headers+body;
  }
}
