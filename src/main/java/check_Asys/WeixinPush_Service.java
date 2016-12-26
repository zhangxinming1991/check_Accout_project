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


/**
 * WeixinPush_Service ΢�����ͷ���
 * ���÷�ʽ�����ο�Test_Controller����DebugWeixinPush��
 * 1.����Push_Template
 * 2.��ʼ��Push_Template
 * 3.����Push_OpSelect
 * @author zhangxinming
 *
 */
public class WeixinPush_Service {

	private static Logger logger = LogManager.getLogger(WeixinPush_Service.class);
	private static Logger logger_error = LogManager.getLogger("error");
	
	//String TestUrl = "http://119.29.235.201:8080/check_Accout/Test_Controller/DebugRegNotePush";
	//String TestUrl = "http://192.168.137.1:8080/check_Accout/Test_Controller/DebugRegNotePush";
	String pushoneUrl = "http://baas/weixin/weixin/sendMessage";//��������
	String pushmanyUrl = "http://baas/weixin/weixin/sendMessages";//��������
	/*ģ��Ŷ���*/
	public static final int REGISTER_NOTE = 1;//�˻�ע��֪ͨ
	public static final int REGISTE_CHECK = 2;//ע����˽��֪ͨ
	public static final int BING_ACCOUT = 3;//���û�
	public static final int LOOSE_ACCOUT = 4;//�û����
	public static final int CHANGE_SCORE = 5;//���ֱ��
	public static final int CONVERT_SCORE = 6;//�һ�����
	public static final int DELIVER_GOODS = 7;//����֪ͨ
	public static final int Map_PayMes = 8;//��˸����¼
	
	//create more
	/*ģ��Ŷ���*/
	
