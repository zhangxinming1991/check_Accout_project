package check_Asys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.naming.NamingContextBindingsEnumeration;

import com.mysql.fabric.xmlrpc.base.Data;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

import dao.Agent_Dao;
import dao.BInput_Backup_Dao;
import dao.BankInput_Dao;
import dao.BankInput_His_Dao;
import dao.CaresultHistory_Dao;
import dao.ConnectPerson_Dao;
import dao.CusSdStore_Backup_Dao;
import dao.Ori_BackUp_Dao;
import dao.PayRecordCache_Dao;
import dao.PayRecordHistory_Dao;
import dao.PayRecord_Dao;
import dao.SendStore_Dao;
import dao.Total_Account_Dao;
import entity.Agent;
import entity.BankInput;
import entity.BankInputBackup;
import entity.BankInputHistory;
import entity.CaresultHistory;
import entity.ConnectPerson;
import entity.CusSdstoreBackup;
import entity.CusSecondstore;
import entity.OriOrder;
import entity.OriOrderBackup;
import entity.OriOrderId;
import entity.PayRecord;
import entity.PayRecordCache;
import entity.PayRecordHistory;
import file_op.Excel_RW;
import file_op.Excel_RW.Excel_Row;
import jdk.nashorn.api.scripting.JSObject;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * AutoCheckAuccount ����ҵ����ÿһ�������ľ���ʵ�ֶ�����������棬ͨ���������������ĺ�����ʵ�ֶ��˲���
 * @author zhangxinming
 * @version 1.0.0
 */
public class AutoCheckAuccount {
	private static Logger logger = LogManager.getLogger(AutoCheckAuccount.class);
	private static Logger logger_error = LogManager.getLogger("error");
	BankInput_Dao bDao = null;
	PayRecord_Dao pDao = null;
	Total_Account_Dao tDao = null;
	SendStore_Dao sDao = null;
	PayRecordHistory_Dao pHDao = null;
	CaresultHistory_Dao cDao = null;
	PayRecordCache_Dao pCDao = null;
	ConnectPerson_Dao cPerson_Dao = null;
	Ori_BackUp_Dao oUp_Dao = null;
	BInput_Backup_Dao bInput_Backup_Dao = null;
	Agent_Dao agent_Dao = null;
	CusSdStore_Backup_Dao cBackup_Dao = null;
	BankInput_His_Dao bHis_Dao = null;
	
