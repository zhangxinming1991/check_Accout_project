package controller;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sound.midi.MidiDevice.Info;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mysql.fabric.Response;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;
import com.sun.org.apache.xml.internal.security.utils.SignerOutputStream;
import com.sun.xml.internal.ws.policy.EffectiveAlternativeSelector;

import check_Asys.CheckAcManage;
import check_Asys.OpLog_Service;
import check_Asys.Person_Manage;
import check_Asys.Person_Manage.Login_Mange;
import check_Asys.Person_Manage.Register_Manage;
import check_Asys.Person_Manage.Weixin_Managr;
import check_Asys.Person_Manage.DB_Operator;
import dao.Agent_Dao;
import dao.Assistance_Dao;
import dao.ConnectPerson_Dao;
import en_de_code.ED_Code;
import encrypt_decrpt.AES;
import entity.Agent;
import entity.Assistance;
import entity.Backup;
import entity.ConnectPerson;
import entity.OpLog;
import entity.OriOrder;
import entity.WeixinBindConnectPerson;
import file_op.AnyFile_Op;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import random_create.RandomCreate;
import entity.Assistance;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

/**
 * PMController 人员管理接口类 ，提供的接口包括用户注册，控制用户权限,操作日志查看，数据库备份
 * @author zhangxinming
 * @modify LinLi
 * @version 1.1.0
 */
@Controller
public class PMController {
	
	private static Logger logger = LogManager.getLogger(PMController.class);
	private static Logger logger_error = LogManager.getLogger("error");
	public final static SessionFactory wFactory = new Configuration().configure().buildSessionFactory();
	public  final static Person_Manage pManage = new Person_Manage(wFactory);
	private static OpLog_Service oLog_Service = new OpLog_Service(wFactory);
	private static AES ase = new AES();
	
	
	/**
	 * ModifyClientMes 客户修改个人信息
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/modifyAssistanceMes")
	public void ModifyAssistanceMes(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get modifyClientMes request***");
		
		JSONObject re_jsonobject = new JSONObject();
		
    	String workId = null;
    	String name = null;
    	String phone = null;
    	String email = null;
    	String agentid = null;
    	String agentname = null;
        try {
			String request_s = IOUtils.toString(request.getInputStream());
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("receive" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			workId = jstr.getString("username");//获取登录id
			name = jstr.getString("name");//获取登录密码
			phone = jstr.getString("phone");
			email = jstr.getString("email");
		//	agentid = jstr.getString("agentid");
			agentname = jstr.getString("agentname");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger_error.error("获取提交参数失败" + e);
			e.printStackTrace();
			
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "获取提交参数失败");
			Common_return_en(response,re_jsonobject);
			return;
		}
        
        Assistance mf_assis = new Assistance();
        mf_assis.setWorkId(workId);
        mf_assis.setName(name);
        mf_assis.setPhone(phone);
        mf_assis.setEmail(email);
        
        re_jsonobject = pManage.ModifyAssistanceMes(mf_assis);
        if (re_jsonobject.getInt("flag") == 0) {
			re_jsonobject.element("flag", 0);
			re_jsonobject.element("errmsg", "修改个人信息成功");
			Common_return_en(response,re_jsonobject);
			oLog_Service.AddLog(ChangeUsertypeToChinses(re_jsonobject.getString("usertype")), workId, OpLog_Service.MODIFY_MES, OpLog_Service.result_success);
			return;
		}
        else if (re_jsonobject.getInt("flag") == -1) {
			oLog_Service.AddLog(ChangeUsertypeToChinses(re_jsonobject.getString("usertype")), workId, OpLog_Service.MODIFY_MES, OpLog_Service.result_success);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "修改个人信息失败");
			Common_return_en(response,re_jsonobject);
			return;
		}
        else {
			oLog_Service.AddLog(ChangeUsertypeToChinses(re_jsonobject.getString("usertype")), workId, OpLog_Service.MODIFY_MES, OpLog_Service.result_success);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "用户名不存在");
			Common_return_en(response,re_jsonobject);
			return;
		}
	}
	
	/**
	 * 转换用户类型为对应中文
	 * @param usertype
	 * @return
	 */
	public String ChangeUsertypeToChinses(String usertype){
		if (usertype.equals("bm")) {
			return "超级管理员";
		}
		else if (usertype.equals("bu")) {
			return "代理商财务";
		}
		else if (usertype.equals("ba")) {
			return "代理商管理员";
		}
		else {
			return "未知用户类型";
		}
	}
	
	/**
	 * GetResetPwdVerifyCode 获取重设密码的验证码
	 */
	@RequestMapping(value="/getresetpwdverifycode")
	public void  GetResetPwdVerifyCode(HttpServletRequest request,HttpServletResponse response) {
		logger.info("***Get getresetpwdverifycode request***");
		
    	String username = null;
    	String verify_way = null;
    	
     /* 	username = request.getParameter("username");
    	verify_way = request.getParameter("verify_way");*/
    	JSONObject re_jsonobject = new JSONObject();
    	
      try {
			String request_s = IOUtils.toString(request.getInputStream());
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("receive" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			username = jstr.getString("username");//获取登录id
			verify_way = jstr.getString("verify_way");//获取登录密码
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger_error.error("获取提交参数失败" + e);
			e.printStackTrace();
			
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "获取提交参数失败");
			Common_return_en(response,re_jsonobject);
		}
        
		//判断用户是否存在
		Assistance fAssistance = pManage.aS_Dao.findById(Assistance.class, username);
		if (fAssistance == null) {
			logger_error.error("用户名不存在");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "用户名不存在");
			Common_return_en(response,re_jsonobject);
		}
		
