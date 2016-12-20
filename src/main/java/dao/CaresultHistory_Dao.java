package dao;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import entity.BankInput;
import entity.CaresultHistory;

/**
 * CaresultHistory_Dao �������ݿ������ʷ����ķ���dao
 * @author zhangxinming
 * @version 1.0.0
 *
 */
public class CaresultHistory_Dao {
	private static Logger logger = LogManager.getLogger(CaresultHistory_Dao.class);
	private static Logger logger_error = LogManager.getLogger("error");
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public CaresultHistory_Dao(SessionFactory wFactory) {
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
	
	public boolean update(CaresultHistory cHistory){
		try {
			beginTransaction();
			session.update(cHistory);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("save failed");
			return false;
		}
	}
	
	/*����ɾ����¼*/
	public void DeleteChistoryTbByElement(String filed,String filedvalue,String owner){
		try {
			beginTransaction();
			String de_all_hql = "delete from CaresultHistory where " + filed + " = :filedname" + " and " + "owner = :owner_value";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvalue)
			.setParameter("owner_value", owner)
			.executeUpdate();
			endTransaction();		
			logger.info("���� " + filed + "=" + filedvalue + "ɾ�����ݳɹ�");
		} catch (RuntimeException e) {
			// TODO: handle exception
			//System.out.println("delete ChistoryTbByElement failed" + e);
			logger.info("���� " + filed + "=" + filedvalue + "ɾ�����ݳɹ�");
		}
	}
	
	/*��ȡ�������ݱ�*/
	public java.util.List GetTolBankIns(){
		session = sessionFactory.openSession();
		String hql_select_all = "from CaresultHistory";
		java.util.List bankins =  (java.util.List) session.createQuery(hql_select_all).list();
		session.close();
		return bankins;
	}
	
	/*Ѱ��ƥ��ĳ��ɼ�¼*/
	public List<CaresultHistory> FindBySpeElement_AND(String filed1,String filed2,Object value1,Object value2,String owner){
		
		/*�޸�ƥ�����*/
		String fdclient_hql = "select crhistory from CaresultHistory crhistory where " +  filed1 + " = :value1" + " and " + filed2 + " = :value2" + " and " + "owner = :owner_value";//����һ��
		/*�޸�ƥ�����*/

		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(fdclient_hql);
			query.setParameter("value1", value1);
			query.setParameter("value2", value2);
			query.setParameter("owner_value", owner);
			java.util.List<CaresultHistory> crhistorys = query.list();
			session.close();
	
			return crhistorys;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find crhistorysand failed");
			return null;
		}
	}
	
	/*Ѱ��ƥ��ĳ��ɼ�¼*/
	public List<CaresultHistory> FindBySpeElement(String filed1,Object value1,String owner){		
		/*�޸�ƥ�����*/
		String fdclient_hql = "select crhistory from CaresultHistory crhistory where " +  filed1 + " = :value1" + " and " + "owner = :owner_value";//����һ��
		/*�޸�ƥ�����*/

		try {			
			session = sessionFactory.openSession();
			Query query = session.createQuery(fdclient_hql);
			query.setParameter("value1", value1);
			query.setParameter("owner_value", owner);
			java.util.List<CaresultHistory> crhistorys = query.list();
			session.close();
			
			return crhistorys;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("����:" + filed1 + "=" + value1 + "���Ҷ��˽��ʧ��:" + e);
			return null;
		}
	}
	
	/*Ѱ��ƥ��ĳ��ɼ�¼*/
	public List<CaresultHistory> FindBySpeElement_ByPage(String filed1,Object value1,String owner,int offset,int pagesize){		
		/*�޸�ƥ�����*/
		String fdclient_hql = "select crhistory from CaresultHistory crhistory where " +  filed1 + " = :value1" + " and " + "owner = :owner_value";//����һ��
		/*�޸�ƥ�����*/

		try {			
			session = sessionFactory.openSession();
			Query query = session.createQuery(fdclient_hql)
					.setParameter("value1", value1)
					.setParameter("owner_value", owner)
					.setFirstResult(offset)
					.setMaxResults(pagesize);
			java.util.List<CaresultHistory> crhistorys = query.list();
			session.close();
			
			return crhistorys;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("����:" + filed1 + "=" + value1 + "���Ҷ��˽��ʧ��:" + e);
			return null;
		}
	}
	
	/*Ѱ��ƥ��ĳ��ɼ�¼*/
	public List<CaresultHistory> FindBySpeElementLimit(String filed1,Object value1,String owner){		
		/*�޸�ƥ�����*/
		String fdclient_hql = "select crhistory from CaresultHistory crhistory where " +  filed1 + " = :value1" + " and " + "owner = :owner_value";//����һ��
		/*�޸�ƥ�����*/

		try {			
			session = sessionFactory.openSession();
			Query query = session.createQuery(fdclient_hql);
			query.setParameter("value1", value1);
			query.setParameter("owner_value", owner);
			query.setFirstResult(0);
			query.setMaxResults(1);
			java.util.List<CaresultHistory> crhistorys = query.list();
			session.close();
			
			return crhistorys;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("find crhistorys failed");
			return null;
		}
	}
	
	/*�����¼*/
	public void add(CaresultHistory crhistory){
		try {
			beginTransaction();
			session.save(crhistory);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("add crhistory failed");
		}
	}
	

}
