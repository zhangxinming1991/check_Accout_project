package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

import check_Asys.ConnectP_PayService;
import check_Asys.OpLog_Service;
import en_de_code.ED_Code;
import encrypt_decrpt.AES;
import entity.ConnectPerson;
import entity.PayRecord;
import entity.PayRecordCache;
import entity.PayRecordHistory;
import file_op.AnyFile_Op;
import httpexcutor.MediaDownloadRequestExecutor;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * ConnectP_Pay_Controller �����û��ĸ�����Ϣ�ϴ�
 * @author Simon
 *
 */
@Controller
public class ConnectP_Pay_Controller {
	
	private static Logger logger = LogManager.getLogger(ConnectP_Pay_Controller.class);
	private static Logger logger_error = LogManager.getLogger("error");
	/*ȫ�ֱ���*/
	public final static SessionFactory wFactory = new Configuration().configure().buildSessionFactory();
	public final static ConnectP_PayService cps = new ConnectP_PayService(wFactory);
	private static OpLog_Service oLog_Service = new OpLog_Service(wFactory);
	/*ȫ�ֱ���*/
	
	/**
	 * upload_pay_2 �ϴ�������Ϣ
	 * @category app��web�ͻ����ϴ�������Ϣ�ӿ�
	 * @param request
	 * @param response
	 * @param mfile
	 * @author zhangxinming
	 */
	@RequestMapping(value="/upload_pay_2")
	public void upload_pay_2(HttpServletRequest request,HttpServletResponse response,@RequestParam("file") MultipartFile mfile){
		logger.info("***Get upload_pay_2 request***");
		JSONObject re_jsonobject = new JSONObject();
/*		String request_s = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				logger.info(request_s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
		String username = null;
		String payer = null;
		String pay_money = null;
		String pay_way = null;
		String pay_account = null;
		String many_pay = null;
		String owner = null;
		String receiver = null;
		try {
			username = AES.aesDecrypt(request.getParameter("username"),AES.key);
			payer = cps.cDao.findById(ConnectPerson.class, username).getCompany();//��ȡ�ͻ�����
			pay_money = AES.aesDecrypt(request.getParameter("pay_money"),AES.key); //��ȡ������
			pay_way = new String(AES.aesDecrypt(request.getParameter("pay_way"),AES.key).getBytes("GBK"),"GBK");//��ȡ���ʽ
			pay_account = AES.aesDecrypt(request.getParameter("pay_account"),AES.key);//��ȡ�����˺�
			logger.info("�����˺�Ϊ" + pay_account);
			many_pay = AES.aesDecrypt(request.getParameter("many_pay"),AES.key);//��ȡ����ĺ�ͬ�������Ϣ
			receiver = new String(AES.aesDecrypt(request.getParameter("receiver"),AES.key).getBytes("GBK"),"GBK");//��ȡ�����������Ϣ
			owner = AES.aesDecrypt(request.getParameter("owner"),AES.key);//��ȡ�����¼������������Ϣ
			logger.info(owner);
			ED_Code.printHexString(owner.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger_error.error("�����ϴ�������Ϣ����");
			oLog_Service.AddLog(OpLog_Service.utype_cp, username, OpLog_Service.Upload_Pay, OpLog_Service.result_failed);
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "�����ϴ�������Ϣ����");
			Common_return(response,re_jsonobject);
			
			e.printStackTrace();
		}//��ȡ�û���
		
		/*��ȡ����ĺ�ͬ�������Ϣ*/
		JSONArray jamany_pay = JSONArray.fromObject(many_pay);
		for(int i = 0;i<jamany_pay.size();i++){
			JSONObject jomany_pay = (JSONObject) jamany_pay.get(i);
			Double money = Double.parseDouble((String)jomany_pay.get("money"));//������Ϊ�յĻ�
			jomany_pay.put("money", money);
			jamany_pay.set(i, jomany_pay);
		}
		String new_many_pay = jamany_pay.toString();
		logger.info("�����ͬ�������Ϣ");
		/*��ȡ����ĺ�ͬ�������Ϣ*/

		String savedir = request.getServletContext().getRealPath("/" + "�����¼/" + owner + "/" + payer);
		
		Date date = new Date();
		SimpleDateFormat sFormatf = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
		SimpleDateFormat sFormatt = new SimpleDateFormat("yyyy��MM��dd��HH:mm:ss");
		sFormatf.format(date);
		String fileName = sFormatf.format(date) + mfile.getOriginalFilename().substring(mfile.getOriginalFilename().lastIndexOf("."));
		
		String caid = CreateCaid(owner);
		
		PayRecordCache ipayRecord = new PayRecordCache();
		ipayRecord.setPayer(payer);
		ipayRecord.setOwner(owner);
		ipayRecord.setManyPay(new_many_pay);
		ipayRecord.setPayWay(pay_way);
		ipayRecord.setReceiver(receiver);
		ipayRecord.setPayMoney(Double.parseDouble(pay_money));
		ipayRecord.setPayAccount(pay_account);
		ipayRecord.setPass(false);
		ipayRecord.setLinkCer("/check_Accout/" + "�����¼/" + owner + "/" + payer + "/" + fileName);
		ipayRecord.setUploadTime(sFormatt.format(date));
		ipayRecord.setConnPerson(username);
		ipayRecord.setIsconnect(false);
		ipayRecord.setCaid(caid);
		
		cps.Upload_Pay(ipayRecord,mfile,savedir,fileName);//�����ϴ�����
		
		oLog_Service.AddLog(OpLog_Service.utype_cp, username, OpLog_Service.Upload_Pay, OpLog_Service.result_success);
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "�ϴ��ɹ�");
		Common_return(response,re_jsonobject);
	}
	
	/**
	 * upload_pay_weixin �ϴ�������Ϣ
	 * @category ΢�Ŷ��ϴ�������Ϣ�ӿ�
	 * @param request
	 * @param response
	 * @param mfile
	 * @author zhangxinming
	 */
	@RequestMapping(value="/upload_pay_weixin")
	public void upload_pay_weixin(HttpServletRequest request,HttpServletResponse response){
		logger.info("***get upload_pay_weixin request***");
		JSONObject re_jsonobject = new JSONObject();
		
/*		String request_s = null;
		try {
				request_s = IOUtils.toString(request.getInputStream());
				logger.info(request_s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		String username = null;
		String payer = null;
		String pay_money = null;
		String pay_way = null;
		String pay_account = null;
		String many_pay = null;
		String owner = null;
		String receiver = null;
		String imageUrl = null;
		String actual_payer = null;
		String actual_payTime = null;
		String paymentNature = null;
		try {
			actual_payer = AES.aesDecrypt(request.getParameter("actualPayer"),AES.key);
			logger.info("actual_payer:" + actual_payer);
			
			paymentNature = AES.aesDecrypt(request.getParameter("paymentNature"),AES.key);
			logger.info("paymentNature:" + paymentNature);
			
			actual_payTime = AES.aesDecrypt(request.getParameter("actualPayTime"),AES.key);
			logger.info("actual_payTime:" + actual_payTime);
			
			imageUrl = AES.aesDecrypt(request.getParameter("imageUrl"),AES.key);
			logger.info("imageUrl��" + imageUrl);
			
			username = AES.aesDecrypt(request.getParameter("username"),AES.key);
			logger.info("username��" + username);
			
			payer = cps.cDao.findById(ConnectPerson.class, username).getCompany();//��ȡ�ͻ�����
			logger.info("payer��" + payer);
			
			pay_money = AES.aesDecrypt(request.getParameter("payMoney"),AES.key); //��ȡ������
			logger.info("pay_money��" + pay_money);
			
			pay_way = new String(AES.aesDecrypt(request.getParameter("payWay"),AES.key).getBytes("GBK"),"GBK");//��ȡ���ʽ
			logger.info("pay_way:" + pay_way);
			
			pay_account = AES.aesDecrypt(request.getParameter("payAccount"),AES.key);//��ȡ�����˺�
			logger.info("pay_account��" + pay_account);
			
			many_pay = AES.aesDecrypt(request.getParameter("manyPay"),AES.key);//��ȡ����ĺ�ͬ�������Ϣ
			logger.info("many_pay��" + many_pay);
			
			receiver = new String(AES.aesDecrypt(request.getParameter("receiver"),AES.key).getBytes("GBK"),"GBK");//��ȡ�����������Ϣ
			logger.info("receiver��" + receiver);
			
			owner = AES.aesDecrypt(request.getParameter("owner"),AES.key);//��ȡ�����¼������������Ϣ
			logger.info("owner:" + owner);
			//ED_Code.printHexString(owner.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger_error.error("����΢���ϴ�������Ϣ����" + e);
			oLog_Service.AddLog(OpLog_Service.utype_cp, username, OpLog_Service.Upload_Pay_Wexin, OpLog_Service.result_failed);
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "����΢���ϴ�������Ϣ����");
			Common_return(response,re_jsonobject);
			
			e.printStackTrace();
			return;
		}//��ȡ�û���
		
		/*��ȡ����ĺ�ͬ�������Ϣ*/
		JSONArray jamany_pay = JSONArray.fromObject(many_pay);
		for(int i = 0;i<jamany_pay.size();i++){
			JSONObject jomany_pay = (JSONObject) jamany_pay.get(i);
			Double money = Double.parseDouble((String)jomany_pay.get("money"));//������Ϊ�յĻ�
			jomany_pay.put("money", money);
			jamany_pay.set(i, jomany_pay);
		}
		String new_many_pay = jamany_pay.toString();
		/*��ȡ����ĺ�ͬ�������Ϣ*/
		
		String savedir = request.getServletContext().getRealPath("/" + "�����¼/" + owner + "/" + payer);
		
		Date date = new Date();
		SimpleDateFormat sFormatf = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
		SimpleDateFormat sFormatt = new SimpleDateFormat("yyyy��MM��dd��HH:mm:ss");
		sFormatf.format(date);

		String fileName = sFormatf.format(date);
		
		String caid = CreateCaid(owner);
		
		int offset = cps.GetMaxId_InPayCWH();
		
		PayRecordCache ipayRecord = new PayRecordCache();
		ipayRecord.setId(offset+1);
		ipayRecord.setPayer(payer);
		ipayRecord.setOwner(owner);
		ipayRecord.setManyPay(new_many_pay);
		ipayRecord.setPayWay(pay_way);
		ipayRecord.setReceiver(receiver);
		ipayRecord.setPayMoney(Double.parseDouble(pay_money));
		ipayRecord.setPayAccount(pay_account);
		ipayRecord.setPass(false);
		ipayRecord.setUploadTime(sFormatt.format(date));
		ipayRecord.setConnPerson(username);
		ipayRecord.setIsconnect(false);
		ipayRecord.setCaid(caid);
		ipayRecord.setActualPayer(actual_payer);
		ipayRecord.setActualPayTime(actual_payTime);
		ipayRecord.setPaymentNature(paymentNature);
		
		String newfilename = null;
		try {
			
			CloseableHttpClient client = HttpClients.createDefault();
			newfilename = new MediaDownloadRequestExecutor().Excute_post(client, null, imageUrl, fileName,savedir);
			ipayRecord.setLinkCer("/check_Accout/" + "�����¼/" + owner + "/" + payer + "/" + newfilename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger_error.error("��ȡ΢�ŷ�����ͼƬʧ��" + e);
			oLog_Service.AddLog(OpLog_Service.utype_cp, username, OpLog_Service.Upload_Pay_Wexin, OpLog_Service.result_failed);
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "��ȡ΢�ŷ�����ͼƬʧ��");
			Common_return(response,re_jsonobject);
			return;
			
		}
		
		boolean flag = cps.Upload_Pay(ipayRecord,null,savedir,newfilename);
		
		if (flag == true) {
			re_jsonobject.element("flag", 0);
			re_jsonobject.element("errmsg", "�ϴ��ɹ�");
			oLog_Service.AddLog(OpLog_Service.utype_cp, username, OpLog_Service.Upload_Pay_Wexin, OpLog_Service.result_success);			
		}
		else {
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "�ϴ�ʧ��");
			oLog_Service.AddLog(OpLog_Service.utype_cp, username, OpLog_Service.Upload_Pay_Wexin, OpLog_Service.result_failed);			
		}
		
		Common_return(response,re_jsonobject);
		return;
	}
	
	/**
	 * update_paymes_weixin ����΢���ϴ���δ������Ϣ
	 * @param request
	 * @param response
	 * @author zhangxinming
	 */
	@RequestMapping(value="/updatePayMesWeixin")
	public void update_paymes_weixin(HttpServletRequest request,HttpServletResponse response){
		logger.info("***Get updatePayMesWeixin request");
		JSONObject re_jsonobject = new JSONObject();
		
		String imageUrl = request.getParameter("imageUrl");
		int id = -1;
		String username = null;
		String payer = null;
		String pay_money = null;
		String pay_way = null;
		String pay_account = null;
		String many_pay = null;
		String owner = null;
		String receiver = null;
		String actual_payer = null;
		String actual_payTime = null;
		String paymentNature = null;
		String newfilename = null;
		try {			
			actual_payer = AES.aesDecrypt(request.getParameter("actualPayer"),AES.key);
			logger.info("actual_payer:" + actual_payer);
			
			paymentNature = AES.aesDecrypt(request.getParameter("paymentNature"),AES.key);
			logger.info("paymentNature:" + paymentNature);
			
			actual_payTime = AES.aesDecrypt(request.getParameter("actualPayTime"),AES.key);
			logger.info("actual_payTime:" + actual_payTime);
			
			username = AES.aesDecrypt(request.getParameter("username"),AES.key);
			logger.info("username:" + username);
			
			payer = cps.cDao.findById(ConnectPerson.class, username).getCompany();//��ȡ�ͻ�����
			logger.info("payer:" + payer);
			
			pay_money = AES.aesDecrypt(request.getParameter("payMoney"),AES.key); //��ȡ������
			logger.info("pay_money:" + pay_money);
			
			pay_way = new String(AES.aesDecrypt(request.getParameter("payWay"),AES.key).getBytes("GBK"),"GBK");//��ȡ���ʽ
			logger.info("pay_way:" + pay_way);
			
			pay_account = AES.aesDecrypt(request.getParameter("payAccount"),AES.key);//��ȡ�����˺�
			logger.info("pay_account��" + pay_account);
			
			many_pay = AES.aesDecrypt(request.getParameter("manyPay"),AES.key);//��ȡ����ĺ�ͬ�������Ϣ
			logger.info("many_pay��" + many_pay);
			
			receiver = new String(AES.aesDecrypt(request.getParameter("receiver"),AES.key).getBytes("GBK"),"GBK");//��ȡ�����������Ϣ
			logger.info("receiver��" + receiver);
			
			owner = AES.aesDecrypt(request.getParameter("owner"),AES.key);//��ȡ�����¼������������Ϣ
			logger.info("owner��" + owner);
			
			/*��ȡ����ĺ�ͬ�������Ϣ*/
			JSONArray jamany_pay = JSONArray.fromObject(many_pay);
			for(int i = 0;i<jamany_pay.size();i++){
				JSONObject jomany_pay = (JSONObject) jamany_pay.get(i);
				Double money = Double.parseDouble((String)jomany_pay.get("money"));//������Ϊ�յĻ�
				jomany_pay.put("money", money);
				jamany_pay.set(i, jomany_pay);
			}
			String new_many_pay = jamany_pay.toString();
			/*��ȡ����ĺ�ͬ�������Ϣ*/
			Date date = new Date();
			SimpleDateFormat sFormatf = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
			SimpleDateFormat sFormatt = new SimpleDateFormat("yyyy��MM��dd��HH:mm:ss");
			sFormatf.format(date);
			String caid = CreateCaid(owner);
			String savedir = null;
			
			if (imageUrl == null) {//������ϴ�΢������
				logger.info("not to update the picture");
			}
			else {
				logger.info("update picture");
				imageUrl = AES.aesDecrypt(request.getParameter("imageUrl"),AES.key);
				logger.info("imageUrl��" + imageUrl);
				
				savedir = request.getServletContext().getRealPath("/" + "�����¼/" + owner + "/" + payer);

				String fileName = sFormatf.format(date);
				
				CloseableHttpClient client = HttpClients.createDefault();
				newfilename = new MediaDownloadRequestExecutor().Excute_post(client, null, imageUrl, fileName,savedir);
			}
			
			String id_s = AES.aesDecrypt(request.getParameter("id"), AES.key);
			logger.info("id_s:" + id_s);
			
			id = Integer.parseInt(id_s);
			logger.info("id" + id);
			
			PayRecordCache ipayRecord = cps.pCDao.findById(PayRecordCache.class, id);
			if (ipayRecord == null) {//��������¼�Ѿ���ת�Ƶ�������
				PayRecord fRecord2 = cps.pDao.findById(PayRecord.class, id);
				fRecord2.setPayer(payer);
				fRecord2.setOwner(owner);
				fRecord2.setManyPay(new_many_pay);
				fRecord2.setPayWay(pay_way);
				fRecord2.setReceiver(receiver);
				fRecord2.setPayMoney(Double.parseDouble(pay_money));
				fRecord2.setPayAccount(pay_account);
				fRecord2.setPass(false);
				fRecord2.setUploadTime(sFormatt.format(date));
				fRecord2.setConnPerson(username);
				fRecord2.setIsconnect(false);
				fRecord2.setCaid(caid);
				fRecord2.setActualPayer(actual_payer);
				fRecord2.setActualPayTime(actual_payTime);
				fRecord2.setPaymentNature(paymentNature);
				if (newfilename != null) {//����ƾ֤�и���
					fRecord2.setLinkCer("/check_Accout/" + "�����¼/" + owner + "/" + payer + "/" + newfilename);
					cps.Save_UploadPicture(null,savedir,newfilename);
				}
				
				cps.pDao.update(fRecord2);
			}
			else {//��������¼���ڻ�����
				ipayRecord.setPayer(payer);
				ipayRecord.setOwner(owner);
				ipayRecord.setManyPay(new_many_pay);
				ipayRecord.setPayWay(pay_way);
				ipayRecord.setReceiver(receiver);
				ipayRecord.setPayMoney(Double.parseDouble(pay_money));
				ipayRecord.setPayAccount(pay_account);
				ipayRecord.setPass(false);
				ipayRecord.setUploadTime(sFormatt.format(date));
				ipayRecord.setConnPerson(username);
				ipayRecord.setIsconnect(false);
				ipayRecord.setCaid(caid);
				ipayRecord.setActualPayer(actual_payer);
				ipayRecord.setActualPayTime(actual_payTime);
				ipayRecord.setPaymentNature(paymentNature);
				if (newfilename != null) {//����ƾ֤�и���
					ipayRecord.setLinkCer("/check_Accout/" + "�����¼/" + owner + "/" + payer + "/" + newfilename);
					cps.Save_UploadPicture(null,savedir,newfilename);
				}
				
				cps.pCDao.update(ipayRecord);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger_error.error("����΢���ϴ�������Ϣ����" + e);
			oLog_Service.AddLog(OpLog_Service.utype_cp, username, OpLog_Service.Update_Pay_Weixin, OpLog_Service.result_failed);
			
			re_jsonobject.element("flag", -1);
			re_jsonobject.element("errmsg", "����΢���ϴ�������Ϣ����");
			Common_return(response,re_jsonobject);
			return;
		}
		
		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "���³ɹ�");
		oLog_Service.AddLog(OpLog_Service.utype_cp, username, OpLog_Service.Upload_Pay_Wexin, OpLog_Service.result_success);
		Common_return(response,re_jsonobject);
		return;
	}

	/**
	 * Get_CandA ��ȡ�ͻ��ĺ�ͬ���˺���Ϣ
	 * @param request
	 * @param response
	 * @category �ͻ��˽ӿ�
	 * @author zhangxinming
	 */
	@RequestMapping(value="/get_contractandaccout")
	public void Get_CandA(HttpServletRequest request,HttpServletResponse response) {
		logger.info("***Get get_contractandaccout request***");
		// TODO Auto-generated constructor stub
		String username = null;
		try {
			username = AES.aesDecrypt(request.getParameter("username"),AES.key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject re_object = cps.Get_CandA(username);
		
		Get_CandA_return(response,re_object);
	}
	
	/**
	 * Check_PayMes ��ѯ�ͻ�������Ϣ
	 * @param request
	 * @param response
	 * @category �ͻ��˽ӿ�
	 * @author zhangxinming
	 */
	@RequestMapping(value="/check_pay_mes")
	public void Check_PayMes(HttpServletRequest request,HttpServletResponse response){
		logger.info("***check_pay_mes request***");
		JSONObject re_jsonobject = new JSONObject();
		
		String username = null;
		try {
			username = AES.aesDecrypt(request.getParameter("username"),AES.key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger_error.error("��ȡ�ύ����ʧ��" + e);
			e.printStackTrace();
			
			re_jsonobject.element("flag", 0);
			re_jsonobject.element("��ȡ�ύ����ʧ��", "�ϴ��ɹ�");
			Common_return(response,re_jsonobject);
		}
		ConnectPerson check_cp = cps.cDao.findById(ConnectPerson.class, username);
		String companyid = check_cp.getCompany();
		List<PayRecordCache> payRecordCaches = cps.pCDao.GetPayRecordsTbByElment("payer", companyid);
		List<PayRecord> payRecords = cps.pDao.FindBySpeElement_S("payer", companyid);
		List<PayRecordHistory> payRecordHistories =  cps.pHDao.GetPrecordTbByElement("payer", companyid);
		
		LinkedList<Object> re_list = new LinkedList<Object>();
		for (int i = 0; i < payRecordHistories.size(); i++) {
			re_list.addFirst(payRecordHistories.get(i));
		}
		for (int i = 0; i < payRecords.size(); i++) {
			re_list.addFirst(payRecords.get(i));
		}
		for (int i = 0; i < payRecordCaches.size(); i++) {
			re_list.addFirst(payRecordCaches.get(i));
		}


		re_jsonobject.element("flag", 0);
		re_jsonobject.element("errmsg", "���سɹ�");
		OneKeyArray_return_enData(response,re_jsonobject,"data",re_list);

	}
		
	/**
	 * CreateCaid �����ض��Ķ���id,���id������ÿ�������̼�ÿ���µĶ��˵Ĳ�ͬ
	 * @param owner
	 * @return
	 * @author zhangxinming
	 */
	public String CreateCaid(String owner){
		//�����µĶ���id
		Date datey = new Date();
		Date datem = new Date();
		SimpleDateFormat sdfy = new SimpleDateFormat("yyyy");
		SimpleDateFormat sdfm = new SimpleDateFormat("MM");
		String dateys = sdfy.format(datey);//����
		String datems = sdfm.format(datem);//����
		String caid = dateys + "-" + datems + "-" + owner;//����id
		
		return caid;
	}

    /**
     * OneKeyArray_return_enData ��һ�����������Ϣ�ķ��أ�ֻ���ܾ�����Ϣ
     * @param response
     * @param re_json ���������������Ϣ
     * @param key ������Ϣ��key
     * @param data ������Ϣ
     */
    public void OneKeyArray_return_enData(HttpServletResponse response,JSONObject re_json,String key,Object data){
    	response.addHeader("Access-Control-Allow-Origin", "*");
    	response.setCharacterEncoding("utf-8");
    	JSONObject re_object =  re_json;//���ݲ����е���������
    	

		JSONArray aes_object = JSONArray.fromObject(data);
		logger.info(aes_object);
		String aes_object_s = aes_object.toString();
		String en_s = null;
		try {
			en_s = AES.aesEncrypt(aes_object_s,AES.key);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		re_object.element(key, en_s);
		
		/*����json���ݸ�ǰ̨*/
		try {
			logger.info("send content��" + re_object.toString());
			Writer writer = response.getWriter();
			writer.write(re_object.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
    }

    /**
     * Get_CandA_return ���ؿͻ��ĺ�ͬ���˺���Ϣ
     * @param response
     * @param re_object
     * @author zhagnxinming
     */
	public void Get_CandA_return(HttpServletResponse response,JSONObject re_object){
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.setCharacterEncoding("utf-8");
		JSONObject new_object = new JSONObject();
		
		new_object.element("flag", 0);
		new_object.element("errmsg", "���سɹ�");
		
		try {
			logger.info(re_object.getString("many_pay"));
			logger.info(re_object.getString("accout"));
			String en_many_pay = AES.aesEncrypt(re_object.getString("many_pay"),AES.key);
			String en_accout = AES.aesEncrypt(re_object.getString("accout"),AES.key);
			new_object.element("many_pay", en_many_pay);
			new_object.element("accout", en_accout);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/*����json���ݸ�ǰ̨*/
		System.out.println(new_object.toString());
		try {
			Writer writer = response.getWriter();
			writer.write(new_object.toString());
		} catch (IOException e) {
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
		response.addHeader("Access-Control-Allow-Origin", "*");	
		
		JSONObject re_object =  jsonObject;//���ݲ����е���������
		
		/*����json���ݸ�ǰ̨*/
		logger.info("send:" + re_object.toString());
		try {
			Writer writer = response.getWriter();
			writer.write(re_object.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*����json���ݸ�ǰ̨*/
	}
}
