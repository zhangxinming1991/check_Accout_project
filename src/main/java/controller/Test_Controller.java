package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mysql.fabric.xmlrpc.base.Data;

import check_Asys.CheckAcManage;
import check_Asys.OpLog_Service;
import check_Asys.WeixinPush_Service;
import check_Asys.WeixinPush_Service.Push_Template;
import encrypt_decrpt.AES;
import net.sf.json.JSONObject;

@Controller
public class Test_Controller {
	private static Logger logger = LogManager.getLogger(Check_MainController.class);
	private static Logger logger_error = LogManager.getLogger("error");
	/*全局变量*/
	public final static SessionFactory wFactory = new Configuration().configure().buildSessionFactory();
	public final static CheckAcManage cOp = new CheckAcManage(wFactory);
	private static OpLog_Service oLog_Service = new OpLog_Service(wFactory);
	/*全局变量*/
	
	
	@RequestMapping(value="DebugWeixinPush")
	public void DebugWeixinPush(HttpServletRequest request,HttpServletResponse response) throws Exception{
		logger.info("***Get DebugWeixinPush request***");
		
		WeixinPush_Service wp_ser = new WeixinPush_Service();
		Push_Template pushTemplate = wp_ser.new Push_Template();
		Date date = new Date();
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy年MM月dd日_HH时mm分ss秒");
		String dates = sFormat.format(date);
		logger.info(date);
		pushTemplate.Create_RegNoteTemplate("张三", "1234", dates);
		wp_ser.Push_OpSelect(WeixinPush_Service.REGISTER_NOTE, pushTemplate);
	}
	
	@RequestMapping(value="DebugRecPush")
	public void DebugRecPush(HttpServletRequest request,HttpServletResponse response) throws Exception{
		logger.info("***Get DebugRecPush request***");
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String data;
		String template;
		data = request.getParameter("data");
		JSONObject jdata_0 = JSONObject.fromObject(data);
		String accoutPwd = AES.aesDecrypt(jdata_0.getString("keyword2"), AES.key);
	/*	try {
		//	data = AES.aesDecrypt(data, AES.key);
			data = URLDecoder.decode(data, "UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		template = request.getParameter("template");
		
		JSONObject jdata = JSONObject.fromObject(data);
		String accoutName = jdata.getString("keyword1");
		String operationTime = jdata.getString("keyword3");
		String first = jdata.getString("first");
		String remark = jdata.getString("remark");
		
		logger.info(template + ":" + accoutName + ":" + accoutPwd + ":" + operationTime);
		JSONObject jsonObject = new JSONObject();
		jsonObject.element("errmsg", "receive ok");
	//	Common_return_en(response,jsonObject);
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
		
		/*传递json数据给前台*/
		logger.info(re_object.toString());
		try {
			Writer writer = response.getWriter();
			writer.write(re_object.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*传递json数据给前台*/
    }
}