		//判断获取验证码的方式
		if (verify_way.equals("mobile")) {
			logger.warn("暂时不支持手机获取验证码的方式");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "暂时不支持手机获取验证码的方式");
			Common_return_en(response,re_jsonobject);
		}
		
		//生成验证码及时间点
		String verify_code = RandomCreate.createRandomString(6);
		Double timepoint = Double.parseDouble(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		
		  String to_test = "851592974@qq.com";
		  String to_test_1 = fAssistance.getEmail();
		  logger.info("***" + to_test_1.length());
		ED_Code.printHexString(to_test.getBytes());
		ED_Code.printHexString(fAssistance.getEmail().getBytes());
	      String from = "simon_nudt@sina.com";

	      // Assuming you are sending email from localhost
	      String host = "smtp.sina.com";

	      // Get system properties
	      Properties properties = System.getProperties();

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", host);
	      properties.put("mail.smtp.auth", "true");
	      properties.put("mail.debug", "true");
	      properties.put("mail.store.protocol", "pop3");
	      properties.put("mail.transport.protocol", "smtp");
	      properties.put("mail.user", "simon_nudt@sina.com");
	        // 访问SMTP服务时需要提供的密码
	      properties.put("mail.password", "ABCD1234");
	      
	        // 构建授权信息，用于进行SMTP进行身份验证
	        Authenticator authenticator = new Authenticator() {
	            @Override
	            protected PasswordAuthentication getPasswordAuthentication() {
	                // 用户名、密码
	                String userName = "simon_nudt@sina.com";
	                String password = "ABCD1234";
	                return new PasswordAuthentication(userName, password);
	            }
	        };
	      // Get the default Session object.
	      Session session = Session.getDefaultInstance(properties, authenticator);

	      try {
	    	  // Create a default MimeMessage object.
	          MimeMessage message = new MimeMessage(session);

	          // Set From: header field of the header.
	          message.setFrom(new InternetAddress(from));

	          // Set To: header field of the header.
	          message.addRecipient(Message.RecipientType.TO, new InternetAddress(to_test_1));

	          // Set Subject: header field
	          message.setSubject("重置密码所需的验证码");
	          
	          // Send the actual HTML message, as big as you like
	          String show = "<h1>重置密码所需的验证码如下:<h1></br>";
	          show = "<h1>" + verify_code + "<h1></br>";
	          message.setContent(show, " text/html;charset=UTF-8");
	          
	          // Send message
	          Transport.send(message);
	          logger.info("Sent message successfully....");
	  		fAssistance.setVerifyCode(verify_code);
			fAssistance.setTimepoint(timepoint);
			pManage.aS_Dao.update(fAssistance);
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	         logger_error.error(mex);
	      }
	      
			re_jsonobject.element("flag", 0);
			re_jsonobject.element("errmsg", "验证码已经发到邮箱，邮件有可能被当成垃圾邮件，请注意!");
			Common_return_en(response,re_jsonobject);
	}
	
	/**
	 * ResetPwd 重设密码
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/resetpwd")
	public void ResetPwd(HttpServletRequest request,HttpServletResponse response) {
		logger.info("***Get ResetPwd request***");
		try {
			request.setCharacterEncoding("utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String username = request.getParameter("username");
		String resetid = request.getParameter("resetid");
		
		Assistance fAssistance = pManage.aS_Dao.findById(Assistance.class, username);
		if (fAssistance == null) {
			logger_error.error("用户不存在");
			try {
				response.getWriter().write("重置失败");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		if (!fAssistance.getResetId().equals(resetid)) {
			logger_error.error("重置失败,resetid无效");
			try {
				response.getWriter().write("重置失败");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		String pwd = request.getParameter("pwd");
		String pwd_again = request.getParameter("pwd_again");
		logger.info(username + ":" + pwd);
		if (!pwd.equals(pwd_again)) {
			logger_error.error("密码不一致");
			return;
		}
		
		fAssistance.setPassword(pwd);
		fAssistance.setResetId("****");
		pManage.aS_Dao.update(fAssistance);
		
		try {
			response.sendRedirect("http://119.29.235.201:8080/check_Accout");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * GetWeiXinMes
	 */
	@RequestMapping(value="/getweixinmes")
	public void GetWeiXinMes(HttpServletRequest request,HttpServletResponse response) {
		logger.info("***Get getweixinmes request***");
		JSONObject re_json = new JSONObject();
		Weixin_Managr wManagr = pManage.new Weixin_Managr();
		String action = null;
		action = request.getParameter("action");
		logger.info("action：" + action);
		
		if (action.equals("query")) {
			String weixinid = null;
			try {
				weixinid = AES.aesDecrypt(request.getParameter("weixinid"),AES.key);
				logger.info(weixinid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger_error.error("获取微信参数失败:" + e);
				re_json.element("flag", -1);
				re_json.element("errmsg", "获取微信参数失败:" + e);
				response.addHeader("Access-Control-Allow-Origin", "*");	
				Common_return(response,re_json);
				return;
			}
			
/*			List<WeixinBindConnectPerson> fwxbc = pManage.weixinbc_Dao.FindBySpeElement_S("weixinid", weixinid);
			if (fwxbc.size() == 0) {
				logger.warn("微信无效，没有找到相应的微信绑定对账联系人信息");
				re_json.element("flag", -1);
				re_json.element("errmsg", "微信无效，没有找到相应的微信绑定对账联系人信息");
				Common_return(response,re_json);
			}
			else {
				String cp_username = fwxbc.get(0).getUsername();
				ConnectPerson fPerson = pManage.cDao.findById(ConnectPerson.class, cp_username);
				if (fPerson == null) {
					logger.error("用户名无效，找不到相应对账联系人");
					re_json.element("flag", -1);
					re_json.element("errmsg", "用户名无效，没有找到相应的对账联系人");
					Common_return(response,re_json);
				}
				else {
					re_json.element("flag", 0);
					re_json.element("errmsg", "找到对应的对账联系人");
					OneKeyData_return_enData(response, re_json, "connectp", fPerson);
				}
			}*/
			
			re_json = wManagr.Is_WeixinBind(weixinid);
			response.addHeader("Access-Control-Allow-Origin", "*");	
			Common_return(response, re_json);		
		}
		else if (action.equals("delete")) {
			String username = null;
			try {
				username = AES.aesDecrypt(request.getParameter("username"),AES.key);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger_error.error("获取参数失败" + e);
				
				re_json.element("flag", -1);
				re_json.element("errmsg", "获取参数失败" + e);
				response.addHeader("Access-Control-Allow-Origin", "*");	
				Common_return(response,re_json);
				return;
			}
		
		/*	WeixinBindConnectPerson fwxbc = pManage.weixinbc_Dao.findById(WeixinBindConnectPerson.class, username);
			if (fwxbc == null) {
				logger_error.error("无法定位微信绑定对账联系人信息");
				re_json.element("flag", -1);
				re_json.element("errmsg", "无法定位微信绑定对账联系人信息");
				Common_return(response,re_json);
			}
			else {
				if(pManage.weixinbc_Dao.delete(fwxbc)){
					re_json.element("flag", 0);
					re_json.element("errmsg", "已删除相应的对账联系人信息");
					Common_return(response,re_json);					
				}
				else {
					re_json.element("flag", -1);
					re_json.element("errmsg", "删除失败");
					Common_return(response,re_json);	
				}
			}*/
			
			re_json = wManagr.Delet_ConnectpWeixin(username);
			response.addHeader("Access-Control-Allow-Origin", "*");	
			Common_return(response, re_json);
		}
		else if (action.equals("insert")) {
			String weixinid = null;
			String username = null;
			
			try {
				username = AES.aesDecrypt(request.getParameter("username"),AES.key);
				weixinid = AES.aesDecrypt(request.getParameter("weixinid"),AES.key);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger_error.error("获取参数失败" + e);
				re_json.element("flag", -1);
				re_json.element("errmsg", "已删除相应的对账联系人信息");
				response.addHeader("Access-Control-Allow-Origin", "*");	
				Common_return(response,re_json);	
				return;
			}
			
	/*		WeixinBindConnectPerson fwxbc = pManage.weixinbc_Dao.findById(WeixinBindConnectPerson.class, username);
			if (fwxbc == null) {
				logger.info("微信绑定用户信息不存在");
				WeixinBindConnectPerson in_wxc = new WeixinBindConnectPerson();
				in_wxc.setUsername(username);
				in_wxc.setWeixinid(weixinid);
				if(pManage.weixinbc_Dao.add(in_wxc)){
					re_json.element("flag", 0);
					re_json.element("errmsg", "添加微信绑定对账联系人记录成功");
					Common_return(response,re_json);	
				}
				else {
					re_json.element("flag", -1);
					re_json.element("errmsg", "添加微信绑定对账联系人记录失败");
					Common_return(response,re_json);	
				}
			}
			else {
				fwxbc.setUsername(username);
				fwxbc.setWeixinid(weixinid);
				if(pManage.weixinbc_Dao.update(fwxbc)){
					re_json.element("flag", 0);
					re_json.element("errmsg", "更新微信绑定对账联系人成功");
					Common_return(response,re_json);					
				}
				else {
					re_json.element("flag", -1);
					re_json.element("errmsg", "更新微信绑定对账联系人失败");
					Common_return(response,re_json);	
				}
			}*/
			re_json = wManagr.InOrUdWeixinMes(username,weixinid);
			response.addHeader("Access-Control-Allow-Origin", "*");	
			Common_return(response, re_json);
		}
		else {
			logger_error.error("未知action:【" + action + "】");
			
		}
		
	}
	
	/**
	 * ResetPwdLink 接受重设密码的请求，并且转发请求到重设密码页面
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/resetpwdlink")
	public void ResetPwdLink(HttpServletRequest request,HttpServletResponse response) {
		logger.info("***Get ResetPwdLink request***");
		try {
			request.setCharacterEncoding("utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String resetid = request.getParameter("resetid");
		String username = request.getParameter("username");
		logger.info(username + ":" + resetid);
		
		//判断重置id是否有效
		Assistance fAssistance = pManage.aS_Dao.findById(Assistance.class, username);
		if (fAssistance == null) {
			logger_error.error("用户名不存在");
			try {
				response.setCharacterEncoding("utf-8");
				response.getWriter().write("用户名存在");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		if (!fAssistance.getResetId().equals(resetid)) {
			logger_error.error("验证出错，无法重置密码");
			try {
				response.setCharacterEncoding("utf-8");
				response.getWriter().write("验证出错，无法重置密码");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		Double curtime = Double.parseDouble(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		if(curtime - fAssistance.getTimepoint() > 60){
			logger_error.error("验证链接已经过期");
			try {
				response.setCharacterEncoding("utf-8");
				response.getWriter().write("验证链接已经过期");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		try {
			request.setAttribute("username", username);
			request.setAttribute("resetid", resetid);
			request.getRequestDispatcher("/Test_ResetPwd.jsp").forward(request,response);
			//response.sendRedirect("/check_Accout/Test_ResetPwd.jsp");
		} catch (IOException | ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * ForgetAndResetPwd 忘记密码
	 */
	@RequestMapping(value="/forgetandsendmail")
	public void ForgetandSendmail(HttpServletRequest request,HttpServletResponse response){
	/*	HttpSession user_session = request.getSession(false);
		if (user_session == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "登录超时，请重新登录");
			Common_return_en(response,jsonObject);
			return;
		}*/
		logger.info("***Get forgetandsendmail request***");
		
		JSONObject re_jsonobject = new JSONObject();  	
		
    	String username = null;
    	String verify_code = null;
    /*  	username = request.getParameter("username");
    	verify_code = request.getParameter("verify_code");*/

      try {
			String request_s = IOUtils.toString(request.getInputStream());
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("receive" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			username = jstr.getString("username");//获取登录id
			verify_code = jstr.getString("verify_code");//获取登录密码
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger_error.error("获取提交参数失败" + e);
			e.printStackTrace();
			
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "获取提交参数失败");
			Common_return_en(response,re_jsonobject);
			return;
		}

		 // Recipient's email ID needs to be mentioned.
        Assistance fAssistance = pManage.aS_Dao.findById(Assistance.class, username);
        if (fAssistance == null) {
        	logger_error.error("用户不存在");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "用户不存在");
			Common_return_en(response,re_jsonobject);
			return;
		}
        
        if (!fAssistance.getVerifyCode().equals(verify_code)) {
        	logger_error.error("验证码错误");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "验证码错误");
			Common_return_en(response,re_jsonobject);
			return;
		}
        
		Double curtime = Double.parseDouble(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		if((curtime - fAssistance.getTimepoint()) > 60*3){
			logger_error.error("验证链接已经过期");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "获取提交参数失败");
			Common_return_en(response,re_jsonobject);
			return;
		}
        
	      String to = fAssistance.getEmail();

	      // Sender's email ID needs to be mentioned
	      String from = "simon_nudt@sina.com";

	      // Assuming you are sending email from localhost
	      String host = "smtp.sina.com";

	      // Get system properties
	      Properties properties = System.getProperties();

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", host);
	      properties.put("mail.smtp.auth", "true");
	      properties.put("mail.debug", "true");
	      properties.put("mail.store.protocol", "pop3");
	      properties.put("mail.transport.protocol", "smtp");
	      properties.put("mail.user", "simon_nudt@sina.com");
	        // 访问SMTP服务时需要提供的密码
	      properties.put("mail.password", "ABCD1234");
	      
	        // 构建授权信息，用于进行SMTP进行身份验证
	        Authenticator authenticator = new Authenticator() {
	            @Override
	            protected PasswordAuthentication getPasswordAuthentication() {
	                // 用户名、密码
	                String userName = "simon_nudt@sina.com";
	                String password = "ABCD1234";
	                return new PasswordAuthentication(userName, password);
	            }
	        };
	      // Get the default Session object.
	      Session session = Session.getDefaultInstance(properties, authenticator);

	      try {
	    	  // Create a default MimeMessage object.
	          MimeMessage message = new MimeMessage(session);

	          // Set From: header field of the header.
	          message.setFrom(new InternetAddress(from));

	          // Set To: header field of the header.
	          message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

	          // Set Subject: header field
	          message.setSubject("密码重设提示");
	          
	          // Send the actual HTML message, as big as you like
	          String show = "<h1>请点击下面的链接进行密码重设<h1></br>";
	          String resetid = RandomCreate.createRandomString(10);
	   //       String link_mes = "http://119.29.235.201:8080/check_Accout/PMController/resetpwdlink?" + "username=" + username + "&resetid=" + RandomCreate.createRandomString(10);
	          String link_mes = "http://119.29.235.201:8080/check_Accout/PMController/resetpwdlink?" + "username=" + username + "&resetid=" + resetid;
	          //String link_mes = "http://192.168.137.1:8080/check_Accout/PMController/resetpwdlink?" + "username=" + username + "&resetid=" + resetid;
	          String link = "<a href=\"" + link_mes;
	          link = link + "\">" + link_mes + "</a></br>";
	          show = show + link;
	          message.setContent(show, " text/html;charset=UTF-8");
	          
	          // Send message
	          Transport.send(message);
	          logger.info("Sent message successfully....");
	          
				re_jsonobject.element("flag", 0);
				re_jsonobject.element("errmsg", "验证码已经发到邮箱，邮件有可能被当成垃圾邮件，请注意!");
				fAssistance.setVerifyCode("****");
				fAssistance.setTimepoint(curtime);
				fAssistance.setResetId(resetid);
				pManage.aS_Dao.update(fAssistance);
				
				Common_return_en(response,re_jsonobject);
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	}
		
	/**
	 * Signout 注销登录
	 * @category 所有用户接口
	 * @param request
	 * @param response
	 * @author zhangxinming
	 */
	@RequestMapping(value="/signout")
	public void Signout(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get Signout request***");
		logger.info("hello");
		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "登录超时");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		String usertype = (String) session.getAttribute("usertype");
		String username  = (String) session.getAttribute("workId");
		if (usertype.equals("bu")) {//对账联系人
			oLog_Service.AddLog(OpLog_Service.utype_as, username, OpLog_Service.SIGNOUT, OpLog_Service.result_success);
			re_jsonobject.element("flag", 0);
			re_jsonobject.element("errmsg", "注销成功");
			
		}
		else if (usertype.equals("bm")) {//管理人员
			oLog_Service.AddLog(OpLog_Service.utype_ma, username, OpLog_Service.SIGNOUT, OpLog_Service.result_success);
			re_jsonobject.element("flag", 0);
			re_jsonobject.element("errmsg", "注销成功");
			
		}
		else{
			logger_error.error("未知用户类型");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "注销失败，未知用户类型");
		}
		Common_return_en(response,re_jsonobject);
	//	Signout_return(response);

	}
	

	/**
	 * Asssistance_login 代理商财务登录
	 * @category 代理商财务接口
	 * @param as
	 * @param model
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @author zhangxinming
	 */
    @RequestMapping(value="/login")
    public void Asssistance_login(Assistance as , Model model,HttpServletRequest request,HttpServletResponse response) throws ServletException{
    	logger.info("***Get Asssistance_login request***");   	
    	String work_id = null;
    	String password = null;
    	//String lgtype = null;
    	int newpay_num = 0;
    	
    	
        try {
			String request_s = IOUtils.toString(request.getInputStream());
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("receive" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			work_id = jstr.getString("uid");//获取登录id
			password = jstr.getString("upwd");//获取登录密码
			//lgtype = jstr.getString("from"); 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger_error.error("获取提交参数失败" + e);
			e.printStackTrace();
			
			JSONObject re_jsonobject = new JSONObject();
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "获取提交参数失败");
			Common_return_en(response,re_jsonobject);
		}
        
        Login_Mange login_Mange = pManage.new Login_Mange();
        
        JSONObject jsonObject = login_Mange.LgEnter_Select(work_id, password);
        int isillegal = jsonObject.getInt("flag");
        String role = jsonObject.getString("role");
        
        if (isillegal == 0) {
        	String agentid = (String) pManage.aS_Dao.findById(Assistance.class, work_id).getAgentid();
        	HttpSession session = request.getSession();//创建session
    		System.out.println("login success");
			session.setAttribute("usertype", role);
    		session.setAttribute("workId", work_id);
    		session.setAttribute("agentid", agentid);
    		session.setMaxInactiveInterval(0);
    		
    		newpay_num = pManage.pCDao.GetPayRecordsTb(agentid).size();
    		
    		Asssistance_login_return(0,work_id,password,role,newpay_num,response);
    		if (role.equals("bu")) {
    			oLog_Service.AddLog(OpLog_Service.utype_as, work_id, OpLog_Service.Log, OpLog_Service.result_success);
			}
    		else if (role.equals("bm")) {
    			oLog_Service.AddLog(OpLog_Service.utype_ma, work_id, OpLog_Service.Log, OpLog_Service.result_success);
			}
    		else if(role.equals("ba")){
    			oLog_Service.AddLog(OpLog_Service.utype_am, work_id, OpLog_Service.Log, OpLog_Service.result_success);
    		}
    		else{
    			oLog_Service.AddLog(OpLog_Service.utype_un, work_id, OpLog_Service.Log, OpLog_Service.result_success);
			}
    		
		}
        else{
        	Asssistance_login_return(isillegal,work_id,password,role,newpay_num,response);
    		if (role.equals("bu")) {
    			oLog_Service.AddLog(OpLog_Service.utype_as, work_id, OpLog_Service.Log, OpLog_Service.result_failed);
			}
    		else if (role.equals("bm")) {
    			oLog_Service.AddLog(OpLog_Service.utype_ma, work_id, OpLog_Service.Log, OpLog_Service.result_failed);
			}
    		else if(role.equals("ba")){
    			oLog_Service.AddLog(OpLog_Service.utype_am, work_id, OpLog_Service.Log, OpLog_Service.result_failed);
    		}
    		else{
    			oLog_Service.AddLog(OpLog_Service.utype_un, work_id, OpLog_Service.Log, OpLog_Service.result_failed);
			}
        }  	
    }
    
    
    /**
     * Assistance_register 财务人员注册
     * @category 代理商财务人员注册接口
     * @param request
     * @param response
     * @author zhangxinming
     * @throws Exception 
     */
    @RequestMapping(value="/as_register")
    public void Assistance_register(HttpServletRequest request,HttpServletResponse response) throws Exception{
    	logger.info("Get as_register request");
    	JSONObject re_jsonobject = new JSONObject();
    	
		String request_s = null;
		String request_s_en = null;
		
		//JSONObject jstr = JSONObject.fromObject(request_s_en);
    	String work_id = null;//用户
    	String name = null;//真实姓名
    	String phone = null;//电话号码
    	String email = null;
    	String password = null;
    	String usertype = null;
    	String agentid = null;
    	
		try {
				request_s = IOUtils.toString(request.getInputStream());
			/*	InputStream inputStream = request.getInputStream();
				byte[] readbuf = new byte[200];
				inputStream.read(readbuf);
				request_s = new String(readbuf, "utf-8");
				request_s = readbuf.toString();*/
				request_s_en = AES.aesDecrypt(request_s, AES.key);
				logger.info("receive:" + request_s_en);
				
				JSONObject jstr = JSONObject.fromObject(request_s_en);
		    	work_id = jstr.getString("username");//用户
		    	name = jstr.getString("name");//真实姓名
		    	phone = jstr.getString("phone");//电话号码
		    	email = jstr.getString("email");
		    	password = jstr.getString("password");
		    	usertype = jstr.getString("role");
		    	agentid = jstr.getString("agentid");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger_error.error("获取提交参数失败" + e);			
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "获取提交参数失败" + e);
			Common_return_en(response,re_jsonobject);
			return;
		}
    	
    	Assistance assistance = new Assistance();
    	assistance.setWorkId(work_id);
    	assistance.setName(name);
    	assistance.setPhone(phone);
    	assistance.setEmail(email);
    	assistance.setPassword(password);
    	// 前端直接传用户身份对应的编码：代理商财务=bu 代理商管理员=bam
    	assistance.setUsertype(usertype);
    	assistance.setAgentid(agentid);
  
    	Register_Manage regmanager = pManage.new Register_Manage();
    	re_jsonobject = regmanager.RgEnter_Select(assistance, "as");
    	
    	//Assistance_Register_return(response,flag);
    	Common_return_en(response, re_jsonobject);
    }
      
    /**
     * Conectp_register 对账联系人注册  客户注册接口
     * @param request
     * @param response
     * @author zhangxinming
     * @throws Exception 
     */
    @RequestMapping(value="/conectp_register")
    public void Conectp_register(HttpServletRequest request,HttpServletResponse response) throws Exception{
    	logger.info("***Get Conectp_register request***");
    	try {
			request.setCharacterEncoding("utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	String username = null;
    	String phone = null;
    	String company = null;
    	String agent = null;
    	String real_name = null;
    	String register_way = null;
    	String companyid = null;
    	String weixin = null;
    	String password = null;
    	String email = null;
    	String contractMes = null;
    	String cardid = null;
    	String weixinid = null;
    	
		try {
			username = AES.aesDecrypt(request.getParameter("username"),AES.key);
	    	phone =  AES.aesDecrypt(request.getParameter("phone"),AES.key);//电话
	    	company =  new String(AES.aesDecrypt(request.getParameter("company"),AES.key).getBytes("GBK"),"GBK");//公司名称
	    	agent = AES.aesDecrypt(request.getParameter("agent"),AES.key);//所属代理商id
	    	real_name = new String(AES.aesDecrypt(request.getParameter("real_name"),AES.key).getBytes("GBK"),"GBK");//真实姓名
	    	register_way = AES.aesDecrypt(request.getParameter("register_way"),AES.key);//注册方式
	    	weixin = AES.aesDecrypt(request.getParameter("weixin"),AES.key);//微信号
	    	companyid = AES.aesDecrypt(request.getParameter("companyid"),AES.key);//公司id
	    	password = AES.aesDecrypt(request.getParameter("password"),AES.key);//密码
	    	email = AES.aesDecrypt(request.getParameter("email"),AES.key);//邮箱
	    	contractMes = AES.aesDecrypt(request.getParameter("contract_mes"),AES.key);//有效凭证
	    	cardid = AES.aesDecrypt(request.getParameter("cardid"),AES.key);//对账联系人身份证
	    	weixinid = AES.aesDecrypt(request.getParameter("weixinid"),AES.key);//
	    	logger.info("weixinid:" + weixinid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//用户名

    	
    	ConnectPerson inConnectPerson = new ConnectPerson();
    	inConnectPerson.setUsername(username);
    	inConnectPerson.setPassword(password);
    	inConnectPerson.setCompany(company);
    	inConnectPerson.setRealName(real_name);
    	inConnectPerson.setWeixin(weixin);
    	inConnectPerson.setAgent(agent);
    	inConnectPerson.setPhone(phone);
    	inConnectPerson.setCompanyid(companyid);
    	inConnectPerson.setRegisterWay(register_way);
    	inConnectPerson.setFlag(Person_Manage.REG_NEW);//设置注册状态为等待审阅
    	inConnectPerson.setEmail(email);
    	inConnectPerson.setContractMes(contractMes);
    	inConnectPerson.setCardid(cardid);
    	inConnectPerson.setWeixinid(weixinid);
    	inConnectPerson.setScore(0);
    	
    	Register_Manage register_Manage = pManage.new Register_Manage();
    	JSONObject flag = register_Manage.RgEnter_Select(inConnectPerson, "cp");
    	
		response.setCharacterEncoding("utf-8");
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	Common_return(response, flag);
    }
    
    /**
     * Verify_Register 系统管理员对注册请求进行确认  系统管理员接口
     * @param request
     * @param response
     * @author zhangxinming
     */
    @RequestMapping(value="/verify_register")
    public void Verify_Register(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get verify_register request***");
		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "登录超时");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		String request_s = null;
		String request_s_en = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_en = AES.aesDecrypt(request_s, AES.key);
				logger.info("request content:" + request_s_en);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject jstr = JSONObject.fromObject(request_s_en);
		
		String reg_type = jstr.getString("reg_type");
    	String username = null;
    	if (reg_type.equals("as")) {
    	//	username = request.getParameter("workId");
    		username = jstr.getString("id");
		}
    	else{//对账联系人
    		    		//username = request.getParameter("username");
    		username = jstr.getString("id");
    	}

    	int flag = jstr.getInt("regflag");
    	logger.info(flag);
    	Register_Manage register_Manage = pManage.new Register_Manage();
    	JSONObject jsonObject = register_Manage.Verify_RgRequest(reg_type,username, flag);
    	
    	//Verify_Register_return(response,jsonObject);
    	re_jsonobject = jsonObject;
    	Common_return_en(response, re_jsonobject);
    }

    /**
     * Control_Power 系统管理员对用户权限进行控制 
     * @author 系统管理员接口 
     * @param request
     * @param response
     * @author zhangxinming
     */
    @RequestMapping(value="control_power")
    public void Control_Power(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get control_power request***");
		
		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "登录超时");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		String request_s = null;
		String request_s_en = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_en = AES.aesDecrypt(request_s, AES.key);
				logger.info("request content:" + request_s_en);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject jstr = JSONObject.fromObject(request_s_en);
		String control_type = jstr.getString("control_type");//控制类型，as:代理商财务  cp:对账联系人
		int ctlflag = jstr.getInt("ctlflag"); //控制的结果  0：正常  -3：锁定 
		String id = jstr.getString("id");//控制对象的id
		
		boolean existAS = false;
		if(ctlflag == 0)
			existAS = pManage.checkExistAS(id);
		if(existAS == true){
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "该代理商已存在可用财务，不能解锁");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		pManage.Control_Power(control_type,ctlflag,id);//调用权限控制处理
		
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "控制权限成功");
		Common_return_en(response,re_jsonobject);
    }
   
   
    /**
     * Connectp_login 对账联系人登录 
     * @author  客户接口
     * @param request
     * @param response
     * @author zhangxinming
     */   
    @RequestMapping(value="/ConnectPLogin")
    public void Connectp_login(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get ConnectPLogin request***");
    	
    	JSONObject re_jsonobject = new JSONObject();

    	String username = null;
    	String password = null;
		try {
			username = AES.aesDecrypt(request.getParameter("username"),AES.key);
			password = AES.aesDecrypt(request.getParameter("password"),AES.key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Login_Mange login_Mange = pManage.new Login_Mange();
		int isllegal = login_Mange.LgEnter_ConnectP(username, password);
		
		if (isllegal == 0) {
			re_jsonobject.element("status", isllegal);
			re_jsonobject.element("errmsg", "登录成功");
			OneKeyData_return_enData(response, re_jsonobject, "connectp", pManage.cDao.findById(ConnectPerson.class, username));
		}
		else if (isllegal == -1) {//用户名不存在
			logger_error.error("登录失败");
			re_jsonobject.element("status", isllegal);
			re_jsonobject.element("errmsg", "用户名不存在");
			Common_return(response, re_jsonobject);
		}
		else if (isllegal == -3) {
			re_jsonobject.element("status", -1);
			re_jsonobject.element("errmsg", "注册审阅中");
			Common_return(response, re_jsonobject);
		}
		else if (isllegal == -4) {
			re_jsonobject.element("status", -1);
			re_jsonobject.element("errmsg", "用户已经被锁定，请联系管理员");
			Common_return(response, re_jsonobject);
		}
		else {//密码错误
			logger_error.error("密码错误");
			re_jsonobject.element("status", isllegal);
			re_jsonobject.element("errmsg", "密码错误");
			Common_return(response, re_jsonobject);
		}
    }
  
    /**
     * get_agentcodeAname 获取代理商的id及名字 
     * @category  对账联系人注册接口
     * @param request
     * @param response
     * @author zhangxinming
     */
    @RequestMapping(value="get_agentcodeAname")
    public void Get_AgentCAN(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get get_agentcodeAname request***");
    	
    	JSONObject re_jsonobject = new JSONObject();
    	
    	JSONArray re_jarry = pManage.Get_AgentCAN();//进入获取信息处理
    	
    	/*返回数据到前台*/
    	re_jsonobject.element("flag", 0);
    	re_jsonobject.element("errmsg", "获取代理商及id成功");
    	OneKeyData_return_enall(response, re_jsonobject, "data", re_jarry);
    	/*返回数据到前台*/
    }
    
    /**
     * Watch 后台管理系统的查看功能
     * @category 管理员接口
     * @param request
     * @param response
     * @author zhangxinming
     */
    @RequestMapping(value="watch")
    public void Watch(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get watch request***");

		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "登录超时");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		String request_s = null;
		String request_s_de = null;
		String watch_type = null;
		int pagenum = -1;
		String user_type = session.getAttribute("usertype").toString();
		String agent_id = session.getAttribute("agentid").toString();
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_de = AES.aesDecrypt(request_s, AES.key);//解密数据
				logger.info("received content:" + request_s_de);
				JSONObject jstr = JSONObject.fromObject(request_s_de);
				watch_type = jstr.getString("watch_type");//具体查看的类型
				pagenum = jstr.getInt("pagenum");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger_error.error("查看失败" + e);
		}
		
		int pagesize = 10;
		int offset = (pagenum-1)*10;
		
		re_jsonobject = pManage.Watch(watch_type,offset,pagesize, user_type, agent_id);//进入查看处理
    	//int num = pManage.opLog_Dao.GetOpLogTb_Num();
    	/*返回数据到前台*/
    	re_jsonobject.element("flag", 0);
    	re_jsonobject.element("errmsg", "查看成功");
    	//re_jsonobject.element("totalpage", 2);
    	//OneKeyData_return_enall(response, re_jsonobject, "data", re_list);
    	Common_return_en(response, re_jsonobject);
    	/*返回数据到前台*/
    }
    
    /**
     * BackUpDatabase 备份数据库
     * @param request
     * @param response
     */
    @RequestMapping(value="backupdb")
    public void BackUpDatabase(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get BackUpDatabase request***");
    	
		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "登录超时");
			Common_return_en(response,re_jsonobject);
			return;
		}
    	
    	DB_Operator dOperator = pManage.new DB_Operator();
    	String savedir = request.getServletContext().getRealPath("/" + DB_Operator.dirname);
    	
    	String filename = dOperator.produce_time() + ".sql";
    	dOperator.BackUp_db(savedir, filename);
    	
    	re_jsonobject.element("flag", 0);
    	re_jsonobject.element("errmsg", "备份成功");
    	Common_return_en(response, re_jsonobject);
    }
    
    /**
     * Verify_Restore 提交恢复sql确定
     * @param request
     * @param response
     */
    @RequestMapping(value="verify_backup")
    public void Verify_Restore(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get Verify_Restore request***");
    	
		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "登录超时");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		int backupid = -1;
		 try {
				String request_s = IOUtils.toString(request.getInputStream());
				String request_s_de = AES.aesDecrypt(request_s, AES.key);
				logger.info("receive" + request_s_de);
				JSONObject jstr = JSONObject.fromObject(request_s_de);
				backupid = jstr.getInt("id");//获取登录id
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
    	DB_Operator dOperator = pManage.new DB_Operator();
    	int flag = dOperator.Restore(backupid);
    	
    	if (flag == -1) {
	    	re_jsonobject.element("flag", -1);
	    	re_jsonobject.element("errmsg", "恢复失败");
		}
    	else {
	    	re_jsonobject.element("flag", 0);
	    	re_jsonobject.element("errmsg", "恢复成功");
		}

    	Common_return_en(response, re_jsonobject);
    }
    
    /**
     * Choose_Restore 选择恢复的sql
     * @param request
     * @param response
     */
    @RequestMapping(value="choose_backup")
    public void Choose_Restore(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get Choose_Restore request***");
    	
		JSONObject re_jsonobject = new JSONObject();
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "登录超时");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		List<Backup> fBackups = pManage.bUp_Dao.GetTolTb();
		if (fBackups != null) {
	    	re_jsonobject.element("flag", 0);
	    	re_jsonobject.element("errmsg", "操作成功");
	    	
	    	OneKeyData_return_enall(response,re_jsonobject,"data",fBackups);
		}
		else{
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "操作失败");
			Common_return_en(response,re_jsonobject);
		}
    }
    
    /**
     * Asssistance_login_return 财务人员登录返回
     * @param flag
     * @param username 用户名
     * @param password 密码
     * @param role 用户类型
     * @param newpay_num 新的付款信息数目
     * @param response
     * @author zhangxinming
     */
    public void Asssistance_login_return(int flag,String username,String password,String role,int newpay_num,HttpServletResponse response){
		response.setCharacterEncoding("utf-8");
		JSONObject userJ =  new JSONObject();//传递参数中的最外层对象
		JSONObject resJ=new JSONObject();
		if (flag == -1) {//用户不存在
				resJ.put("flag", -1);
				resJ.put("errmsg", "用户名或密码错误");
		}
		else if (flag == -2) {
			resJ.put("flag", -1);
			resJ.put("errmsg", "用户名或密码错误");
		}
		else if(flag == -3){
			resJ.put("flag", -3);
			resJ.put("errmsg", "注册审核中");
		}
		else if (flag == -4) {
			resJ.put("flag", -4);
			resJ.put("errmsg", "该账号已被锁定，请联系系统管理员");
		}
		else {
				Assistance fAssistance = pManage.aS_Dao.findById(Assistance.class, username);
				userJ.element("role", role);//将数组添加到对象中
				userJ.element("uid", username);
				userJ.element("upwd", password);
				userJ.element("name", fAssistance.getName());
				userJ.element("phone", fAssistance.getPhone());
				userJ.element("email", fAssistance.getEmail());
				if (role.equals("bu") || role.equals("ba")) {
					String agentid = fAssistance.getAgentid();
					Agent agentInfo = pManage.getAgentById(agentid);
					if(agentInfo != null)
						userJ.element("agentname", agentInfo.getAgentName());
					else 
						userJ.element("agentname","未知代理商");
				}
				
				if (newpay_num > 0) {
					resJ.put("isnewpay", 1);
				}
				else{
					resJ.put("isnewpay", 0);
				}
				
				resJ.put("flag", 0);
				resJ.put("user", userJ);
				resJ.put("token","ok");
				resJ.put("newpay_num", newpay_num);
		}
    	
    	Writer writer;
		try {
			logger.info(resJ.toString());
			writer = response.getWriter();
			String en_s = AES.aesEncrypt(resJ.toString(),AES.key);
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * OneKeyData_return 带一个具体数据的前台返回
     * @param response
     * @param re_json 操作结果及具体信息
     * @param key 具体信息的key
     * @param data 具体信息
     * @category管理员接口
     */
    public void OneKeyData_return_enall(HttpServletResponse response,JSONObject re_json,String key,Object data){
		response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//传递参数中的最外层对象
		re_object.element(key, data);
		
		/*传递json数据给前台*/
		try {
			logger.info("send content:" + re_object.toString());
			Writer writer = response.getWriter();
			String en_s = AES.aesEncrypt(re_object.toString(), AES.key);
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*传递json数据给前台*/
    }
    
    /**
     * OneKeyData_return_enData 带一个具体加密信息的返回，只加密具体信息
     * @param response
     * @param re_json 操作结果及具体信息
     * @param key 具体信息的key
     * @param data 具体信息
     * @客户返回接口
     */
    public void OneKeyData_return_enData(HttpServletResponse response,JSONObject re_json,String key,Object data){
		response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//传递参数中的最外层对象
    	

		JSONObject aes_object = JSONObject.fromObject(data);
		String aes_object_s = aes_object.toString();
		logger.info("Before ：[aesEncrypt],context is:" + aes_object_s);
		String en_s = null;
		try {
			en_s = AES.aesEncrypt(aes_object_s,AES.key);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		re_object.element(key, en_s);
		
		/*传递json数据给前台*/
		try {
			logger.info("send content：" + re_object.toString());
			Writer writer = response.getWriter();
			writer.write(re_object.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*传递json数据给前台*/
    }
    
    /**
     * Common_return 不带具体信息的返回
     * @param response
     * @param re_json 操作结果及信息
     * @author zhangxinming
     */
    public void Common_return(HttpServletResponse response,JSONObject re_json){
		response.setCharacterEncoding("utf-8");
	//	response.addHeader("Access-Control-Allow-Origin", "*");	
    	JSONObject re_object =  re_json;//传递参数中的最外层对象
		
		/*传递json数据给前台*/
		logger.info(re_object.toString());
		try {
			Writer writer = response.getWriter();
			writer.write(re_object.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*传递json数据给前台*/
    }

    /**
     * Common_return_en 不带具体信息的加密返回
     * @param response
     * @param re_json 操作结果及信息
     * @author zhangxinming
     */
    public void Common_return_en(HttpServletResponse response,JSONObject re_json){
		response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//传递参数中的最外层对象
		
    	String en_s = null;
		/*传递json数据给前台*/
		logger.info(re_object.toString());
		try {
			Writer writer = response.getWriter();
			en_s = AES.aesEncrypt(re_object.toString(), AES.key);
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*传递json数据给前台*/
    }
}


