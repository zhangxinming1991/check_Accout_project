package check_Asys;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.hibernate.SessionFactory;
import org.springframework.web.multipart.MultipartFile;

import com.sun.org.apache.bcel.internal.generic.GOTO;
import com.sun.org.apache.regexp.internal.recompile;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;

import check_Asys.CheckAcManage.Watch_Object;
import dao.Agent_Dao;
import dao.Assistance_Dao;
import dao.BInput_Backup_Dao;
import dao.BankInput_Dao;
import dao.CaresultHistory_Dao;
import dao.ConnectPerson_Dao;
import dao.CusSdStore_Backup_Dao;
import dao.OpLog_Dao;
import dao.Ori_BackUp_Dao;
import dao.PayRecordCache_Dao;
import dao.PayRecordHistory_Dao;
import dao.PayRecord_Dao;
import dao.ScoreIncreaseRecord_Dao;
import dao.SendStore_Dao;
import dao.Total_Account_Dao;
import en_de_code.ED_Code;
import entity.Agent;
import entity.BankInput;
import entity.CaresultHistory;
import entity.ConnectPerson;
import entity.CusSecondstore;
import entity.OpLog;
import entity.OriOrder;
import entity.PayRecord;
import entity.PayRecordHistory;
import entity.ScoreIncreaseRecord;
import file_op.AnyFile_Op;
import file_op.Excel_RW;
import file_op.AnyFile_Op.AnyFileElement;
import file_op.Excel_RW.Excel_Row;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * ����ϵͳ��ҵ����񣨶��˷���ʵ���࣬�����ʵ���˶���ϵͳ��ҵ����
 * @author zhangxinming
 *
 */
public class CheckAcManage {
	private static Logger logger = LogManager.getLogger(CheckAcManage.class);
	private static Logger logger_error = LogManager.getLogger("error");
	public AutoCheckAuccount auccount;
	public CheckARseult cARseult;//���˽��
	public Dao_List dao_List;
	public FormProduce formProduce;
	public byte m_op;
	public static final String IMPORT = "import";//����(��ò���ʮ�����Ʊ�ʾ)
	public static final String WATCH = "watch";//�鿴
	public static final String CHECK = "check";//����
	public static final String MAP = "map";//Ѱ��ƥ��ĳ��ɼ�¼
	public static final String START_CHECKWORK="start_work";//��ʼ����
	public static final String RES_PAYR = "payrecord";//������Ϣ
	public static final String RES_ORIA = "ori_account";//ԭʼ�˵�
	public static final String RES_BANKTs = "bankTs";//δ�������ɼ�¼
	public static final String RES_CAHISTORY = "caresult_history";//���˽��
	public static final String RES_PAYCACHE = "pay_cache";//���ϴ��ĸ�����Ϣ
	public static final String WATCH_CARes = "watch_cares";//�鿴���˽��
	public static final String EXPORT_CARes = "export_cares";//�������˽��
	public static final String ENTRER_CaModel = "enter_camodel";//�������ģʽ
	public static final String CANCEL_CaAgain = "cancel_calagin";//ȡ�������¶���
	public static final String FREEBACK = "freeback";//���ػ���
	public static final String HISCA = "historyca";
	
	public static final String SaveDirName_Orider = "OrderForms";
	public static final String SaveDirName_BankInput = "BankinputForms";
	public static final String FileName_Orider = "_orider.xlsx";
	public static final String FileName_BankInput = "_bankinput.xlsx";
	public String checkresult_url;
	
	public CheckAcManage(SessionFactory wFactory) {
		// TODO Auto-generated constructor stub
		  dao_List = new Dao_List(wFactory);
		  auccount = new AutoCheckAuccount(dao_List.bDao,dao_List.pDao,dao_List.tDao,dao_List.sDao,dao_List.pHDao,dao_List.cDao,dao_List.pCDao,dao_List.cPerson_Dao,dao_List.oUp_Dao,dao_List.bInput_Backup_Dao,dao_List.agent_Dao,dao_List.cBackup_Dao);
		  cARseult = new CheckARseult(dao_List);
		  formProduce = new FormProduce(wFactory, dao_List,cARseult);
	}
	  
	public void Close_All_Dao(){
		dao_List.tDao.Close_Connect(); 
		dao_List.pDao.Close_Connect();
		dao_List.bDao.Close_Connect();
		dao_List.sDao.Close_Connect();
	}
	  