	/**
	 * Push_OpSelect ����ѡ��
	 * @param pushUrl �������ӣ���������
	 * @param pushType ��������
	 * @param pushTemplate ���͵�ģ������
	 * @return
	 * @author zhangxinming
	 */
	public int Push_OpSelect(String pushUrl,int pushType,Push_Template pushTemplate) {
		CloseableHttpClient client = HttpClients.createDefault();
		String para = null;
		
		String data_key = "?data=";
		String data_data = pushTemplate.data.toString();
		
		String color_key = "&color=";
		String color_data = pushTemplate.color.toString();
		try {
			/*����data���ݣ��ȼ��ܣ�����url����*/
			data_data = AES.aesEncrypt(data_data, AES.key);
			logger.info("After Encrypt:" + data_data);
			data_data = URLEncoder.encode(data_data, "UTF-8");
			logger.info("After URLEnco:" + data_data);
			color_data = URLEncoder.encode(color_data, "UTF-8");
			/*����data���ݣ��ȼ��ܣ�����url����*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		para = data_key + data_data + "&template=" + pushTemplate.template + "&user=" + pushTemplate.user + color_key + color_data;
		String request_url = pushUrl + para;
		String res = new MediaDownloadRequestExecutor().WeiXinPush(client, null, request_url,pushTemplate);
		logger.info(res);
	
		return 0;
	}
	
	/**
	 * 
	 * @author zhangxinming
	 *
	 */
	public class Push_Template{
		int template;//ģ����
		String user;//�û�΢��id
		JSONObject data;//
		JSONObject color;//?��ֵ��ʽ
		
		public  Push_Template() {
			data = new JSONObject();
			color = new JSONObject();
		}
		
		/**
		 * Create_RegNoteTemplate:����ע��֪ͨģ��
		 * @param userid ΢��id
		 * @param accoutName �˻�����
		 * @param accoutPwd �˻�����
		 * @param operationTime ����ʱ��
		 * @return
		 * @throws Exception 
		 * @author zhangxinming
		 */
		public boolean Create_RegNoteTemplate(String userid,String accoutName,String accoutPwd,String operationTime) throws Exception{
			
			try {
				template = REGISTER_NOTE;
				String first = "���ã�������һ����ͨ��ע�������ύ�ɹ���";
				String remark = "�˺���Ҫ����ͨ������ܵ�¼�������ĵȴ���";
				this.user = AES.aesEncrypt(userid, AES.key);
				
				String anutf_8 = new String(accoutName.getBytes("utf-8"),"utf-8");
				logger.info(anutf_8);
				String firstutf_8 = new String(first.getBytes("utf-8"), "UTF-8");
				String remarkutf_8 = new String(remark.getBytes("utf-8"), "utf-8");
				data.element("first", firstutf_8);
				data.element("keyword1", anutf_8);
				//data.element("keyword2", AES.aesEncrypt(accoutPwd, AES.key));
				data.element("keyword2", accoutPwd);
				data.element("keyword3", operationTime);
				data.element("remark", remarkutf_8);
				
				color.element("keyword1", "#FFFACD");
				color.element("keyword2", "#FFF8DC");
				color.element("keyword3", "#FFEFDB");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			} 

			//data.element("data", data_body);
			return true;
		}
		
		/**
		 * Create_RegCheck_Template:����ע����˽��֪ͨ
		 * @param userid �û�΢��id
		 * @param username ����
		 * @param checkResult ��˽��
		 * @return
		 * @author zhangxinming
		 */
		public boolean Create_RegCheck_Template(String userid,String username,String checkResult){
			template = REGISTE_CHECK;
			String first = "��ϲ��������һ����ͨ��ע��������ͨ����";
			String remark = "��ǰ����/����ܰ��˺ţ�";
			try {	
				this.user = AES.aesEncrypt(userid, AES.key);
				data.element("first", first);
				data.element("keyword1", username);
				data.element("keyword2", checkResult);
				data.element("remark", remark);
				
				color.element("keyword1", "#FFFACD");
				color.element("keyword2", "#FFF8DC");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		
		/**
		 * Create_BingAccout_Template:�û���
		 * @param userid
		 * @param bingAccout
		 * @param bingTime
		 * @return
		 * @author zhangxinming
		 */
		public boolean Create_BingAccout_Template(String userid,String bingAccout,String bingTime){
			template = BING_ACCOUT;
			String first = "���ã����˺��뵱ǰ΢�ź��Ѱ󶨳ɹ���";
			String remark = "��л���ʹ�á�";
			try {	
				this.user = AES.aesEncrypt(userid, AES.key);
				data.element("first", first);
				data.element("keyword1", bingAccout);
				data.element("keyword2", bingTime);
				data.element("remark", remark);
				
				color.element("keyword1", "#FFFACD");
				color.element("keyword2", "#FFF8DC");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		
		/**
		 * Create_LooseAccout_Template:�û����
		 * @param userid
		 * @param looseAccout
		 * @param looseTime
		 * @return
		 */
		public boolean Create_LooseAccout_Template(String userid,String looseAccout,String looseTime){
			template = LOOSE_ACCOUT;
			String first = "���ã������˺��ѽ���󶨣�";
			String remark = "��л���ʹ�á�";
			try {	
				this.user = AES.aesEncrypt(userid, AES.key);
				data.element("first", first);
				data.element("keyword1", looseAccout);
				data.element("keyword2", looseTime);
				data.element("remark", remark);
				
				color.element("keyword1", "#FFFACD");
				color.element("keyword2", "#FFF8DC");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		
		/**
		 * Create_MapPayMes_Template ��˸����¼
		 * @param userid
		 * @param id
		 * @param opResult
		 * @param opTime
		 * @return
		 * @author zhangxinming
		 */
		public boolean Create_MapPayMes_Template(String userid,String id,String opResult,String opTime){
			template = Map_PayMes;
			String first = "���ã����ϴ��ĸ����¼���Ľ�����£�";
			String remark = "�������ʣ�����ϵϵͳ����Ա��";
			try {	
				this.user = AES.aesEncrypt(userid, AES.key);
				data.element("first", first);
				data.element("keyword1", id);
				data.element("keyword2", opResult);
				data.element("keyword3", opTime);
				data.element("remark", remark);
				
				color.element("keyword1", "#FFFACD");
				color.element("keyword2", "#FFF8DC");
				color.element("keyword3", "#FFEFDB");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		
		/**
		 * Create_ChangeScore_Template ���ֱ��
		 * @param userid
		 * @param sourceChange
		 * @param state
		 * @param totalSource
		 * @return
		 */
		public boolean Create_ChangeScore_Template(String userid,String sourceChange,String state,String totalSource){
			template = CHANGE_SCORE;
			String first = "�����˺�(1111)��һ�����ֱ��֪ͨ��Ϣ��";
			String remark = "��ͨ�����ֶһ���������һ���ף��죡";
			try {	
				this.user = AES.aesEncrypt(userid, AES.key);
				data.element("first", first);
				data.element("keyword1", sourceChange);
				data.element("keyword2", state);
				data.element("keyword3", totalSource);
				data.element("remark", remark);
				
				color.element("keyword1", "#FFFACD");
				color.element("keyword2", "#FFF8DC");
				color.element("keyword3", "#FFEFDB");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		
		/**
		 * Create_ConvertScore_Template �һ�����
		 * @param userid
		 * @param payTime
		 * @param convertMes
		 * @param clientMes
		 * @param convertAgent
		 * @return
		 */
		public boolean Create_ConvertScore_Template(String userid,String payTime,String convertMes,String clientMes,String convertAgent){
			template = CONVERT_SCORE;
			String first = "�����˺�(1111)���ֶһ������ύ�ɹ���";
			String remark = "�һ�������Ҫ3-4�죬�����ĵȴ���";
			try {	
				this.user = AES.aesEncrypt(userid, AES.key);
				data.element("first", first);
				data.element("keyword1", payTime);
				data.element("keyword2", convertMes);
				data.element("keyword3", clientMes);
				data.element("keyword4", convertAgent);
				data.element("remark", remark);
				
				color.element("keyword1", "#FFFACD");
				color.element("keyword2", "#FFF8DC");
				color.element("keyword3", "#FFEFDB");
				color.element("keyword4", "#FFE1FF");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		
		/**
		 * Create_DeliverGoods_Template ����֪ͨ
		 * @param userid
		 * @param logisticsId
		 * @param orderId
		 * @param deliverTime
		 * @return
		 * @author zhangxinming
		 */
		public boolean Create_DeliverGoods_Template(String userid,String logisticsId,String orderId,String deliverTime){
			template = DELIVER_GOODS;
			String first = "�ף���Ʒ�Ѿ������ˣ��뼰ʱ��������״̬Ŷ~";
			String remark = "����ʱ�䣺"+ deliverTime +" �������ʣ�����ʱ��������ϵ��лл��";
			try {	
				this.user = AES.aesEncrypt(userid, AES.key);
				data.element("first", first);
				data.element("keyword1", logisticsId);
				data.element("keyword2", orderId);
				data.element("keyword3", deliverTime);
				data.element("remark", remark);
				
				color.element("keyword1", "#FFFACD");
				color.element("keyword2", "#FFF8DC");
				color.element("keyword3", "#FFEFDB");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		//Create more Template
	}
}
