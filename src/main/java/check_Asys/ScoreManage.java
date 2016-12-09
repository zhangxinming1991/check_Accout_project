package check_Asys;

import java.sql.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import dao.Agent_Dao;
import dao.Assistance_Dao;
import dao.ConnectPersonScoreInfo_Dao;
import dao.ConnectPerson_Dao;
import dao.ScoreExchangeRecord_Dao;
import dao.ScoreIncreaseRecord_Dao;
import entity.ConnectPerson;
import entity.ConnectPersonScoreInfo;
import entity.ScoreExchangeRecord;
import entity.ScoreIncreaseRecord;


/**
 * ScoreManage 用户积分查看管理
 * @author LiLin
 * @version 1.0.0
 */
public class ScoreManage {
	
	private static Logger logger = LogManager.getLogger(ScoreManage.class);
	private static Logger logger_error = LogManager.getLogger("error");
	private SessionFactory mFactory;
	
	/*连接数据库表的dao*/
	private Assistance_Dao assistance_Dao;               // 代理商财务dao
	private ConnectPersonScoreInfo_Dao cps_Dao; // 客户积分汇总记录dao 
	private ScoreIncreaseRecord_Dao sir_Dao;   	//   积分增加记录dao
	private ScoreExchangeRecord_Dao ser_Dao; 	//	  积分兑换记录dao
	/*连接数据库表的dao*/

	/*查看资源的定义*/
	public static final String OP_LOG = "op_log";//所有用户的操作日志
	private static final String EXTYPE_MONEY = "红包";
	private static final String EXTYPE_GIFT = "礼品";
	private static final String EXTYPE_ERROE = "未知";
	private static final String INSC = "已到账";
	private static final String EXWA = "未兑换";
	private static final String EXSC = "已兑换";
	private static final String EXFA= "兑换失败";
	private static final String EXING= "兑换中";
	
	public ScoreManage(SessionFactory wFactory) {
		// TODO Auto-generated constructor stub
		mFactory = wFactory;
		
		assistance_Dao = new Assistance_Dao(wFactory);
		cps_Dao = new ConnectPersonScoreInfo_Dao(wFactory);
		sir_Dao = new ScoreIncreaseRecord_Dao(wFactory);
		ser_Dao = new ScoreExchangeRecord_Dao(wFactory);
	}
	
	/**
	 * 插入一条积分兑换记录
	 * @param ser
	 */
	public void insertExchangeRecord(ScoreExchangeRecord ser){
		logger.info("插入一条客户积分兑换申请");
		ser_Dao.add(ser);
	}
	
	/** 
	 * 根据代理商信息获取指定位置指定个数的客户积分信息， 用于web前端显示
	 * @param agentId
	 * @param offset
	 * @param pagesize
	 * @return 填充好的json对象
	 */
	public JSONObject getScoreInfoByAgent(String agentId, int offset, int pagesize){
		logger.info("获取各代理商对应客户的积分信息");
		int pageNum = 0;
		JSONObject reJsonObject = new JSONObject();
		List<ConnectPersonScoreInfo> resultList = cps_Dao.getInfoByAgentId(agentId, offset, pagesize);
		int num = cps_Dao.getNumByAgentId(agentId);
		pageNum = num % pagesize > 0 ? num / pagesize + 1 : num /pagesize;
	
		JSONArray dataArray = returnScoreData(resultList);
		reJsonObject.element("data", dataArray);
		reJsonObject.element("totalpage", pageNum);
		return reJsonObject;
	}
	/**
	 * 获取指定位置及指定个数的客户积分信息，用于web前端显示
	 * @param offset
	 * @param pagesize
	 * @return 填充好的json对象
	 */
	public JSONObject getScoreAllInfo(int offset, int pagesize){
		logger.info("获取所有客户的积分信息");
		int pageNum = 0;
		JSONObject reJsonObject = new JSONObject();
		List<ConnectPersonScoreInfo> resultList = cps_Dao.getAllInfo(offset, pagesize);
		int num = cps_Dao.getAllNum();
		pageNum = num % pagesize > 0 ? num / pagesize + 1 : num /pagesize;
		JSONArray dataArray = returnScoreData(resultList);
		reJsonObject.element("data", dataArray);
		reJsonObject.element("totalpage", pageNum);
		return reJsonObject;
	}
	