	public Object OpSelect(String operation,Object object,Owner owner){
		Object re_object = null;
		if (operation.equals(IMPORT)) {//�ϴ���������ɱ�
			Import_Object select = (Import_Object) object;
			return Import(select.import_type,select.file,select.operator,select.savepath,select.filename,select.caid);
		}
		else if(operation.equals(MAP)){
			Map_Object map_Object = (Map_Object) object;
			List<BankInput> fBankInputs = Map(map_Object,owner.work_id);
			re_object = fBankInputs;
		}
		else if(operation.equals(CHECK)){
			
		}
		else if(operation.equals(START_CHECKWORK)){
			String caid = (String) object;

			re_object = StartCheckWork(owner.work_id);
		}
		else if (operation.equals(WATCH_CARes)) {
			Watch_CAResObject watch_select = (Watch_CAResObject) object;
			re_object = Watch_CheckAResult(watch_select,watch_select.operator);
		}
		else if (operation.equals(EXPORT_CARes)) {
			Export_CAResObject export_CAResObject = (Export_CAResObject) object;
			Export_CheckA_Result(export_CAResObject,owner);
		}
		else if (operation.equals(ENTRER_CaModel)) {
			//Enter_CaModel(owner.work_id);
			String who = (String) object;
			String caid = auccount.Enter_CaModel(owner.work_id);
			JSONObject jsonObject = new JSONObject();
			/*�����ϴ��ϴ���ʱ����ϴ��Ľ��*/
		/*	List<OpLog> fLogs = dao_List.opLog_Dao.FindBySpeElement_S_ByOwner("content", OpLog_Service.IMPORT, who);
			
			JSONObject jsonObject = new JSONObject();
			if (fLogs.size() == 0) {
				jsonObject.element("caid", caid);
				jsonObject.element("flag", 0);
				jsonObject.element("lastUploadResult", "û���ϴ���¼");
				jsonObject.element("lastUploadTime", "1997/00/00/_00:00:00");
			}
			else {
				String time = fLogs.get(fLogs.size() - 1).getTime();
				String result = fLogs.get(fLogs.size() - 1).getResult();
				String curym = new SimpleDateFormat("yyyy/MM").format(new Date());
				
				String yearanmonth = caid.substring(0, 7);
				String newym = yearanmonth.replace('-', '/');
				//if (time.contains(curym)) {
				if (time.contains(newym)) {
					jsonObject.element("caid", caid);
					jsonObject.element("flag", 0);
					jsonObject.element("lastUploadResult", result);
					jsonObject.element("lastUploadTime", time.replace('/', '-'));
				}
				else {
					jsonObject.element("caid", caid);
					jsonObject.element("flag", 0);
					jsonObject.element("lastUploadResult", "����û���ϴ���¼");
					jsonObject.element("lastUploadTime", "1997/00/00/_00:00:00");
				}
			}	*/
			jsonObject = FindLastUploadTime(who,caid);
			jsonObject.element("caid", caid);
			jsonObject.element("flag", 0);
			/*�����ϴ��ϴ���ʱ����ϴ��Ľ��*/	
			re_object = jsonObject;
		}
		else if (operation.equals(CANCEL_CaAgain)) {
			String caid = (String)object;
			CancelAndCaAgain(owner.work_id,caid);
			
			JSONObject jsonObject = new JSONObject();
			jsonObject = FindLastUploadTime(owner.who, caid);
			re_object = jsonObject;
		}
		else if (operation.equals(FREEBACK)) {
			FreeBackToCustom(owner.work_id,owner.who);
		}
		else if (operation.equals(HISCA)) {
			String caid = (String)object;
			JSONObject jsonObject = new JSONObject();
			jsonObject = FindLastUploadTime(owner.who, caid);
			re_object = jsonObject;
		}
		else{
			System.out.println("unknow operation");
		}
		
		return re_object;
	}
	
	public JSONObject FindLastUploadTime(String who,String caid){
		/*�����ϴ��ϴ���ʱ����ϴ��Ľ��*/
		List<OpLog> fLogs = dao_List.opLog_Dao.FindBySpeElement_S_ByOwner("content", OpLog_Service.IMPORT, who);
		
		JSONObject jsonObject = new JSONObject();
		if (fLogs.size() == 0) {
			jsonObject.element("lastUploadResult", "û���ϴ���¼");
			jsonObject.element("lastUploadTime", "1997/00/00/_00:00:00");
		}
		else {
			String time = fLogs.get(fLogs.size() - 1).getTime();
			String result = fLogs.get(fLogs.size() - 1).getResult();
			String curym = new SimpleDateFormat("yyyy/MM").format(new Date());
			
			String yearanmonth = caid.substring(0, 7);
			String newym = yearanmonth.replace('-', '/');
			//if (time.contains(curym)) {
			if (time.contains(newym)) {
				jsonObject.element("lastUploadResult", result);
				jsonObject.element("lastUploadTime", time.replace('/', '-'));
			}
			else {
				jsonObject.element("lastUploadResult", "����û���ϴ���¼");
				jsonObject.element("lastUploadTime", "1997/00/00/_00:00:00");
			}
		}	
		/*�����ϴ��ϴ���ʱ����ϴ��Ľ��*/
		
		return jsonObject;
	}
	
