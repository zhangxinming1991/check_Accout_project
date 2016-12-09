package dao;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import entity.ScoreIncreaseRecord;

/**
 * ScoreIncreaseRecord_Dao 连接数据库新增积分信息表的服务dao
 * @author LinLi
 * @version 1.0.0
 *
 */
public class ScoreIncreaseRecord_Dao {
	private static Logger logger = LogManager.getLogger(PayRecord_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	
	public ScoreIncreaseRecord_Dao(SessionFactory wFactory) {
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
	
	public void add(ScoreIncreaseRecord in_sir){
		try {
			beginTransaction();
			session.save(in_sir);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("save failed");
		}
	}
	
	public List<ScoreIncreaseRecord> getInfoByUsername(String username){
		try{
			beginTransaction();
			
			String sqlString = "from ScoreIncreaseRecord where username = :username order by time desc";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<ScoreIncreaseRecord> infos = session.createQuery(sqlString)
						.setParameter("username", username)
						.list();
			endTransaction();
			return infos;
		}catch(RuntimeException e){
			System.out.println("get info failed");
			return null;
		}
	}
	
	public List<ScoreIncreaseRecord> getInfoByUsername(String username, int num){
		try{
			beginTransaction();
			
			String sqlString = "from ScoreIncreaseRecord where username = :username order by time desc";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<ScoreIncreaseRecord> infos = session.createQuery(sqlString)
						.setParameter("username", username)
						.setMaxResults(num)
						.list();
			endTransaction();
			return infos;
		}catch(RuntimeException e){
			System.out.println("get info failed");
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

