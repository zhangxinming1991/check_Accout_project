package dao;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.Gift;
import entity.LogisticInfo;

/**
 * LogisticInfo_Dao 连接数据库物流信息表的服务dao
 * @author LinLi
 * @version 1.0.0
 *
 */
public class LogisticInfo_Dao {
	
	private static Logger logger = LogManager.getLogger(LogisticInfo_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	
	public LogisticInfo_Dao(SessionFactory wFactory) {
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
	
	public void add(LogisticInfo lInfo){
		try {
			beginTransaction();
			session.save(lInfo);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("save failed");
		}
	}
	
	public LogisticInfo getInfoByRandKey(String randKey){
		LogisticInfo logisticInfo = null;
		try{
			beginTransaction();
			String sqlString = "from LogisticInfo where randKey = :randKey";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<LogisticInfo> resultList = session.createQuery(sqlString)
																	.setParameter("randKey", randKey)
																	.list();
			logisticInfo = resultList.isEmpty() ? null : resultList.get(0);
			endTransaction();
		}catch(RuntimeException e){
			System.out.println(e);
		}
		return logisticInfo;
	}
	
	public void upload(LogisticInfo logisticInfo){
		try{
			beginTransaction();
			session.save(logisticInfo);
			String sqlString = "update ScoreExchangeRecord set status = 2  where randKey = :randKey";
			int result = session.createQuery(sqlString)
																	.setParameter("randKey", logisticInfo.getRandKey())
																	.executeUpdate();
			endTransaction();
		}catch(RuntimeException e){
			System.out.println(e);
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