	/*ͳһ�����ͻ�*/
	public void FreeBackToCustom(String owner,String hander){
		List<PayRecord> pList = dao_List.pDao.GetPrecordTbByElement("owner", owner);
		
		for (int i = 0; i < pList.size(); i++) {
			PayRecord pRecord = pList.get(i);
			boolean freeback = auccount.isFreeBack(pRecord,owner);
			if (freeback) {
				/*�������*/
				ScoreIncreaseRecord in_sr = new ScoreIncreaseRecord();
				in_sr.setHander(hander);
				
				String connectp = pRecord.getConnPerson();
				in_sr.setUsername(connectp);
				
				Date date = new Date();
				Timestamp time = new Timestamp(date.getTime());
				in_sr.setTime(time);
				in_sr.setDescription("ƥ��ͨ������û���");
				
				Integer status = 2;
				in_sr.setStatus(status.byteValue());
				int source = ChangeMoneyTosource(pRecord.getPayMoney(),0.001);
				in_sr.setIncreaseScore(source);
				dao_List.sRc_Dao.add(in_sr);
				/*�������*/
			}
		}
	}
	
	/**/
	public int ChangeMoneyTosource(Double pay_money,Double coefficient){
		return (int) (pay_money * coefficient);
	}
	
	/*�������ģʽ*/
	public void Enter_CaModel(String owner){
		auccount.Enter_CaModel(owner);
	}
	
	/**
	 * Import_Initial ����ĳ�ʼ������
	 * @param import_type ��������
	 * @param owner ӵ����
	 */
	public void Import_Initial(String caid,char import_type,String owner){
		if (auccount.CreateCaid(owner).equals(caid)) {//���¶���
			if (import_type == 'A') {
				auccount.TransferCus_WareaToBackup(owner);
				auccount.TransferOri_WareaToBackup(owner);//ת�ƻ����������
			}
			else if (import_type == 'B') {
				auccount.TransferBinput_WareaToBackup(owner);//ת�Ƴ��ɵ�������
			}
			else {
				logger_error.error("��������δ֪");
			}			
		}
		else {//��ʷ����
			if (import_type == 'A') {
				dao_List.tDao.DeleteOoriderByElement("owner", owner);
				dao_List.sDao.DeleteTbByElement("owner", owner);
			}
			else if (import_type == 'B') {
				dao_List.bDao.DeleteBinputTbByElement("owner", owner);
			}
			else {
				logger_error.error("��������δ֪");
			}	
		}

	}
	
	/**
	 * Import_AfterWork ���ݵ�������Ľ�������в�ͬ�ĺ�������
	 * @param flag
	 */
	public void Import_AfterWork(int flaga,int flagb,String owner,MultipartFile rfilea,MultipartFile rfileb,String filenamea,String filenameb,String savedira,String savedirb,String caid){
		if (auccount.CreateCaid(owner).equals(caid)) {//���¶���

			if (flaga == 0 && flagb == 0) {//�������ϴ��ɹ�
				logger.info("���¶���,ɾ���������Ļ����¼�ͳ��ɼ�¼");
				dao_List.cBackup_Dao.DeleteTbByElement("owner", owner);
				dao_List.oUp_Dao.DeleteOBackupByElement("owner", owner);
				dao_List.bInput_Backup_Dao.DeleteBInputBupByElement("owner", owner);
			}
		}
		
		if (flaga == 0 && flagb == 0) {//�������ϴ��ɹ�
			logger.info("�����ͳ��ɱ��ϴ��ɹ�");
		
			AnyFile_Op aOp = new AnyFile_Op();
			long filesizea = rfilea.getSize();
			long filesizeb = rfileb.getSize();
			//	String fileName = eCode.ISO_To_UTF8(rfile.getOriginalFilename());//���ļ����ַ���ת��utf-8��ʽ
			AnyFileElement aElementa = aOp.new AnyFileElement(filenamea, savedira, (int)filesizea);
			AnyFileElement aElementb = aOp.new AnyFileElement(filenameb, savedirb, (int)filesizeb);
			
			File dira = aOp.CreateDir(savedira);//��������Ŀ¼
			File dirb = aOp.CreateDir(savedirb);//��������Ŀ¼
			
			File wFilea = aOp.CreateFile(aElementa.dirname, aElementa.filename);//����������ļ�	
			byte read_ba[] = aOp.ReadFile(rfilea);//��ȡ�ϴ����ļ�������
			aOp.WriteFile(aElementa,read_ba,wFilea);//���ļ����浽������ָ��Ŀ¼��
			
			File wFileb = aOp.CreateFile(aElementb.dirname, aElementb.filename);//����������ļ�
			byte read_bb[] = aOp.ReadFile(rfileb);//��ȡ�ϴ����ļ�������
			aOp.WriteFile(aElementb,read_bb,wFileb);//���ļ����浽������ָ��Ŀ¼��
		}
		else {
			logger_error.error("��������δ֪");
		}
	}
	
