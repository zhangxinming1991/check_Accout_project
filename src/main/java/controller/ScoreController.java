package controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import net.sf.json.JSONObject;
import check_Asys.ScoreManage;
import random_create.RandomCreate;
import encrypt_decrpt.AES;
import entity.ConnectPersonScoreInfo;
import entity.Gift;
import entity.ScoreExchangeRecord;


/**
 * ScoreController �ͻ����ֲ鿴��������web���󲢷�����Ӧ����
 * @author LinLi
 * @modify LinLi
 * @version 1.1.0
 */
@Controller
public class ScoreController {
	
	private static Logger logger = LogManager.getLogger(ScoreController.class);
	private static Logger logger_error = LogManager.getLogger("error");
	public final static SessionFactory wFactory = new Configuration().configure().buildSessionFactory();
	public  final static ScoreManage scoreManage = new ScoreManage(wFactory);
	private static AES ase = new AES();
	
	private static final String descriptionMoney = "�һ��ֽ�";
	private static final String descriptionGift = "�һ���Ʒ";
	private static final int PAGESIZE = 10;
		
	/**
	 * �û��ύ���ֶһ�����
	 * @address: /check_Accout/ScoreController/insert_exrecord
	 * @input : username��exchange_score��exchange_type��serial_number
	 * @output: {flag, errormsg}
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/insert_exrecord")
	public void insertExRecord(HttpServletRequest request, HttpServletResponse response){
		
		logger.info("������ֶһ�");
		response.addHeader("Access-Control-Allow-Origin", "*");	
		String username;
		int exchangeScore;
		byte exchangeType;
		byte status = 0;
		Timestamp applicaTime;
		String randKey;
		String serialId;
		String description;
		JSONObject re_jsonobject = new JSONObject();
		try {
			@SuppressWarnings("deprecation")
			String request_s = IOUtils.toString(request.getInputStream());
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("�ӿ�insertExRecord receive" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			username = jstr.getString("username");
			exchangeScore = jstr.getInt("exchange_score");
			exchangeType = (byte) jstr.getInt("exchange_type");
			serialId = jstr.getString("serial_number");
			applicaTime = new Timestamp(System.currentTimeMillis());
			randKey = RandomCreate.createRandomString(18);
			if(exchangeType == 0)
				description = descriptionMoney;
			else
				description = descriptionGift;
			int score = scoreManage.getCurrentScoreByUsername(username);
			if(score < exchangeScore){
				re_jsonobject.element("flag", -2);
				re_jsonobject.element("errmsg", "��ǰӵ�л����������������");
				logger_error.error("��ǰӵ�л����������������");
				Common_return_en(response,re_jsonobject);
			}
			ScoreExchangeRecord scoreExchangeRecord = new ScoreExchangeRecord(username,
					exchangeScore, exchangeType, status, applicaTime, randKey, description);
			int result = scoreManage.insertExchangeRecord(scoreExchangeRecord);
			if(result != 0 ){
				re_jsonobject.element("flag", -1);
				re_jsonobject.element("errmsg", "��治�㣬��ѡ����������");
			}
			else{			
				re_jsonobject.element("flag", 0);
				re_jsonobject.element("errmsg", "�һ������ύ�ɹ�");
			}
			Common_return_en(response,re_jsonobject);
		}catch(Exception e){
			logger_error.error("��ȡ�ύ����ʧ��" + e);
			logger_error.error(e);
			e.printStackTrace();
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��ȡ�ύ����ʧ��");
			Common_return_en(response,re_jsonobject);
		}
	}
	
	/**
	 * ��������Ա��ȡ�û�������Ϣ
	 * @address: /check_Accout/ScoreController/all_scoreinfos
	 * @input : pagenum ҳ��
	 * @output: {data:[{"agent","agentName","cardId","company","companyId","email",
	 * 				"exchangedScore","exchangingScore","phone","realName", 
	 * 				"registerWay""score", "username" ,"weiXin"}],totalpage, flag, errormsg}
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping(value="/all_scoreinfos")
	public void getScoreInfos(HttpServletRequest request, HttpServletResponse response){
		logger.info("��������Ա�鿴�����û��Ļ������");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bm")){
			logger_error.error("���û����ǳ�������Ա");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "Ȩ�޴���");
			Common_return_en(response,re_jsonobject);
		}
		String request_s;
		String request_s_de;
		int pagenum = 0;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//��������
			logger.info("�ӿ�all_scoreinfos received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			pagenum = jstr.getInt("pagenum");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("��������ʧ��");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��������ʧ��");
			Common_return_en(response,re_jsonobject);
		}
		int offset = (pagenum - 1) * PAGESIZE;
		
		re_jsonobject = scoreManage.getScoreAllInfo(offset, PAGESIZE);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "�鿴�ɹ�");
		
		Common_return_en(response, re_jsonobject);
			
	}
	/**
	 * �����̲����ȡ��Ϣ
	 * @address: /check_Accout/ScoreController/agent_scoreinfos
	 * @input : pagenum ҳ��
	 * @output: {data:[{"agentName","cardId","company","email","exchangedScore","status"
	 * "exchangingScore","phone","realName", "registerWay", "score", "username" ,"weiXin"}],totalpage , flag, errormsg}
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping(value="/agent_scoreinfos")
	public void getScoreInfosByAgent(HttpServletRequest request, HttpServletResponse response){
		logger.info("�����̲���鿴��Ӧ�û��Ļ������");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		String agentId = session.getAttribute("agentid").toString();
		if(!usertype.equals("bu")){
			logger_error.error("���û����Ǵ����̲���");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "Ȩ�޴���");
			Common_return_en(response,re_jsonobject);
		}
		String request_s;
		String request_s_de;
		int pagenum = 0;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//��������
			logger.info("�ӿ�agent_scoreinfos received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			pagenum = jstr.getInt("pagenum");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("��������ʧ��");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��������ʧ��");
			Common_return_en(response,re_jsonobject);
		}
		int offset = (pagenum - 1) * PAGESIZE;
		
		re_jsonobject = scoreManage.getScoreInfoByAgent(agentId, offset, PAGESIZE);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "�鿴�ɹ�");
		
		Common_return_en(response, re_jsonobject);
			
	}
	/**
	 * �����û�������Ϣ����
	 * @address: /check_Accout/ScoreController/download_scoreinfo
	 * @input: null
	 * @output: {url, flag, errmsg}
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/download_scoreinfo")
	public void createScoreInfoExcel(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		String usertype = session.getAttribute("usertype").toString();
		String agentId = session.getAttribute("agentid").toString();
		String dirPath =  request.getServletContext().getRealPath("/��������/scoreinfo");
		String fileName = "�ͻ����ֱ���.xlsx";
		if(usertype.equals("bm")){
			logger.info("�ͻ����ֱ�������");
			List<ConnectPersonScoreInfo> infos = scoreManage.getScoreAllInfo();
			scoreManage.produceCPScoreExcel(infos, dirPath, fileName);
		}else if(usertype.equals("bu")){
			logger.info("�ͻ����ֱ�������");
			fileName = agentId + "_�ͻ����ֱ���.xlsx";
			List<ConnectPersonScoreInfo> infos = scoreManage.getScoreInfoByAgent(agentId);
			scoreManage.produceCPScoreExcel(infos, dirPath, fileName);
		}else{
			logger_error.error("�û���ݴ���");
		}
		String urlString = "/check_Accout/��������/scoreinfo" + "/" + fileName;
		JSONObject returnJsonObject = new JSONObject();
		returnJsonObject.element("url", urlString);
		returnJsonObject.element("flag", 0);
		returnJsonObject.element("errmsg", "���ɱ���ɹ�");
		
		Common_return_en(response, returnJsonObject);
	}

	/**
	 * �鿴�û������������
	 * @address: /check_Accout/ScoreController/score_records
	 * @input: username  �ͻ�username
	 * @output: {data:[{time, change, status, hander, serial, description}], totalapge, flag, errmsg}
	 * @param request
	 * @param response
	 */
	@RequestMapping (value="/score_records")
	public void getScoreRecords(HttpServletRequest request, HttpServletResponse response){
		logger.info("�鿴�û��Ļ�������");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bu") && !usertype.equals("bm")){
			logger_error.error("���û�û��Ȩ�޲鿴");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "Ȩ�޴���");
			Common_return_en(response,re_jsonobject);
		}
		String request_s;
		String request_s_de;
		String username = "";
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//��������
			logger.info("�ӿ�score_records received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			username = jstr.getString("username");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("��������ʧ��");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��������ʧ��");
			Common_return_en(response,re_jsonobject);
		}
		
		re_jsonobject = scoreManage.getScoreRecord(username, 1);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "�鿴�ɹ�");
		
		Common_return_en(response, re_jsonobject);
	}
	
	/**
	 * ��������Ա������ֶһ�
	 * @address: /check_Accout/ScoreController/manage_exchange
	 * @input : pagenum ҳ��
	 * @output: {data:[{"agentName","username","realName","weiXin","company","exchangeScore",
	 * "exchangeType","exchangeCategory","applicaTime","finishTime","randKey","hander","status","description"}], totalpage, flag, errormsg}
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping(value="/manage_exchange")
	public void ManageExchange(HttpServletRequest request, HttpServletResponse response){
		logger.info("��������Ա���������û��Ļ��ֶһ�");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bm")){
			logger_error.error("���û����ǳ�������Ա");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "Ȩ�޴���");
			Common_return_en(response,re_jsonobject);
		}
		String request_s;
		String request_s_de;
		int pagenum = 0;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//��������
			logger.info("�ӿ�manage_exchange received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			pagenum = jstr.getInt("pagenum");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("��������ʧ��");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��������ʧ��");
			Common_return_en(response,re_jsonobject);
		}
		int offset = (pagenum - 1) * PAGESIZE;
		
		re_jsonobject = scoreManage.getExchangeInfos(offset, PAGESIZE);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "�鿴�ɹ�");
		
		Common_return_en(response, re_jsonobject);
			
	}
	/**
	 * �����̲���鿴�û��Ķһ����
	 * @address: /check_Accout/ScoreController/agent_exchangeinfos
	 * @input : pagenum ҳ��
	 * @output: { {data:[{"agentName","username","realName","weiXin","company","exchangeScore",
	 * "exchangeType","exchangeCategory","applicaTime","finishTime","randKey", "hander","status","description"}], totalpage,flag, errormsg}
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping(value="/agent_exchangeinfos")
	public void getExchangeInfosByAgent(HttpServletRequest request, HttpServletResponse response){
		logger.info("�����̲���鿴��Ӧ�û��Ļ��ֶһ����");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		String agentId = session.getAttribute("agentid").toString();
		if(!usertype.equals("bu")){
			logger_error.error("���û����Ǵ����̲���");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "Ȩ�޴���");
			Common_return_en(response,re_jsonobject);
		}
		String request_s;
		String request_s_de;
		int pagenum = 0;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//��������
			logger.info("�ӿ�agent_exchangeinfos received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			pagenum = jstr.getInt("pagenum");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("��������ʧ��");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��������ʧ��");
			Common_return_en(response,re_jsonobject);
		}
		int offset = (pagenum - 1) * PAGESIZE;
		
		re_jsonobject = scoreManage.getExchangeInfosByAgentId(agentId, offset, PAGESIZE);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "�鿴�ɹ�");
		
		Common_return_en(response, re_jsonobject);
	}
	
	/**
	 * �����û����ֶһ��������
	 * @address: /check_Accout/ScoreController/download_exchangeinfo
	 * @input: null
	 * @output: {url, flag, errmsg}
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/download_exchangeinfo")
	public void createExchangeInfoExcel(HttpServletRequest request, HttpServletResponse response){
		HttpSession session = request.getSession();
		 String usertype = session.getAttribute("usertype").toString();
		String agentId = session.getAttribute("agentid").toString();
		String dirPath =  request.getServletContext().getRealPath("/��������/exchanginfo");
		String fileName = "";
		if(usertype.equals("bm")){
			logger.info("�ͻ����ֶһ���������");
			JSONObject infos = scoreManage.getExchangeInfos();
			fileName = "�ͻ����ֶһ�����.xlsx";
			scoreManage.produceCPSExchangeExcel(infos.getJSONArray("data"), dirPath, fileName);
		}else if(usertype.equals("bu")){
			logger.info("�ͻ����ֶһ���������");
			JSONObject infos = scoreManage.getExchangeInfosByAgentId(agentId);
			fileName = agentId + "_�ͻ����ֶһ�����.xlsx";
			scoreManage.produceCPSExchangeExcel(infos.getJSONArray("data"), dirPath, fileName);
		}else{
			logger_error.error("�û���ݴ���");
		}
		String urlString = "/check_Accout/��������/exchanginfo" + "/" + fileName;
		JSONObject returnJsonObject = new JSONObject();
		returnJsonObject.element("url", urlString);
		returnJsonObject.element("flag", 0);
		returnJsonObject.element("errmsg", "���ɱ���ɹ�");
		
		Common_return_en(response, returnJsonObject);
	}
	/**
	 * ��������Ա��׼��Ʒ�һ�
	 * @address: /check_Accout/ScoreController/approval_exchange
	 * @input : randKey ��ˮ��
	 * @output: {flag, errormsg}
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping(value="/approval_exchange")
	public void approvalExchange(HttpServletRequest request, HttpServletResponse response){
		logger.info("��������Ա��׼���ֶһ�");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bm")){
			logger_error.error("���û�û��Ȩ����׼���ֶһ�");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "Ȩ�޴���");
			Common_return_en(response,re_jsonobject);
		}
		String workId = session.getAttribute("workId").toString();
		String request_s;
		String request_s_de;
		String randKey = null;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//��������
			logger.info("�ӿ�approval_exchange received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			randKey = jstr.getString("randKey");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("��������ʧ��");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��������ʧ��");
			Common_return_en(response,re_jsonobject);
		}
		scoreManage.updateExchangeStatus(randKey,(byte)1, workId);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "��׼�ɹ�");
		
		Common_return_en(response, re_jsonobject);
	}
    /**
     * Common_return_en ����������Ϣ�ļ��ܷ���
     * @param response
     * @param re_json �����������Ϣ
     * @author zhangxinming
     */
    private void Common_return_en(HttpServletResponse response,JSONObject re_json){
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
    /**
     * �����б��ϴ��ӿ�
     * @address:  /check_Accout/ScoreController/upload_gift
     * @input : MultipartFile
     * @output: {flag, errmsg}
     * @param request
     * @param mfileA
     * @param response
     * @throws IOException
     */
    @RequestMapping(value="/upload_gift")
    public void uploadGift(HttpServletRequest request, @RequestParam("file") CommonsMultipartFile mfile,HttpServletResponse response) throws IOException{
    	logger.info("�ϴ���Ʒ��Ϣ");
    	HttpSession session = request.getSession();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bm")){
			logger_error.error("���û����ǳ�������Ա����Ȩ�ϴ���Ʒ��Ϣ");
			JSONObject re_jsonobject = new JSONObject();
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "Ȩ�޴���");
			Common_return_en(response,re_jsonobject);
		}
    	InputStream infos = mfile.getInputStream();
    	JSONObject returnObject = scoreManage.uploadInfo(infos, "gift");
    	Common_return_en(response, returnObject);
    }
    /**
     * �ϴ�������Ϣ�ӿ�
     * @address:  /check_Accout/ScoreController/upload_logistic
     * @input: MultipartFile
     * @output: {flag, errmsg}
     * @param request
     * @param mfileA
     * @param response
     * @throws IOException
     */
    @RequestMapping(value="/upload_logistic")
    public void uploadLogistic(HttpServletRequest request,@RequestParam("file") MultipartFile mfile,HttpServletResponse response) throws IOException{
    	logger.info("�ϴ�������Ϣ");
    	HttpSession session = request.getSession();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bm")){
			logger_error.error("���û����ǳ�������Ա����Ȩ�ϴ�������Ϣ");
			JSONObject re_jsonobject = new JSONObject();
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "Ȩ�޴���");
			Common_return_en(response,re_jsonobject);
		}
    	InputStream infos = mfile.getInputStream();
    	JSONObject returnObject = scoreManage.uploadInfo(infos, "logistic");
    	Common_return_en(response, returnObject);
    }
    
	/**
	 * �鿴�����������
	 * @address: /check_Accout/ScoreController/logistic_info
	 * @input: randkey  ��ˮ��
	 * @output: {data:{user, phone, address, logisticCompany, logisticNumber}, flag, errmsg}
	 * @param request
	 * @param response
	 */
	@RequestMapping (value="/logistic_info")
	public void getLogisticInfo(HttpServletRequest request, HttpServletResponse response){
		logger.info("�鿴��Ʒ�һ���������");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bu") && !usertype.equals("bm")){
			logger_error.error("���û�û��Ȩ�޲鿴");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "Ȩ�޴���");
			Common_return_en(response,re_jsonobject);
		}
		String request_s;
		String request_s_de;
		String randKey = "";
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//��������
			logger.info("�ӿ�logistic_info received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			randKey = jstr.getString("randkey");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("��������ʧ��");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��������ʧ��");
			Common_return_en(response,re_jsonobject);
		}
		
		re_jsonobject = scoreManage.getLogisticInfo(randKey);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "�鿴�ɹ�");
		Common_return_en(response, re_jsonobject);
	}
	
	/**
	 * ������Ʒ��Ϣ����
	 * @address: /check_Accout/ScoreController/download_giftinfo
	 * @input: null
	 * @output: {url, flag, errmsg}
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/download_giftinfo")
	public void createGiftInfoExcel(HttpServletRequest request, HttpServletResponse response){
		logger.info("��Ʒ��Ϣ��������");
		HttpSession session = request.getSession();
		JSONObject returnJsonObject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bm")){
			logger_error.error("�û���ݴ���");
			returnJsonObject.element("flag", -1);
			returnJsonObject.element("errmsg", "Ȩ�޲���");
			Common_return_en(response, returnJsonObject);
		}
		String dirPath =  request.getServletContext().getRealPath("/��������");
		String fileName = "��Ʒ��Ϣ.xlsx";
		List<Gift> infos = scoreManage.getGiftInfos();
		scoreManage.produceGiftInfoExcel(infos, dirPath, fileName);
		String urlString = "/check_Accout/��������" + "/" + fileName;
		
		returnJsonObject.element("url", urlString);
		returnJsonObject.element("flag", 0);
		returnJsonObject.element("errmsg", "���ɱ���ɹ�");
		
		Common_return_en(response, returnJsonObject);
	}
}


