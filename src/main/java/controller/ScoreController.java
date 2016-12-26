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
 * ScoreController 客户积分查看管理，接收web请求并返回相应数据
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
	
	private static final String descriptionMoney = "兑换现金";
	private static final String descriptionGift = "兑换礼品";
	private static final int PAGESIZE = 10;
		
	/**
	 * 用户提交积分兑换申请
	 * @address: /check_Accout/ScoreController/insert_exrecord
	 * @input : username、exchange_score、exchange_type、serial_number
	 * @output: {flag, errormsg}
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/insert_exrecord")
	public void insertExRecord(HttpServletRequest request, HttpServletResponse response){
		
		logger.info("申请积分兑换");
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
			logger.info("接口insertExRecord receive" + request_s_de);
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
				re_jsonobject.element("errmsg", "当前拥有积分少于所申请积分");
				logger_error.error("当前拥有积分少于所申请积分");
				Common_return_en(response,re_jsonobject);
			}
			ScoreExchangeRecord scoreExchangeRecord = new ScoreExchangeRecord(username,
					exchangeScore, exchangeType, status, applicaTime, randKey, description);
			int result = scoreManage.insertExchangeRecord(scoreExchangeRecord);
			if(result != 0 ){
				re_jsonobject.element("flag", -1);
				re_jsonobject.element("errmsg", "库存不足，请选择其他礼物");
			}
			else{			
				re_jsonobject.element("flag", 0);
				re_jsonobject.element("errmsg", "兑换申请提交成功");
			}
			Common_return_en(response,re_jsonobject);
		}catch(Exception e){
			logger_error.error("获取提交参数失败" + e);
			logger_error.error(e);
			e.printStackTrace();
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "获取提交参数失败");
			Common_return_en(response,re_jsonobject);
		}
	}
	
	/**
	 * 超级管理员获取用户积分信息
	 * @address: /check_Accout/ScoreController/all_scoreinfos
	 * @input : pagenum 页码
	 * @output: {data:[{"agent","agentName","cardId","company","companyId","email",
	 * 				"exchangedScore","exchangingScore","phone","realName", 
	 * 				"registerWay""score", "username" ,"weiXin"}],totalpage, flag, errormsg}
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping(value="/all_scoreinfos")
	public void getScoreInfos(HttpServletRequest request, HttpServletResponse response){
		logger.info("超级管理员查看所有用户的积分情况");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bm")){
			logger_error.error("该用户不是超级管理员");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "权限错误");
			Common_return_en(response,re_jsonobject);
		}
		String request_s;
		String request_s_de;
		int pagenum = 0;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//解密数据
			logger.info("接口all_scoreinfos received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			pagenum = jstr.getInt("pagenum");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("参数解析失败");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "参数解析失败");
			Common_return_en(response,re_jsonobject);
		}
		int offset = (pagenum - 1) * PAGESIZE;
		
		re_jsonobject = scoreManage.getScoreAllInfo(offset, PAGESIZE);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "查看成功");
		
		Common_return_en(response, re_jsonobject);
			
	}
	/**
	 * 代理商财务获取信息
	 * @address: /check_Accout/ScoreController/agent_scoreinfos
	 * @input : pagenum 页码
	 * @output: {data:[{"agentName","cardId","company","email","exchangedScore","status"
	 * "exchangingScore","phone","realName", "registerWay", "score", "username" ,"weiXin"}],totalpage , flag, errormsg}
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping(value="/agent_scoreinfos")
	public void getScoreInfosByAgent(HttpServletRequest request, HttpServletResponse response){
		logger.info("代理商财务查看对应用户的积分情况");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		String agentId = session.getAttribute("agentid").toString();
		if(!usertype.equals("bu")){
			logger_error.error("该用户不是代理商财务");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "权限错误");
			Common_return_en(response,re_jsonobject);
		}
		String request_s;
		String request_s_de;
		int pagenum = 0;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//解密数据
			logger.info("接口agent_scoreinfos received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			pagenum = jstr.getInt("pagenum");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("参数解析失败");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "参数解析失败");
			Common_return_en(response,re_jsonobject);
		}
		int offset = (pagenum - 1) * PAGESIZE;
		
		re_jsonobject = scoreManage.getScoreInfoByAgent(agentId, offset, PAGESIZE);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "查看成功");
		
		Common_return_en(response, re_jsonobject);
			
	}
	/**
	 * 下载用户积分信息报表
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
		String dirPath =  request.getServletContext().getRealPath("/报表中心/scoreinfo");
		String fileName = "客户积分报表.xlsx";
		if(usertype.equals("bm")){
			logger.info("客户积分报表下载");
			List<ConnectPersonScoreInfo> infos = scoreManage.getScoreAllInfo();
			scoreManage.produceCPScoreExcel(infos, dirPath, fileName);
		}else if(usertype.equals("bu")){
			logger.info("客户积分报表下载");
			fileName = agentId + "_客户积分报表.xlsx";
			List<ConnectPersonScoreInfo> infos = scoreManage.getScoreInfoByAgent(agentId);
			scoreManage.produceCPScoreExcel(infos, dirPath, fileName);
		}else{
			logger_error.error("用户身份错误");
		}
		String urlString = "/check_Accout/报表中心/scoreinfo" + "/" + fileName;
		JSONObject returnJsonObject = new JSONObject();
		returnJsonObject.element("url", urlString);
		returnJsonObject.element("flag", 0);
		returnJsonObject.element("errmsg", "生成报表成功");
		
		Common_return_en(response, returnJsonObject);
	}

	/**
	 * 查看用户积分详情入口
	 * @address: /check_Accout/ScoreController/score_records
	 * @input: username  客户username
	 * @output: {data:[{time, change, status, hander, serial, description}], totalapge, flag, errmsg}
	 * @param request
	 * @param response
	 */
	@RequestMapping (value="/score_records")
	public void getScoreRecords(HttpServletRequest request, HttpServletResponse response){
		logger.info("查看用户的积分详情");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bu") && !usertype.equals("bm")){
			logger_error.error("该用户没有权限查看");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "权限错误");
			Common_return_en(response,re_jsonobject);
		}
		String request_s;
		String request_s_de;
		String username = "";
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//解密数据
			logger.info("接口score_records received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			username = jstr.getString("username");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("参数解析失败");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "参数解析失败");
			Common_return_en(response,re_jsonobject);
		}
		
		re_jsonobject = scoreManage.getScoreRecord(username, 1);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "查看成功");
		
		Common_return_en(response, re_jsonobject);
	}
	
	/**
	 * 超级管理员管理积分兑换
	 * @address: /check_Accout/ScoreController/manage_exchange
	 * @input : pagenum 页码
	 * @output: {data:[{"agentName","username","realName","weiXin","company","exchangeScore",
	 * "exchangeType","exchangeCategory","applicaTime","finishTime","randKey","hander","status","description"}], totalpage, flag, errormsg}
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping(value="/manage_exchange")
	public void ManageExchange(HttpServletRequest request, HttpServletResponse response){
		logger.info("超级管理员管理所有用户的积分兑换");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bm")){
			logger_error.error("该用户不是超级管理员");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "权限错误");
			Common_return_en(response,re_jsonobject);
		}
		String request_s;
		String request_s_de;
		int pagenum = 0;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//解密数据
			logger.info("接口manage_exchange received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			pagenum = jstr.getInt("pagenum");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("参数解析失败");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "参数解析失败");
			Common_return_en(response,re_jsonobject);
		}
		int offset = (pagenum - 1) * PAGESIZE;
		
		re_jsonobject = scoreManage.getExchangeInfos(offset, PAGESIZE);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "查看成功");
		
		Common_return_en(response, re_jsonobject);
			
	}
	/**
	 * 代理商财务查看用户的兑换情况
	 * @address: /check_Accout/ScoreController/agent_exchangeinfos
	 * @input : pagenum 页码
	 * @output: { {data:[{"agentName","username","realName","weiXin","company","exchangeScore",
	 * "exchangeType","exchangeCategory","applicaTime","finishTime","randKey", "hander","status","description"}], totalpage,flag, errormsg}
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping(value="/agent_exchangeinfos")
	public void getExchangeInfosByAgent(HttpServletRequest request, HttpServletResponse response){
		logger.info("代理商财务查看对应用户的积分兑换情况");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		String agentId = session.getAttribute("agentid").toString();
		if(!usertype.equals("bu")){
			logger_error.error("该用户不是代理商财务");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "权限错误");
			Common_return_en(response,re_jsonobject);
		}
		String request_s;
		String request_s_de;
		int pagenum = 0;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//解密数据
			logger.info("接口agent_exchangeinfos received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			pagenum = jstr.getInt("pagenum");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("参数解析失败");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "参数解析失败");
			Common_return_en(response,re_jsonobject);
		}
		int offset = (pagenum - 1) * PAGESIZE;
		
		re_jsonobject = scoreManage.getExchangeInfosByAgentId(agentId, offset, PAGESIZE);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "查看成功");
		
		Common_return_en(response, re_jsonobject);
	}
	
	/**
	 * 下载用户积分兑换情况报表
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
		String dirPath =  request.getServletContext().getRealPath("/报表中心/exchanginfo");
		String fileName = "";
		if(usertype.equals("bm")){
			logger.info("客户积分兑换报表下载");
			JSONObject infos = scoreManage.getExchangeInfos();
			fileName = "客户积分兑换报表.xlsx";
			scoreManage.produceCPSExchangeExcel(infos.getJSONArray("data"), dirPath, fileName);
		}else if(usertype.equals("bu")){
			logger.info("客户积分兑换报表下载");
			JSONObject infos = scoreManage.getExchangeInfosByAgentId(agentId);
			fileName = agentId + "_客户积分兑换报表.xlsx";
			scoreManage.produceCPSExchangeExcel(infos.getJSONArray("data"), dirPath, fileName);
		}else{
			logger_error.error("用户身份错误");
		}
		String urlString = "/check_Accout/报表中心/exchanginfo" + "/" + fileName;
		JSONObject returnJsonObject = new JSONObject();
		returnJsonObject.element("url", urlString);
		returnJsonObject.element("flag", 0);
		returnJsonObject.element("errmsg", "生成报表成功");
		
		Common_return_en(response, returnJsonObject);
	}
	/**
	 * 超级管理员批准礼品兑换
	 * @address: /check_Accout/ScoreController/approval_exchange
	 * @input : randKey 流水号
	 * @output: {flag, errormsg}
	 * @param request
	 * @param response
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping(value="/approval_exchange")
	public void approvalExchange(HttpServletRequest request, HttpServletResponse response){
		logger.info("超级管理员批准积分兑换");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bm")){
			logger_error.error("该用户没有权限批准积分兑换");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "权限错误");
			Common_return_en(response,re_jsonobject);
		}
		String workId = session.getAttribute("workId").toString();
		String request_s;
		String request_s_de;
		String randKey = null;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//解密数据
			logger.info("接口approval_exchange received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			randKey = jstr.getString("randKey");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("参数解析失败");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "参数解析失败");
			Common_return_en(response,re_jsonobject);
		}
		scoreManage.updateExchangeStatus(randKey,(byte)1, workId);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "批准成功");
		
		Common_return_en(response, re_jsonobject);
	}
    /**
     * Common_return_en 不带具体信息的加密返回
     * @param response
     * @param re_json 操作结果及信息
     * @author zhangxinming
     */
    private void Common_return_en(HttpServletResponse response,JSONObject re_json){
		response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//传递参数中的最外层对象
		
    	String en_s = null;
		/*传递json数据给前台*/
		logger.info(re_object.toString());
		try {
			Writer writer = response.getWriter();
			en_s = AES.aesEncrypt(re_object.toString(), AES.key);
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*传递json数据给前台*/
    }
    /**
     * 礼物列表上传接口
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
    	logger.info("上传礼品信息");
    	HttpSession session = request.getSession();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bm")){
			logger_error.error("该用户不是超级管理员，无权上传礼品信息");
			JSONObject re_jsonobject = new JSONObject();
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "权限错误");
			Common_return_en(response,re_jsonobject);
		}
    	InputStream infos = mfile.getInputStream();
    	JSONObject returnObject = scoreManage.uploadInfo(infos, "gift");
    	Common_return_en(response, returnObject);
    }
    /**
     * 上传物流信息接口
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
    	logger.info("上传物流信息");
    	HttpSession session = request.getSession();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bm")){
			logger_error.error("该用户不是超级管理员，无权上传物流信息");
			JSONObject re_jsonobject = new JSONObject();
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "权限错误");
			Common_return_en(response,re_jsonobject);
		}
    	InputStream infos = mfile.getInputStream();
    	JSONObject returnObject = scoreManage.uploadInfo(infos, "logistic");
    	Common_return_en(response, returnObject);
    }
    
	/**
	 * 查看物流详情入口
	 * @address: /check_Accout/ScoreController/logistic_info
	 * @input: randkey  流水号
	 * @output: {data:{user, phone, address, logisticCompany, logisticNumber}, flag, errmsg}
	 * @param request
	 * @param response
	 */
	@RequestMapping (value="/logistic_info")
	public void getLogisticInfo(HttpServletRequest request, HttpServletResponse response){
		logger.info("查看礼品兑换物流详情");
		HttpSession session = request.getSession(false);
		JSONObject re_jsonobject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bu") && !usertype.equals("bm")){
			logger_error.error("该用户没有权限查看");
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "权限错误");
			Common_return_en(response,re_jsonobject);
		}
		String request_s;
		String request_s_de;
		String randKey = "";
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//解密数据
			logger.info("接口logistic_info received content:" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			randKey = jstr.getString("randkey");
		} catch (Exception e) {
			e.printStackTrace();
			logger_error.error("参数解析失败");
			logger_error.error(e);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "参数解析失败");
			Common_return_en(response,re_jsonobject);
		}
		
		re_jsonobject = scoreManage.getLogisticInfo(randKey);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "查看成功");
		Common_return_en(response, re_jsonobject);
	}
	
	/**
	 * 下载礼品信息报表
	 * @address: /check_Accout/ScoreController/download_giftinfo
	 * @input: null
	 * @output: {url, flag, errmsg}
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/download_giftinfo")
	public void createGiftInfoExcel(HttpServletRequest request, HttpServletResponse response){
		logger.info("礼品信息报表下载");
		HttpSession session = request.getSession();
		JSONObject returnJsonObject = new JSONObject();
		String usertype = session.getAttribute("usertype").toString();
		if(!usertype.equals("bm")){
			logger_error.error("用户身份错误");
			returnJsonObject.element("flag", -1);
			returnJsonObject.element("errmsg", "权限不够");
			Common_return_en(response, returnJsonObject);
		}
		String dirPath =  request.getServletContext().getRealPath("/报表中心");
		String fileName = "礼品信息.xlsx";
		List<Gift> infos = scoreManage.getGiftInfos();
		scoreManage.produceGiftInfoExcel(infos, dirPath, fileName);
		String urlString = "/check_Accout/报表中心" + "/" + fileName;
		
		returnJsonObject.element("url", urlString);
		returnJsonObject.element("flag", 0);
		returnJsonObject.element("errmsg", "生成报表成功");
		
		Common_return_en(response, returnJsonObject);
	}
}


