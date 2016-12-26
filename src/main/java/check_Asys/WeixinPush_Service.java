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
 * WeixinPush_Service 微信推送服务
 * 调用方式：（参考Test_Controller：：DebugWeixinPush）
 * 1.创建Push_Template
 * 2.初始化Push_Template
 * 3.调用Push_OpSelect
 * @author zhangxinming
 *
 */
public class WeixinPush_Service {

	private static Logger logger = LogManager.getLogger(WeixinPush_Service.class);
	private static Logger logger_error = LogManager.getLogger("error");
	
	//String TestUrl = "http://119.29.235.201:8080/check_Accout/Test_Controller/DebugRegNotePush";
	//String TestUrl = "http://192.168.137.1:8080/check_Accout/Test_Controller/DebugRegNotePush";
	String pushoneUrl = "http://baas/weixin/weixin/sendMessage";//单条推送
	String pushmanyUrl = "http://baas/weixin/weixin/sendMessages";//多条推送
	/*模板号定义*/
	public static final int REGISTER_NOTE = 1;//账户注册通知
	public static final int REGISTE_CHECK = 2;//注册审核结果通知
	public static final int BING_ACCOUT = 3;//绑定用户
	public static final int LOOSE_ACCOUT = 4;//用户解绑
	public static final int CHANGE_SCORE = 5;//积分变更
	public static final int CONVERT_SCORE = 6;//兑换积分
	public static final int DELIVER_GOODS = 7;//发货通知
	public static final int Map_PayMes = 8;//审核付款记录
	
	//create more
	/*模板号定义*/
	
	/**
	 * Push_OpSelect 推送选择
	 * @param pushUrl 推送连接，不带参数
	 * @param pushType 推送类型
	 * @param pushTemplate 推送的模板内容
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
			/*处理data数据，先加密，在做url编码*/
			data_data = AES.aesEncrypt(data_data, AES.key);
			logger.info("After Encrypt:" + data_data);
			data_data = URLEncoder.encode(data_data, "UTF-8");
			logger.info("After URLEnco:" + data_data);
			color_data = URLEncoder.encode(color_data, "UTF-8");
			/*处理data数据，先加密，在做url编码*/
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
		int template;//模板编号
		String user;//用户微信id
		JSONObject data;//
		JSONObject color;//?赋值方式
		
		public  Push_Template() {
			data = new JSONObject();
			color = new JSONObject();
		}
		
		/**
		 * Create_RegNoteTemplate:创建注册通知模板
		 * @param userid 微信id
		 * @param accoutName 账户名称
		 * @param accoutPwd 账户密码
		 * @param operationTime 操作时间
		 * @return
		 * @throws Exception 
		 * @author zhangxinming
		 */
		public boolean Create_RegNoteTemplate(String userid,String accoutName,String accoutPwd,String operationTime) throws Exception{
			
			try {
				template = REGISTER_NOTE;
				String first = "您好，您在三一对账通的注册申请提交成功！";
				String remark = "账号需要审批通过后才能登录，请耐心等待！";
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
		 * Create_RegCheck_Template:推送注册审核结果通知
		 * @param userid 用户微信id
		 * @param username 姓名
		 * @param checkResult 审核结果
		 * @return
		 * @author zhangxinming
		 */
		public boolean Create_RegCheck_Template(String userid,String username,String checkResult){
			template = REGISTE_CHECK;
			String first = "恭喜，您在三一对账通的注册申请已通过。";
			String remark = "请前往绑定/解绑功能绑定账号！";
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
		 * Create_BingAccout_Template:用户绑定
		 * @param userid
		 * @param bingAccout
		 * @param bingTime
		 * @return
		 * @author zhangxinming
		 */
		public boolean Create_BingAccout_Template(String userid,String bingAccout,String bingTime){
			template = BING_ACCOUT;
			String first = "您好，你账号与当前微信号已绑定成功：";
			String remark = "感谢你的使用。";
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
		 * Create_LooseAccout_Template:用户解绑
		 * @param userid
		 * @param looseAccout
		 * @param looseTime
		 * @return
		 */
		public boolean Create_LooseAccout_Template(String userid,String looseAccout,String looseTime){
			template = LOOSE_ACCOUT;
			String first = "您好，以下账号已解除绑定：";
			String remark = "感谢你的使用。";
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
		 * Create_MapPayMes_Template 审核付款记录
		 * @param userid
		 * @param id
		 * @param opResult
		 * @param opTime
		 * @return
		 * @author zhangxinming
		 */
		public boolean Create_MapPayMes_Template(String userid,String id,String opResult,String opTime){
			template = Map_PayMes;
			String first = "您好，你上传的付款记录审阅结果如下：";
			String remark = "如有疑问，请联系系统管理员。";
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
		 * Create_ChangeScore_Template 积分变更
		 * @param userid
		 * @param sourceChange
		 * @param state
		 * @param totalSource
		 * @return
		 */
		public boolean Create_ChangeScore_Template(String userid,String sourceChange,String state,String totalSource){
			template = CHANGE_SCORE;
			String first = "您的账号(1111)有一个积分变更通知信息：";
			String remark = "可通过积分兑换功能申请兑换，祝愉快！";
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
		 * Create_ConvertScore_Template 兑换积分
		 * @param userid
		 * @param payTime
		 * @param convertMes
		 * @param clientMes
		 * @param convertAgent
		 * @return
		 */
		public boolean Create_ConvertScore_Template(String userid,String payTime,String convertMes,String clientMes,String convertAgent){
			template = CONVERT_SCORE;
			String first = "您的账号(1111)积分兑换申请提交成功：";
			String remark = "兑换过程需要3-4天，请耐心等待！";
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
		 * Create_DeliverGoods_Template 发货通知
		 * @param userid
		 * @param logisticsId
		 * @param orderId
		 * @param deliverTime
		 * @return
		 * @author zhangxinming
		 */
		public boolean Create_DeliverGoods_Template(String userid,String logisticsId,String orderId,String deliverTime){
			template = DELIVER_GOODS;
			String first = "亲，礼品已经启程了，请及时留意物流状态哦~";
			String remark = "发货时间："+ deliverTime +" 若有疑问，请随时与我们联系，谢谢！";
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
