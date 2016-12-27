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
	
	/**
	 * DebugWeixinPush:调试【微信推送】推送
	 * @param request
	 * @param response
	 * @throws Exception
	 * @author zhangxinming
	 */
	@RequestMapping(value="DebugWeixinPush")
	public void DebugWeixinPush(HttpServletRequest request,HttpServletResponse response) throws Exception{
		logger.info("***Get DebugWeixinPush request***");
		
		WeixinPush_Service wp_ser = new WeixinPush_Service();
		
		Date date = new Date();
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
		String dates = sFormat.format(date);
		logger.info(date);
		String userid = "oN6los7yaXFPenJdeUqxKP4kdCk8";
		String url = null;
		
		/*账户注册通知*/
		url = wp_ser.pushoneUrl;
		Push_Template regNotepushTemplate = wp_ser.new Push_Template();
		regNotepushTemplate.Create_RegNoteTemplate(userid,"张三", "1234", dates);
		wp_ser.Push_OpSelect(url,WeixinPush_Service.REGISTER_NOTE, regNotepushTemplate);
		/*账户注册通知*/
		/*注册审核结果通知*/
		url = wp_ser.pushoneUrl;
		Push_Template regCheckPushTemplate = wp_ser.new Push_Template();
		regCheckPushTemplate.Create_RegCheck_Template(userid, "zhangsan", "通过");
		wp_ser.Push_OpSelect(url,WeixinPush_Service.REGISTE_CHECK, regCheckPushTemplate);
		/*注册审核结果通知*/
	}
	
	/**
	 * DebugRegNotePush:调试【账户注册通知】推送接受
	 * @param request
	 * @param response
	 * @throws Exception
	 * @author zhangxinming
	 */
	@RequestMapping(value="DebugRegNotePush")
	public void DebugRegNotePush(HttpServletRequest request,HttpServletResponse response) throws Exception{
		logger.info("***Get DebugRegNotePush request***");
		JSONObject jsonObject = new JSONObject();
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/*获取传递参数*/
		String template = request.getParameter("template");
		String user = AES.aesDecrypt(request.getParameter("user"),AES.key);
		String data = AES.aesDecrypt(request.getParameter("data"), AES.key);
		String color = request.getParameter("color");
		/*获取传递参数*/
		
		/*json数据转换*/
		JSONObject jdata = JSONObject.fromObject(data);
		String accoutName = jdata.getString("keyword1");
		String accoutPwd = jdata.getString("keyword2");
		String operationTime = jdata.getString("keyword3");
		String first = jdata.getString("first");
		String remark = jdata.getString("remark");
		
		JSONObject jcolor = JSONObject.fromObject(color);
		/*json数据转换*/

		/*打印传递的参数*/
		logger.info("[template]:" +template);
		logger.info("[user]:" + user);
		logger.info("[accoutName]:" + accoutName);
		logger.info("[operationTime]:" + operationTime);
		logger.info("[color]:" + color);
		logger.info("[first]:" + first);
		logger.info("[remark]:" + remark);
		/*打印传递的参数*/
		
		jsonObject.element("errmsg", "RegNotePush receive ok");
		Common_return_en(response,jsonObject);
		
		logger.info("***END DebugRegNotePush request***");
	
		return;
	}
	
	/**
	 * DebugRegCheckPush:调试【注册审核结果通知】推送接受
	 * @param request
	 * @param response
	 * @author zhangxinming
	 * @throws Exception 
	 */
	@RequestMapping(value="DebugRegCheckPush")
	public void DebugRegCheckPush(HttpServletRequest request,HttpServletResponse response) throws Exception{
		logger.info("***Get DebugRegCheckPush request***");
		
		JSONObject jsonObject = new JSONObject();
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/*获取传递参数*/
		String template = request.getParameter("template");
		String user = AES.aesDecrypt(request.getParameter("user"),AES.key);
		String data = AES.aesDecrypt(request.getParameter("data"), AES.key);
		String color = request.getParameter("color");
		/*获取传递参数*/
		
		/*json参数转换*/
		JSONObject jdata = JSONObject.fromObject(data);
		String username = jdata.getString("keyword1");
		String checkResult = jdata.getString("keyword2");
		String first = jdata.getString("first");
		String remark = jdata.getString("remark");
		
		JSONObject jcolor = JSONObject.fromObject(color);
		/*json参数转换*/
		
		
		/*打印传递的参数*/
		logger.info("[template]:" +template);
		logger.info("[user]:" + user);
		logger.info("[username]:" + username);
		logger.info("[checkResult]:" + checkResult);
		logger.info("[color]:" + color);
		logger.info("[first]:" + first);
		logger.info("[remark]:" + remark);
		/*打印传递的参数*/
		
		jsonObject.element("errmsg", "RegCheckPush receive ok");
		Common_return_en(response,jsonObject);
		
		logger.info("***END DebugRegCheckPush request***");
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
