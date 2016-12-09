package check_Asys;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.web.multipart.MultipartFile;

import com.sun.org.apache.regexp.internal.recompile;

import check_Asys.CheckAcManage.Dao_List;
import dao.Assistance_Dao;
import dao.BankInput_Dao;
import dao.ConnectPerson_Dao;
import dao.PayRecordCache_Dao;
import dao.PayRecordHistory_Dao;
import dao.PayRecord_Dao;
import dao.SendStore_Dao;
import dao.Total_Account_Dao;
import entity.ConnectPerson;
import entity.CusSecondstore;
import entity.PayRecord;
import entity.PayRecordCache;
import file_op.AnyFile_Op;
import file_op.AnyFile_Op.AnyFileElement;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * �ͻ��ϴ���Ϣ�����࣬������ṩ���ܿͻ��ϴ�������Ϣ�ķ���
 * @author zhangxinming 
 * @version 1.0.0
 *
 */
public class ConnectP_PayService {
	private static Logger logger = LogManager.getLogger(ConnectP_PayService.class);
	public PayRecord_Dao pDao;
	public ConnectPerson_Dao cDao;
	public PayRecordCache_Dao pCDao;
	public PayRecordHistory_Dao pHDao;
	public SendStore_Dao sDao;
	
	public ConnectP_PayService(SessionFactory wFactory) {
		// TODO Auto-generated constructor stub
		pDao = new PayRecord_Dao(wFactory);
		cDao = new ConnectPerson_Dao(wFactory);
		pCDao = new PayRecordCache_Dao(wFactory);
		pHDao = new PayRecordHistory_Dao(wFactory);
		sDao = new SendStore_Dao(wFactory);
	}
	
	/**
	 * Upload_Pay ���ܿͻ��ϴ��ĸ�����Ϣ
	 * @param pRecord ������Ϣ�����ֲ���
	 * @param mfile ������Ϣ��ƾ֤����
	 * @param savedir ƾ֤�����·��
	 * @param fileName ƾ֤������ļ���
	 * @author zhangxinming
	 */
	public void Upload_Pay(PayRecordCache pRecord,MultipartFile mfile,String savedir,String fileName){
		
		if (mfile != null) {
			AnyFile_Op aOp= new AnyFile_Op();
			long filesize = mfile.getSize();
		
			AnyFileElement aElement = aOp.new AnyFileElement(fileName, savedir, (int)filesize);
			
			/*��ȡ�������ļ�*/
			aOp.CreateDir(aElement.dirname);
			File upload_file = aOp.CreateFile(aElement.dirname, aElement.filename);
			byte read_b[] = aOp.ReadFile(mfile);
			aOp.WriteFile(aElement, read_b, upload_file);
			/*��ȡ�������ļ�*/	
		}
		
		pRecord.setPass(false);
		pRecord.setLinkCer("/check_Accout/" + "�����¼/" + pRecord.getOwner() + "/" + pRecord.getPayer() + "/" + fileName);
		pCDao.add(pRecord);
	}
	
	public void Save_UploadPicture(MultipartFile mfile,String savedir,String fileName){
		if (mfile != null) {
			AnyFile_Op aOp= new AnyFile_Op();
			long filesize = mfile.getSize();
		
			AnyFileElement aElement = aOp.new AnyFileElement(fileName, savedir, (int)filesize);
			
			/*��ȡ�������ļ�*/
			aOp.CreateDir(aElement.dirname);
			File upload_file = aOp.CreateFile(aElement.dirname, aElement.filename);
			byte read_b[] = aOp.ReadFile(mfile);
			aOp.WriteFile(aElement, read_b, upload_file);
			/*��ȡ�������ļ�*/	
		}
	}
	
	/**
	 * GetMaxId_InPayCWH����ȡ�����¼�������е����id��¼����Ϣ
	 * @return
	 */
	public int GetMaxId_InPayCWH(){
		int maxid_c = -1;
		int maxid_w = -1;
		int maxid_h = -1;
		int maxid = -1;
		
		maxid_c = pCDao.GetMaxID();
		maxid_w = pDao.GetMaxID();
		maxid_h = pHDao.GetMaxID();
		
		if (maxid_w > maxid_c) {
			maxid = maxid_w;
		}
		else {
			maxid = maxid_c;
		}
		if (maxid_h > maxid) {
			maxid = maxid_h;
		}
		return maxid;
	}
	
	/**
	 * Upload_PayToWArea �ú���������
	 * @param pRecord
	 * @param mfile
	 * @param savedir
	 * @param fileName
	 * @deprecated
	 * @author zhangxinming
	 */
	public void Upload_PayToWArea(PayRecord pRecord,MultipartFile mfile,String savedir,String fileName){
		
		AnyFile_Op aOp= new AnyFile_Op();
		long filesize = mfile.getSize();
		AnyFileElement aElement = aOp.new AnyFileElement(fileName, savedir, (int)filesize);
		
		/*��ȡ�������ļ�*/
		aOp.CreateDir(aElement.dirname);
		File upload_file = aOp.CreateFile(aElement.dirname, aElement.filename);
		byte read_b[] = aOp.ReadFile(mfile);
		aOp.WriteFile(aElement, read_b, upload_file);
		/*��ȡ�������ļ�*/
		
		pRecord.setPass(false);
		pRecord.setLinkCer("/check_Accout/" + "�����¼/" + pRecord.getOwner() + "/" + pRecord.getPayer() + "/" + fileName);
		pDao.add(pRecord);
	}

	/**
	 * Get_CandA ��ȡ�ͻ��ĺ�ͬ�ź͸����˺���Ϣ���ڿͻ��ϴ���ʱ���ṩ���û�ѡ�񣬲��ÿͻ�����
	 * @param username ������ϵ�˵��û���
	 * @return
	 * @author zhangxinming
	 */
	public JSONObject Get_CandA(String username){
		ConnectPerson fPerson = cDao.findById(ConnectPerson.class, username);
		CusSecondstore customs = sDao.findById(CusSecondstore.class, fPerson.getCompany());
		
		/*��ȡ��ͬ��Ϣ*/
		JSONObject jCandA = new JSONObject();

		JSONArray jmany_pay = new JSONArray();
		String many_pay = customs.getContractMes();
		if (many_pay == null) {
			jCandA.element("many_pay", jmany_pay);//��ͬ��Ϣ
		}
		else{
				jmany_pay = JSONArray.fromObject(many_pay);
				jCandA.element("many_pay", jmany_pay);//��ͬ��Ϣ
		}
		/*��ȡ��ͬ��Ϣ*/
		
		/*��ȡ�˺���Ϣ*/
		JSONArray jaccout = new JSONArray();
		String accout = customs.getAccoutMes();
		if (accout == null) {
			jCandA.element("accout", jaccout);
		}
		else{
			jaccout = JSONArray.fromObject(accout);
			jCandA.element("accout", jaccout);
		}

		logger.info(jaccout.size());
		/*��ȡ�˺���Ϣ*/
		
		return jCandA;
	}
}
