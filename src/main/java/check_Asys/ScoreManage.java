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
 * ScoreManage �û����ֲ鿴����
 * @author LiLin
 * @version 1.0.0
 */
public class ScoreManage {
	
	private static Logger logger = LogManager.getLogger(ScoreManage.class);
	private static Logger logger_error = LogManager.getLogger("error");
	private SessionFactory mFactory;
	
	/*�������ݿ���dao*/
	private Assistance_Dao assistance_Dao;               // �����̲���dao
	private ConnectPersonScoreInfo_Dao cps_Dao; // �ͻ����ֻ��ܼ�¼dao 
	private ScoreIncreaseRecord_Dao sir_Dao;   	//   �������Ӽ�¼dao
	private ScoreExchangeRecord_Dao ser_Dao; 	//	  ���ֶһ���¼dao
	/*�������ݿ���dao*/

	/*�鿴��Դ�Ķ���*/
	public static final String OP_LOG = "op_log";//�����û��Ĳ�����־
	private static final String EXTYPE_MONEY = "���";
	private static final String EXTYPE_GIFT = "��Ʒ";
	private static final String EXTYPE_ERROE = "δ֪";
	private static final String INSC = "�ѵ���";
	private static final String EXWA = "δ�һ�";
	private static final String EXSC = "�Ѷһ�";
	private static final String EXFA= "�һ�ʧ��";
	private static final String EXING= "�һ���";
	
	public ScoreManage(SessionFactory wFactory) {
		// TODO Auto-generated constructor stub
		mFactory = wFactory;
		
		assistance_Dao = new Assistance_Dao(wFactory);
		cps_Dao = new ConnectPersonScoreInfo_Dao(wFactory);
		sir_Dao = new ScoreIncreaseRecord_Dao(wFactory);
		ser_Dao = new ScoreExchangeRecord_Dao(wFactory);
	}
	
	/**
	 * ����һ�����ֶһ���¼
	 * @param ser
	 */
	public void insertExchangeRecord(ScoreExchangeRecord ser){
		logger.info("����һ���ͻ����ֶһ�����");
		ser_Dao.add(ser);
	}
	
	/** 
	 * ���ݴ�������Ϣ��ȡָ��λ��ָ�������Ŀͻ�������Ϣ�� ����webǰ����ʾ
	 * @param agentId
	 * @param offset
	 * @param pagesize
	 * @return ���õ�json����
	 */
	public JSONObject getScoreInfoByAgent(String agentId, int offset, int pagesize){
		logger.info("��ȡ�������̶�Ӧ�ͻ��Ļ�����Ϣ");
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
	 * ��ȡָ��λ�ü�ָ�������Ŀͻ�������Ϣ������webǰ����ʾ
	 * @param offset
	 * @param pagesize
	 * @return ���õ�json����
	 */
	public JSONObject getScoreAllInfo(int offset, int pagesize){
		logger.info("��ȡ���пͻ��Ļ�����Ϣ");
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
	 * ��ȡ�����û��Ļ����������������Excel
	 * @return
	 */
	public List<ConnectPersonScoreInfo> getScoreAllInfo(){
		return cps_Dao.getAllInfo();
	}
	
	/**
	 * ���ݴ�������Ϣ��ȡ���������û��Ļ����������������Excel
	 * @param agentId
	 * @return
	 */
	public List<ConnectPersonScoreInfo> getScoreInfoByAgent(String agentId){
		return cps_Dao.getInfoByAgentId(agentId);
	}
	
	/**
	 * ��ȡһ�������Ķһ���¼
	 * @param username
	 * @param num  0��ȫ����¼
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
	 * ��ȡһ�������Ļ��ֽ��˼�¼
	 * @param username
	 * @param num  0����ȡȫ����¼
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
				tmp.element("registerWay", "����");
			else if(connectPersonScoreInfo.getRegisterWay().equals("C"))
				tmp.element("registerWay", "��˾");
			else 
				tmp.element("registerWay", "δ֪");
			if(tmp.getInt("exchangingScore") == 0)
				tmp.element("status", "����");
			else 
				tmp.element("status", "�һ���");
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
					tmp.element("exchangeCategory", "�ֽ�");
				}
				else if(scoreExchangeRecord.getExchangeType() == 1){
					tmp.element("exchangeType", EXTYPE_GIFT);
					tmp.element("exchangeCategory", "�����");
				}
				else{
					tmp.element("exchangeType", EXTYPE_ERROE);
					tmp.element("exchangeCategory", "δ֪");
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
