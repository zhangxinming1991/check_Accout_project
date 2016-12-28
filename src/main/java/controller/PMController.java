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
 * PMController ��Ա����ӿ��� ���ṩ�Ľӿڰ����û�ע�ᣬ�����û�Ȩ��,������־�鿴�����ݿⱸ��
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
	 * ModifyClientMes �ͻ��޸ĸ�����Ϣ
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
			workId = jstr.getString("username");//��ȡ��¼id
			name = jstr.getString("name");//��ȡ��¼����
			phone = jstr.getString("phone");
			email = jstr.getString("email");
		//	agentid = jstr.getString("agentid");
			agentname = jstr.getString("agentname");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger_error.error("��ȡ�ύ����ʧ��" + e);
			e.printStackTrace();
			
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��ȡ�ύ����ʧ��");
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
			re_jsonobject.element("errmsg", "�޸ĸ�����Ϣ�ɹ�");
			Common_return_en(response,re_jsonobject);
			oLog_Service.AddLog(ChangeUsertypeToChinses(re_jsonobject.getString("usertype")), workId, OpLog_Service.MODIFY_MES, OpLog_Service.result_success);
			return;
		}
        else if (re_jsonobject.getInt("flag") == -1) {
			oLog_Service.AddLog(ChangeUsertypeToChinses(re_jsonobject.getString("usertype")), workId, OpLog_Service.MODIFY_MES, OpLog_Service.result_success);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "�޸ĸ�����Ϣʧ��");
			Common_return_en(response,re_jsonobject);
			return;
		}
        else {
			oLog_Service.AddLog(ChangeUsertypeToChinses(re_jsonobject.getString("usertype")), workId, OpLog_Service.MODIFY_MES, OpLog_Service.result_success);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "�û���������");
			Common_return_en(response,re_jsonobject);
			return;
		}
	}
	
	/**
	 * ת���û�����Ϊ��Ӧ����
	 * @param usertype
	 * @return
	 */
	public String ChangeUsertypeToChinses(String usertype){
		if (usertype.equals("bm")) {
			return "��������Ա";
		}
		else if (usertype.equals("bu")) {
			return "�����̲���";
		}
		else if (usertype.equals("ba")) {
			return "�����̹���Ա";
		}
		else {
			return "δ֪�û�����";
		}
	}
	
	/**
	 * GetResetPwdVerifyCode ��ȡ�����������֤��
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
			username = jstr.getString("username");//��ȡ��¼id
			verify_way = jstr.getString("verify_way");//��ȡ��¼����
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger_error.error("��ȡ�ύ����ʧ��" + e);
			e.printStackTrace();
			
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��ȡ�ύ����ʧ��");
			Common_return_en(response,re_jsonobject);
		}
        
		//�ж��û��Ƿ����
		Assistance fAssistance = pManage.aS_Dao.findById(Assistance.class, username);
		if (fAssistance == null) {
			logger_error.error("�û���������");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "�û���������");
			Common_return_en(response,re_jsonobject);
		}
		
		//�жϻ�ȡ��֤��ķ�ʽ
		if (verify_way.equals("mobile")) {
			logger.warn("��ʱ��֧���ֻ���ȡ��֤��ķ�ʽ");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��ʱ��֧���ֻ���ȡ��֤��ķ�ʽ");
			Common_return_en(response,re_jsonobject);
		}
		
		//������֤�뼰ʱ���
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
	        // ����SMTP����ʱ��Ҫ�ṩ������
	      properties.put("mail.password", "ABCD1234");
	      
	        // ������Ȩ��Ϣ�����ڽ���SMTP���������֤
	        Authenticator authenticator = new Authenticator() {
	            @Override
	            protected PasswordAuthentication getPasswordAuthentication() {
	                // �û���������
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
	          message.setSubject("���������������֤��");
	          
	          // Send the actual HTML message, as big as you like
	          String show = "<h1>���������������֤������:<h1></br>";
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
			re_jsonobject.element("errmsg", "��֤���Ѿ��������䣬�ʼ��п��ܱ����������ʼ�����ע��!");
			Common_return_en(response,re_jsonobject);
	}
	
	/**
	 * ResetPwd ��������
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
			logger_error.error("�û�������");
			try {
				response.getWriter().write("����ʧ��");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		if (!fAssistance.getResetId().equals(resetid)) {
			logger_error.error("����ʧ��,resetid��Ч");
			try {
				response.getWriter().write("����ʧ��");
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
			logger_error.error("���벻һ��");
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
		logger.info("action��" + action);
		
		if (action.equals("query")) {
			String weixinid = null;
			try {
				weixinid = AES.aesDecrypt(request.getParameter("weixinid"),AES.key);
				logger.info(weixinid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger_error.error("��ȡ΢�Ų���ʧ��:" + e);
				re_json.element("flag", -1);
				re_json.element("errmsg", "��ȡ΢�Ų���ʧ��:" + e);
				response.addHeader("Access-Control-Allow-Origin", "*");	
				Common_return(response,re_json);
				return;
			}
			
/*			List<WeixinBindConnectPerson> fwxbc = pManage.weixinbc_Dao.FindBySpeElement_S("weixinid", weixinid);
			if (fwxbc.size() == 0) {
				logger.warn("΢����Ч��û���ҵ���Ӧ��΢�Ű󶨶�����ϵ����Ϣ");
				re_json.element("flag", -1);
				re_json.element("errmsg", "΢����Ч��û���ҵ���Ӧ��΢�Ű󶨶�����ϵ����Ϣ");
				Common_return(response,re_json);
			}
			else {
				String cp_username = fwxbc.get(0).getUsername();
				ConnectPerson fPerson = pManage.cDao.findById(ConnectPerson.class, cp_username);
				if (fPerson == null) {
					logger.error("�û�����Ч���Ҳ�����Ӧ������ϵ��");
					re_json.element("flag", -1);
					re_json.element("errmsg", "�û�����Ч��û���ҵ���Ӧ�Ķ�����ϵ��");
					Common_return(response,re_json);
				}
				else {
					re_json.element("flag", 0);
					re_json.element("errmsg", "�ҵ���Ӧ�Ķ�����ϵ��");
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
				logger_error.error("��ȡ����ʧ��" + e);
				
				re_json.element("flag", -1);
				re_json.element("errmsg", "��ȡ����ʧ��" + e);
				response.addHeader("Access-Control-Allow-Origin", "*");	
				Common_return(response,re_json);
				return;
			}
		
		/*	WeixinBindConnectPerson fwxbc = pManage.weixinbc_Dao.findById(WeixinBindConnectPerson.class, username);
			if (fwxbc == null) {
				logger_error.error("�޷���λ΢�Ű󶨶�����ϵ����Ϣ");
				re_json.element("flag", -1);
				re_json.element("errmsg", "�޷���λ΢�Ű󶨶�����ϵ����Ϣ");
				Common_return(response,re_json);
			}
			else {
				if(pManage.weixinbc_Dao.delete(fwxbc)){
					re_json.element("flag", 0);
					re_json.element("errmsg", "��ɾ����Ӧ�Ķ�����ϵ����Ϣ");
					Common_return(response,re_json);					
				}
				else {
					re_json.element("flag", -1);
					re_json.element("errmsg", "ɾ��ʧ��");
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
				logger_error.error("��ȡ����ʧ��" + e);
				re_json.element("flag", -1);
				re_json.element("errmsg", "��ɾ����Ӧ�Ķ�����ϵ����Ϣ");
				response.addHeader("Access-Control-Allow-Origin", "*");	
				Common_return(response,re_json);	
				return;
			}
			
	/*		WeixinBindConnectPerson fwxbc = pManage.weixinbc_Dao.findById(WeixinBindConnectPerson.class, username);
			if (fwxbc == null) {
				logger.info("΢�Ű��û���Ϣ������");
				WeixinBindConnectPerson in_wxc = new WeixinBindConnectPerson();
				in_wxc.setUsername(username);
				in_wxc.setWeixinid(weixinid);
				if(pManage.weixinbc_Dao.add(in_wxc)){
					re_json.element("flag", 0);
					re_json.element("errmsg", "���΢�Ű󶨶�����ϵ�˼�¼�ɹ�");
					Common_return(response,re_json);	
				}
				else {
					re_json.element("flag", -1);
					re_json.element("errmsg", "���΢�Ű󶨶�����ϵ�˼�¼ʧ��");
					Common_return(response,re_json);	
				}
			}
			else {
				fwxbc.setUsername(username);
				fwxbc.setWeixinid(weixinid);
				if(pManage.weixinbc_Dao.update(fwxbc)){
					re_json.element("flag", 0);
					re_json.element("errmsg", "����΢�Ű󶨶�����ϵ�˳ɹ�");
					Common_return(response,re_json);					
				}
				else {
					re_json.element("flag", -1);
					re_json.element("errmsg", "����΢�Ű󶨶�����ϵ��ʧ��");
					Common_return(response,re_json);	
				}
			}*/
			re_json = wManagr.InOrUdWeixinMes(username,weixinid);
			response.addHeader("Access-Control-Allow-Origin", "*");	
			Common_return(response, re_json);
		}
		else {
			logger_error.error("δ֪action:��" + action + "��");
			
		}
		
	}
	
	/**
	 * ResetPwdLink ����������������󣬲���ת��������������ҳ��
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
		
		//�ж�����id�Ƿ���Ч
		Assistance fAssistance = pManage.aS_Dao.findById(Assistance.class, username);
		if (fAssistance == null) {
			logger_error.error("�û���������");
			try {
				response.setCharacterEncoding("utf-8");
				response.getWriter().write("�û�������");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		if (!fAssistance.getResetId().equals(resetid)) {
			logger_error.error("��֤�����޷���������");
			try {
				response.setCharacterEncoding("utf-8");
				response.getWriter().write("��֤�����޷���������");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		Double curtime = Double.parseDouble(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		if(curtime - fAssistance.getTimepoint() > 60){
			logger_error.error("��֤�����Ѿ�����");
			try {
				response.setCharacterEncoding("utf-8");
				response.getWriter().write("��֤�����Ѿ�����");
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
	 * ForgetAndResetPwd ��������
	 */
	@RequestMapping(value="/forgetandsendmail")
	public void ForgetandSendmail(HttpServletRequest request,HttpServletResponse response){
	/*	HttpSession user_session = request.getSession(false);
		if (user_session == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "��¼��ʱ�������µ�¼");
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
			username = jstr.getString("username");//��ȡ��¼id
			verify_code = jstr.getString("verify_code");//��ȡ��¼����
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger_error.error("��ȡ�ύ����ʧ��" + e);
			e.printStackTrace();
			
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��ȡ�ύ����ʧ��");
			Common_return_en(response,re_jsonobject);
			return;
		}

		 // Recipient's email ID needs to be mentioned.
        Assistance fAssistance = pManage.aS_Dao.findById(Assistance.class, username);
        if (fAssistance == null) {
        	logger_error.error("�û�������");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "�û�������");
			Common_return_en(response,re_jsonobject);
			return;
		}
        
        if (!fAssistance.getVerifyCode().equals(verify_code)) {
        	logger_error.error("��֤�����");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��֤�����");
			Common_return_en(response,re_jsonobject);
			return;
		}
        
		Double curtime = Double.parseDouble(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
		if((curtime - fAssistance.getTimepoint()) > 60*3){
			logger_error.error("��֤�����Ѿ�����");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��ȡ�ύ����ʧ��");
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
	        // ����SMTP����ʱ��Ҫ�ṩ������
	      properties.put("mail.password", "ABCD1234");
	      
	        // ������Ȩ��Ϣ�����ڽ���SMTP���������֤
	        Authenticator authenticator = new Authenticator() {
	            @Override
	            protected PasswordAuthentication getPasswordAuthentication() {
	                // �û���������
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
	          message.setSubject("����������ʾ");
	          
	          // Send the actual HTML message, as big as you like
	          String show = "<h1>������������ӽ�����������<h1></br>";
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
				re_jsonobject.element("errmsg", "��֤���Ѿ��������䣬�ʼ��п��ܱ����������ʼ�����ע��!");
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
	 * Signout ע����¼
	 * @category �����û��ӿ�
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
			re_jsonobject.element("errmsg", "��¼��ʱ");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		String usertype = (String) session.getAttribute("usertype");
		String username  = (String) session.getAttribute("workId");
		if (usertype.equals("bu")) {//������ϵ��
			oLog_Service.AddLog(OpLog_Service.utype_as, username, OpLog_Service.SIGNOUT, OpLog_Service.result_success);
			re_jsonobject.element("flag", 0);
			re_jsonobject.element("errmsg", "ע���ɹ�");
			
		}
		else if (usertype.equals("bm")) {//������Ա
			oLog_Service.AddLog(OpLog_Service.utype_ma, username, OpLog_Service.SIGNOUT, OpLog_Service.result_success);
			re_jsonobject.element("flag", 0);
			re_jsonobject.element("errmsg", "ע���ɹ�");
			
		}
		else{
			logger_error.error("δ֪�û�����");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "ע��ʧ�ܣ�δ֪�û�����");
		}
		Common_return_en(response,re_jsonobject);
	//	Signout_return(response);

	}
	

	/**
	 * Asssistance_login �����̲����¼
	 * @category �����̲���ӿ�
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
			work_id = jstr.getString("uid");//��ȡ��¼id
			password = jstr.getString("upwd");//��ȡ��¼����
			//lgtype = jstr.getString("from"); 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger_error.error("��ȡ�ύ����ʧ��" + e);
			e.printStackTrace();
			
			JSONObject re_jsonobject = new JSONObject();
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��ȡ�ύ����ʧ��");
			Common_return_en(response,re_jsonobject);
		}
        
        Login_Mange login_Mange = pManage.new Login_Mange();
        
        JSONObject jsonObject = login_Mange.LgEnter_Select(work_id, password);
        int isillegal = jsonObject.getInt("flag");
        String role = jsonObject.getString("role");
        
        if (isillegal == 0) {
        	String agentid = (String) pManage.aS_Dao.findById(Assistance.class, work_id).getAgentid();
        	HttpSession session = request.getSession();//����session
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
     * Assistance_register ������Աע��
     * @category �����̲�����Աע��ӿ�
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
    	String work_id = null;//�û�
    	String name = null;//��ʵ����
    	String phone = null;//�绰����
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
		    	work_id = jstr.getString("username");//�û�
		    	name = jstr.getString("name");//��ʵ����
		    	phone = jstr.getString("phone");//�绰����
		    	email = jstr.getString("email");
		    	password = jstr.getString("password");
		    	usertype = jstr.getString("role");
		    	agentid = jstr.getString("agentid");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger_error.error("��ȡ�ύ����ʧ��" + e);			
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��ȡ�ύ����ʧ��" + e);
			Common_return_en(response,re_jsonobject);
			return;
		}
    	
    	Assistance assistance = new Assistance();
    	assistance.setWorkId(work_id);
    	assistance.setName(name);
    	assistance.setPhone(phone);
    	assistance.setEmail(email);
    	assistance.setPassword(password);
    	// ǰ��ֱ�Ӵ��û���ݶ�Ӧ�ı��룺�����̲���=bu �����̹���Ա=bam
    	assistance.setUsertype(usertype);
    	assistance.setAgentid(agentid);
  
    	Register_Manage regmanager = pManage.new Register_Manage();
    	re_jsonobject = regmanager.RgEnter_Select(assistance, "as");
    	
    	//Assistance_Register_return(response,flag);
    	Common_return_en(response, re_jsonobject);
    }
      
    /**
     * Conectp_register ������ϵ��ע��  �ͻ�ע��ӿ�
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
	    	phone =  AES.aesDecrypt(request.getParameter("phone"),AES.key);//�绰
	    	company =  new String(AES.aesDecrypt(request.getParameter("company"),AES.key).getBytes("GBK"),"GBK");//��˾����
	    	agent = AES.aesDecrypt(request.getParameter("agent"),AES.key);//����������id
	    	real_name = new String(AES.aesDecrypt(request.getParameter("real_name"),AES.key).getBytes("GBK"),"GBK");//��ʵ����
	    	register_way = AES.aesDecrypt(request.getParameter("register_way"),AES.key);//ע�᷽ʽ
	    	weixin = AES.aesDecrypt(request.getParameter("weixin"),AES.key);//΢�ź�
	    	companyid = AES.aesDecrypt(request.getParameter("companyid"),AES.key);//��˾id
	    	password = AES.aesDecrypt(request.getParameter("password"),AES.key);//����
	    	email = AES.aesDecrypt(request.getParameter("email"),AES.key);//����
	    	contractMes = AES.aesDecrypt(request.getParameter("contract_mes"),AES.key);//��Чƾ֤
	    	cardid = AES.aesDecrypt(request.getParameter("cardid"),AES.key);//������ϵ�����֤
	    	weixinid = AES.aesDecrypt(request.getParameter("weixinid"),AES.key);//
	    	logger.info("weixinid:" + weixinid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//�û���

    	
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
    	inConnectPerson.setFlag(Person_Manage.REG_NEW);//����ע��״̬Ϊ�ȴ�����
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
     * Verify_Register ϵͳ����Ա��ע���������ȷ��  ϵͳ����Ա�ӿ�
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
			re_jsonobject.element("errmsg", "��¼��ʱ");
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
    	else{//������ϵ��
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
     * Control_Power ϵͳ����Ա���û�Ȩ�޽��п��� 
     * @author ϵͳ����Ա�ӿ� 
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
			re_jsonobject.element("errmsg", "��¼��ʱ");
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
		String control_type = jstr.getString("control_type");//�������ͣ�as:�����̲���  cp:������ϵ��
		int ctlflag = jstr.getInt("ctlflag"); //���ƵĽ��  0������  -3������ 
		String id = jstr.getString("id");//���ƶ����id
		
		boolean existAS = false;
		if(ctlflag == 0)
			existAS = pManage.checkExistAS(id);
		if(existAS == true){
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "�ô������Ѵ��ڿ��ò��񣬲��ܽ���");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		pManage.Control_Power(control_type,ctlflag,id);//����Ȩ�޿��ƴ���
		
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "����Ȩ�޳ɹ�");
		Common_return_en(response,re_jsonobject);
    }
   
   
    /**
     * Connectp_login ������ϵ�˵�¼ 
     * @author  �ͻ��ӿ�
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
			re_jsonobject.element("errmsg", "��¼�ɹ�");
			OneKeyData_return_enData(response, re_jsonobject, "connectp", pManage.cDao.findById(ConnectPerson.class, username));
		}
		else if (isllegal == -1) {//�û���������
			logger_error.error("��¼ʧ��");
			re_jsonobject.element("status", isllegal);
			re_jsonobject.element("errmsg", "�û���������");
			Common_return(response, re_jsonobject);
		}
		else if (isllegal == -3) {
			re_jsonobject.element("status", -1);
			re_jsonobject.element("errmsg", "ע��������");
			Common_return(response, re_jsonobject);
		}
		else if (isllegal == -4) {
			re_jsonobject.element("status", -1);
			re_jsonobject.element("errmsg", "�û��Ѿ�������������ϵ����Ա");
			Common_return(response, re_jsonobject);
		}
		else {//�������
			logger_error.error("�������");
			re_jsonobject.element("status", isllegal);
			re_jsonobject.element("errmsg", "�������");
			Common_return(response, re_jsonobject);
		}
    }
  
    /**
     * get_agentcodeAname ��ȡ�����̵�id������ 
     * @category  ������ϵ��ע��ӿ�
     * @param request
     * @param response
     * @author zhangxinming
     */
    @RequestMapping(value="get_agentcodeAname")
    public void Get_AgentCAN(HttpServletRequest request,HttpServletResponse response){
    	logger.info("***Get get_agentcodeAname request***");
    	
    	JSONObject re_jsonobject = new JSONObject();
    	
    	JSONArray re_jarry = pManage.Get_AgentCAN();//�����ȡ��Ϣ����
    	
    	/*�������ݵ�ǰ̨*/
    	re_jsonobject.element("flag", 0);
    	re_jsonobject.element("errmsg", "��ȡ�����̼�id�ɹ�");
    	OneKeyData_return_enall(response, re_jsonobject, "data", re_jarry);
    	/*�������ݵ�ǰ̨*/
    }
    
    /**
     * Watch ��̨����ϵͳ�Ĳ鿴����
     * @category ����Ա�ӿ�
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
			re_jsonobject.element("errmsg", "��¼��ʱ");
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
				request_s_de = AES.aesDecrypt(request_s, AES.key);//��������
				logger.info("received content:" + request_s_de);
				JSONObject jstr = JSONObject.fromObject(request_s_de);
				watch_type = jstr.getString("watch_type");//����鿴������
				pagenum = jstr.getInt("pagenum");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger_error.error("�鿴ʧ��" + e);
		}
		
		int pagesize = 10;
		int offset = (pagenum-1)*10;
		
		re_jsonobject = pManage.Watch(watch_type,offset,pagesize, user_type, agent_id);//����鿴����
    	//int num = pManage.opLog_Dao.GetOpLogTb_Num();
    	/*�������ݵ�ǰ̨*/
    	re_jsonobject.element("flag", 0);
    	re_jsonobject.element("errmsg", "�鿴�ɹ�");
    	//re_jsonobject.element("totalpage", 2);
    	//OneKeyData_return_enall(response, re_jsonobject, "data", re_list);
    	Common_return_en(response, re_jsonobject);
    	/*�������ݵ�ǰ̨*/
    }
    
    /**
     * BackUpDatabase �������ݿ�
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
			re_jsonobject.element("errmsg", "��¼��ʱ");
			Common_return_en(response,re_jsonobject);
			return;
		}
    	
    	DB_Operator dOperator = pManage.new DB_Operator();
    	String savedir = request.getServletContext().getRealPath("/" + DB_Operator.dirname);
    	
    	String filename = dOperator.produce_time() + ".sql";
    	dOperator.BackUp_db(savedir, filename);
    	
    	re_jsonobject.element("flag", 0);
    	re_jsonobject.element("errmsg", "���ݳɹ�");
    	Common_return_en(response, re_jsonobject);
    }
    
    /**
     * Verify_Restore �ύ�ָ�sqlȷ��
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
			re_jsonobject.element("errmsg", "��¼��ʱ");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		int backupid = -1;
		 try {
				String request_s = IOUtils.toString(request.getInputStream());
				String request_s_de = AES.aesDecrypt(request_s, AES.key);
				logger.info("receive" + request_s_de);
				JSONObject jstr = JSONObject.fromObject(request_s_de);
				backupid = jstr.getInt("id");//��ȡ��¼id
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
    	DB_Operator dOperator = pManage.new DB_Operator();
    	int flag = dOperator.Restore(backupid);
    	
    	if (flag == -1) {
	    	re_jsonobject.element("flag", -1);
	    	re_jsonobject.element("errmsg", "�ָ�ʧ��");
		}
    	else {
	    	re_jsonobject.element("flag", 0);
	    	re_jsonobject.element("errmsg", "�ָ��ɹ�");
		}

    	Common_return_en(response, re_jsonobject);
    }
    
    /**
     * Choose_Restore ѡ��ָ���sql
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
			re_jsonobject.element("errmsg", "��¼��ʱ");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		List<Backup> fBackups = pManage.bUp_Dao.GetTolTb();
		if (fBackups != null) {
	    	re_jsonobject.element("flag", 0);
	    	re_jsonobject.element("errmsg", "�����ɹ�");
	    	
	    	OneKeyData_return_enall(response,re_jsonobject,"data",fBackups);
		}
		else{
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "����ʧ��");
			Common_return_en(response,re_jsonobject);
		}
    }
    
    /**
     * Asssistance_login_return ������Ա��¼����
     * @param flag
     * @param username �û���
     * @param password ����
     * @param role �û�����
     * @param newpay_num �µĸ�����Ϣ��Ŀ
     * @param response
     * @author zhangxinming
     */
    public void Asssistance_login_return(int flag,String username,String password,String role,int newpay_num,HttpServletResponse response){
		response.setCharacterEncoding("utf-8");
		JSONObject userJ =  new JSONObject();//���ݲ����е���������
		JSONObject resJ=new JSONObject();
		if (flag == -1) {//�û�������
				resJ.put("flag", -1);
				resJ.put("errmsg", "�û������������");
		}
		else if (flag == -2) {
			resJ.put("flag", -1);
			resJ.put("errmsg", "�û������������");
		}
		else if(flag == -3){
			resJ.put("flag", -3);
			resJ.put("errmsg", "ע�������");
		}
		else if (flag == -4) {
			resJ.put("flag", -4);
			resJ.put("errmsg", "���˺��ѱ�����������ϵϵͳ����Ա");
		}
		else {
				Assistance fAssistance = pManage.aS_Dao.findById(Assistance.class, username);
				userJ.element("role", role);//��������ӵ�������
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
						userJ.element("agentname","δ֪������");
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
     * OneKeyData_return ��һ���������ݵ�ǰ̨����
     * @param response
     * @param re_json ���������������Ϣ
     * @param key ������Ϣ��key
     * @param data ������Ϣ
     * @category����Ա�ӿ�
     */
    public void OneKeyData_return_enall(HttpServletResponse response,JSONObject re_json,String key,Object data){
		response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//���ݲ����е���������
		re_object.element(key, data);
		
		/*����json���ݸ�ǰ̨*/
		try {
			logger.info("send content:" + re_object.toString());
			Writer writer = response.getWriter();
			String en_s = AES.aesEncrypt(re_object.toString(), AES.key);
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
    }
    
    /**
     * OneKeyData_return_enData ��һ�����������Ϣ�ķ��أ�ֻ���ܾ�����Ϣ
     * @param response
     * @param re_json ���������������Ϣ
     * @param key ������Ϣ��key
     * @param data ������Ϣ
     * @�ͻ����ؽӿ�
     */
    public void OneKeyData_return_enData(HttpServletResponse response,JSONObject re_json,String key,Object data){
		response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//���ݲ����е���������
    	

		JSONObject aes_object = JSONObject.fromObject(data);
		String aes_object_s = aes_object.toString();
		logger.info("Before ��[aesEncrypt],context is:" + aes_object_s);
		String en_s = null;
		try {
			en_s = AES.aesEncrypt(aes_object_s,AES.key);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		re_object.element(key, en_s);
		
		/*����json���ݸ�ǰ̨*/
		try {
			logger.info("send content��" + re_object.toString());
			Writer writer = response.getWriter();
			writer.write(re_object.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
    }
    
    /**
     * Common_return ����������Ϣ�ķ���
     * @param response
     * @param re_json �����������Ϣ
     * @author zhangxinming
     */
    public void Common_return(HttpServletResponse response,JSONObject re_json){
		response.setCharacterEncoding("utf-8");
	//	response.addHeader("Access-Control-Allow-Origin", "*");	
    	JSONObject re_object =  re_json;//���ݲ����е���������
		
		/*����json���ݸ�ǰ̨*/
		logger.info(re_object.toString());
		try {
			Writer writer = response.getWriter();
			writer.write(re_object.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
    }

    /**
     * Common_return_en ����������Ϣ�ļ��ܷ���
     * @param response
     * @param re_json �����������Ϣ
     * @author zhangxinming
     */
    public void Common_return_en(HttpServletResponse response,JSONObject re_json){
		response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//���ݲ����е���������
		
    	String en_s = null;
		/*����json���ݸ�ǰ̨*/
		logger.info(re_object.toString());
		try {
			Writer writer = response.getWriter();
			en_s = AES.aesEncrypt(re_object.toString(), AES.key);
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
    }
}


