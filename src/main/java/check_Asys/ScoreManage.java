package check_Asys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.hibernate.SessionFactory;

import check_Asys.WeixinPush_Service.Push_Template;
import sun.net.www.content.image.gif;
import dao.Agent_Dao;
import dao.Assistance_Dao;
import dao.ConnectPersonScoreInfo_Dao;
import dao.ConnectPerson_Dao;
import dao.Gift_Dao;
import dao.LogisticInfo_Dao;
import dao.ScoreExchangeRecord_Dao;
import dao.ScoreIncreaseRecord_Dao;
import entity.Agent;
import entity.Assistance;
import entity.ConnectPerson;
import entity.ConnectPersonScoreInfo;
import entity.Gift;
import entity.LogisticInfo;
import entity.ScoreExchangeRecord;
import entity.ScoreIncreaseRecord;
import file_op.AnyFile_Op;


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
	private Agent_Dao agent_Dao;                                      // ��������Ϣdao
	private Assistance_Dao assistance_Dao;             		  // �����̲���dao
	private ConnectPerson_Dao cPerson_Dao;				 // �ͻ���Ϣdao
	private ConnectPersonScoreInfo_Dao cps_Dao;	 // �ͻ����ֻ��ܼ�¼dao 
	private ScoreIncreaseRecord_Dao sir_Dao;   			//   �������Ӽ�¼dao
	private ScoreExchangeRecord_Dao ser_Dao; 		//	  ���ֶһ���¼dao
	private Gift_Dao gift_Dao;												// ������Ϣdao
	private LogisticInfo_Dao logisticInfo_Dao;				//  �һ�����������Ϣ
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
		
		agent_Dao = new Agent_Dao(wFactory);
		assistance_Dao = new Assistance_Dao(wFactory);
		cPerson_Dao = new ConnectPerson_Dao(wFactory);
		cps_Dao = new ConnectPersonScoreInfo_Dao(wFactory);
		sir_Dao = new ScoreIncreaseRecord_Dao(wFactory);
		ser_Dao = new ScoreExchangeRecord_Dao(wFactory);
		gift_Dao = new Gift_Dao(wFactory);
		logisticInfo_Dao = new LogisticInfo_Dao(wFactory);
	}
	
	/**
	 * ����һ�����ֶһ���¼
	 * @param ser
	 */
	public int insertExchangeRecord(String username, ScoreExchangeRecord ser, String description){
		logger.info("����һ���ͻ����ֶһ�����");
		Gift gift = gift_Dao.getInfoById(ser.getExchangeType());
		if(gift.getStock() <= 0)
			return -1;
		else
			ser_Dao.add(ser);
		
		// ΢�����ͻ��ֶһ��ύ������Ϣ
		ConnectPerson person = cPerson_Dao.findById(ConnectPerson.class, username);
		String openId = person.getWeixinid();
		String clientMes = person.getRealName();
		String agentID = person.getAgent();
		String convertAgent = agent_Dao.findById(Agent.class, agentID).getAgentName();
		Date date = new Date();
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss");
		String payTime = sFormat.format(date);
	
		WeixinPush_Service wp_ser = new WeixinPush_Service();
		Push_Template pushMessage = wp_ser.new Push_Template();
		int index = description.indexOf("�һ�");
		String convertMes = description.substring(index + 1, description.length());
		pushMessage.Create_ConvertScore_Template(username, openId, payTime, convertMes, clientMes, convertAgent);
		wp_ser.Push_OpSelect(wp_ser.pushoneUrl,WeixinPush_Service.CONVERT_SCORE, pushMessage);
		return 0;
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
			if(status == 1 || status == 0)
				exchangeTmp.element("status", EXING);
			else if(status == 2)
				exchangeTmp.element("status", EXSC);
			else 
				exchangeTmp.element("status", EXFA);
			if(scoreExchangeRecord.getHander() != null){
				System.out.println("hander ="+ scoreExchangeRecord.getHander());
				String hander = assistance_Dao.GetTotalTbByElement("workId", scoreExchangeRecord.getHander()).get(0).getName();
				exchangeTmp.element("hander", hander);
			}
			else
				exchangeTmp.element("hander", "");
			exchangeTmp.element("randKey", scoreExchangeRecord.getRandKey());
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
			if(scoreIncreaseRecord.getHander() != null){
				String hander = assistance_Dao.GetTotalTbByElement("workId", scoreIncreaseRecord.getHander()).get(0).getName();
				increaseTmp.element("hander", hander);
			}
			else
				increaseTmp.element("hander", "");
			increaseTmp.element("randKey", "");
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
	
	public void updateExchangeStatus(String randKey, byte status, String hander)
	{
		ser_Dao.updateStatus(randKey, status, hander);
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
				byte exchangeType = scoreExchangeRecord.getExchangeType();
				Gift gift = gift_Dao.getInfoById(exchangeType);
				if(gift != null){
					tmp.element("exchangeCategory", gift.getGift());
					if(exchangeType == 0)
						tmp.element("exchangeType", EXTYPE_MONEY);
					else
						tmp.element("exchangeType", EXTYPE_GIFT);
				}
				else{
					tmp.element("exchangeCategory", "δ֪");
					tmp.element("exchangeType", EXTYPE_ERROE);
				}
				String applicaTime = scoreExchangeRecord.getApplicaTime() == null ? "" : scoreExchangeRecord.getApplicaTime().toString();
				tmp.element("applicaTime", applicaTime);
				String finishTime = scoreExchangeRecord.getFinishTime() == null? "" : scoreExchangeRecord.getFinishTime().toString();
				tmp.element("finishTime", finishTime);
				tmp.element("randKey", scoreExchangeRecord.getRandKey());
				if(scoreExchangeRecord.getHander() != null){
					String hander = assistance_Dao.GetTotalTbByElement("workId", scoreExchangeRecord.getHander()).get(0).getName();
					tmp.element("hander", hander);
				}
				else
					tmp.element("hander", "");
				if(scoreExchangeRecord.getExchangeType() == 0)
					tmp.element("status", EXSC);
				else if(scoreExchangeRecord.getStatus() == 0)
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
	
	public JSONObject getLogisticInfo(String randKey){
		JSONObject returnData = new JSONObject();  
		LogisticInfo logisticInfo = logisticInfo_Dao.getInfoByRandKey(randKey);
		 returnData.element("data", logisticInfo);
		 return returnData;
	}
	
    /**
     * �����û�����execl�ļ�
     * @param infos
     * @param dirPath
     * @param fileName
     */
    public void produceCPScoreExcel(List<ConnectPersonScoreInfo> infos, String dirPath, String fileName)
    {
    	 AnyFile_Op anyFile_Op = new AnyFile_Op();
    	File dir = anyFile_Op.CreateDir(dirPath);//��������Ŀ¼
		File file = anyFile_Op.CreateFile(dirPath,fileName);//���������ļ�
		
		// ����һ��workbook����һ��Excel��
		HSSFWorkbook wb = new HSSFWorkbook();
		// ��workbook�����һ��sheet����ӦExcel�ļ��е�sheet
		HSSFSheet sheet = wb.createSheet("�ͻ�������Ϣ");
		// ���ø�ʽ������
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		
		// ��sheet���½���ͷ
		final String[] headStrings = {"����������", "�û���", "��ʵ����", "΢�ź�", "�ͻ���", "��ǰ����", "�Ѷһ�����", "���ڶһ�����", "״̬"};
		final int[] columnWidths = {3000, 3000, 3000, 5000, 8000, 3000, 3000, 3000, 3000};
		final String[] detailHeadStrings = {"ʱ��", "���ֱ䶯", "״̬", "������", "��ˮ��", "����"};
		final int[] detailColumnWidths = {3000, 3000, 3000, 3000, 5000, 8000};
		HSSFRow row = sheet.createRow(0);
		for(int i = 0; i < headStrings.length; i++){
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(headStrings[i]);
			cell.setCellStyle(style);
			// �����п�
			sheet.setColumnWidth(i, columnWidths[i]);
		}
		for(int i = 0; i < infos.size(); i++){
			row = sheet.createRow(i + 1);
			ConnectPersonScoreInfo connectPersonScoreInfo = infos.get(i);
			HSSFCell cell = row.createCell(0);
			cell.setCellValue(connectPersonScoreInfo.getAgentName());
			cell = row.createCell(1);
			cell.setCellValue(connectPersonScoreInfo.getUsername());		
			cell = row.createCell(2);
			cell.setCellValue(connectPersonScoreInfo.getRealName());
			cell = row.createCell(3);
			cell.setCellValue(connectPersonScoreInfo.getWeiXin());
			cell = row.createCell(4);
			cell.setCellValue(connectPersonScoreInfo.getCompany());
			cell = row.createCell(5);
			cell.setCellValue(connectPersonScoreInfo.getScore());
			cell = row.createCell(6);
			if(connectPersonScoreInfo.getExchangedScore() == null)
				cell.setCellValue(0);
			else
				cell.setCellValue(connectPersonScoreInfo.getExchangedScore());
			cell = row.createCell(7);
			if(connectPersonScoreInfo.getExchangingScore() == null)
				cell.setCellValue(0);
			else
				cell.setCellValue(connectPersonScoreInfo.getExchangingScore());
			cell = row.createCell(8);
			if(connectPersonScoreInfo.getExchangingScore() == null)
				cell.setCellValue("����");
			else
				cell.setCellValue("���ڶһ�");
			
			// �û���ϸ
			String username = connectPersonScoreInfo.getUsername();
			JSONObject userScoreData = this.getScoreRecord(username, 0);
			JSONArray userScoreRecordsArray = userScoreData.getJSONArray("data");
			if(userScoreRecordsArray.isEmpty())
				continue;
			HSSFSheet userScoreRecordsSheet = wb.createSheet(username);
			HSSFRow detailRow = userScoreRecordsSheet.createRow(0);
			HSSFCell detailCell;
			for(int k = 0; k < detailHeadStrings.length; k++){
				detailCell = detailRow.createCell(k);
				detailCell.setCellValue(detailHeadStrings[k]);
				detailCell.setCellStyle(style);
				// �����п�
				userScoreRecordsSheet.setColumnWidth(k, detailColumnWidths[k]);
			}
			for(int j = 0; j < userScoreRecordsArray.size(); j++){
				JSONObject jsonObject = userScoreRecordsArray.getJSONObject(j);
				detailRow = userScoreRecordsSheet.createRow(j + 1);
				detailCell = detailRow.createCell(0);
				detailCell.setCellValue(jsonObject.getString("time"));
				detailCell = detailRow.createCell(1);
				detailCell.setCellValue(jsonObject.getString("change"));
				detailCell = detailRow.createCell(2);
				detailCell.setCellValue(jsonObject.getString("status"));
				detailCell = detailRow.createCell(3);
				detailCell.setCellValue(jsonObject.getString("hander"));
				detailCell = detailRow.createCell(4);
				detailCell.setCellValue(jsonObject.getString("randKey"));
				detailCell = detailRow.createCell(5);
				detailCell.setCellValue(jsonObject.getString("description"));
				
			}
		}
				
	    // ���ļ�����ָ��λ��
	    try{
	    	System.out.print(dirPath);
	    	FileOutputStream outer = new FileOutputStream(file);
	    	wb.write(outer);
	    	outer.close();
	    }catch(Exception e){
	    	logger_error.error("����Excelʧ��");
	    	logger_error.error(e);
	    	e.printStackTrace();
	    }
    }

    /**
     * ���ɻ��ֶһ���¼execl�ļ�
     * @param infos
     * @param dirPath
     * @param fileName
     */
    public void produceCPSExchangeExcel(JSONArray infos, String dirPath, String fileName)
    {
    	 AnyFile_Op anyFile_Op = new AnyFile_Op();
    	File dir = anyFile_Op.CreateDir(dirPath);//��������Ŀ¼
		File file = anyFile_Op.CreateFile(dirPath,fileName);//���������ļ�
		
		// ����һ��workbook����һ��Excel��
		HSSFWorkbook wb = new HSSFWorkbook();
		// ��workbook�����һ��sheet����ӦExcel�ļ��е�sheet
		HSSFSheet sheet = wb.createSheet("�ͻ����ֶһ���¼");
		// ���ø�ʽ������
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		
		// ��sheet���½���ͷ
		final String[] headStrings = {"����������", "�û���", "��ʵ����", "΢�ź�", "�ͻ���", "�Ѷһ�����"
						, "�һ�����", "��Ʒ����", "����ʱ��", "���ʱ��", "״̬", "˵��"};
		final int[] columnWidths = {3000, 3000, 3000, 5000, 8000, 3000, 3000, 3000, 5000, 5000, 3000, 3000, 5000};
		HSSFRow row = sheet.createRow(0);
		for(int i = 0; i < headStrings.length; i++){
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(headStrings[i]);
			cell.setCellStyle(style);
			// �����п�
			sheet.setColumnWidth(i, columnWidths[i]);
		}

		for(int i = 0; i < infos.size(); i++){
			row = sheet.createRow(i + 1);
			JSONObject tmp = infos.getJSONObject(i);
			HSSFCell cell = row.createCell(0);
			cell.setCellValue(tmp.getString("agentName"));
			cell = row.createCell(1);
			cell.setCellValue(tmp.getString("username"));
			cell = row.createCell(2);
			cell.setCellValue(tmp.getString("realName"));
			cell = row.createCell(3);
			cell.setCellValue(tmp.getString("weiXin"));
			cell = row.createCell(4);
			cell.setCellValue(tmp.getString("company"));
			cell = row.createCell(5);
			cell.setCellValue(tmp.getString("exchangeScore"));
			cell = row.createCell(6);
			cell.setCellValue(tmp.getString("exchangeType"));
			cell = row.createCell(7);
			cell.setCellValue(tmp.getString("exchangeCategory"));
			cell = row.createCell(8);
			cell.setCellValue(tmp.getString("applicaTime"));
			cell = row.createCell(9);
			cell.setCellValue(tmp.getString("finishTime"));
			cell = row.createCell(10);
			cell.setCellValue(tmp.getString("status"));
			cell = row.createCell(11);
			cell.setCellValue(tmp.getString("description"));
		}
				
	    // ���ļ�����ָ��λ��
	    try{
	    	FileOutputStream outer = new FileOutputStream(file);
	    	wb.write(outer);
	    	outer.close();
	    }catch(Exception e){
	    	logger_error.error("����Excelʧ��");
	    	logger_error.error(e);
	    	e.printStackTrace();
	    }
    }
    
    public List<Gift> getGiftInfos(){
    	return gift_Dao.getInfos();
    }
    /**
     * ������Ʒ��Ϣexecl�ļ�
     * @param infos
     * @param dirPath
     * @param fileName
     */
    public void produceGiftInfoExcel(List<Gift> infos, String dirPath, String fileName)
    {
    	 AnyFile_Op anyFile_Op = new AnyFile_Op();
    	File dir = anyFile_Op.CreateDir(dirPath);//��������Ŀ¼
		File file = anyFile_Op.CreateFile(dirPath,fileName);//���������ļ�
		
		// ����һ��workbook����һ��Excel��
		HSSFWorkbook wb = new HSSFWorkbook();
		// ��workbook�����һ��sheet����ӦExcel�ļ��е�sheet
		HSSFSheet sheet = wb.createSheet("��Ʒ");
		// ���ø�ʽ������
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		
		// ��sheet���½���ͷ
		final String[] headStrings = {"��Ʒ����", "���", "�һ��������"};
		final int[] columnWidths = {5000, 3000, 3000};
		HSSFRow row = sheet.createRow(0);
		for(int i = 0; i < headStrings.length; i++){
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(headStrings[i]);
			cell.setCellStyle(style);
			// �����п�
			sheet.setColumnWidth(i, columnWidths[i]);
		}

		for(int i = 0; i < infos.size(); i++){
			row = sheet.createRow(i + 1);
			Gift gift = infos.get(i);
			HSSFCell cell = row.createCell(0);
			cell.setCellValue(gift.getGift());
			cell = row.createCell(1);
			cell.setCellValue(gift.getStock());
			cell = row.createCell(2);
			cell.setCellValue(gift.getScore());
		}
				
	    // ���ļ�����ָ��λ��
	    try{
	    	FileOutputStream outer = new FileOutputStream(file);
	    	wb.write(outer);
	    	outer.close();
	    }catch(Exception e){
	    	logger_error.error("����Excelʧ��");
	    	logger_error.error(e);
	    	e.printStackTrace();
	    }
    }
    /**
     * �����ϴ���excel
     * @param infos
     * @param type
     * @return
     * @throws IOException
     */
    public  JSONObject uploadInfo(InputStream infos, String type) throws IOException{
    	JSONObject result = new JSONObject();
	    	if(type.equals("gift")){
		    	List<Gift> giftList = handleGiftExcel(infos);
		    	if(giftList == null){
		    		result.element("flag", -1);
		        	result.element("errmsg", "Excel�ļ���ʽ����");
		        	return result;
		    	}
		    	if(giftList.isEmpty()){
		    		result.element("flag", -2);
		        	result.element("errmsg", "Excel�ļ�Ϊ��");
		        	return result;
		    	}
		    	System.out.println(giftList.size());
		    	for(int i = 0; i < giftList.size(); i++){
		    		Gift gift = giftList.get(i);
		    		gift_Dao.update(gift);
		    	}
	    	}
	    	else if(type.equals("logistic")){
		    	List<LogisticInfo> logisticList = handleLogisticExcel(infos);
		    	if(logisticList == null){
		    		result.element("flag", -1);
		        	result.element("errmsg", "Excel�ļ���ʽ����");
		        	return result;
		    	}
		    	if(logisticList.isEmpty()){
		    		result.element("flag", -2);
		        	result.element("errmsg", "Excel�ļ�Ϊ��");
		        	return result;
		    	}
		    	System.out.println(logisticList.size());
		    	for(int i = 0; i < logisticList.size(); i++){
		    		LogisticInfo logisticInfo = logisticList.get(i);
		    		if(logisticInfo_Dao.upload(logisticInfo) > 0){ 		
			    		// ΢����������������Ϣ
		    			ScoreExchangeRecord scoreExchangeRecord = ser_Dao.getInfoByRandKey(logisticInfo.getRandKey());
			    		ConnectPerson person = cPerson_Dao.findById(ConnectPerson.class, scoreExchangeRecord.getUsername());
			    		String openId = person.getWeixinid();
			    		Date date = new Date();
			    		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss");
			    		String deliverTime = sFormat.format(date);
			    	
			    		WeixinPush_Service wp_ser = new WeixinPush_Service();
			    		Push_Template pushMessage = wp_ser.new Push_Template();
			    		pushMessage.Create_DeliverGoods_Template(openId, logisticInfo.getLogisticNumber(), logisticInfo.getRandKey(), deliverTime);
			    		wp_ser.Push_OpSelect(wp_ser.pushoneUrl,WeixinPush_Service.DELIVER_GOODS, pushMessage);
		    		}
		    	}
	    	}
	    	else{
	    		result.element("flag", -3);
	    	    result.element("errmsg", "�ϴ����ʹ���");
	    	    return result;
	    	}
	    
	    result.element("flag", 0);
	    result.element("errmsg", "�ϴ��ɹ�");
	    return result;
    }
    
    private  List<Gift> handleGiftExcel(InputStream infos) throws IOException{
    	HSSFWorkbook wb = new HSSFWorkbook(infos);
    	HSSFSheet sheet = wb.getSheetAt(0);
    	List<Gift> giftList = new ArrayList<>();
    	for(Iterator iterator = sheet.rowIterator(); iterator.hasNext();){
    		HSSFRow row = (HSSFRow)iterator.next();
    		if(row.getRowNum() == 0)
    			continue;
    		Gift gift = new Gift();
    		HSSFCell giftCell = row.getCell(0);
    		if(giftCell.getCellType() != HSSFCell.CELL_TYPE_STRING)
    			return null;
    		gift.setGift(giftCell.getStringCellValue());
    		giftCell = row.getCell(1);
    		if(giftCell.getCellType() != HSSFCell.CELL_TYPE_NUMERIC)
    			return null;
    		gift.setStock((int) giftCell.getNumericCellValue());
    		giftCell = row.getCell(2);
    		if(giftCell.getCellType() != HSSFCell.CELL_TYPE_NUMERIC)
    			return null;
    		gift.setScore((int) giftCell.getNumericCellValue());
    		giftList.add(gift);
    	}
    	return giftList;
    }
    
    private  List<LogisticInfo> handleLogisticExcel(InputStream infos) throws IOException{
    	HSSFWorkbook wb = new HSSFWorkbook(infos);
    	HSSFSheet sheet = wb.getSheetAt(0);
    	List<LogisticInfo> logisticList = new ArrayList<>();
    	for(Iterator iterator = sheet.rowIterator(); iterator.hasNext();){
    		HSSFRow row = (HSSFRow)iterator.next();
    		if(row.getRowNum() == 0)
    			continue;
    		LogisticInfo logisticInfo = new LogisticInfo();
    		HSSFCell logisticCell = row.getCell(0);
    		if(logisticCell.getCellType() != HSSFCell.CELL_TYPE_STRING)
    			return null;
    		logisticInfo.setRandKey(logisticCell.getStringCellValue());
    		logisticCell = row.getCell(1);
    		if(logisticCell.getCellType() != HSSFCell.CELL_TYPE_STRING)
    			return null;
    		logisticInfo.setUser(logisticCell.getStringCellValue());
    		logisticCell = row.getCell(2);
    		if(logisticCell.getCellType() != HSSFCell.CELL_TYPE_STRING)
    			return null;
    		logisticInfo.setPhone(logisticCell.getStringCellValue());
    		logisticCell = row.getCell(3);
    		if(logisticCell.getCellType() != HSSFCell.CELL_TYPE_STRING)
    			return null;
    		logisticInfo.setAddress(logisticCell.getStringCellValue());
    		logisticCell = row.getCell(4);
    		if(logisticCell.getCellType() != HSSFCell.CELL_TYPE_STRING)
    			return null;
    		logisticInfo.setLogisticCompany(logisticCell.getStringCellValue());
    		logisticCell = row.getCell(5);
    		if(logisticCell.getCellType() != HSSFCell.CELL_TYPE_STRING)
    			return null;
    		logisticInfo.setLogisticNumber(logisticCell.getStringCellValue());
    		
    		logisticList.add(logisticInfo);
    	}
    	return logisticList;
    }
    
    public String getGift(int id){
    	Gift gift = gift_Dao.getInfoById(id);
    	if(gift != null)
    		return gift.getGift();
    	else
    		return null;
    }
}
