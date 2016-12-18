package dao;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import entity.BankInput;
import entity.BankInputHistory;
import entity.PayRecordHistory;


public class BankInput_His_Dao {
	private static Logger logger = LogManager.getLogger(BackUp_Dao.class);
	private static Logger logger_error = LogManager.getLogger("error");
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public BankInput_His_Dao(SessionFactory wFactory) {
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
	
	public void add(BankInputHistory in_bankIn){
		try {
			beginTransaction();
			session.save(in_bankIn);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("��ӳ�����ʷ��¼ʧ��" + e);
		}
	}
	
	/*��ȡ�������ݱ�*/
	public java.util.List GetBInputHisByElment(String field,Object value){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from BankInputHistory where " + field + " = :value";
		java.util.List orders =  (java.util.List) session.createQuery(hql_select_all)
				.setParameter("value", value)
				.list();
		session.close();
		return orders;
	}
	
	/**/
	public void DeleteBInputHisByElement(String filed,String filedvalue){
		try {
			beginTransaction();
			String de_all_hql = "delete from BankInputHistory where " + filed + " = :filedname";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvalue)
			.executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("ɾ�����ݳ��ɱ���ʷ��¼ʧ��" + e);
		}
	}
	
	/**/
	public void DeleteBInputHisByElement_AND(String filed,String filedvalue,String filed2,String value2){
		try {
			beginTransaction();
			String de_all_hql = "delete from BankInputHistory where " + filed + " = :filedname" + " and " + filed2 + " = :value2";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvalue)
			.setParameter("value2", value2)
			.executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("ɾ�����ݳ��ɱ���ʷ��¼ʧ��" + e);
		}
	}
	
	/*Ѱ��ƥ��ĳ��ɼ�¼*/
	public List<BankInputHistory> FindBySpeElement_AND(String filed1,String filed2,Object value1,Object value2){
		
		/*�޸�ƥ�����*/
		String fdclient_hql = "select payh from BankInputHistory payh where " +  filed1 + " = :value1" + " and " + filed2 + " = :value2";//����һ��

		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(fdclient_hql);
			query.setParameter("value1", value1);
			query.setParameter("value2", value2);
			java.util.List<BankInputHistory> payHistorys = query.list();
			session.close();
			return payHistorys;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find BankInputHistory failed");
			return null;
		}
	}
	
	/**
	 * GetMaxID �������id�ļ�¼��id
	 * @return
	 */
	public int GetMaxID(){
		String hql_getmaxid = "SELECT binput from BankInputHistory binput where id = (SELECT max(id) FROM BankInputHistory)";
		
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(hql_getmaxid);
			java.util.List<BankInputHistory> bInputs = query.list();
			session.close();
			
			if (bInputs.size() > 0) {
				return bInputs.get(0).getId();
			}
			else {
				logger.warn("BankInputHistory ��Ϊ��");
				return 0;
			}
			
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("��ѯ���idʧ��" + e);
			return -1;
		}	
	}

	
}