	/**
	 * Import 
	 * @param import_type
	 * @param rfile
	 * @param owner
	 * @param savedir
	 * @param filename
	 */
	public JSONObject Import(char import_type,MultipartFile rfile,String owner,String savedir,String filename,String caid){
		JSONObject re_js = new JSONObject();
		
		Import_Initial(caid,import_type,owner);
		
		if (import_type == 'A') {//����ԭʼ�˵�
			logger.info("import Account");
			
			/*����excel������*/
			Excel_RW excel_RW = new Excel_RW();//����excel����
			ArrayList<Excel_Row> totalA_table = null;
			try {
				totalA_table = excel_RW.ReadExcel_Table(rfile.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Agent fagent = dao_List.agent_Dao.findById(Agent.class, owner);
			JSONObject re_TT = excel_RW.Table_To_Ob_OriOrders(totalA_table,fagent);//��excel��ת���ɶ���
			/*����excel������*/	
			if (re_TT.getInt("flag") == -1) {
				re_js.element("flag", -1);
				re_js.element("errmsg", re_TT.getString("errmsg"));
				return re_js;
			}
			JSONArray jsonArray = (JSONArray) re_TT.get("orders");
			OriOrder[]orders = (OriOrder[]) JSONArray.toArray(jsonArray, OriOrder.class);
			
			/*д�����ݿ�*/
			for (OriOrder order:orders) {
				
				/*�������ϵ����Ϣ*/
				List<ConnectPerson> fPersons = dao_List.cPerson_Dao.FindBySpeElement("companyid", order.getId().getCuscompanyid(), owner);
				if (fPersons.isEmpty() !=  true) {
					order.setCustomname(fPersons.get(0).getRealName());
					order.setCustomphone(fPersons.get(0).getPhone());
					order.setCustomweixin(fPersons.get(0).getWeixin());
				}
				/*�������ϵ����Ϣ*/
				
				/*������̲�����Ϣ*/
				Agent agent = dao_List.agent_Dao.findById(Agent.class, owner);
				order.setAsname(agent.getAgentConnectpname());
				order.setAsphone(agent.getAgentCpphone());
				order.setAsemail(agent.getAgentCpemail());
				/*������̲�����Ϣ*/
				
				order.setConnectBank(null);
				dao_List.tDao.add(order);
				auccount.ConnectAccountWithCustom(order);  //��ӻ���ˢ�¿ͻ���ͬ��Ϣ
			}
			/*д�����ݿ�*/
			re_js.element("flag", 0);
			re_js.element("errmsg", "��������ɹ�");
			return re_js;
		}
		else if (import_type == 'B') {//�������м�¼
			logger.info("import bank");
			auccount.ResetCustomAccoutMsg(owner);
			/*����excel�ļ�*/
			Excel_RW excel_RW = new Excel_RW();//����excel����
			ArrayList<Excel_Row> totalA_table = null;
			try {
				totalA_table = excel_RW.ReadExcel_Table(rfile.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ArrayList<BankInput> bankInputs = excel_RW.Table_To_Ob_BankIn(totalA_table,owner);
			/*����excel�ļ�*/
			
			/*д�����ݿ�*/
			int offsetid = dao_List.bDao.GetMaxID();
			for (int i = 0; i < bankInputs.size(); i++) {
				BankInput bankInput = bankInputs.get(i);
				offsetid = offsetid + 1;
				bankInput.setId(offsetid);
				dao_List.bDao.add(bankInput);
				auccount.ConnectBankinputWithCustom(bankInput);//��ȡ�ͻ��ĺ�ͬ��Ϣ
			}
			/*д�����ݿ�*/
			re_js.element("flag", 0);
			re_js.element("errmsg", "������ɱ�ɹ�");
			return re_js;
		}
		else {
			logger.info("[Import]:unknow import");
			re_js.element("flag", -1);
			re_js.element("errmsg", "δ֪��������");
			return re_js;
		}
	}
	
	/*�������˽��*/
	public void Export_CheckA_Result(Export_CAResObject eResObject,Owner owner){
		//formProduce.CreateForm(eResObject.exportlist,owner.work_id, owner.user_type, eResObject.dir,eResObject.filename);
	}
	
	/*�鿴����*/
	public JSONObject Watch(Watch_Object location,Owner owner,int offset,int pagesize){
		JSONObject re_json = new JSONObject();
		java.util.List list = null;
		int num = 0;
		switch (location.watch_type) {
		case 'T'://�鿴���ű�
			if (location.table_name.equals(CheckAcManage.RES_PAYR)) {//�鿴�����¼
				logger.info("watch payrecord table");
				/*���ݲ�ͬ���û����ͻ�ȡ��ͬ�Ķ�������*/
					if (owner.user_type.equals("bu")) {//������Ա
						//list = dao_List.pDao.FindBySpeElement_S("owner", owner.work_id);
						list = dao_List.pDao.FindBySpeElement_S_Page("owner", owner.work_id, offset, pagesize);
						num = dao_List.pDao.GetPayTb_Num_ByElement("owner", owner.work_id);
					}
					else if(owner.user_type.equals("bm")){//�ܼ�
						
					}
					else {
						System.out.println("unknow usetype");
					}
				//	list = pDao.GetTolAccount();
					//list = dao_List.pDao.FindBySpeElement_S_N("pass", true);
					/*���ݲ�ͬ���û����ͻ�ȡ��ͬ�Ķ�������*/
			}
			else if (location.table_name.equals(CheckAcManage.RES_ORIA)) {//�鿴ԭʼ�˵�
				System.out.println("watch ori_account table");
				/*���ݲ�ͬ���û����ͻ�ȡ��ͬ�Ķ�������*/
				if (owner.user_type.equals("bu")) {
					list = dao_List.tDao.FindBySpeElement_S("owner", owner.work_id);	
				}
				else if(owner.user_type.equals("bm")){
					list = dao_List.tDao.GetTolAccount();
				}
				else {
					System.out.println("unknow usetype");
				}
				/*���ݲ�ͬ���û����ͻ�ȡ��ͬ�Ķ�������*/
			}
			else if(location.table_name.equals(CheckAcManage.RES_BANKTs)){
				System.out.println("watch bankInputs table");
			//	list = bDao.GetTolBankIns();
				list = dao_List.bDao.FindBySpeElement("status", false,owner.work_id);
				
			}
			else if (location.table_name.equals(CheckAcManage.RES_CAHISTORY)) {
				logger.info("�鿴���˼�¼��ʷ");
				list = dao_List.cDao.FindBySpeElement_ByPage("cayear", location.cayear, owner.work_id,offset,pagesize);
				logger.info(list.size() + location.cayear + owner.work_id);
				
				JSONArray orders = new JSONArray();//json���е�����
				
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
				re_json.element("data", orders);//��������ӵ�������
				return re_json;
			}
			else if(location.table_name.equals(CheckAcManage.RES_PAYCACHE)){
				logger.info("�鿴Ԥ�����¼��ʷ" + "owner=" + owner.work_id);
				list = dao_List.pCDao.GetPayRecordsTb(owner.work_id);
				logger.info(list.size());
			}
			else {
				logger.error("δ֪�鿴����");
			}
			break;
		case 'S'://�鿴������¼
			break;
		default:
			System.out.println("unknow type" + location.watch_type);
			break;
		}
		
		re_json.element("data", list);
		if (num % 10 > 0) {//������ҳ��
			re_json.element("totalpage", num/10 + 1);
		}
		else {
			re_json.element("totalpage", num/10);
		}
		return re_json;
		//return list;
	}

	/*Ѱ��ƥ�估ȷ��Ψһƥ�书��*/
	public List<BankInput> Map(Map_Object map_Object,String owner){
		List<BankInput> resultMapp = null;
		
		if (map_Object.map_opString.equals("find_map")) {//���Һ͸����¼��صĳ��ɼ�¼
			PayRecord pRecord = dao_List.pDao.findById(PayRecord.class, map_Object.pay_id);//��ȡ�����������¼��id
			resultMapp = auccount.MappPayToBank(pRecord.getId(),owner);
		}
		else if (map_Object.map_opString.equals("cer_map")) {//��ȡΨһ���������ĳ��ɼ�¼
			BankInput bInput = dao_List.bDao.findById(BankInput.class, map_Object.bank_id);
			PayRecord pRecord = dao_List.pDao.findById(PayRecord.class, map_Object.pay_id);
			
			if (pRecord.getIsconnect() == true) {//���֮ǰ�Ѿ��󶨳���
				auccount.CancelConnecttBWithP(pRecord.getId(), pRecord.getBankinputId());//ȡ��֮ǰ�İ���Ϣ�����������¼�����ɼ�¼	
			}
			auccount.ConnectBankWithPay(map_Object.pay_id, map_Object.bank_id,pRecord.getContractNum(),pRecord.getManyPay());
			
			resultMapp = new ArrayList<BankInput>();
			resultMapp.add(0, bInput);	
		}
		else if (map_Object.map_opString.equals("cancel_map")) {
			auccount.CancelConnectBkAndPay(map_Object.pay_id, map_Object.bank_id);
		}
		else {
			System.out.println("[Map]:unknow map_op:" + map_Object.map_opString);
			
		}
		
		return resultMapp;
	}
	
	/*���Ĺ���*/
	public void Check(int id,String check_op){
			PayRecord pRecord = dao_List.pDao.findById(PayRecord.class, id);//��ȡ�����������¼��id
			
			if (check_op.equals("yes")) {
				
			}
			else if(check_op.equals("no")){
				
			}
	}
	
	/*��ʼ���˹���*/
	public JSONObject StartCheckWork(String owner){
		JSONObject jmesg = new JSONObject();
		jmesg.element("flag", -1);
		int flag = -1;
		if(auccount.IsStartCheckWork(owner).getInt("flag") == -1){
			jmesg.element("flag", -1);
			jmesg.element("errmsg", auccount.IsStartCheckWork(owner).getString("errmsg"));
			return jmesg;
			//return -1;
		}
		else if (auccount.IsStartCheckWork(owner).getInt("flag") ==0){//�������ˣ����ɼ�¼���������������
			List lbank = dao_List.bDao.GetTolBankInsByElement("owner",owner);
			
			System.out.println(lbank.size());
			for (int i = 0; i < lbank.size(); i++) {
				BankInput bInput = (BankInput) lbank.get(i);
				logger.info("***����" + bInput.getId() + "���ɼ�¼�Ĺ���***");
				if (bInput.getIsConnect() == true) {//ʹ�ú�ͬ��ȥ���������
					logger.info("ʹ�ú�ͬ��ȥ��������" + bInput.getPayer());
					jmesg = auccount.ConnectBankWithAccount(bInput,owner);
					if ((int)jmesg.get("flag") == -1) {
						logger.info("ʹ�ú�ͬ��ȥ��������" + bInput.getPayer() + "ʧ��");
					}
				}
				
				if ((int)jmesg.get("flag") == -1) {//ʹ�ÿͻ�����ȥ���������
					CusSecondstore fcustom = dao_List.sDao.findById(CusSecondstore.class, bInput.getPayer());//�ĳ��ó��ɼ�¼�еİ󶨵��ֻ��û��ĵ�λid�������֤ȥ�ң�ͬʱ�ͻ����е������ĳɻ�����пͻ���id
					if (fcustom == null) {
						logger.warn("�Ҳ���������Ϊ��" + bInput.getPayer() + "�Ŀͻ�");
					}
					
					else {
						if (fcustom.getOwner().equals(owner)) {//�������̸�����ϢҲ��Ϊ�ж�����
							if(fcustom.getContractMes() == null){//û�к�ͬ��Ϣ
								logger.info(fcustom.getClient() + "�޺�ͬ��Ϣ");
								auccount.ConnectBankWithCustom(bInput,bInput.getPayer(),bInput.getMoney());
							}
							else {
								if (fcustom.getContractNum() <= 1) {
									logger.info(fcustom.getClient() + "ֻ��һ����������,ֱ�ӹ����������");
									String paymentNature = JSONArray.fromObject(fcustom.getContractMes()).getString(0);
									auccount.ConnectBankWithAccount_Only(bInput,paymentNature);//����ͻ�ֻ��һ����ͬ����ֻ��һ����ͬǷ��øú�ͬ��ȥ����
								}
								else {//������ɼ�¼�������ͻ�����
									logger.info("����" + bInput.getId() + ":" + bInput.getPayer() + "���ɼ�¼���ͻ�����");
									auccount.ConnectBankWithCustom(bInput,bInput.getPayer(),bInput.getMoney());
								}
							}
	
						}
						else{
							logger.warn("���ɼ�¼�Ĵ����̺Ϳͻ����µĴ�������Ϣ��һ�£�");
						}						
					}
				}
				logger.info("***����" + bInput.getId() + "�����¼�Ĺ���***");
			}
			
			jmesg.element("flag", 0);
			jmesg.element("errmsg", "���˳ɹ�");
			return jmesg;
		}
		else {
			jmesg.element("flag", -5);
			jmesg.element("errormsg", auccount.IsStartCheckWork(owner).getString("errormsg"));
			return jmesg;
		}
	}

	
	/*���˺�鿴���˽��*/
	public Object Watch_CheckAResult(Watch_CAResObject wCaResObject,String owner){
		List<BankInput> fBankInputs = null;
		List<PayRecord> fPayRecords = null;
		List<OriOrder> fOrders = null;
		if (wCaResObject.watch_restype.equals("bfailconnect")) {//���1���޷������ĺ�ͬ�Ż��߿ͻ����µĳ��ɼ�¼
			fBankInputs = cARseult.Produce_BInputFailConnect(owner);
			cARseult.Test_ResultB("FailConnect",fBankInputs);
			return fBankInputs;
		}
		else if (wCaResObject.watch_restype.equals("btocontract")) {//���2����������ͬ�ŵĳ��ɼ�¼
			fBankInputs = cARseult.Produce_BInputToContract(owner);
			cARseult.Test_ResultB("btocontract", fBankInputs);
			return fBankInputs;
		}
		else if (wCaResObject.watch_restype.equals("btoclient")) {//���3���������ͻ����µĳ��ɼ�¼
			fBankInputs = cARseult.Produce_BInputToClient(owner);
			cARseult.Test_ResultB("btoclient", fBankInputs);
			return fBankInputs;
		}
		else if (wCaResObject.watch_restype.equals("bnopay")) {//���4��û�й�����������Ϣ�ĳ��ɼ�¼(�ͻ�û���ϴ�������Ϣ)
			fBankInputs = cARseult.Produce_BInputNoPayRecord(owner);
			cARseult.Test_ResultB("bnopay", fBankInputs);
			return fBankInputs;
		}
		else if (wCaResObject.watch_restype.equals("bhaspay")) {//���5��������������Ϣ�ĳ��ɼ�¼
			fBankInputs = cARseult.Produce_BInputHasPayRecord(owner);
			cARseult.Test_ResultB("bhaspay", fBankInputs);
			return fBankInputs;
		}
		else if (wCaResObject.watch_restype.equals("phasbinput")) {//���6��������������Ϣ���ֻ������¼
			fPayRecords = cARseult.Produce_PayHasBInput(owner);
			cARseult.Test_ResultP("phasbinput", fPayRecords);
			return fPayRecords;
		}
		else if (wCaResObject.watch_restype.equals("truepnobinput")) {//���7��û�й�����������Ϣ����ʵ�ֻ������¼(�߿�)
			fPayRecords = cARseult.Produce_TruePayNoBInput(owner);
			cARseult.Test_ResultP("truepnobinput", fPayRecords);
			return fPayRecords;
		}
		else if (wCaResObject.watch_restype.equals("falsepnobinput")) {//���8��û�й�����������Ϣ����ʵ�ֻ������¼(������Ϣ)
			fPayRecords = cARseult.Produce_FalsePayNoBInput(owner);
			cARseult.Test_ResultP("falsepnobinput", fPayRecords);
			return fPayRecords;
		}
		else if(wCaResObject.watch_restype.equals("onobinput")){//���9������û���յ�����Ļ����¼
			fOrders = cARseult.Produce_OriorderNoBInput(owner);
			cARseult.Test_ResultO("onobinput",fOrders);
			return fOrders;
		}
		else if(wCaResObject.watch_restype.equals("ohasbinput")){//���10���������յ�����Ļ����¼
			fOrders = cARseult.Produce_OriorderHasBInput(owner);
			cARseult.Test_ResultO("onobinput",fOrders);
			return fOrders;
		}
		//��Ӹ���Ķ��˽��
		else{
			System.out.println("unknow request of watch result_type");
			return null;
		}
	}
	
	/*ȡ�������¶���*/
	public Object  CancelAndCaAgain(String owner,String caid){
		int flag = -1;
		auccount.CancelAndCaAgain(owner, caid);
		
		flag = 0;
		return flag;
	}
	
	public class Map_Object{
		public String map_opString;
		public int pay_id;
		public int bank_id;
		
		public Map_Object(String map_opString,int pay_id){
			this.map_opString = map_opString;
			this.pay_id = pay_id;
		}
		public Map_Object(String map_opString,int pay_id,int bank_id) {
			// TODO Auto-generated constructor stub
			this.map_opString = map_opString;
			this.pay_id = pay_id;
			this.bank_id = bank_id;
		}
	}
	
	public class Import_Object{
		public 	String operator;//������
		public char import_type;
		public MultipartFile file;
		public String savepath;//����Ŀ¼
		public String filename;
		public String caid;
		
		public Import_Object(char import_type,MultipartFile file,String operator,String savepath,String filename,String caid) {
			this.import_type = import_type;
			this.file = file;
			this.operator = operator;
			this.savepath = savepath;
			this.filename = filename;
			this.caid = caid;
		}
	}
	
	public class Watch_Object{//order_num��id�ĳ�ֻ�ܴ���ĳһ���Ľṹ
		public char watch_type;
		public String table_name;
		public String order_num;//ԭʼ�˵�����
		public int id;//���죬����δ������������
		public String cayear;//������ʷ��������
		public Watch_Object(){
		
		}
	}
	
	public Watch_Object Create_Watch_Object(JSONObject jstr){
		Watch_Object wObject = new Watch_Object();//���ò鿴�Ĳ���
		wObject.watch_type = (char) jstr.getString("watch_type").charAt(0);//���ò鿴������,ǰ̨����
		wObject.table_name = jstr.getString("table_name");//�鿴��Դ����,ǰ̨����
		wObject.order_num = null;//ǰ̨����
		wObject.id = -1;//ǰ̨����
		if (jstr.has("year")) {
			wObject.cayear = (String) jstr.getString("year");	
		}
		return wObject;
	}
	
	public class Dao_List{
		public Total_Account_Dao tDao;//�������˵�ҵ��
		public PayRecord_Dao pDao;
		public BankInput_Dao bDao;
		public SendStore_Dao sDao;
		public Assistance_Dao aDao;
		public PayRecordHistory_Dao pHDao;
		public CaresultHistory_Dao cDao;
		public PayRecordCache_Dao pCDao;
		public Agent_Dao agent_Dao;
		public ConnectPerson_Dao cPerson_Dao;
		public Ori_BackUp_Dao oUp_Dao;
		public BInput_Backup_Dao bInput_Backup_Dao;
		public CusSdStore_Backup_Dao cBackup_Dao;
		public OpLog_Dao opLog_Dao;
		public ScoreIncreaseRecord_Dao sRc_Dao;
		
		public Dao_List(SessionFactory wFactory){
			  tDao = new Total_Account_Dao(wFactory); 
			  pDao = new PayRecord_Dao(wFactory);
			  bDao = new BankInput_Dao(wFactory);
			  sDao = new SendStore_Dao(wFactory);
			  aDao = new Assistance_Dao(wFactory);
			  pHDao = new PayRecordHistory_Dao(wFactory);
			  cDao = new CaresultHistory_Dao(wFactory);
			  pCDao = new PayRecordCache_Dao(wFactory);
			  agent_Dao = new Agent_Dao(wFactory);
			  cPerson_Dao = new ConnectPerson_Dao(wFactory);
			  oUp_Dao = new Ori_BackUp_Dao(wFactory);
			  bInput_Backup_Dao = new BInput_Backup_Dao(wFactory);
			  cBackup_Dao = new CusSdStore_Backup_Dao(wFactory);
			  opLog_Dao = new OpLog_Dao(wFactory);
			  sRc_Dao = new ScoreIncreaseRecord_Dao(wFactory);
		}
	}
	
	public class Watch_CAResObject{
		public String watch_restype;
		public String operator;
		public  Watch_CAResObject(String watch_restype,String operator){
			this.watch_restype = watch_restype;
			this.operator = operator;
		}
	}
	
	public class Export_CAResObject{
		public String dir;
		public String filename;
		public List<String> exportlist;
		
		public Export_CAResObject(String dir,List<String> exportlist,String filename){
			this.dir = dir;
			this.exportlist = exportlist;
			this.filename = filename;
		}
	}
	
	public class Owner{
		public String work_id;//������id
		public String user_type;
		public String who;//�û���
		public Owner() {
	}
		// TODO Auto-generated constructor stub
	}
}
	

