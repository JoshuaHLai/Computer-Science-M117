<html>
<%=request.getAttribute("script") %>
<head>
    <title><%= request.getAttribute("title") %></title>
</head>
<body>
  <%= request.getAttribute("content")%>
</body>
</html>
