package dao;

import java.io.Serializable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import entity.PayRecord;
import entity.PayRecordCache;
import sun.util.logging.resources.logging;

/**
 * PayRecord_Dao �������ݿ⸶����Ϣ��ķ���dao
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class PayRecord_Dao {
	private static Logger logger = LogManager.getLogger(PayRecord_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public PayRecord_Dao(SessionFactory wFactory) {
	//	sessionFactory = new Configuration().configure().buildSessionFactory();
		sessionFactory = wFactory;
	}
	
	protected void beginTransaction(){
		session = sessionFactory.openSession();
		transaction = session.beginTransaction();
	}
	
	protected void endTransaction() {
		transaction.commit();
		session.close();
	}
	
	public void add(PayRecord in_pR){
		try {
			beginTransaction();
			session.save(in_pR);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("save failed");
		}
	}
	
	public void addlist(List<PayRecord> in_payrecord){
		for (int i = 0; i < in_payrecord.size(); i++) {
			add(in_payrecord.get(i));
		}
	}
	
	public void delete(PayRecord de_pR){
		try {
			beginTransaction();
			session.delete(de_pR);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("delete failed");
		}
	}
	
	/**
	 * GetMaxID �������id�ļ�¼��id
	 * @return
	 */
	public int GetMaxID(){
		String hql_getmaxid = "SELECT precord from PayRecord precord where id = (SELECT max(id) FROM PayRecord)";
		
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(hql_getmaxid);
			java.util.List<PayRecord> pCaches = query.list();
			session.close();
			
			if (pCaches.size() > 0) {
				return pCaches.get(0).getId();
			}
			else {
				logger.warn("pCaches ��Ϊ��");
				return 0;
			}
			
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("��ѯ���idʧ��" + e);
			return -1;
		}	
	}
	
	public void DeletePrecordTb(){
		try {
			beginTransaction();
			String de_all_hql = "delete from PayRecord";
			session.createQuery(de_all_hql).executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("delete all failed" + e);
		}
	}
	
	public void DeletePrecordTbByElement(String filed,String filedvalue){
		try {
			beginTransaction();
			String de_all_hql = "delete from PayRecord where " + filed + " = :filedname";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvalue)
			.executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("delete PrecordTbByElement failed" + e);
		}
	}
	
	public PayRecord findById(Class<PayRecord> cla,Serializable id){
		try {
			session = sessionFactory.openSession();
			PayRecord find_PayR = (PayRecord) session.get(cla, id);
			session.close();
			return find_PayR;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("findById failed");
			return null;
		}
	}
	
	/*����ָ���ֶν��в��ң��ֶ�����Ϊ�ַ�������*/
	public java.util.List<PayRecord> FindBySpeElement_S(String filed,String value){
		String fdclient_hql = "select order from PayRecord order where " +  filed + " = :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<PayRecord> payRecords = session.createQuery(fdclient_hql).setParameter("value", value).list();
			session.close();
	
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + filed + "=" + value + "���Ҹ����¼ʧ��" + e);
			return null;
		}
	}
	
	/*����ָ���ֶν��в��ң��ֶ�����Ϊ�ַ�������*/
	public java.util.List<PayRecord> FindBySpeElement_S_Page(String filed,String value,int offset,int pageszie){
		String fdclient_hql = "select order from PayRecord order where " +  filed + " = :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<PayRecord> payRecords = session.createQuery(fdclient_hql)
					.setParameter("value", value)
					.setFirstResult(offset)
					.setMaxResults(pageszie)
					.list();
			session.close();
	
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + filed + "=" + value + "���Ҹ����¼ʧ��" + e);
			return null;
		}
	}
	
	/*��ȡ�������ݱ�*/
	public int GetPayTb_Num_ByElement(String filed,String value){
		int num = 0;
		try {
				session = sessionFactory.openSession();
				String hql_select_all = "select count(*) from PayRecord where " +  filed + " = :value";
				Query query =   session.createQuery(hql_select_all).setParameter("value", value);
				num = ((Long)query.uniqueResult()).intValue();
				session.close();
				return num;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("��ȡ������ʧ��" + e);
			return -1;
		}
		
	}
	
	/*����ָ���ֶν��в��ң��ֶ�����Ϊ�ַ�������*/
	public java.util.List<PayRecord> FindBySpeElement_S_limit(String filed,String value){
		String fdclient_hql = "select order from PayRecord order where " +  filed + " = :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<PayRecord> payRecords = session.createQuery(fdclient_hql)
					.setParameter("value", value)
					.setFirstResult(0)
					.setMaxResults(1)
					.list();
			session.close();
	
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("�����Ʋ��Ҹ�����Ϣʧ��" + e);
			return null;
		}
	}
	
	/*����ָ���ֶν��в��ң��ֶ�����Ϊ�ַ�������*/
	public java.util.List<PayRecord> FindBySpeElement(String filed,Object value,String owner){
		String fdclient_hql = "select order from PayRecord order where " +  filed + " = :value" + " and " + "owner = :owner_value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<PayRecord> payRecords = session.createQuery(fdclient_hql)
					.setParameter("value", value)
					.setParameter("owner_value", owner)
					.list();
			session.close();
	
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + filed + "=" + value + "���Ҹ����¼ʧ��" + e);
			//System.out.println("find client failed");
			return null;
		}
	}
	
	/*����ָ���ֶν��в��ң��ֶ�����Ϊ�ַ�������*/
	public java.util.List<PayRecord> FindBySpeElement_S_N(String filed,Object value){
		String fdclient_hql = "select order from PayRecord order where " +  filed + " != :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<PayRecord> payRecords = session.createQuery(fdclient_hql).setParameter("value", value).list();
			session.close();
	
			for (int i = 0; i < payRecords.size(); i++) {
				System.out.println(payRecords.get(i).getContractNum());
			}
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find client failed");
			return null;
		}
	}
	
	public boolean update(PayRecord pRecord){
		try {
			beginTransaction();
			session.update(pRecord);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("payrecord update failed");
			return false;
		}
	}
	
	/*��ȡ�������ݱ�*/
	public List<PayRecord> GetPrecordTb(){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from PayRecord";
		List<PayRecord> payr =   session.createQuery(hql_select_all).list();
		session.close();
		return payr;
	}
	
	/*��ȡ�������ݱ�*/
	public List<PayRecord> GetPrecordTbByElement(String filed,String filedvalue){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from PayRecord where " + filed + " = :filename";
		List<PayRecord> payr =   session.createQuery(hql_select_all)
				.setParameter("filename", filedvalue)
				.list();
		session.close();
		return payr;
	}
	
	/*��ָ���ֶν����޸�*/
	public boolean ModifySpeElement(String filed,Object value,int payid){
		session = sessionFactory.openSession();
		String md_hql = "update PayRecord payR set payR.bankinput = :bid where payR.id =:payid";
		Query query = session.createQuery(md_hql);
		query.setParameter("bid", value);
		query.setParameter("payid", payid);
		query.executeUpdate();
		session.close();
		return true;
	}
	
	public List<PayRecord> FindPayToBInput(boolean isConnect_value,String owner_value){
		String pToBInput = "select payrecord from PayRecord payrecord where " +  "isconnect" + " = :isconnect" + " and " + "owner = :owner";//����һ��
	//	String pToBInput = "from PayRecord";//����һ��
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(pToBInput);
			query.setParameter("isconnect", isConnect_value);
			query.setParameter("owner", owner_value);
			java.util.List<PayRecord> payRecords = query.list();
			session.close();
			
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find pToBInput failed:" + e);
			return null;
		}	
	}
	
	public List<PayRecord> FindPayNoBInput(Character checkResult_value,String owner_value){
		String pToBInput = "select payrecord from PayRecord payrecord where " +  "isconnect" + " = :isconnect" + " and " + "checkResult" + " = :checkResult" + " and " + "owner = :owner";//����һ��
	//	String pToBInput = "from PayRecord";//����һ��
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(pToBInput);
			query.setParameter("isconnect", false);
			query.setParameter("checkResult", checkResult_value);
			query.setParameter("owner", owner_value);
			java.util.List<PayRecord> payRecords = query.list();
			session.close();
			
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find pToBInput failed:" + e);
			return null;
		}	
	}
	
	public void Close_Connect(){
		
/*		try {
			sessionFactory.close();
		} catch (RuntimeException e) {
			// TODO: handle exception
		}*/
	}
}

