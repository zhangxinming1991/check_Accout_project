package controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.List;

import javafx.scene.control.Cell;

import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.startup.WebAnnotationSet;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sun.rowset.internal.Row;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import check_Asys.ScoreManage;
import random_create.RandomCreate;
import encrypt_decrpt.AES;
import entity.ConnectPersonScoreInfo;
import entity.ScoreExchangeRecord;
import file_op.AnyFile_Op;


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
	 * @input : username、exchange_score、exchange_type
	 * @output: {flag, errormsg}
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/insert_exrecord")
	public void insertExRecord(HttpServletRequest request, HttpServletResponse response){
		
		logger.info("申请积分兑换");
		String username;
		int exchangeScore;
		byte exchangeType;
		byte status = 0;
		Timestamp applicaTime;
		String randKey;
		String description;
		JSONObject re_jsonobject = new JSONObject();
		try {
			@SuppressWarnings("deprecation")
			String request_s = IOUtils.toString(request.getInputStream());
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("接口insert_exrecord receive" + request_s_de);
			JSONObject jstr = JSONObject.fromObject(request_s_de);
			username = jstr.getString("username");
			exchangeScore = jstr.getInt("exchange_score");
			exchangeType = (byte) jstr.getInt("exchange_type");
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
			ScoreExchangeRecord scoreExchangeRecord = new ScoreExchangeRecord(username,exchangeScore, exchangeType, status, applicaTime, randKey, description);
			scoreManage.insertExchangeRecord(scoreExchangeRecord);
			re_jsonobject.element("flag", 0);
			re_jsonobject.element("errmsg", "兑换申请提交成功");
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
		// String usertype = session.getAttribute("usertype").toString();
		// String agentId = session.
		String usertype = "bm";
		String agentId = "gd0001";
		String dirPath =  request.getServletContext().getRealPath("/报表中心/scoreinfo");
		String fileName = "客户积分报表.xlsx";
		if(usertype.equals("bm")){
			logger.info("客户积分报表下载");
			List<ConnectPersonScoreInfo> infos = scoreManage.getScoreAllInfo();
			produceCPScoreExcel(infos, dirPath, fileName);
		}else if(usertype.equals("bu")){
			logger.info("客户积分报表下载");
			fileName = agentId + "_客户积分报表";
			List<ConnectPersonScoreInfo> infos = scoreManage.getScoreInfoByAgent(agentId);
			produceCPScoreExcel(infos, dirPath, fileName);
		}else{
			logger_error.error("用户身份错误");
		}
		String urlString = "/check_Accout/scoreinfo" + "/" + fileName;
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
	@RequestMapping (value="/score_reocrds")
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
			logger.info("接口all_scoreinfos received content:" + request_s_de);
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
	 * "exchangeType","exchangeCategory","applicaTime","finishTime","randKey","status","description"}], totalpage, flag, errormsg}
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
	 * "exchangeType","exchangeCategory","applicaTime","finishTime","randKey", "status","description"}], totalpage,flag, errormsg}
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
		// String usertype = session.getAttribute("usertype").toString();
		//String agentId = session.getAttribute("agentid").toString();
		String usertype = "bm";
		String agentId = "gd0001";
		String dirPath =  request.getServletContext().getRealPath("/报表中心/exchanginfo");;
		String fileName = "";
		if(usertype.equals("bm")){
			logger.info("客户积分兑换报表下载");
			JSONObject infos = scoreManage.getExchangeInfos();
			fileName = "客户积分兑换报表.xlsx";
			produceCPSExchangeExcel(infos.getJSONArray("data"), dirPath, fileName);
		}else if(usertype.equals("bu")){
			logger.info("客户积分兑换报表下载");
			JSONObject infos = scoreManage.getExchangeInfosByAgentId(agentId);
			fileName = agentId + "_客户积分兑换报表";
			produceCPSExchangeExcel(infos.getJSONArray("data"), dirPath, fileName);
		}else{
			logger_error.error("用户身份错误");
		}
		String urlString = "/check_Accout/scoreinfo" + "/" + fileName;
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
		String request_s;
		String request_s_de;
		String randKey = null;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			request_s_de = AES.aesDecrypt(request_s, AES.key);	//解密数据
			logger.info("接口all_scoreinfos received content:" + request_s_de);
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
		scoreManage.updateExchangeStatus(randKey);
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
     * 生成用户积分execl文件
     * @param infos
     * @param dirPath
     * @param fileName
     */
    private void produceCPScoreExcel(List<ConnectPersonScoreInfo> infos, String dirPath, String fileName)
    {
    	 AnyFile_Op anyFile_Op = new AnyFile_Op();
    	File dir = anyFile_Op.CreateDir(dirPath);//创建保存目录
		File file = anyFile_Op.CreateFile(dirPath,fileName);//创建保存文件
		
		// 创建一个workbook，即一个Excel表
		HSSFWorkbook wb = new HSSFWorkbook();
		// 在workbook中添加一个sheet，对应Excel文件中的sheet
		HSSFSheet sheet = wb.createSheet("客户积分信息");
		// 设置格式：居中
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		
		// 在sheet中新建表头
		final String[] headStrings = {"所属代理商", "用户名", "真实姓名", "微信号", "客户名", "当前积分", "已兑换积分", "正在兑换积分", "状态"};
		final int[] columnWidths = {3000, 3000, 3000, 5000, 8000, 3000, 3000, 3000, 3000};
		final String[] detailHeadStrings = {"时间", "积分变动", "状态", "操作人", "流水号", "描述"};
		final int[] detailColumnWidths = {3000, 3000, 3000, 3000, 5000, 8000};
		HSSFRow row = sheet.createRow(0);
		for(int i = 0; i < headStrings.length; i++){
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(headStrings[i]);
			cell.setCellStyle(style);
			// 设置列宽
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
				cell.setCellValue("正常");
			else
				cell.setCellValue("正在兑换");
			
			// 用户明细
			String username = connectPersonScoreInfo.getUsername();
			JSONObject userScoreData = scoreManage.getScoreRecord(username, 0);
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
				// 设置列宽
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
				detailCell.setCellValue(jsonObject.getString("serial"));
				detailCell = detailRow.createCell(5);
				detailCell.setCellValue(jsonObject.getString("description"));
				
			}
		}
				
	    // 将文件存在指定位置
	    try{
	    	FileOutputStream outer = new FileOutputStream(file);
	    	wb.write(outer);
	    	outer.close();
	    }catch(Exception e){
	    	logger_error.error("生成Excel失败");
	    	logger_error.error(e);
	    	e.printStackTrace();
	    }
    }

    /**
     * 生成积分兑换记录execl文件
     * @param infos
     * @param dirPath
     * @param fileName
     */
    private void produceCPSExchangeExcel(JSONArray infos, String dirPath, String fileName)
    {
    	 AnyFile_Op anyFile_Op = new AnyFile_Op();
    	File dir = anyFile_Op.CreateDir(dirPath);//创建保存目录
		File file = anyFile_Op.CreateFile(dirPath,fileName);//创建保存文件
		
		// 创建一个workbook，即一个Excel表
		HSSFWorkbook wb = new HSSFWorkbook();
		// 在workbook中添加一个sheet，对应Excel文件中的sheet
		HSSFSheet sheet = wb.createSheet("客户积分兑换记录");
		// 设置格式：居中
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		
		// 在sheet中新建表头
		final String[] headStrings = {"所属代理商", "用户名", "真实姓名", "微信号", "客户名", "已兑换积分"
						, "兑换类型", "礼品类型", "申请时间", "完成时间", "状态", "说明"};
		final int[] columnWidths = {3000, 3000, 3000, 5000, 8000, 3000, 3000, 3000, 5000, 5000, 3000, 3000, 5000};
		HSSFRow row = sheet.createRow(0);
		for(int i = 0; i < headStrings.length; i++){
			HSSFCell cell = row.createCell(i);
			cell.setCellValue(headStrings[i]);
			cell.setCellStyle(style);
			// 设置列宽
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
				
	    // 将文件存在指定位置
	    try{
	    	FileOutputStream outer = new FileOutputStream(file);
	    	wb.write(outer);
	    	outer.close();
	    }catch(Exception e){
	    	logger_error.error("生成Excel失败");
	    	logger_error.error(e);
	    	e.printStackTrace();
	    }
    }
}


