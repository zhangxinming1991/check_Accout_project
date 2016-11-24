<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>密码重设</title>
</head>
<body>
	密码重设<br>
		<form name="Test_Map_form" action="/check_Accout/PMController/resetpwd" method ="post">
		用户名：<%=request.getAttribute("username") %><br>
		新密码:<input type="password" name="pwd" value=""><br>
		确认新密码：<input type="password" name="pwd_again" value=""><br>
		<input type="hidden" name="username" value=<%=request.getAttribute("username") %>><br>
		<input type="hidden" name="resetid" value=<%=request.getAttribute("resetid") %>><br>
		<input type="submit" name="resetpwd" value="确认修改">
	</form>
</body>
</html>