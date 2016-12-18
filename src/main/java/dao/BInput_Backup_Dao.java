package dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import entity.BankInputBackup;
import entity.PayRecord;


public class BInput_Backup_Dao {
	private static Logger logger = LogManager.getLogger(BInput_Backup_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public BInput_Backup_Dao(SessionFactory wFactory) {
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
	
	public void add(BankInputBackup in_bankIn){
		try {
			beginTransaction();
			session.save(in_bankIn);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("添加出纳备份记录失败" + e);
		}
	}
	
	/*获取整张数据表*/
	public java.util.List GetBInputBupByElment(String field,Object value){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from BankInputBackup where " + field + " = :value";
		java.util.List orders =  (java.util.List) session.createQuery(hql_select_all)
				.setParameter("value", value)
				.list();
		session.close();
		return orders;
	}
	
	/*获取整张数据表*/
	public java.util.List GetBInputBupByElment_AND(String field,Object value,String filed1,Object value1){
		
		session = sessionFactory.openSession();
		String hql_select_all = "from BankInputBackup where " + field + " = :value" + " and " + filed1 + " = :value1";
		java.util.List orders =  (java.util.List) session.createQuery(hql_select_all)
				.setParameter("value", value)
				.setParameter("value1", value1)
				.list();
		session.close();
		return orders;
	}
	
	/**/
	public void DeleteBInputBupByElement(String filed,String filedvalue){
		try {
			beginTransaction();
			String de_all_hql = "delete from BankInputBackup where " + filed + " = :filedname";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvalue)
			.executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("删除备份出纳表记录失败" + e);
		}
	}
	
	public void DeleteBInputBupByElement_AND(String filed,String filedvalue,String filed1,String filedvalue1){
		try {
			beginTransaction();
			String de_all_hql = "delete from BankInputBackup where " + filed + " = :filedname" + " and " + filed1 + " = :filedvalue1";
			session.createQuery(de_all_hql)
			.setParameter("filedname", filedvalue)
			.setParameter("filedvalue1", filedvalue1)
			.executeUpdate();
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("删除备份出纳表记录失败" + e);
		}
	}

	/**
	 * GetMaxID 查找最大id的记录的id
	 * @return
	 */
	public int GetMaxID(){
		String hql_getmaxid = "SELECT binput from BankInputBackup binput where id = (SELECT max(id) FROM BankInputBackup)";
		
		try {
			
			session = sessionFactory.openSession();
			Query query = session.createQuery(hql_getmaxid);
			java.util.List<BankInputBackup> pCaches = query.list();
			session.close();
			
			if (pCaches.size() > 0) {
				return pCaches.get(0).getId();
			}
			else {
				logger.warn("BankInputBackup 表为空");
				return 0;
			}
			
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("查询最大id失败" + e);
			return -1;
		}	
	}
}
