package check_Asys;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.org.apache.bcel.internal.generic.NEW;

import encrypt_decrpt.AES;
import httpexcutor.MediaDownloadRequestExecutor;
import net.sf.json.JSONObject;

public class WeixinPush_Service {

	private static Logger logger = LogManager.getLogger(WeixinPush_Service.class);
	private static Logger logger_error = LogManager.getLogger("error");
	
	String TestUrl = "http://119.29.235.201:8080/check_Accout/Test_Controller/DebugRecPush";
	//String TestUrl = "http://192.168.137.1:8080/check_Accout/Test_Controller/DebugRecPush";
	String WeixinUrl = "http://baas/weixin/weixin/sendMessage";
	public static final int REGISTER_NOTE = 1;
	
	public int Push_OpSelect(int pushType,Push_Template pushTemplate) {
		
		switch (pushType) {
		case REGISTER_NOTE:
			logger.info("�˻�ע��֪ͨ��������");
			
			CloseableHttpClient client = HttpClients.createDefault();
			
			String para = null;
			String data_key = "?data=";
			String data_data = pushTemplate.data.toString();
			try {
				data_data = URLEncoder.encode(data_data, "UTF-8");
				logger.info(data_data);
		//		data_data = AES.aesEncrypt(data_data, AES.key);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			para = data_key + data_data + "&template=" + pushTemplate.template;
			String request_url = TestUrl + para;
			String res = new MediaDownloadRequestExecutor().WeiXinPush(client, null, request_url,pushTemplate);
			logger.info(res);
			break;
		default:
			logger_error.error("δ֪��������");
			break;
		}
		return 0;
	}
	
	public class Push_Template{
		int template;//ģ����
		String user;
		JSONObject data;
		JSONObject color;
		
		public  Push_Template() {
			data = new JSONObject();
		}
		
		/**
		 * Create_RegNoteTemplate:����ע��֪ͨģ��
		 * @param accoutName
		 * @param accoutPwd
		 * @param operationTime
		 * @return
		 * @throws Exception 
		 */
		public boolean Create_RegNoteTemplate(String accoutName,String accoutPwd,String operationTime) throws Exception{
			
			template = REGISTER_NOTE;
			JSONObject data_body = new JSONObject();
			String first = "���ã�������һ����ͨ��ע�������ύ�ɹ���";
			String remark = "�˺���Ҫ����ͨ������ܵ�¼�������ĵȴ���";
	//		
			try {
				String anutf_8 = new String(accoutName.getBytes("utf-8"),"utf-8");
				logger.info(anutf_8);
				String firstutf_8 = new String(first.getBytes("utf-8"), "UTF-8");
				String remarkutf_8 = new String(remark.getBytes("utf-8"), "utf-8");
				data.element("first", firstutf_8);
				data.element("keyword1", anutf_8);
				data.element("keyword2", AES.aesEncrypt(accoutPwd, AES.key));
				data.element("keyword3", operationTime);
				data.element("remark", remarkutf_8);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			//data.element("data", data_body);
			return true;
		}
	}
}
