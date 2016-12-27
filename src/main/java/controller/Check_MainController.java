package controller;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mysql.fabric.xmlrpc.base.Data;
import com.sun.jndi.url.corbaname.corbanameURLContextFactory;
import com.sun.org.apache.bcel.internal.generic.NEW;

import check_Asys.AutoCheckAuccount;
import check_Asys.CheckAcManage;
import check_Asys.WeixinPush_Service;
import check_Asys.CheckAcManage.Export_CAResObject;
import check_Asys.CheckAcManage.Import_Object;
import check_Asys.CheckAcManage.Map_Object;
import check_Asys.CheckAcManage.Owner;
import check_Asys.CheckAcManage.Watch_CAResObject;
import check_Asys.CheckAcManage.Watch_Object;
import check_Asys.WeixinPush_Service.Push_Template;
import check_Asys.OpLog_Service;
import controller.FormManagerController.OwerAtr;
import en_de_code.ED_Code;
import encrypt_decrpt.AES;
import entity.Assistance;
import entity.BankInput;
import entity.CaresultHistory;
import entity.ConnectPerson;
import entity.CusSecondstore;
import entity.OriOrder;
import entity.OriOrderId;
import entity.PayRecord;
import entity.PayRecordCache;
import entity.ScoreIncreaseRecord;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Check_MainController ����ҵ������࣬������ṩ�˶��˵�����ҵ�񣬰�������������̣��ϴ������ͳ��ɱ����ĸ�����Ϣ��ִ�ж��˲������鿴���˽��
 * �Լ����¶��ˣ���Ԥ�����ϴ��ĸ�����Ϣ���鿴��ʷ���˽��
 * @author zhangxinming
 * @version 1.0.0
 */
@Controller
public class Check_MainController {
	private static Logger logger = LogManager.getLogger(Check_MainController.class);
	private static Logger logger_error = LogManager.getLogger("error");
	/*ȫ�ֱ���*/
	public final static SessionFactory wFactory = new Configuration().configure().buildSessionFactory();
	public final static CheckAcManage cOp = new CheckAcManage(wFactory);
	private static OpLog_Service oLog_Service = new OpLog_Service(wFactory);
	/*ȫ�ֱ���*/
	
	/**
	 * Enter_CaModel �������ģʽ
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="enter_camodel")
	public void Enter_CaModel(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get enter_camodel request***");
		
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "��¼��ʱ�������µ�¼");
			Common_return_en(response,jsonObject);
			logger.info("***Get enter_camodel request***");
			return;
		}
		String who = (String)session.getAttribute("workId");//��ȡ�û���
		String agentid = (String)session.getAttribute("agentid");//���ò����ߵ�����������id
		Owner owner = cOp.new Owner();
		owner.work_id = agentid;
		owner.who = who;
		
		String caid = cOp.auccount.CreateCaid(agentid);
		String savedir_A = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_Orider);
		String filenameA = caid + CheckAcManage.FileName_Orider;
		Import_Object import_Object = cOp.new Import_Object('N', null, null, savedir_A, filenameA, null);
		
//		String caid = (String) cOp.OpSelect(CheckAcManage.ENTRER_CaModel, null,owner);//���������˾�ģʽ����
		jsonObject = (JSONObject) cOp.OpSelect(CheckAcManage.ENTRER_CaModel, import_Object,owner);//���������˾�ģʽ����
		if (jsonObject.getString("caid") != null) {
			oLog_Service.AddLog(OpLog_Service.utype_as, who, OpLog_Service.ENTRER_CaModel, OpLog_Service.result_success);//���������־
			jsonObject.element("flag", 0);
			jsonObject.element("errmsg", "�������ģʽ�ɹ�");
			//OneKeyData_return(response, jsonObject, "caid", caid);;//���ؽ������ģʽ�����Ľ��
			Common_return_en(response,jsonObject);
		}
		else{
			oLog_Service.AddLog(OpLog_Service.utype_as, who, OpLog_Service.ENTRER_CaModel, OpLog_Service.result_failed);
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "��������idʧ�ܣ��������ģʽʧ��");
			Common_return_en(response,jsonObject);
		}
		logger.info("***Get enter_camodel request***");
		return;
	}
	
	/**
	 * GoToMain �������˵�,�ú����Ѿ�������
	 * @param request
	 * @param response
	 * @deprecated
	 * @author zhangxinming
	 */
	@RequestMapping(value="goto_main")
	public void GoToMain(HttpServletRequest request,HttpServletResponse response){
		logger.info("goto_main request");
		HttpSession session = request.getSession(false);
		if (session == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "��¼��ʱ�������µ�¼");
			Common_return_en(response,jsonObject);
			return;
		}
		
		String agent_id = (String)session.getAttribute("agentid");
		//���ظ�������ĸ����¼��Ŀ
		int newpay_num = 0;
		newpay_num = cOp.dao_List.pCDao.GetPayRecordsTb(agent_id).size();
		