	/**
	 * 获取所有用户的积分情况，用于生成Excel
	 * @return
	 */
	public List<ConnectPersonScoreInfo> getScoreAllInfo(){
		return cps_Dao.getAllInfo();
	}
	
	/**
	 * 根据代理商信息获取旗下所有用户的积分情况，用于生成Excel
	 * @param agentId
	 * @return
	 */
	public List<ConnectPersonScoreInfo> getScoreInfoByAgent(String agentId){
		return cps_Dao.getInfoByAgentId(agentId);
	}
	
	/**
	 * 获取一定数量的兑换记录
	 * @param username
	 * @param num  0：全部记录
	 * @return
	 */
	private List<ScoreExchangeRecord> getExchangeRecords(String username, int num)
	{
		List<ScoreExchangeRecord> scoreExchangeRecords;
		if(num == 0)
			scoreExchangeRecords = ser_Dao.getInfoByUsername(username);
		else
			scoreExchangeRecords = ser_Dao.getInfoByUsername(username, num);
		for(int i = 0; i < scoreExchangeRecords.size(); i++){
			ScoreExchangeRecord scoreExchangeRecord = scoreExchangeRecords.get(i);
			if(scoreExchangeRecord.getHander() != null){
				String handerName = assistance_Dao.GetTotalTbByElement("workId", scoreExchangeRecord.getHander()).get(0).getName();
				scoreExchangeRecord.setHander(handerName);
			}
			else
				scoreExchangeRecord.setHander("");
		}
		return scoreExchangeRecords;
	}
	
	/**
	 * 获取一定数量的积分进账记录
	 * @param username
	 * @param num  0：获取全部记录
	 * @return
	 */
	private List<ScoreIncreaseRecord>getIncreaseRecords(String username, int num)
	{
		List<ScoreIncreaseRecord> scoreIncreaseRecords;
		if(num == 0)
			scoreIncreaseRecords = sir_Dao.getInfoByUsername(username);
		else
			scoreIncreaseRecords = sir_Dao.getInfoByUsername(username, num);
		for(int i = 0; i < scoreIncreaseRecords.size(); i++){
			ScoreIncreaseRecord scoreIncreaseRecord = scoreIncreaseRecords.get(i);
			if(scoreIncreaseRecord.getHander() != null){
				String hander = assistance_Dao.GetTotalTbByElement("workId", scoreIncreaseRecord.getHander()).get(0).getName();
				scoreIncreaseRecord.setHander(hander);
			}
			else
				scoreIncreaseRecord.setHander("");
		}
		return scoreIncreaseRecords;
	}
	
	public JSONObject getScoreRecord(String username, int num){
		JSONObject result = new JSONObject();
		JSONArray dataArray = new JSONArray();
		List<ScoreExchangeRecord> scoreExchangeRecords = getExchangeRecords(username, num);
		List<ScoreIncreaseRecord> scoreIncreaseRecords = getIncreaseRecords(username, num);
		for(int i = 0; i < scoreExchangeRecords.size(); i++){
			JSONObject exchangeTmp = new JSONObject();
			ScoreExchangeRecord scoreExchangeRecord = scoreExchangeRecords.get(i);
			Date date = new Date(scoreExchangeRecord.getApplicaTime().getTime());
			exchangeTmp.element("time", date.toString());
			exchangeTmp.element("change", "-" + scoreExchangeRecord.getExchangeScore());
			byte status =  scoreExchangeRecord.getStatus();
			if(status == 1)
				exchangeTmp.element("status", EXING);
			else if(status == 2)
				exchangeTmp.element("status", EXSC);
			else 
				exchangeTmp.element("status", EXFA);
			exchangeTmp.element("hander", scoreExchangeRecord.getHander());
			exchangeTmp.element("serial", scoreExchangeRecord.getSerialNumber());
			exchangeTmp.element("description", scoreExchangeRecord.getDescription());
			dataArray.add(exchangeTmp);
		}
		for(int i = 0; i < scoreIncreaseRecords.size(); i++){
			JSONObject increaseTmp= new JSONObject();
			ScoreIncreaseRecord scoreIncreaseRecord = scoreIncreaseRecords.get(i);
			Date date = new Date(scoreIncreaseRecord.getTime().getTime());
			increaseTmp.element("time", date.toString());
			increaseTmp.element("change", "+" + scoreIncreaseRecord.getIncreaseScore());
			increaseTmp.element("status", INSC);
			increaseTmp.element("hander", scoreIncreaseRecord.getHander());
			increaseTmp.element("serial", "");
			increaseTmp.element("description", scoreIncreaseRecord.getDescription());
			dataArray.add(increaseTmp);
		}
		result.element("data", dataArray);
		return result;
	}
	
