package dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import entity.AllPayrecord;

public class AllPayRecord_Dao {
	private static Logger logger = LogManager.getLogger(PayRecord_Dao.class);
	private static Logger logger_error = LogManager.getLogger("error");
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public AllPayRecord_Dao(SessionFactory wFactory) {
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
	
	/*根据指定字段进行查找，字段类型为字符串类型*/
	public java.util.List<AllPayrecord> FindBySpeElement_S_Page(String filed,String value,int offset,int pageszie){
		String fdclient_hql = "select order from AllPayrecord order where " +  filed + " = :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<AllPayrecord> payRecords = session.createQuery(fdclient_hql)
					.setParameter("value", value)
					.setFirstResult(offset)
					.setMaxResults(pageszie)
					.list();
			session.close();
	
			return payRecords;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("根据" + filed + "=" + value + "查找付款记录失败" + e);
			return null;
		}
	}
	
	/*获取整张数据表*/
	public int GetPayTb_Num_ByElement(String filed,String value){
		int num = 0;
		try {
				session = sessionFactory.openSession();
				String hql_select_all = "select count(*) from AllPayrecord where " +  filed + " = :value";
				Query query =   session.createQuery(hql_select_all).setParameter("value", value);
				num = ((Long)query.uniqueResult()).intValue();
				session.close();
				return num;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("获取总条数失败" + e);
			return -1;
		}
		
	}
	
}