		Goto_Main_return(response,newpay_num);
	}
	
	/**
	 * Upload_file �ϴ����������
	 * @param request
	 * @param mfileA (A:accout)����excel��
	 * @param mfileB (B:bankinput)����excel��
	 * @param response
	 */
	@RequestMapping(value="/upload")
	public void Upload_file(HttpServletRequest request,@RequestParam("fileA") MultipartFile mfileA,@RequestParam("fileB") MultipartFile mfileB,HttpServletResponse response){
		logger.info("***Get upload request***");
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "��¼��ʱ�������µ�¼");
			Common_return_en(response,jsonObject);
			return;
		}
		String workId = (String)session.getAttribute("workId");//��ȡ�û���
		String agentid = (String)session.getAttribute("agentid");//���߲���������������id
		
		String caid = null;
		try {
			caid = AES.aesDecrypt(request.getParameter("caid"), AES.key);
			logger.info("����idΪ" + caid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*��ʼ������*/
		//cOp.dao_List.tDao.DeleteOoriderByElement("owner", agentid);//��������Լ��Ļ����
		//cOp.dao_List.bDao.DeleteBinputTbByElement("owner", agentid);//��������Լ��ĳ��ɱ�
		//ת�ƻ����������
		
		//ת�Ƴ��ɱ�������
		cOp.auccount.ResetPayRecord(agentid);//���ø�������ļ�¼
		/*��ʼ������*/
		
		String orifilename = caid + CheckAcManage.FileName_Orider;//����excel�����ļ���
		String binputfilename = caid + CheckAcManage.FileName_BankInput;//����excel�����ļ���
		if (mfileA.getOriginalFilename().equals("") || mfileB.getOriginalFilename().equals("") ) {//�ļ��ĺϷ����ж�
			logger.error("�����ļ�����ͬʱ����");
			oLog_Service.AddLog(OpLog_Service.utype_as,workId,OpLog_Service.IMPORT, OpLog_Service.result_failed);
			
			/*����*/
			JSONObject re_json = new JSONObject();
			re_json.element("flag", -1);
			re_json.element("errmsg", "�����ļ�����ͬʱ����");
			Common_return_en(response, re_json);
			/*����*/
		}
		else{
				String savedir_A = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_Orider);
				Import_Object iObjecta = cOp.new Import_Object('A', mfileA,agentid,savedir_A,orifilename,caid);
				JSONObject re_json_a = (JSONObject) cOp.OpSelect(CheckAcManage.IMPORT,iObjecta,null);//�ϴ������
				if (re_json_a.getInt("flag") == -1) {
					oLog_Service.AddLog(OpLog_Service.utype_as,workId,OpLog_Service.IMPORT, OpLog_Service.result_failed);
					Common_return_en(response, re_json_a);
					return;
				}
				
				String savedir_B = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_BankInput);
				Import_Object iObjectb = cOp.new Import_Object('B', mfileB,agentid,savedir_B,binputfilename,caid);
				JSONObject re_json_b = (JSONObject) cOp.OpSelect(CheckAcManage.IMPORT,iObjectb,null);//�ϴ����ɱ�
				if (re_json_b.getInt("flag") == -1) {
					oLog_Service.AddLog(OpLog_Service.utype_as,workId,OpLog_Service.IMPORT, OpLog_Service.result_failed);
					Common_return_en(response, re_json_b);
					return;
				}
				
				int flaga = re_json_a.getInt("flag");
				int flagb = re_json_b.getInt("flag");
				
				cOp.Import_AfterWork(flaga, flagb, agentid, mfileA, mfileB, orifilename, binputfilename, savedir_A,savedir_B,caid);
				oLog_Service.AddLog(OpLog_Service.utype_as,workId,OpLog_Service.IMPORT, OpLog_Service.result_success);
				/*����*/
				JSONObject re_json = new JSONObject();
				re_json.element("flag", 0);
				re_json.element("errmsg", "�ϴ��ɹ�");
				Common_return_en(response, re_json);
				/*����*/
		}
	}
	
	/**
	 * uploadBinput_incre ����ʽ�ϴ����ɼ�¼
	 */
	@RequestMapping(value="/uploadBinput_incre")
	public void uploadBinput_incre(HttpServletRequest request,@RequestParam("fileB") MultipartFile mfileB,HttpServletResponse response){
		logger.info("***Get uploadBinput_incre request***");
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "��¼��ʱ�������µ�¼");
			Common_return_en(response,jsonObject);
			return;
		}
		String workId = (String)session.getAttribute("workId");//��ȡ�û���
		String agentid = (String)session.getAttribute("agentid");//���߲���������������id
		
		String caid = null;
		try {
			caid = AES.aesDecrypt(request.getParameter("caid"), AES.key);
			logger.info("����idΪ" + caid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String binputfilename = caid + CheckAcManage.FileName_BankInput;//����excel�����ļ���
		String savedir_B = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_BankInput);
		Import_Object iObjectb = cOp.new Import_Object('I', mfileB,agentid,savedir_B,binputfilename,caid);
		JSONObject re_json_b = (JSONObject) cOp.OpSelect(CheckAcManage.IMPORT,iObjectb,null);//�ϴ����ɱ�
		if (re_json_b.getInt("flag") == -1) {
			oLog_Service.AddLog(OpLog_Service.utype_as,workId,OpLog_Service.IMPORT_INCRE, OpLog_Service.result_failed);
			Common_return_en(response, re_json_b);
			return;
		}
		
		oLog_Service.AddLog(OpLog_Service.utype_as,workId,OpLog_Service.IMPORT_INCRE, OpLog_Service.result_success);
		/*����*/
		JSONObject re_json = new JSONObject();
		re_json.element("flag", 0);
		re_json.element("errmsg", "����ʽ�ϴ��ɹ�");
		Common_return_en(response, re_json);
		/*����*/
		
	}
	
	/**
	 * map ���������¼�ͳ��ɼ�¼
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/Map")
	public void map(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get Map request***");
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "��¼��ʱ�������µ�¼");
			Common_return_en(response,jsonObject);
			return;
		}
		String agent_id = (String)session.getAttribute("agentid");
		String who = (String) session.getAttribute("workId");
		
		Owner owner = cOp.new Owner();
		owner.work_id = agent_id;
		
		String map_op = null;
		int pay_id;
		int bank_id;
		Map_Object map_Object = null;
		
		String request_s = null;
		String request_s_de = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_de = AES.aesDecrypt(request_s, AES.key);//��������
				logger.info("receive" + request_s_de);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject jstr = JSONObject.fromObject(request_s_de);
		pay_id = jstr.getInt("pay_id");
		map_op = jstr.getString("map_op");
		
		if (map_op.equals("cer_map")) {//ȷ��ƥ��
			bank_id = jstr.getInt("bank_id");
			map_Object = cOp.new Map_Object(map_op, pay_id,bank_id);
			
			/*�������*/
	/*		ScoreIncreaseRecord in_sr = new ScoreIncreaseRecord();
			in_sr.setHander(who);
			
			String client = cOp.dao_List.pDao.findById(PayRecord.class, pay_id).getPayer();
			in_sr.setUsername(client);
			
			Date date = new Date();
			Timestamp time = new Timestamp(date.getTime());
			in_sr.setTime(time);
			in_sr.setDescription("ƥ��ͨ������û���");
			Integer source = 5;
			in_sr.setStatus(source.byteValue());
			cOp.dao_List.sRc_Dao.add(in_sr);*/
			/*�������*/
		}
		else if(map_op.equals("find_map")){//����ƥ��
			map_Object = cOp.new Map_Object(map_op, pay_id);
		}
		else if(map_op.equals("cancel_map")){//ȡ��ƥ��
			bank_id = jstr.getInt("bank_id");
			map_Object = cOp.new Map_Object(map_op, pay_id,bank_id);
		}
		else {
			System.out.println("unknow map_op");
			return;
		}
		
		List<BankInput> fBankInputs = (List<BankInput>)cOp.OpSelect(CheckAcManage.MAP, map_Object,owner);

		jsonObject.element("flag", 0);
		jsonObject.element("errmsg", "�����ɹ�");
		OneKeyData_return(response, jsonObject, "data", fBankInputs);
	}
	
	/**
	 * Watch_Mes �鿴���������ɼ������¼��Ϣ
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/Watch")
	public void Watch_Mes(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get Watch Request***");
		response.setCharacterEncoding("utf-8");//���ñ����ʽ
		response.setContentType("text/html; charset=UTF-8"); 
		JSONObject re_jsonobject = new JSONObject();
		HttpSession session = request.getSession(false);
		if (session == null) {
			logger.info("session is null");
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��¼��ʱ�������µ�¼");
			Common_return_en(response,re_jsonobject);
			return;
		}
		
		Owner owner = cOp.new Owner();
		String work_id = (String)session.getAttribute("workId");//���ò�����id
		owner.work_id = cOp.dao_List.aDao.findById(Assistance.class, work_id).getAgentid();//���ò�����id
		owner.user_type = (String) session.getAttribute("usertype");
		
		String request_s = null;
		String request_s_de = null;
		
		Watch_Object wObject = null;
		
		int pagenum = 1;
		int pagesize = 10;
		int offset = (pagenum-1)*10;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_de = AES.aesDecrypt(request_s, AES.key);
				
				logger.info("receive" + request_s_de);
				JSONObject jstr = JSONObject.fromObject(request_s_de);
				wObject = cOp.Create_Watch_Object(jstr);//���ò鿴����
				
				if (!wObject.table_name.equals("caresult_history")) {
					pagenum = jstr.getInt("pagenum");
					offset = (pagenum-1)*10;
					pagesize = 10;
				}
				else{
					pagenum = 1;
					offset = (pagenum-1)*10;
					pagesize = 12;
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("��ȡ�ύ����ʧ��" + e);
			e.printStackTrace();
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��ȡ�ύ����ʧ��");
			Common_return_en(response,re_jsonobject);
		}
		
		//java.util.List list = cOp.Watch(wObject, owner,offset,pagesize);
		re_jsonobject = cOp.Watch(wObject, owner,offset,pagesize);
		//Watch_return(list,response,wObject);//�������ݵ�ǰ̨
    	re_jsonobject.element("flag", 0);
    	re_jsonobject.element("errmsg", "�鿴�ɹ�");
		Common_return_en(response, re_jsonobject);
		return;
	//	cOp.Close_All_Dao();
	}
	
	/**
	 * Check ���ĸ�����Ϣ
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/Check")
	public void Check(HttpServletRequest request,HttpServletResponse response){
		logger.info("Get Map request");
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "��¼��ʱ�������µ�¼");
			Common_return_en(response,jsonObject);
			return;
		}
		try {
			String request_s = IOUtils.toString(request.getInputStream());
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("receive" + request_s_de);
			JSONObject jstr  = JSONObject.fromObject(request_s_de);
			int id = jstr.getInt("id");
			String op_result = jstr.getString("op_result");
			System.out.println(id + ":" + op_result); 
			
			PayRecord pRecord = cOp.dao_List.pDao.findById(PayRecord.class, id);
			
			/*���������Ϣ�Ѿ��󶨳��ɣ���ȡ����*/
			if (pRecord.getIsconnect() == true) {
				int payid = pRecord.getId();
				System.out.println(payid);
				int bankInput_id = pRecord.getBankinputId();
				cOp.auccount.CancelConnecttBWithP(payid, bankInput_id);
			}
			/*���������Ϣ�Ѿ��󶨳��ɣ���ȡ����*/
			pRecord = cOp.dao_List.pDao.findById(PayRecord.class, id);
			pRecord.setCheckResult(op_result.charAt(0));
			cOp.dao_List.pDao.update(pRecord);
			
			// ΢��������˸����¼�����Ϣ
			WeixinPush_Service wp_ser = new WeixinPush_Service();
			String url = wp_ser.pushoneUrl;
			String username = pRecord.getConnPerson();
			String weixinId = cOp.getWeiXinId(username);	
			Date date = new Date();
			SimpleDateFormat sFormat = new SimpleDateFormat("yyyy��MM��dd�� HH:mm:ss");
			String opTime = sFormat.format(date);
			Push_Template pushMessage = wp_ser.new Push_Template();
			pushMessage.Create_MapPayMes_Template(weixinId, username, pRecord.getCheckResult().toString(), opTime);
			wp_ser.Push_OpSelect(url,WeixinPush_Service.Map_PayMes, pushMessage);
			
			jsonObject.element("flag", 0);
			jsonObject.element("errmsg", "���ĳɹ�");
			OneKeyData_return(response, jsonObject, "checkResult", String.valueOf(op_result.charAt(0)));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Start_CheckA_Work ִ�ж��˲���
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/Start_CheckA_Work")
	public void Start_CheckA_Work(HttpServletRequest request,HttpServletResponse response){
		logger.info("Get Start_CheckA_Work request");
		JSONObject jmesg =  new JSONObject();//���ݲ����е���������
		HttpSession session = request.getSession(false);
		if (session == null) {
			logger.info("session is null");
			jmesg.element("flag", -1);
			jmesg.element("errmsg", "��¼��ʱ�������µ�¼");
			
			Common_return_en(response, jmesg);
			return;
		}
		
		String work_id = (String) session.getAttribute("workId");
		String agent_id = (String)session.getAttribute("agentid");
		String userType = (String) session.getAttribute("usertype");
		
		Owner owner = cOp.new Owner();
		owner.work_id = agent_id;
		
		String request_s;
		JSONObject jstr;
		String caid = null;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("receive" + request_s_de);
			jstr = JSONObject.fromObject(request_s_de);
			caid = jstr.getString("caid");
			logger.info(caid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String opString = CheckAcManage.START_CHECKWORK;
		jmesg = (JSONObject)cOp.OpSelect(opString, caid, owner);
		
		/*��������*/
		List<String> exportlist = new ArrayList<String>();
		exportlist.add("export_totalori");
		exportlist.add("export_OriorderHasBInput");
		exportlist.add("export_OriorderNoBInput");	
		
		exportlist.add("export_TruePayNoBInput");
		exportlist.add("export_FalsePayNoBInput");
		exportlist.add("export_PayHasBinput");
	
		exportlist.add("export_BInputToContract");
		exportlist.add("export_BInputToClient");
		exportlist.add("export_BInputFailConnect");
		
		String dir = request.getServletContext().getRealPath("/��������/");
		String chinese_caid = ChangeCaidToChinese(caid);
		String filename = chinese_caid + "_���˽��.xlsx";

		cOp.formProduce.CreateForm(exportlist, agent_id, userType, dir, filename);
		/*��������*/
		
		String checkresult_url = "/check_Accout/��������" + "/" + filename;
		if (jmesg.getInt("flag") == 0) {
			logger.info("���˲����ɹ�");
			cOp.auccount.DealAfterCaSucces(owner.work_id,caid,checkresult_url);
			oLog_Service.AddLog(OpLog_Service.utype_as, work_id, OpLog_Service.START_CHECKWORK, OpLog_Service.result_success);
		}
		else{
			logger.info("���˲���ʧ��");
			cOp.auccount.DealAfterCaSucces(owner.work_id,caid,checkresult_url);
			oLog_Service.AddLog(OpLog_Service.utype_as, work_id, OpLog_Service.START_CHECKWORK, OpLog_Service.result_failed);
		}
		
		if (!caid.equals(cOp.auccount.CreateCaid(agent_id))) {
			logger.info("����Ϊ��ʷ���ˣ���ָ����˻���");
		}
		
		Common_return_en(response,jmesg);
		return;
	}
	
	/**
	 * ������caid ת��������
	 * @param caid
	 * @return
	 */
	public String ChangeCaidToChinese(String caid) {
		String date = caid.substring(0, 8);
		String owner = caid.substring(8);
		String chinese_owner = null;
		if (owner.equals("gd0001")) {
			chinese_owner = "�㶫������";
			caid = date + chinese_owner;
		}
		else if (owner.equals("gx0001")) {
			chinese_owner = "����������";
			caid = date + chinese_owner;
		}
		else if (owner.equals("ah0001")) {
			chinese_owner = "���մ�����";
			caid = date + chinese_owner;
		}
		else if (owner.equals("hb0001")) {
			chinese_owner = "����������";
			caid = date + chinese_owner;
		}
		else if (owner.equals("hn0001")) {
			chinese_owner = "���ϴ�����";
			caid = date + chinese_owner;
		}
		else if (owner.equals("jx0001")) {
			chinese_owner = "����������";
			caid = date + chinese_owner;	
		}
		else if (owner.equals("xc0001")) {
			chinese_owner = "����������";
			caid = date + chinese_owner;
		}
		else if (owner.equals("xj0001")) {
			chinese_owner = "�½�������";
			caid = date + chinese_owner;
		}
		else {
			chinese_owner = "δ֪������";
			caid = date + chinese_owner;
		}
		
		return caid;
	}
	
	/**
	 * Watch_CheckA_Result �鿴���˽��
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="/Watch_CheckA_Result")
	public void Watch_CheckA_Result(HttpServletRequest request,HttpServletResponse response) {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		if (session == null) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "��¼��ʱ�������µ�¼");
			Common_return_en(response,jsonObject);
			return;
		}
		String workId = (String)session.getAttribute("workId");
		String agentid = (String)session.getAttribute("agentid");
		
		String watch_type = null;
		String request_s = null;
		String request_s_de = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_de = AES.aesDecrypt(request_s, AES.key);
				logger.info("receive" + request_s_de);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject jstr = JSONObject.fromObject(request_s_de);
		
		watch_type = jstr.getString("watch_type");
		Watch_CAResObject wObject = cOp.new Watch_CAResObject(watch_type,agentid);
		Object res_list = cOp.OpSelect(CheckAcManage.WATCH_CARes, wObject,null);
		
		Watch_CARes_return(response,res_list,watch_type);
	}

	/**
	 * Export_CheckA_Result �������˽��
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="Export_CheckA_Result")
	public void Export_CheckA_Result(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get_Export_CheckA_Result***");
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "��¼��ʱ�������µ�¼");
			Common_return_en(response,jsonObject);
			return;
		}
		
		String work_id = (String) session.getAttribute("workId");
		String owner = (String)session.getAttribute("agentid");
		String caid = null;
		String request_s = null;
		JSONObject jstr = null;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			logger.info("receive en:" + request_s);
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("receive de:" + request_s_de);
			if (request_s_de == null) {
				logger_error.error("�޷���ȡcaid����");
				logger_error.error("ǰ̨���ݲ���--����ǰ��" + request_s);
				logger_error.error("ǰ̨���ݲ���--���ܺ�:" + request_s_de);
				jsonObject.element("flag", -1);
				jsonObject.element("errmsg", "�޷���ȡcaid����");
				Common_return_en(response,jsonObject);	
				return;
			}
			jstr = JSONObject.fromObject(request_s_de);
			caid = jstr.getString("caid");
			logger.info(caid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//String caid = cOp.auccount.CreateCaid(owner);
		String caurl = cOp.dao_List.cDao.FindBySpeElement("caid", caid, owner).get(0).getUrl();
		
		oLog_Service.AddLog(OpLog_Service.utype_as, work_id, OpLog_Service.EXPORT_CARes, OpLog_Service.result_success);
		jsonObject.element("flag", 0);
		jsonObject.element("errmsg", "�����ɹ�");
		OneKeyData_return(response, jsonObject, "cares_url", caurl);
	}
	
	/**
	 * CancelAndStartAgain_CheckA ȡ�������¶���
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="ClAndStAgain_CheckA")
	public void CancelAndStartAgain_CheckA(HttpServletRequest request,HttpServletResponse response){
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "��¼��ʱ�������µ�¼");
			Common_return_en(response,jsonObject);
			return;
		}

		Owner owner = cOp.new Owner();
		owner.work_id = (String)session.getAttribute("agentid");
		String who = (String) session.getAttribute("workId");
		owner.who = who;
		String request_s;
		JSONObject jstr;
		String caid = null;
		try {
			request_s = IOUtils.toString(request.getInputStream());
			String request_s_de = AES.aesDecrypt(request_s, AES.key);
			logger.info("receive" + request_s_de);
			jstr = JSONObject.fromObject(request_s_de);
			caid = jstr.getString("caid");
			logger.info(caid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!caid.equals(cOp.auccount.CreateCaid(owner.work_id))) {
			logger.info("��ʷ���˵����¶���");
	/*		String savedir_A = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_Orider);
			String savedir_B = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_BankInput);
			String filenameA = caid + CheckAcManage.FileName_Orider;
			String filenameB = caid + CheckAcManage.FileName_BankInput;
			
			cOp.auccount.HisCancelAndCaAgain(owner.work_id, caid, savedir_A, savedir_B, filenameA, filenameB);*/
		}
		jsonObject = (JSONObject) cOp.OpSelect(CheckAcManage.CANCEL_CaAgain, caid, owner);
		
		oLog_Service.AddLog(OpLog_Service.utype_as, who, OpLog_Service.CANCEL_CaAgain, OpLog_Service.result_success);
		jsonObject.element("flag", 0);
		jsonObject.element("errmsg", "ȡ�������¶��˳ɹ�");
		Common_return_en(response, jsonObject);
		return;
		
	}
	
	/**
	 * HisClAndSAgain_CheckA ����ʷ���������¶���
	 * @param request
	 * @param response
	 */
	@RequestMapping(value="historyca")
	public void HisClAndSAgain_CheckA(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get historyca  request***");
		
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "��¼��ʱ�������µ�¼");
			Common_return_en(response,jsonObject);
			return;
		}
		String who = (String)session.getAttribute("workId");//��ȡ�û���
		String agentid = (String)session.getAttribute("agentid");//���ò����ߵ�����������id
		
		String request_s = null;
		String request_s_de = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				request_s_de = AES.aesDecrypt(request_s, AES.key);//��������
				logger.info("receive" + request_s_de);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject jstr = JSONObject.fromObject(request_s_de);
		
		String year = Integer.toString(jstr.getInt("year"));
		String month = null;
		if (jstr.getInt("month") < 10) {
			month = "0" + Integer.toString(jstr.getInt("month"));
		}
		else {
			month = Integer.toString(jstr.getInt("month"));
		}
		String caid = year + "-" + month + "-" + agentid;//����id
		String curmonthcaid = cOp.auccount.CreateCaid(agentid);
		if (caid.equals(curmonthcaid)) {//�����ʷ������ѡ���˱��·ݣ������ʾͨ�������;�����ж���
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "ѡ����·�Ϊ���£���ͨ����������[��������]���ж���");
			Common_return_en(response,jsonObject);
			return;
		}
		
		jsonObject.element("flag", 0);
		jsonObject.element("errmsg", "�����ɹ�");
		
		String savedir_A = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_Orider);
		String savedir_B = request.getServletContext().getRealPath("/" + CheckAcManage.SaveDirName_BankInput);
		String filenameA = caid + CheckAcManage.FileName_Orider;
		String filenameB = caid + CheckAcManage.FileName_BankInput;
		
		int flag = cOp.auccount.HisCancelAndCaAgain(agentid, caid, savedir_A, savedir_B, filenameA, filenameB);
	/*	if (flag == -1) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "����ʧ�ܣ���ȷ���ϴζ����Ƿ�ɹ�");
			Common_return_en(response,jsonObject);
		}
		else if (flag == -2) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "�����Ƿ������������ʷ��������ת����ʷ����");
			Common_return_en(response,jsonObject);
		}*/
		if (flag == -1) {
		jsonObject.element("flag", -1);
		jsonObject.element("errmsg", "����ʧ�ܣ���ȷ���ϴζ����Ƿ�ɹ�");
		Common_return_en(response,jsonObject);
		return;
		}
		else {
			Owner owner = cOp.new Owner();
			owner.who = who;
			owner.work_id = agentid;
			jsonObject = (JSONObject) cOp.OpSelect(CheckAcManage.HISCA, caid, owner);
			jsonObject.element("caid", caid);
			jsonObject.element("flag", 0);
			//OneKeyData_return(response, jsonObject, "caid", caid);
			Common_return_en(response,jsonObject);
			return;
		}
	}

	/**
	 * FreeBack ���ϴ���ȷ�ĸ�����Ϣ�Ŀͻ����ػ���
	 * @param request
	 * @param response
	 * @author zhangxinming
	 */
	@RequestMapping(value="freeback")
	public void FreeBack(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get freeback request***");
		HttpSession session = request.getSession(false);
		JSONObject jsonObject = new JSONObject();
		if (session == null) {
			jsonObject.element("flag", -1);
			jsonObject.element("errmsg", "��¼��ʱ�������µ�¼");
			Common_return_en(response,jsonObject);
			return;
		}
		
		String work_id = (String) session.getAttribute("workId");
		String agent_id = (String)session.getAttribute("agentid");
		
		Owner owner = cOp.new Owner();
		owner.work_id = agent_id;
		owner.who = work_id;
		cOp.OpSelect(CheckAcManage.FREEBACK, null, owner);
		
		jsonObject.element("flag", 0);
		jsonObject.element("errmsg", "�����ɹ�");
		Common_return_en(response, jsonObject);
		return;
	}
	
	public void Watch_return(List list,HttpServletResponse response,Watch_Object wobject){
		
		JSONObject ordes_object =  new JSONObject();//���ݲ����е���������
		JSONArray orders = new JSONArray();//json���е�����
		
		if (wobject.table_name.equals(CheckAcManage.RES_CAHISTORY) == true) {
			for(int i = 0;i<list.size();i++){
				CaresultHistory cahistory = (CaresultHistory) list.get(i);
				if (cahistory != null) {
					//System.out.println(object.getPayer());
					String months = cahistory.getCamonth();
					int month = Integer.parseInt(months);
					if (month < 10) {
						months = String.valueOf(month);
					}
					JSONObject caresult = new JSONObject();
					caresult.element("month", months);
					caresult.element("url", cahistory.getUrl());
					orders.add(i,caresult);
				}
				else{
					logger.error("not find the record");
				}
			}
			ordes_object.element("data", orders);//��������ӵ�������
		}
		else {
			ordes_object.element("data", list);//��������ӵ�������
		}
		
		ordes_object.element("flag", 0);
		
		/*����json���ݸ�ǰ̨*/
		System.out.println(ordes_object.toString());
		try {
			Writer writer = response.getWriter();
			logger.info("send:" + ordes_object.toString());
			String en_s = AES.aesEncrypt(ordes_object.toString(), AES.key);//���ܷ��ص�����
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
	}

	public void Watch_CARes_return(HttpServletResponse response,Object res_list,String watch_type){

		response.setCharacterEncoding("utf-8");
		JSONArray data = new JSONArray();//json���е�����
		wRes_ReObject wRes_ReObject;
		if (watch_type.equals("bfailconnect")) {//���1���޷������ĺ�ͬ�Ż��߿ͻ����µĳ��ɼ�¼
			List<Object> failcbinput = (List<Object>) res_list;
			for (int i = 0; i < failcbinput.size(); i++) {
				wRes_ReObject = new wRes_ReObject(failcbinput.get(i), null); 
				JSONObject jObject = JSONObject.fromObject(wRes_ReObject);
				data.add(wRes_ReObject);
			}
		}
		else if (watch_type.equals("btocontract")) {//���2����������ͬ�ŵĳ��ɼ�¼
			
			List<BankInput> tocontractbinput = (List<BankInput>) res_list;
			for (int i = 0; i < tocontractbinput.size(); i++) {
				BankInput bankInput = (BankInput) tocontractbinput.get(i);
				String many_contract = bankInput.getManyContract();
				JSONArray jmany_c = JSONArray.fromObject(many_contract);
				JSONArray jforder = new JSONArray();
				for (int j = 0; j < jmany_c.size(); j++) {
					JSONObject jObject = (JSONObject) jmany_c.get(j);
					String contract = (String)jObject.get("contract");
					//OriOrder fOrder = cOp.dao_List.tDao.findById(OriOrder.class, contract);
					List<OriOrder> fList = cOp.dao_List.tDao.FindBySpeElement_S("client",bankInput.getPayer());
					if (!fList.isEmpty()) {
						OriOrder fOrder = fList.get(0);
						jforder.add(fOrder);						
					}
				}
				wRes_ReObject = new wRes_ReObject(bankInput, jforder); 
				data.add(wRes_ReObject);
			}
		}
		else if (watch_type.equals("btoclient")) {//���3���������ͻ����µĳ��ɼ�¼
			List<BankInput> toclientbinput = (List<BankInput>) res_list;
			for (int i = 0; i < toclientbinput.size(); i++) {
				BankInput bankInput = (BankInput) toclientbinput.get(i);

				JSONArray jclient = new JSONArray();
				jclient.add(bankInput.getConnectClient());
				wRes_ReObject = new wRes_ReObject(bankInput, jclient); 
				
				data.add(wRes_ReObject);
			}
		}
		else if (watch_type.equals("bnopay")) {//���4��û�й�����������Ϣ�ĳ��ɼ�¼(�ͻ�û���ϴ�������Ϣ)
			
		}
		else if (watch_type.equals("bhaspay")) {//���5��������������Ϣ�ĳ��ɼ�¼

		}
		else if (watch_type.equals("phasbinput")) {//���6��������������Ϣ���ֻ������¼
			List<PayRecord> connectbp = (List<PayRecord>) res_list;
			for (int i = 0; i < connectbp.size(); i++) {
				PayRecord fPayRecord = (PayRecord) connectbp.get(i);
				
				JSONArray jbanka = new JSONArray();
				BankInput fInput = cOp.dao_List.bDao.findById(BankInput.class, fPayRecord.getBankinputId());
				jbanka.add(fInput);
				
				wRes_ReObject = new wRes_ReObject(fPayRecord, jbanka); 
				data.add(wRes_ReObject);
			}
		}
		else if (watch_type.equals("truepnobinput")) {//���7��û�й�����������Ϣ����ʵ�ֻ������¼(�߿�)
			List<PayRecord> truenobankp = (List<PayRecord>) res_list;
			for (int i = 0; i < truenobankp.size(); i++) {
				PayRecord fPayRecord = (PayRecord) truenobankp.get(i);
				
				wRes_ReObject = new wRes_ReObject(fPayRecord, null); 
				data.add(wRes_ReObject);
			}
		}
		else if (watch_type.equals("falsepnobinput")) {//���8��û�й�����������Ϣ����ʵ�ֻ������¼(������Ϣ)
			List<PayRecord> falsenobankp = (List<PayRecord>) res_list;
			for (int i = 0; i < falsenobankp.size(); i++) {
				PayRecord bankInput = (PayRecord) falsenobankp.get(i);
				
				wRes_ReObject = new wRes_ReObject(bankInput, null); 
				data.add(wRes_ReObject);
			}
		}
		else if(watch_type.equals("onobinput")){//���9������û���յ�����Ļ����¼
			
			List<OriOrder> noborider = (List<OriOrder>) res_list;
			System.out.println("onobinput size is" + noborider.size());
			for (int i = 0; i < noborider.size(); i++) {
				OriOrder bankInput = (OriOrder) noborider.get(i);
				wRes_ReObject = new wRes_ReObject(bankInput, null); 
				JSONObject jObject = JSONObject.fromObject(wRes_ReObject);
				data.add(wRes_ReObject);
				System.out.println("***"+jObject.toString());
			}
		}
		else if(watch_type.equals("ohasbinput")){//���10���������յ�����Ļ����¼
			List<OriOrder> hasborider = (List<OriOrder>) res_list;

			for (int i = 0; i < hasborider.size(); i++) {
				OriOrder fOrder = (OriOrder) hasborider.get(i);
				
				String connect_contractid =  fOrder.getConnectBank();
				JSONArray jmany_b = JSONArray.fromObject(connect_contractid);
				JSONArray jforder = new JSONArray();
				for (int j = 0; j < jmany_b.size(); j++) {
					int bank_id = (int) jmany_b.get(j);
					
					BankInput fBankInput = cOp.dao_List.bDao.findById(BankInput.class, bank_id);
					jforder.add(fBankInput);
				}
				
				wRes_ReObject = new wRes_ReObject(fOrder, jforder); 
				data.add(wRes_ReObject);
			}
		}
		//��Ӹ���Ķ��˽��
		else{
			logger.error("unknow request of watch result_type");
		}
		
		JSONObject re_object =  new JSONObject();//���ݲ����е���������
		re_object.element("data", data);//��������ӵ�������
		re_object.element("flag", 0);
		
		/*����json���ݸ�ǰ̨*/
		System.out.println(re_object.toString());
		try {
			Writer writer = response.getWriter();
			logger.info("send" + re_object.toString());
			String en_s = AES.aesEncrypt(re_object.toString(), AES.key);//��������
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
	}
	
	/**
	 * Goto_Main_return �������˵�
	 * @param response
	 * @param newpay_num
	 * @deprecated
	 * @author zhangxinming
	 */
	public 	void Goto_Main_return(HttpServletResponse response,int newpay_num){
		JSONObject re_object =  new JSONObject();//���ݲ����е���������
		re_object.element("flag", 0);
		re_object.element("newpay_num", newpay_num);
		if (newpay_num > 0) {
			re_object.element("isnewpay", 1);
		}
		else{
			re_object.element("isnewpay", 0);
		}
		/*����json���ݸ�ǰ̨*/
		System.out.println(re_object.toString());
		try {
			Writer writer = response.getWriter();
			writer.write(re_object.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
	}
	
	/**
	 * OneKeyData_return ��һ��data��Ϣ�ķ���
	 * @param response
	 * @param jsonObject
	 * @param key �ֶ���
	 * @param object ������Ϣ
	 */
	public void OneKeyData_return(HttpServletResponse response,JSONObject jsonObject,String key,Object object){
		response.setCharacterEncoding("utf-8");

		JSONObject ordes_object =  jsonObject;//���ݲ����е���������
		ordes_object.element(key, object);
		
		/*����json���ݸ�ǰ̨*/
		System.out.println(ordes_object.toString());
		try {
			Writer writer = response.getWriter();
			logger.info("send:" + ordes_object.toString());
			String en_s = AES.aesEncrypt(ordes_object.toString(), AES.key);//���ܷ��ص�����
			writer.write(en_s);
			logger.info(ordes_object.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
	}

	/**
	 * Common_return ��ͨ���أ�û�д������data,ֻ�гɹ�����ʧ��
	 * @param response
	 * @param jsonObject
	 */
	public void Common_return(HttpServletResponse response,JSONObject jsonObject){
		response.setCharacterEncoding("utf-8");
		
		JSONObject re_object =  jsonObject;//���ݲ����е���������
		
		/*����json���ݸ�ǰ̨*/
		System.out.println(re_object.toString());
		try {
			Writer writer = response.getWriter();
			logger.info("send:" + re_object.toString());
			String en_s = AES.aesEncrypt(re_object.toString(), AES.key);//���ܷ��ص�����
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
	}
	
    /**
     * Common_return_en ����������Ϣ�ļ��ܷ���
     * @param response
     * @param re_json �����������Ϣ
     * @author zhangxinming
     */
    public void Common_return_en(HttpServletResponse response,JSONObject re_json){
		response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//���ݲ����е���������
		
    	String en_s = null;
		/*����json���ݸ�ǰ̨*/
		logger.info(re_object.toString());
		try {
			Writer writer = response.getWriter();
			en_s = AES.aesEncrypt(re_object.toString(), AES.key);
			logger.info("���ܺ���Ϊ" + en_s);
			writer.write(en_s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
    }
    
	public class wRes_ReObject implements Serializable{
		private Object basicObject;
		private Object connectObject;
		
		public wRes_ReObject(Object basicObject,Object connectObject){
			this.basicObject = basicObject;
			this.connectObject = connectObject;
		}
		
		public Object getBasicObject() {
			return this.basicObject;
		}

		public void setBasicObject(Object connectObject) {
			this.connectObject = connectObject;
		}
		
		public Object getConnectObject() {
			return this.connectObject;
		}

		public void setConnectObject(Object connectObject) {
			this.connectObject = connectObject;
		}
	}
	
}