	public JSONObject getExchangeInfos(int offset, int pagesize)
	{
		JSONObject result = new JSONObject();
		List<ScoreExchangeRecord> scoreExchangeRecords = ser_Dao.getInfos(offset, pagesize);
		int num = ser_Dao.getNum();
		int pageNum = num % pagesize > 0 ? num / pagesize + 1 : num /pagesize;
		JSONArray  dataArray = returnExchangeData(scoreExchangeRecords);
		result.element("data", dataArray);	
		result.element("totalpage", pageNum);
		return result;
	}
	
	public JSONObject getExchangeInfos()
	{
		JSONObject result = new JSONObject();
		List<ScoreExchangeRecord> scoreExchangeRecords = ser_Dao.getInfos();
		JSONArray  dataArray = returnExchangeData(scoreExchangeRecords);
		System.out.println(dataArray.toString());
		result.element("data", dataArray);	
		return result;
	}
	
	public JSONObject getExchangeInfosByAgentId(String agentId, int offset, int pagesize)
	{
		JSONObject result = new JSONObject();
		List<ScoreExchangeRecord> scoreExchangeRecords = ser_Dao.getInfos(offset, pagesize);
		int num = ser_Dao.getNumByAgentId(agentId);
		int pageNum = num % pagesize > 0 ? num / pagesize + 1 : num /pagesize;
		JSONArray  dataArray = returnExchangeData(scoreExchangeRecords);
		for(int i = 0; i < dataArray.size(); i++){
			JSONObject tmp = dataArray.getJSONObject(i);
			if(!tmp.getString("agentId").equals(agentId))
				dataArray.remove(i);
		}
		result.element("data", dataArray);	
		result.element("totalpage", pageNum);
		return result;
	}
	
	public JSONObject getExchangeInfosByAgentId(String agentId)
	{
		JSONObject result = new JSONObject();
		List<ScoreExchangeRecord> scoreExchangeRecords = ser_Dao.getInfos();
		JSONArray  dataArray = returnExchangeData(scoreExchangeRecords);
		for(int i = 0; i < dataArray.size(); i++){
			JSONObject tmp = dataArray.getJSONObject(i);
			if(!tmp.getString("agentId").equals(agentId))
				dataArray.remove(i);
		}
		result.element("data", dataArray);	
		return result;
	}
	
	public int getCurrentScoreByUsername(String username)
	{
		ConnectPersonScoreInfo connectPersonScoreInfo =  cps_Dao.getConnectPersonScoreInfo(username); 
		if(connectPersonScoreInfo != null)
			return connectPersonScoreInfo.getScore();
		else 
			return 0;
	}
	
	public void updateExchangeStatus(String randKey)
	{
		ser_Dao.updateStatus(randKey, (byte)1);
	}
	