	public boolean isFreeBack(PayRecord payRecord,String owner){
		if (payRecord.getFreeback() == true) {
			logger.info(payRecord.getId() + "�����¼�Ѿ������������ظ�������");
			return false;
		}
		else{
			char checkresult = payRecord.getCheckResult();
			if (checkresult == 'Y' || checkresult == 'W') {
				String connp_username = payRecord.getConnPerson();
				ConnectPerson fcPerson = cPerson_Dao.findById(ConnectPerson.class, connp_username);
	
				fcPerson.setScore(fcPerson.getScore() + 10);
				cPerson_Dao.update(fcPerson);
				
				payRecord.setFreeback(true);
				pDao.update(payRecord);
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	public AutoCheckAuccount(BankInput_Dao bDao,PayRecord_Dao pDao,	Total_Account_Dao tDao,SendStore_Dao sDao,PayRecordHistory_Dao pHDao,CaresultHistory_Dao cDao,PayRecordCache_Dao pCDao,ConnectPerson_Dao cPerson_Dao,Ori_BackUp_Dao oUp_Dao,BInput_Backup_Dao bInput_Backup_Dao,Agent_Dao agent_Dao,CusSdStore_Backup_Dao cBackup_Dao,BankInput_His_Dao bHis_Dao) {
		this.bDao = bDao;
		this.pDao = pDao;
		this.tDao = tDao;
		this.sDao = sDao;
		this.pHDao = pHDao;
		this.cDao = cDao;
		this.pCDao = pCDao;
		this.cPerson_Dao = cPerson_Dao;
		this.oUp_Dao = oUp_Dao;
		this.bInput_Backup_Dao = bInput_Backup_Dao;
		this.agent_Dao = agent_Dao;
		this.cBackup_Dao = cBackup_Dao;
		this.bHis_Dao = bHis_Dao;
	}

	/**
	 * MappPayToBank �����ֻ�������Ϣ�пͻ�����ȥƥ������:ƥ����ԣ�ʹ�ø�����Ϣ�еĸ����˺Ϳ����������Ϊƥ�����ݣ�ȥѰ����صĳ��ɼ�¼
	 * @param payid ������Ϣ��id
	 * @return
	 */
	public List<BankInput> MappPayToBank(int payid,String owner){
		logger.info("sayhi");
		List<BankInput> mappedBankInput = null;
		
		PayRecord fRecord = pDao.findById(PayRecord.class, payid);
		String payer = fRecord.getPayer();
		String payee = fRecord.getReceiver();
		
		mappedBankInput = bDao.GetMapBinputs("payer", "payer", payer, payee,owner);//���ݸ����˺��տ���������Ϣȥ���ҳ��ɼ�¼
		if (mappedBankInput == null) {
			System.out.println("not found the mapped bankrecord");
		}
		
		Test_MappPayToBank(mappedBankInput);
		
		return mappedBankInput;
	}
	
	/**
	 * ConnectBankWithPay ������Ҫ���Ψһ���м�¼������������Ϣ
	 * @param payid ������Ϣid
	 * @param bankInput_id ����id
	 * @param contract_num ��ͬ����
	 * @param many_contract ��ͬ������Ϣ
	 * @return
	 */
	public boolean ConnectBankWithPay(int payid,int bankInput_id,String contract_num,String many_contract){

		/*����Ҫ��ɵ�ҵ�����*/
		PayRecord pRecord = pDao.findById(PayRecord.class,payid);
		pRecord.setBankinputId(bankInput_id);
		pRecord.setIsconnect(true);
		pRecord.setCheckResult('Y');
		/*����Ҫ��ɵ�ҵ�����*/
		
		/*����Ҫ��ɵ�ҵ����*/
		BankInput bRecord = bDao.findById(BankInput.class,bankInput_id);
		bRecord.setPayid(payid);
		bRecord.setIsConnect(true);
		bRecord.setContractNum(contract_num);
		bRecord.setManyContract(many_contract);
		/*����Ҫ��ɵ�ҵ����*/
		
		//��Ӹ���ȷ��ƥ���ҵ�����
		
		pDao.update(pRecord);
		bDao.update(bRecord);
		return true;
	}
	
	/**
	 * CancelConnecttBWithP  ȡ��Ψһ���м�¼������������Ϣ
	 * @param payid ������Ϣid
	 * @param bankInput_id ����id
	 * @return
	 */
	public boolean CancelConnecttBWithP(int payid,int bankInput_id){

		/*ȡ���󶨸�����Ϣ��Ҫ��ɵ�ҵ�����*/
		PayRecord pRecord = pDao.findById(PayRecord.class,payid);
		pRecord.setBankinputId(null);
		pRecord.setIsconnect(false);
		pRecord.setCheckResult(null);
		pDao.update(pRecord);
		/*ȡ���󶨸�����Ϣ��Ҫ��ɵ�ҵ�����*/
		
		/*ȡ������Ҫ��ɵ�ҵ����*/
		BankInput bRecord = bDao.findById(BankInput.class,bankInput_id);
		if (bRecord != null) {
			bRecord.setPayid(null);
			bRecord.setIsConnect(false);
			bRecord.setContractNum(null);
			bRecord.setManyContract(null);
			bDao.update(bRecord);
		}
		/*ȡ������Ҫ��ɵ�ҵ����*/
		
		//��Ӹ���ȷ��ƥ���ҵ�����
		
		return true;
	}
	
	/**
	 * ConnectBankWithCustom  �����ɼ�¼�������ͻ�����
	 * @param cInput ������Ϣ
	 * @param truepayer �����˹�˾id
	 * @param truemoney ��ʵ��� 
	 * @return
	 */
	public boolean ConnectBankWithCustom(BankInput cInput,String truepayer,Double truemoney){
		CusSecondstore fcustom = sDao.findById(CusSecondstore.class, truepayer);
		if (fcustom == null) {
			logger.error("���ɼ�¼�������ͻ�����");
			return false;
		}
		
		if (fcustom.getOwner().equals(cInput.getOwner()) == false) {
			logger.info("��������Ϣ��һ��,�޷������ɼ�¼�Ϳͻ�����");
			return false;
		}
		else{
			/*����Ҫ��ɵ�ҵ�����*/
			fcustom.setInput(truemoney + fcustom.getInput());
			fcustom.setUpdateTime(cInput.getInputTime());
			boolean connectResult = sDao.update(fcustom);
			
			if (connectResult == true) {//��������ɹ�,���³��ɼ�¼�Ĺ����ͻ��ֶ�
				cInput.setConnectClient(fcustom.getClient());
				bDao.update(cInput);
			}
			return true;
			/*����Ҫ��ɵ�ҵ�����*/
		}

	}
	
	/**
	 * CancelConnectBkAndPay ȡ�������¼�ͳ��ɼ�¼�İ�
	 * @param payid ������Ϣid
	 * @param bankInput_id ������Ϣid
	 * @return
	 */
	public boolean CancelConnectBkAndPay(int payid,int bankInput_id){
		
		/*�󶨻���ȡ��������Ϣ��Ҫ��ɵ�ҵ�����*/
		PayRecord pRecord = pDao.findById(PayRecord.class,payid);
		pRecord.setBankinputId(0);//����Ϊ0
		pRecord.setIsconnect(false);
		/*�󶨻���ȡ��������Ϣ��Ҫ��ɵ�ҵ�����*/
		
		/*�󶨻���ȡ������Ҫ��ɵ�ҵ����*/
		BankInput bRecord = bDao.findById(BankInput.class,bankInput_id);
		bRecord.setPayid(0);
		bRecord.setIsConnect(false);
		bRecord.setContractNum("");
		bRecord.setManyContract("");
		/*�󶨻���ȡ������Ҫ��ɵ�ҵ����*/
		
		pDao.update(pRecord);
		bDao.update(bRecord);
		return true;
	}
	
	/**
	 * ConnectBankWithAccount ���ɼ�¼�����������ʹ�ú�ͬ�Ź���
	 * @param cInput ���ɼ�¼��Ϣ
	 * @return
	 */
	public JSONObject ConnectBankWithAccount(BankInput cInput,String owner){//������ƵĲ��ã���Ӧ�ü���������ɵ��ͻ����µĹ���
		OriOrder fOrder = null;
		JSONObject re_object = new JSONObject();
		re_object.element("flag", -1);
		
		JSONArray many_contract = JSONArray.fromObject(cInput.getManyContract());
		if (many_contract == null) {
			logger.error("many_contract is null");
			re_object.element("errmsg", "many_contract is null");
			return re_object;
		}
		if (many_contract.size() == 0) {
			logger.warn("û���κλ�����Ϣ");
			re_object.element("errmsg", "û���κλ�����Ϣ");
			return re_object;
		}
		
		for (int j = 0; j < many_contract.size(); j++) {
			JSONObject cotract_money = many_contract.getJSONObject(j);
			
		//	fOrder = tDao.findById(OriOrder.class, cotract_money.getString("contract"));
			String paymentNature = cotract_money.getString("contract");
			String cuscompanyid = cInput.getCuscompanyid();
			if (cuscompanyid == null) {
				logger_error.error("�ͻ���˾idΪnull");
				re_object.element("errmsg", "�ͻ���˾idΪnull");
				return re_object;
			}
			logger.info(cuscompanyid + ":" + paymentNature);
			fOrder = tDao.findById(OriOrder.class, new OriOrderId(cuscompanyid, paymentNature));
			if (fOrder == null || fOrder.getOwner().equals(owner) == false) {
		//		logger.info(fOrder.getOwner() + ":" + owner);
				String errmsg = "��д��" + cotract_money.getString("contract") + "������Ϣ����," + cuscompanyid + "�����ڸû�����Ϣ";
				logger.info(errmsg);
				re_object.element("errmsg", errmsg);
				String truepayer = null;
				if (cInput.getIsConnect() == true) {
					truepayer = pDao.findById(PayRecord.class, cInput.getPayid()).getPayer();
				}
				else{
					logger_error.error("����û�а󶨻�����Ϣ");
					re_object.element("errmsg", "����û�а󶨻�����Ϣ");
					return re_object;
				}
				
				Double money = cotract_money.getDouble("money");

				ConnectBankWithCustom(cInput,truepayer,money);//ʹ�ÿͻ�����ȥ������ͬ��
				continue;
			}
			/*����Ҫ��ɵ�ҵ����*/
			fOrder.setInput(cotract_money.getDouble("money") + fOrder.getInput());
			fOrder.setDebt(fOrder.getDebt() - fOrder.getInput());
			fOrder.setUpdateTime(cInput.getInputTime());
			
			JSONArray jconnectbArray;
			if (fOrder.getConnectBank() == null) {
				jconnectbArray = new JSONArray();
			}
			else {
				String connect_bank = fOrder.getConnectBank();
				jconnectbArray = JSONArray.fromObject(connect_bank);
			}
			jconnectbArray.add(cInput.getId());
			fOrder.setConnectBank(jconnectbArray.toString());
			/*����Ҫ��ɵ�ҵ����*/
			
			if (tDao.update(fOrder) == true) {//��������ɹ�������³��ɼ�¼�Ĺ�����ͬ��Ŀ�ֶ�
				cInput.setConnectNum(cInput.getConnectNum() + 1);
				bDao.update(cInput);
			}			
		}
		re_object.element("flag", 0);
		re_object.element("errmsg", "ʹ�ú�ͬ�Ź��������ɹ�");
		return re_object;
	}
	
	/**
	 * ConnectBankWithAccount_Only ���ɼ�¼������������ͻ�ֻ��һ����ͬ��¼
	 * @param cInput
	 * @return
	 */
	public boolean ConnectBankWithAccount_Only(BankInput cInput,String paymentNature){
		OriOrder fOrder = null;
		boolean connectResult = false;

		logger.info(cInput.getPayer());
		fOrder = tDao.FindByClient(cInput.getPayer()).get(0);
		String cuscompanyid = cInput.getCuscompanyid();

		/*����Ҫ��ɵ�ҵ����*/
		logger.info(cInput.getMoney() + ":" + fOrder.getInput());
		fOrder.setInput(cInput.getMoney() + fOrder.getInput());
		fOrder.setDebt(fOrder.getDebt() - fOrder.getInput());
		fOrder.setUpdateTime(cInput.getInputTime());
		
		JSONArray jconnectbArray;
		if (fOrder.getConnectBank() == null) {
			logger.info("������ConnectBank");
			jconnectbArray = new JSONArray();
		}
		else {
			String connect_bank = fOrder.getConnectBank();
			jconnectbArray = JSONArray.fromObject(connect_bank);
		}
		jconnectbArray.add(cInput.getId());
		fOrder.setConnectBank(jconnectbArray.toString());
		/*����Ҫ��ɵ�ҵ����*/
		 connectResult = tDao.update(fOrder);
		if (connectResult == true) {
			cInput.setConnectNum(cInput.getConnectNum() + 1);
			JSONObject jObject = new JSONObject();
			jObject.put("contract", fOrder.getId().getPaymentNature());
			jObject.put("money", cInput.getMoney());
			JSONArray jArray = new JSONArray();
			jArray.add(jObject);
			cInput.setManyContract(jArray.toString());
			//cInput.setIsConnect(true);
			bDao.update(cInput);
		}
		return true;
	}
	
	/**
	 * IsStartCheckWork �Ƿ����㿪ʼ���˵�����
	 * @return
	 */
	public JSONObject IsStartCheckWork(String owner){
		//�ж��Ƿ����еĸ����¼���Ѿ�����
		JSONObject jmesg = new JSONObject();
		jmesg.element("flag", 0);
		String meString = "";
		List pList = pDao.GetPrecordTbByElement("owner", owner);
		
		for(int i = 0;i<pList.size();i++){
			PayRecord pRecord = (PayRecord)pList.get(i);
			if (pRecord.getCheckResult() == null) {
				logger.info("idΪ " + pRecord.getId() + "�����¼û������");
				jmesg.element("flag", -1);
				meString = meString + "[idΪ " + pRecord.getId() + "�����¼û������] \r\n";
			}
		}
		
		jmesg.element("errmsg", meString);
		return jmesg;
	}
	
	/**
	 * ConnectAccountWithCustom ��ӻ���ˢ�¿ͻ���ͬ��Ϣ
	 * @param order ������Ϣ
	 * @return
	 */
	public boolean ConnectAccountWithCustom(OriOrder order){
		CusSecondstore fcustom = sDao.findById(CusSecondstore.class, order.getClient());
		JSONObject contract_mes = new JSONObject();
		JSONArray acontract_mes = new JSONArray();
		if (fcustom == null) {//�ͻ����в����ڿͻ���Ϣ�����½��û�
			fcustom = new CusSecondstore();
			
	//		contract_mes.put("contract", order.getOrderNum());
			contract_mes.put("contract", order.getId().getPaymentNature());
			contract_mes.put("debt", order.getDebt());
			acontract_mes.add(contract_mes);
			
			/*������Ҫ��ɵ�ҵ�����*/
			fcustom.setInput(0d);
			fcustom.setClient(order.getClient());
			fcustom.setContractMes(acontract_mes.toString());
			fcustom.setContractNum(1);
			fcustom.setOwner(order.getOwner());
			/*������Ҫ��ɵ�ҵ�����*/
			sDao.add(fcustom);
		}
		else {//���¿ͻ���Ϣ
			if (fcustom.getContractMes() == null) {
				
			}
			else{
				acontract_mes = JSONArray.fromObject(fcustom.getContractMes());
			}
			
			contract_mes.put("contract", order.getId().getPaymentNature());
			contract_mes.put("debt", order.getDebt());
			acontract_mes.add(contract_mes);//�����µĺ�ͬ
			
			/*������Ҫ��ɵ�ҵ�����*/
			fcustom.setContractMes(acontract_mes.toString());
			fcustom.setContractNum(fcustom.getContractNum() + 1);
			sDao.update(fcustom);
			/*������Ҫ��ɵ�ҵ�����*/
		}
		return true;
	}
	
	/**
	 * ConnectBankinputWithCustom ��ӻ���ˢ�¿ͻ����˺���Ϣ
	 * @param bankInput ������Ϣ
	 * @return
	 */
	public boolean ConnectBankinputWithCustom(BankInput bankInput){
		CusSecondstore fcustom = sDao.findById(CusSecondstore.class, bankInput.getPayer());
		JSONArray acontract_mes = new JSONArray();
		if (fcustom == null) {

		}
		else {//���¿ͻ���Ϣ
			if (bankInput.getPayerAccount() == null) {
				
			}
			else{
				if (fcustom.getAccoutMes() == null) {
					
				}
				else{
					acontract_mes = JSONArray.fromObject(fcustom.getAccoutMes());
				}
				acontract_mes.add(bankInput.getPayerAccount());//�����µĺ�ͬ
				/*������Ҫ��ɵ�ҵ�����*/
				fcustom.setAccoutMes(acontract_mes.toString());
				sDao.update(fcustom);
				/*������Ҫ��ɵ�ҵ�����*/
			}			
		}
		return true;
	}

	/**
	 * CreateCaid ��������id
	 * @param owner ������id
	 * @return
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
	 * Enter_CaModel �������ģʽ
	 * @param owner
	 * @return
	 */
	public String Enter_CaModel(String owner,String savedirA,String filenameA){
		logger.info("�������ģʽ");
		
		//�����µĶ���id
		Date datey = new Date();
		Date datem = new Date();
		SimpleDateFormat sdfy = new SimpleDateFormat("yyyy");
		SimpleDateFormat sdfm = new SimpleDateFormat("MM");
		String dateys = sdfy.format(datey);//����
		String datems = sdfm.format(datem);//����
		String caid = dateys + "-" + datems + "-" + owner;//����id
		
		
		//��ȡ�����·ݱ��µĶ��˽����¼
		List<CaresultHistory> fCrHistories = cDao.FindBySpeElementLimit("caid", caid, owner);
		if (fCrHistories.isEmpty() == true) {//�����¼Ϊ�գ�������µĶ��˼�¼(���¶��˻�û��ʼ)	
			logger.info("��ʼ���·ݵĶ���");
			
			/*�����ϴζ���id,����Ϊfalse*/
			List<CaresultHistory> fList = cDao.FindBySpeElement("lastcaid", true, owner);
			if (fList.isEmpty()) {
				logger.info("��һ��ʹ��ϵͳ");
			}
			else{
				CaresultHistory fHLast = fList.get(0);
				if (fCrHistories == null) {
				logger_error.error("�ϴζ���caid������");
				}
				else {
					fHLast.setLastcaid(false);//���ϴζ��˵�lastcaid��Ϊfalse
					cDao.update(fHLast);
				}
			}
			//CaresultHistory fHLast = cDao.FindBySpeElement("lastcaid", true, owner).get(0);//����ƿ��

			/*�����ϴζ���id,����Ϊfalse*/
			
			CaresultHistory in_crhistory = new CaresultHistory();//��¼��״̬Ϊ������
			in_crhistory.setCaid(caid);
			in_crhistory.setCayear(dateys);
			in_crhistory.setCamonth(datems);
			in_crhistory.setOwner(owner);
			in_crhistory.setCaresult('D');
			in_crhistory.setLastcaid(true);
			cDao.add(in_crhistory);
			
			//����������ļ�¼�Ƶ�������ʷ��
			TransferPrecord_WAreaToHArea(owner);
			//����������ľɵļ�¼�Ƶ�������ʷ��
			TransferPrecord_CAreaToHArea(owner,caid);
			//�Ӹ������ȡ�����¼
			TransferPrecord_CAreaToWArea(owner,caid);
			//�����ɹ������ļ�¼ת�Ƶ���ʷ��
			TransferBinput_WareaToHistory(owner);
		}
		else{
			//List<PayRecord> fpList = pDao.FindBySpeElement_S_limit("caid", caid);
		//	List<OriOrderBackup> orderBackups = oUp_Dao.GetOriBackupByElment("owner", owner);
		//	if (orderBackups.isEmpty() != true) {//����ʷ�����лص����¶���,����һ����������Ҫ�ж�backup���Ƿ�Ϊ�գ���Ϊ�ճ�����
			if (fCrHistories.get(0).getLastcaid() == false) {
				logger.info("����ʷ�����лص����¶���");
				
				TransferPrecord_WAreaToHArea(owner);//����������¼ת�Ƶ�������ʷ��
				
				TransferPrecord_HAreaToWArea(owner, caid);//��������ʷ����caid�ļ�¼ת�Ƶ�������
				
				//��ջ����ͳ��ɱ���Ϣ
				tDao.DeleteOoriderByElement("owner", owner);
				
				//���������ļ�¼ת�Ƶ���ʷ��
				TransferBinput_WareaToHistory(owner);
				
				//��backup��������������¼�ͳ��ɼ�¼
			//	TransferOri_BackupToWarea(owner);
				File fileA = new File(savedirA + "/" + filenameA);
				if (!fileA.exists()) {
					logger.warn("�����" + filenameA + "�����ڣ������µ���");
				}
				else{
					//ResetCustomToCaDuring(owner);//���ÿͻ���
					
					Excel_RW excel_RW = new Excel_RW();//����excel����
					InputStream inputStream = null;
					try {
						inputStream = new FileInputStream(new File(savedirA + "/" + filenameA));
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						logger_error.error("��ȡ��������" + e);
						e.printStackTrace();
					}
					ArrayList<Excel_Row> totalA_table = excel_RW.ReadExcel_Table(inputStream);
			
					Agent fagent = agent_Dao.findById(Agent.class, owner);
					JSONObject jsonObject = excel_RW.Table_To_Ob_OriOrders(totalA_table,fagent);//��excel��ת���ɶ���
					
					OriOrder[] in_orders = (OriOrder[]) JSONArray.toArray(jsonObject.getJSONArray("orders"), OriOrder.class);
					/*����excel������*/
					
					/*д�����ݿ�*/
					for (OriOrder order:in_orders) {

						
						/*�������ϵ����Ϣ*/
						List<ConnectPerson> fPersons = cPerson_Dao.FindBySpeElement("companyid", order.getId().getCuscompanyid(), owner);
						if (fPersons.isEmpty() !=  true) {
							order.setCustomname(fPersons.get(0).getRealName());
							order.setCustomphone(fPersons.get(0).getPhone());
							order.setCustomweixin(fPersons.get(0).getWeixin());
						}
						/*�������ϵ����Ϣ*/
						
						/*������̲�����Ϣ*/
						Agent agent = agent_Dao.findById(Agent.class, owner);
						order.setAsname(agent.getAgentConnectpname());
						order.setAsphone(agent.getAgentCpphone());
						order.setAsemail(agent.getAgentCpemail());
						/*������̲�����Ϣ*/
						
						order.setConnectBank(null);
						tDao.add(order);
					//	ConnectAccountWithCustom(order);
						
						/*�����¼�Ŀͻ�����Ϊ��˾����ʱ���Żᱻ¼��ͻ�����*/
						String clientname = order.getClient();
						int len = clientname.length();
						if ((clientname.contains("��˾") || clientname.contains("���޹�˾")) && (len > 3)) {
							logger.info(clientname + "���Ϲ�˾���ͣ�����¼��ͻ���");
							ConnectAccountWithCustom(order);  //��ӻ���ˢ�¿ͻ���ͬ��Ϣ
						}
						/*�����¼�Ŀͻ�����Ϊ��˾����ʱ���Żᱻ¼��ͻ�����*/
						
					}
					/*д�����ݿ�*/
				}
				
				//TransferBinput_BackupToWarea(owner);
				//�����µļ�¼ת�ڵ�������������ʷ��¼ת�Ƶ���ʷ��
				TransferBinput_BackupToWarea_AND(owner,caid);
				TransferBinput_BackupToHisarea(owner);
				
				//����������ļ�¼ת�ڵ�������
				TransferPrecord_CAreaToWArea(owner,caid);
				
				//�ͻ���Ϣ������
				sDao.DeleteTbByElement("owner", owner);
				TransferCus_BackupToWarea(owner);//���ͻ���Ϣת�Ƶ�������
				
				CaresultHistory fHLast = cDao.FindBySpeElement("lastcaid", true, owner).get(0);//����ƿ��
				if (fCrHistories == null) {
					logger_error.error("�ϴζ���caid������");
				}
				else {
					fHLast.setLastcaid(false);//���ϴζ��˵�lastcaid��Ϊfalse
					cDao.update(fHLast);
					
					CaresultHistory fHCur = cDao.FindBySpeElement("caid", caid, owner).get(0);//����ƿ��
					fHCur.setLastcaid(true);//�����ζ��˵�lastcaid��Ϊtrue
					cDao.update(fHCur);
				}
			}
			else{//�����ظ�����
				logger.info("�����ظ�����");
				//����������ļ�¼ת�ڵ�������
				TransferPrecord_CAreaToWArea(owner,caid);
			}
			
			/*��������������Ƿ�Ӧ�÷����������˲���ǰһ��*/
			ResetOrider(owner);
			ResetBinputs(owner);
			ResetCustom(owner);
			/*��������������Ƿ�Ӧ�÷����������˲���ǰһ��*/
		}
		//�����˽����ʷ���в���һ���µļ�¼
		
		return caid;
	}
	
	/**
	 * ResetCustomToCaDuring ���ÿͻ���Ϊ�ϴ�ǰ״̬
	 * @param owner
	 */
	public void ResetCustomToCaDuring(String owner){
		//���ÿͻ����input,contract_num,contract_mes�ֶ�
		List<CusSecondstore> lcustoms = sDao.GetCustomTb(owner);
		for (int i = 0; i < lcustoms.size(); i++) {
			CusSecondstore resetcustoms = lcustoms.get(i);
			resetcustoms.setContractNum(0);
			resetcustoms.setContractMes(null);
			resetcustoms.setInput(0d);
			resetcustoms.setUpdateTime(null);
			sDao.update(resetcustoms);
		}
	}
	
	/**
	 * ResetCustomAccoutMsg ���ÿͻ����е��˺���ϢΪ�ϴ�ǰ״̬
	 * @param owner
	 */
	public void ResetCustomAccoutMsg(String owner){
		//���ÿͻ����input,contract_num,contract_mes�ֶ�
		List<CusSecondstore> lcustoms = sDao.GetCustomTb(owner);
		for (int i = 0; i < lcustoms.size(); i++) {
			CusSecondstore resetcustoms = lcustoms.get(i);
			resetcustoms.setAccoutMes(null);
			sDao.update(resetcustoms);
		}
	}
	
	/**
	 * ResetPayRecord ���ø����Ϊ��ʼ״̬
	 * @param owner
	 */
	public void ResetPayRecord(String owner){
		List<PayRecord> pList = pDao.FindBySpeElement("isconnect", true,owner);
		
		for (int i = 0; i < pList.size(); i++) {
			PayRecord pRecord = pList.get(i);
			pRecord.setCheckResult(null);
			pRecord.setIsconnect(false);
			pRecord.setBankinputId(null);
			pDao.update(pRecord);
		}
	}

	/**
	 * ResetOrider ���û����Ϊ��ʼ״̬
	 * @param owner
	 */
	public void ResetOrider(String owner){
		List<OriOrder> fOrders = tDao.FindOriHasBInput(owner);
		
		for (int i = 0; i < fOrders.size(); i++) {
			OriOrder fOrder = fOrders.get(i);
			double input = fOrder.getInput();
			double debet = fOrder.getDebt() + input;
			input = 0d;
			fOrder.setInput(input);
			fOrder.setDebt(debet);
			fOrder.setUpdateTime(null);
			fOrder.setConnectBank(null);
			tDao.update(fOrder);
		}
	}
	
	/**
	 * ResetBinputs ���ó��ɱ�Ϊ����״̬
	 * @param owner
	 */
	public void ResetBinputs(String owner){
		List<BankInput> bankInputs = bDao.FindBySpeElement_Big("connectNum", 0, owner);
		
		for (int i = 0; i < bankInputs.size(); i++) {
			BankInput bInput = bankInputs.get(i);
			bInput.setConnectNum(0);
			bDao.update(bInput);
		}
	}
	
	/**
	 * ResetCustom ���ÿͻ���Ϊ����״̬
	 * @param owner
	 */
	public void ResetCustom(String owner){
		List<CusSecondstore> cList = sDao.FindBySpeElement_Big("input", 0d, owner);
		
		for (int i = 0; i < cList.size(); i++) {
			CusSecondstore custom = cList.get(i);
			custom.setInput(0d);
			custom.setUpdateTime(null);
			sDao.update(custom);
		}
	}
	
	/**
	 * TransferPrecord_WAreaToHArea ת�渶���¼�������ļ�¼��������ʷ������
	 * @param owner
	 */
	public void TransferPrecord_WAreaToHArea(String owner){
		//��ת�渶���¼�������¼��ʷ
		List<PayRecord> payTb = pDao.GetPrecordTbByElement("owner", owner);
		
		if (payTb.isEmpty() == true) {
			logger.warn("�����¼������������" + owner + "�����̼�¼Ϊ��");
			return;
		}
		for (int i = 0; i < payTb.size(); i++) {
			JSONObject jObject = JSONObject.fromObject(payTb.get(i));
			PayRecordHistory pHistory = (PayRecordHistory)JSONObject.toBean(jObject, PayRecordHistory.class);
			pHistory.setCheckResult(payTb.get(i).getCheckResult());
			pHistory.setId(payTb.get(i).getId());
			pHDao.add(pHistory);
		}
		
		pDao.DeletePrecordTbByElement("owner", owner);//��ո����¼
	}
	
	/**
	 * TransferPrecord_CAreaToWArea �Ӹ������ȡ��¼���������
	 * @param owner
	 * @param caid
	 */
	public void TransferPrecord_CAreaToWArea(String owner,String caid){
	//	List<PayRecordCache> payTb = pCDao.GetPayRecordsTb(owner);//ȡ��¼
		List<PayRecordCache> payTb = pCDao.GetTbByElement_Owner("caid",caid,owner);
		if (payTb.isEmpty() == true) {
			logger.warn("�����¼������������" + owner + "�����̼�¼Ϊ��");
			return;
		}
		for (int i = 0; i < payTb.size(); i++) {
			JSONObject jObject = JSONObject.fromObject(payTb.get(i));
			PayRecord pRecord = (PayRecord)JSONObject.toBean(jObject, PayRecord.class);
//			pRecord.setCaid(caid);
			pRecord.setCheckResult(payTb.get(i).getCheckResult());
			pRecord.setId(payTb.get(i).getId());
			pDao.add(pRecord);
		}
		
		pCDao.DeleteRecordsByElement("caid", caid, owner);//����ɾ����¼
	}
	
	/**
	 * TransferPrecord_CAreaToHArea ����������Ĳ��Ǳ��µĸ����¼ת�Ƶ�������ʷ��
	 */
	public void TransferPrecord_CAreaToHArea(String owner,String caid){
		List<PayRecordCache> payTb = pCDao.GetTbByElementNot_Owner("caid",caid,owner);
		if (payTb.isEmpty() == true) {
			logger.warn("�����¼������������" + owner + "�����̼�¼Ϊ��");
			return;
		}
		for (int i = 0; i < payTb.size(); i++) {
			JSONObject jObject = JSONObject.fromObject(payTb.get(i));
			PayRecordHistory pHistory = (PayRecordHistory) JSONObject.toBean(jObject, PayRecordHistory.class);
			pHistory.setCheckResult(payTb.get(i).getCheckResult());
			pHistory.setId(payTb.get(i).getId());
			pHDao.add(pHistory);
		}
		
		pCDao.DeleteTbByEleNot_OWner("caid", caid, owner);
	}
	
	/**
	 * CallBackPrecord_HAreaToWArea �Ӹ����¼��ʷ��ȡ�ر��µĸ����¼
	 * @param owner
	 * @param caid
	 */
	public void TransferPrecord_HAreaToWArea(String owner,String caid){
		//��ת�渶���¼�������¼��ʷ
		List<PayRecordHistory> payHTb = pHDao.FindBySpeElement_AND("caid", "owner", caid, owner);

		if (payHTb.isEmpty() == true) {
			logger.warn("������ʷ���Ҳ��� ����" + owner + "|" + caid + "�����̸����¼");
			return;
		}
		
		for (int i = 0; i < payHTb.size(); i++) {
			JSONObject jObject = JSONObject.fromObject(payHTb.get(i));
			PayRecord pRecord = (PayRecord)JSONObject.toBean(jObject, PayRecord.class);
			pRecord.setCheckResult(payHTb.get(i).getCheckResult());
			pRecord.setId(payHTb.get(i).getId());
			pDao.add(pRecord);
		}
		
		pHDao.DeletePrecordTbByElement("caid", caid, owner);
	}
	
	/**
	 * TransferOri_BackupToWarea ���������Ļ����¼ת�Ƶ�������
	 * @param owner
	 */
	public void TransferOri_BackupToWarea(String owner){
		List<OriOrderBackup> orderBackups = oUp_Dao.GetOriBackupByElment("owner", owner);
		if (orderBackups.isEmpty() == true) {
			logger.warn("�����������" + owner + "�����̵ļ�¼Ϊ��");
			return;
		}
		
		for (int i = 0; i < orderBackups.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(orderBackups.get(i));
			OriOrder order  = (OriOrder) JSONObject.toBean(jsonObject,OriOrder.class);
			order.setConnectBank(orderBackups.get(i).getConnectBank());
			order.setCustomid(orderBackups.get(i).getCustomid());
			tDao.add(order);
		}
		
		oUp_Dao.DeleteOBackupByElement("owner", owner);//ɾ����Ӧ�ı��ݻ����¼��ʵ��ת��
	}
	
	/**
	 * TransferOri_WareaToBackup ת�ƹ������Ļ����¼��������
	 * @param owner
	 */
	public void TransferOri_WareaToBackup(String owner){
		List<OriOrder> oriOrders = tDao.GetTolAccountByElment("owner", owner);
		if (oriOrders.isEmpty() == true) {
			logger.warn("����������" + owner + "�����̵ļ�¼Ϊ��");
			return;
		}
		
		for (int i = 0; i < oriOrders.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(oriOrders.get(i));
			 OriOrderBackup oBackup = (OriOrderBackup) JSONObject.toBean(jsonObject,OriOrderBackup.class);
			 oBackup.setConnectBank(oriOrders.get(i).getConnectBank());
			 oBackup.setCustomid(oriOrders.get(i).getCustomid());
			oUp_Dao.add(oBackup);
		}
		
		tDao.DeleteOoriderByElement("owner", owner);//ɾ�����������¼��ʵ��ת��
	}
	
	/**
	 * TransferBinput_BackupToWarea ת�Ƴ��ɱ�������=owner��¼��������
	 * @param owner
	 */
	public void TransferBinput_BackupToWarea(String owner){
		List<BankInputBackup> bInputBackups = bInput_Backup_Dao.GetBInputBupByElment("owner", owner);
		if (bInputBackups.isEmpty() == true) {
			logger.warn("���ɱ������ļ�¼Ϊ��");
			return;
		}
		
		for (int i = 0; i < bInputBackups.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(bInputBackups.get(i));
			BankInput bInput = (BankInput) JSONObject.toBean(jsonObject,BankInput.class);
			bInput.setPayid(bInputBackups.get(i).getPayid());
			bInput.setConnectNum(bInputBackups.get(i).getConnectNum());
			bInput.setManyContract(bInputBackups.get(i).getManyContract());
			bInput.setConnectClient(bInputBackups.get(i).getConnectClient());
			bInput.setId(bInputBackups.get(i).getId());
			bInput.setCuscompanyid(bInputBackups.get(i).getCuscompanyid());
			bDao.add(bInput);
		}
		
		bInput_Backup_Dao.DeleteBInputBupByElement("owner", owner);//ɾ�����ɱ������ļ�¼��ʵ��ת��
	}
	
	/**
	 * TransferBinput_BackupToHisarea ת�Ƴ��ɱ�������=owner��¼����ʷ��
	 * @param owner
	 */
	public void TransferBinput_BackupToHisarea(String owner){
		List<BankInputBackup> bInputBackups = bInput_Backup_Dao.GetBInputBupByElment("owner", owner);
		if (bInputBackups.isEmpty() == true) {
			logger.warn("���ɱ������ļ�¼Ϊ��");
			return;
		}
		
		for (int i = 0; i < bInputBackups.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(bInputBackups.get(i));
			BankInputHistory bInput = (BankInputHistory) JSONObject.toBean(jsonObject,BankInputHistory.class);
			bInput.setPayid(bInputBackups.get(i).getPayid());
			bInput.setConnectNum(bInputBackups.get(i).getConnectNum());
			bInput.setManyContract(bInputBackups.get(i).getManyContract());
			bInput.setConnectClient(bInputBackups.get(i).getConnectClient());
			bInput.setId(bInputBackups.get(i).getId());
			bInput.setCuscompanyid(bInputBackups.get(i).getCuscompanyid());
			bHis_Dao.add(bInput);
		}
		
		bInput_Backup_Dao.DeleteBInputBupByElement("owner", owner);//ɾ�����ɱ������ļ�¼��ʵ��ת��
	}
	
	/**
	 * TransferBinput_BackupToWarea_AND ת�Ƴ��ɱ�������=owner&&caid��¼��������
	 * @param owner
	 * @param caid
	 */
	public void TransferBinput_BackupToWarea_AND(String owner,String caid){
		List<BankInputBackup> bInputBackups = bInput_Backup_Dao.GetBInputBupByElment_AND("owner", owner,"caid",caid);
		if (bInputBackups.isEmpty() == true) {
			logger.warn("���ɱ������ļ�¼Ϊ��");
			return;
		}
		
		for (int i = 0; i < bInputBackups.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(bInputBackups.get(i));
			BankInput bInput = (BankInput) JSONObject.toBean(jsonObject,BankInput.class);
			bInput.setPayid(bInputBackups.get(i).getPayid());
			bInput.setConnectNum(bInputBackups.get(i).getConnectNum());
			bInput.setManyContract(bInputBackups.get(i).getManyContract());
			bInput.setConnectClient(bInputBackups.get(i).getConnectClient());
			bInput.setId(bInputBackups.get(i).getId());
			bInput.setCuscompanyid(bInputBackups.get(i).getCuscompanyid());
			bDao.add(bInput);
		}
		
		bInput_Backup_Dao.DeleteBInputBupByElement_AND("owner", owner,"caid",caid);//ɾ�����ɱ������ļ�¼��ʵ��ת��
	}
	
	/**
	 * TransferBinput_WareaToBackup ת�ƹ������ĳ��ɼ�¼��������
	 * @param owner
	 */
	public void TransferBinput_WareaToBackup(String owner){
		List<BankInput> bInputs = bDao.GetTolBankInsByElement("owner",owner);
		if (bInputs.isEmpty() == true) {
			logger.info("���ɱ�����Ϊ��");
			return;
		}
		
		for (int i = 0; i < bInputs.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(bInputs.get(i));
			BankInputBackup bankInputBackup = (BankInputBackup) JSONObject.toBean(jsonObject,BankInputBackup.class);
			
			bankInputBackup.setPayid(bInputs.get(i).getPayid());
			bankInputBackup.setConnectNum(bInputs.get(i).getConnectNum());
			bankInputBackup.setManyContract(bInputs.get(i).getManyContract());
			bankInputBackup.setConnectClient(bInputs.get(i).getConnectClient());
			bankInputBackup.setId(bInputs.get(i).getId());
			bankInputBackup.setCuscompanyid(bInputs.get(i).getCuscompanyid());
			bInput_Backup_Dao.add(bankInputBackup);
		}
		
		bDao.DeleteBinputTbByElement("owner", owner);
	}
	
	/**
	 * TransferBinput_WareaToHistory ת�ƹ������ĳ��ɼ�¼����ʷ��
	 * @param owner
	 */
	public void TransferBinput_WareaToHistory(String owner) {
		List<BankInput> bInputs = bDao.GetTolBankInsByElement("owner",owner);
		if (bInputs.isEmpty() == true) {
			logger.info("���ɹ�����Ϊ��");
			return;
		}
		
		for (int i = 0; i < bInputs.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(bInputs.get(i));
			BankInputHistory bankInputHistory = (BankInputHistory) JSONObject.toBean(jsonObject,BankInputHistory.class);
			
			bankInputHistory.setPayid(bInputs.get(i).getPayid());
			bankInputHistory.setConnectNum(bInputs.get(i).getConnectNum());
			bankInputHistory.setManyContract(bInputs.get(i).getManyContract());
			bankInputHistory.setConnectClient(bInputs.get(i).getConnectClient());
			bankInputHistory.setId(bInputs.get(i).getId());
			bankInputHistory.setCuscompanyid(bInputs.get(i).getCuscompanyid());
			bHis_Dao.add(bankInputHistory);
		}
		
		bDao.DeleteBinputTbByElement("owner", owner);
	}
	
	/**
	 * TransferBinput_HisToWarea ת����ʷȥ�ĳ��ɼ�¼��������
	 */
	public void TransferBinput_HisToWarea(String owner,String caid){
		List<BankInputHistory> bHistories = bHis_Dao.FindBySpeElement_AND("owner", "caid", owner, caid);
		
		if (bHistories.isEmpty() == true) {
			logger.info("������ʷ��Ϊ��");
			return;
		}
		
		for (int i = 0; i < bHistories.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(bHistories.get(i));
			BankInput bankInput = (BankInput) JSONObject.toBean(jsonObject,BankInput.class);
			
			bankInput.setPayid(bHistories.get(i).getPayid());
			bankInput.setConnectNum(bHistories.get(i).getConnectNum());
			bankInput.setManyContract(bHistories.get(i).getManyContract());
			bankInput.setConnectClient(bHistories.get(i).getConnectClient());
			bankInput.setId(bHistories.get(i).getId());
			bankInput.setCuscompanyid(bHistories.get(i).getCuscompanyid());
			bDao.add(bankInput);
		}
		
		bHis_Dao.DeleteBInputHisByElement_AND("owner", owner,"caid",caid);
	}
	
	/**
	 * TransferCus_WareaToBackup ��cus_secondstore������ת�Ƶ�backup��
	 * @param owner
	 */
 	public void TransferCus_WareaToBackup(String owner){
		List<CusSecondstore> cList = sDao.GetCustomTb(owner);
		
		for (int i = 0; i < cList.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(cList.get(i));
			CusSdstoreBackup cBackup = (CusSdstoreBackup) JSONObject.toBean(jsonObject, CusSdstoreBackup.class);
			
			cBackup.setAccoutMes(cList.get(i).getAccoutMes());
			cBackup.setContractMes(cList.get(i).getContractMes());
			cBackup_Dao.add(cBackup);
		}
		
		sDao.DeleteTbByElement("owner", owner);
	}
	
	/**
	 * TransferCus_BackupToWarea ��CusSdstoreBackup����ת�Ƶ�������CusSecondstore
	 * @param owner
	 */
	public void TransferCus_BackupToWarea(String owner){
		List<CusSdstoreBackup> cBackups = cBackup_Dao.GetTbByElment("owner", owner);
		
		for (int i = 0; i < cBackups.size(); i++) {
			JSONObject jsonObject = JSONObject.fromObject(cBackups.get(i));
			CusSecondstore secondstore = (CusSecondstore) JSONObject.toBean(jsonObject, CusSecondstore.class);
			
			secondstore.setAccoutMes(cBackups.get(i).getAccoutMes());
			secondstore.setContractMes(cBackups.get(i).getContractMes());
			sDao.add(secondstore);
		}
		
		cBackup_Dao.DeleteTbByElement("owner", owner);
	}

	/*�����ϴ�ҳ�����ظ��Լ��*/
	public void isCaCover(char chooser,String caid,String owner){
		if (chooser == 'Y') {
			TransferPrecord_HAreaToWArea(owner,caid);//�Ӹ����¼��ʷ��ȡ�ظ����¼
			CaresultHistory cHistory = cDao.FindBySpeElement("caid", caid, owner).get(0);//������idΪcaid�Ķ��˽����¼��caresult�ֶ��޸�ΪD
			
			//ǰ�˽����ϴ�ҳ��
		}
		else{
			//ǰ��ҳ�汣�ֲ���
		}
	}
	
	/**
	 * DealAfterCaSucces ���˳ɹ���Ĵ�����
	 * @param owner
	 * @param caid
	 * @param caresult_url
	 */
	public void DealAfterCaSucces(String owner,String caid,String caresult_url){
			
	//	TransferPrecord_WAreaToHArea(owner);//ת�渶���¼�������¼��ʷ
		
		CaresultHistory cHistory = (CaresultHistory) cDao.FindBySpeElement("caid", caid, owner).get(0);//���ݶ���id�ҵ���Ӧ�Ķ��˽����¼
		cHistory.setCaresult('F');//�޸Ķ��˽����¼�Ľ���ֶ�ΪF
		cHistory.setUrl(caresult_url);//���˽��excel��url
		cDao.update(cHistory);
		//ǰ̨����鿴���˽��
	}

	/**
	 * CancelAndCaAgain ȡ�������¶��� 
	 * @param owner ������id
	 * @param caid ����id
	 */
	public void CancelAndCaAgain(String owner,String caid){
		logger.info("����ȡ�������¶��˴���");
		
//		TransferPrecord_CAreaToWArea(owner, caid);//���ݶ���id�Ӹ������ȡ��¼
		List<CaresultHistory> cList = cDao.FindBySpeElement("caid", caid, owner);
		if (cList.isEmpty()) {
			logger.info("������ʷ�в����ڸ��µĶ��˼�¼");
			CaresultHistory in_crhistory = new CaresultHistory();//��¼��״̬Ϊ������
			in_crhistory.setCaid(caid);
			String dateys = caid.substring(0, 4);
			String datems = caid.substring(5, 7);
			logger.info(dateys + ":" + datems);
			in_crhistory.setCayear(dateys);
			in_crhistory.setCamonth(datems);
			in_crhistory.setOwner(owner);
			in_crhistory.setCaresult('D');
			in_crhistory.setLastcaid(true);
			cDao.add(in_crhistory);
		}
		else{
			CaresultHistory cHistory = cList.get(0);
			if (cHistory != null) {
				cHistory.setCaresult('D');//�޸Ķ��˽����¼�Ľ���ֶ�ΪF
				cDao.update(cHistory);				
			}
			else {
				logger_error.info("cHistory Ϊ��");
			}
		}
	
		ResetOrider(owner);//����������Ϣ
		ResetBinputs(owner);//���������Ϣ
		ResetCustom(owner);//����ͻ���Ϣ
		
		//ǰ�˽����ϴ�ҳ��
		logger.info("ǰ�˽����ϴ�ҳ��");
	}
	
	/*����ʷ���˽�������¶���*/
	public int HisCancelAndCaAgain(String owner,String caid,String savedirA,String savedirB,String filenameA,String filenameB){
		
		List<CaresultHistory> fHLasts = cDao.FindBySpeElement("lastcaid", true, owner);
		if (fHLasts == null || fHLasts.size() == 0) {
			logger_error.error("�޷������ϴζ���");
			return -1;
		}
		CaresultHistory	fHLast = fHLasts.get(0);
		
		String curmonthcaid = CreateCaid(owner);
	/*	if (fHLast.getCaid().equals(curmonthcaid) == false) {//����ʷ�������ٴ���ת����ʷ����
			logger_error.error("�����Ƿ������������ʷ��������ת����ʷ����");
			return -2;
		}
		
		if (caid.equals(curmonthcaid) == true) {//ѡ�����ʷ�·�Ϊ����
			logger.info("ѡ�����ʷ�·�Ϊ����");
			return 1;
		}*/
		
		if (fHLast.getCaid().equals(caid) == true) {//��ʷ����ѡ����·�Ϊ�ϴζ��˵��·�
			logger.info("��ʷ����ѡ����·�Ϊ�ϴζ��˵��·�");
			return 1;
		}
		
	/*	cBackup_Dao.DeleteTbByElement("owner", owner);
		TransferCus_WareaToBackup(owner);//���ͻ���Ϣת�Ƶ�������*/
		
	//	TransferOri_WareaToBackup(owner);//ת�ƻ������������
		tDao.DeleteOoriderByElement("owner", owner);//ɾ�����������¼
		
		TransferBinput_WareaToBackup(owner);//ת�Ƴ��ɹ�����������
		
		//��������Ŀ¼�еĻ���ͳ���excel������������Ϣ
		File fileA = new File(savedirA + "/" + filenameA);
		if (!fileA.exists()) {
			logger.warn("�����" + filenameA + "�����ڣ������µ���");
		}
		else{
			//ResetCustomToCaDuring(owner);//���ÿͻ���
			
			Excel_RW excel_RW = new Excel_RW();//����excel����
			InputStream inputStream = null;
			try {
				inputStream = new FileInputStream(new File(savedirA + "/" + filenameA));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				logger_error.error("��ȡ��������" + e);
				e.printStackTrace();
				return -3;
			}
			ArrayList<Excel_Row> totalA_table = excel_RW.ReadExcel_Table(inputStream);
	
			Agent fagent = agent_Dao.findById(Agent.class, owner);
			JSONObject jsonObject = excel_RW.Table_To_Ob_OriOrders(totalA_table,fagent);//��excel��ת���ɶ���
			
			OriOrder[] in_orders = (OriOrder[]) JSONArray.toArray(jsonObject.getJSONArray("orders"), OriOrder.class);
			/*����excel������*/
			
			/*д�����ݿ�*/
			for (OriOrder order:in_orders) {

				
				/*�������ϵ����Ϣ*/
				List<ConnectPerson> fPersons = cPerson_Dao.FindBySpeElement("companyid", order.getId().getCuscompanyid(), owner);
				if (fPersons.isEmpty() !=  true) {
					order.setCustomname(fPersons.get(0).getRealName());
					order.setCustomphone(fPersons.get(0).getPhone());
					order.setCustomweixin(fPersons.get(0).getWeixin());
				}
				/*�������ϵ����Ϣ*/
				
				/*������̲�����Ϣ*/
				Agent agent = agent_Dao.findById(Agent.class, owner);
				order.setAsname(agent.getAgentConnectpname());
				order.setAsphone(agent.getAgentCpphone());
				order.setAsemail(agent.getAgentCpemail());
				/*������̲�����Ϣ*/
				
				order.setConnectBank(null);
				tDao.add(order);
				ConnectAccountWithCustom(order);
			}
			/*д�����ݿ�*/
		}
		
		TransferBinput_HisToWarea(owner, caid);//��������ʷ���ļ�¼ת�Ƶ�������
		
		TransferPrecord_WAreaToHArea(owner);//����������ļ�¼ת�Ƶ���ʷ��
		
		TransferPrecord_HAreaToWArea(owner, caid);//����ʷ����caid��¼ת�Ƶ��������
		
		CancelAndCaAgain(owner,caid);
		
		fHLast.setLastcaid(false);
		cDao.update(fHLast);
		
		CaresultHistory fHCur = cDao.FindBySpeElement("caid", caid, owner).get(0);
		fHCur.setLastcaid(true);
		cDao.update(fHCur);
		return 0;
	}
	
	public void Test_MappPayToBank(List<BankInput> mappedBankInput){
		System.out.println("result [MappPayToBank] is :");
		for(BankInput iBankInputs:mappedBankInput){
			System.out.println(iBankInputs.getPayer() + ":" + iBankInputs.getMoney() + ":" + iBankInputs.getPayee());
		}
	}

}
