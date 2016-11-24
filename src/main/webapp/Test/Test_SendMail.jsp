<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>发送邮件测试</title>
</head>
<body>
	Test_SendMail<br>
	<form name="Test_SendMail_form" action="/check_Accout/PMController/getresetpwdverifycode" method ="post">
		用户名:<input type="text" name="username" value="z1234"><br>
	             验证方式:<input type="text" name="verify_way" value="mail"><br>
		<input type="submit" name="test" value="获取验证码">
	</form>
	
	<form name="Test_SendMail_form" action="/check_Accout/PMController/forgetandsendmail" method ="post">
		用户名:<input type="text" name="username" value="z1234"><br>
	             验证码:<input type="text" name="verify_code" value=""><br>
		<input type="submit" name="test" value="下一步">
	</form>
</body>
</html>