	private JSONArray returnScoreData(List<ConnectPersonScoreInfo> infos)
	{
		JSONArray dataArray = new JSONArray();
		if(infos == null)
			return null;
		for(int i = 0; i < infos.size(); i++){
			ConnectPersonScoreInfo connectPersonScoreInfo = infos.get(i);
			JSONObject tmp = new JSONObject();
			tmp.element("agentName", connectPersonScoreInfo.getAgentName());
			tmp.element("cardId", connectPersonScoreInfo.getCardId());
			tmp.element("company", connectPersonScoreInfo.getCompany());
			tmp.element("username", connectPersonScoreInfo.getUsername());
			tmp.element("email", connectPersonScoreInfo.getEmail());
			tmp.element("realName", connectPersonScoreInfo.getRealName());
			tmp.element("weiXin", connectPersonScoreInfo.getWeiXin());
			if(connectPersonScoreInfo.getExchangedScore() == null)
				tmp.element("exchangedScore", 0);
			else
				tmp.element("exchangedScore", connectPersonScoreInfo.getExchangedScore());
			if(connectPersonScoreInfo.getExchangingScore() == null)
				tmp.element("exchangingScore", 0);
			else
				tmp.element("exchangingScore", connectPersonScoreInfo.getExchangingScore());
			tmp.element("phone", connectPersonScoreInfo.getPhone());
			tmp.element("score", connectPersonScoreInfo.getScore());
			if(connectPersonScoreInfo.getRegisterWay().equals("P"))
				tmp.element("registerWay", "个人");
			else if(connectPersonScoreInfo.getRegisterWay().equals("C"))
				tmp.element("registerWay", "公司");
			else 
				tmp.element("registerWay", "未知");
			if(tmp.getInt("exchangingScore") == 0)
				tmp.element("status", "正常");
			else 
				tmp.element("status", "兑换中");
			dataArray.add(tmp);
		}
		return dataArray;
	}
	
	private JSONArray returnExchangeData(List<ScoreExchangeRecord> infos)
	{
		JSONArray  dataArray = new JSONArray();
		if(infos == null)
			return null;
		for(int i = 0; i < infos.size(); i++){
			JSONObject tmp = new JSONObject();
			ScoreExchangeRecord scoreExchangeRecord = infos.get(i);
			String username = scoreExchangeRecord.getUsername();
			ConnectPersonScoreInfo connectPersonScoreInfo = cps_Dao.getConnectPersonScoreInfo(username);
			if(connectPersonScoreInfo != null){
				tmp.element("agentId", connectPersonScoreInfo.getAgent());
				tmp.element("agentName", connectPersonScoreInfo.getAgentName());
				tmp.element("username", username);
				tmp.element("realName", connectPersonScoreInfo.getRealName());
				tmp.element("weiXin", connectPersonScoreInfo.getWeiXin());
				tmp.element("company", connectPersonScoreInfo.getCompany());
				tmp.element("exchangeScore", scoreExchangeRecord.getExchangeScore());
				if(scoreExchangeRecord.getExchangeType() == 0){
					tmp.element("exchangeType", EXTYPE_MONEY);
					tmp.element("exchangeCategory", "现金");
				}
				else if(scoreExchangeRecord.getExchangeType() == 1){
					tmp.element("exchangeType", EXTYPE_GIFT);
					tmp.element("exchangeCategory", "电风扇");
				}
				else{
					tmp.element("exchangeType", EXTYPE_ERROE);
					tmp.element("exchangeCategory", "未知");
				}
				tmp.element("applicaTime", scoreExchangeRecord.getApplicaTime().toString());
				tmp.element("finishTime", scoreExchangeRecord.getFinishTime().toString());
				tmp.element("randKey", scoreExchangeRecord.getRandKey());
				if(scoreExchangeRecord.getStatus() == 0)
					tmp.element("status", EXWA);
				else if(scoreExchangeRecord.getStatus() == 1)
					tmp.element("status", EXING);
				else if(scoreExchangeRecord.getStatus() == 2)
					tmp.element("status", EXSC);
				else
					tmp.element("status", EXFA);
				tmp.element("description", scoreExchangeRecord.getDescription());	
				dataArray.add(tmp);
			}
		}
		return dataArray;
	}
}
