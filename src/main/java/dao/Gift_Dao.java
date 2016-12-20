package dao;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.Gift;

/**
 * LogisticInfo_Dao 连接数据库物流信息表的服务dao
 * @author LinLi
 * @version 1.0.0
 *
 */
public class Gift_Dao {
	
	private static Logger logger = LogManager.getLogger(Gift_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	
	public Gift_Dao(SessionFactory wFactory) {
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
	
	public void add(Gift gift){
		try {
			beginTransaction();
			session.save(gift);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("save failed");
		}
	}
	
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public List<Gift> getInfos(){
		List<Gift> resultList = null;
		try{
			beginTransaction();
			String sqlString = "from Gift";
			resultList = session.createQuery(sqlString).list();
			endTransaction();
		}catch(RuntimeException e){
			System.out.println(e);
		}
		return resultList;
	}
	
	public Gift getInfoById(int id){
		Gift result = null;
		try{
			beginTransaction();
			String sqlString = "from Gift where id = :id";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<Gift> resultList = session.createQuery(sqlString).setParameter("id", id).list();
			if(resultList != null)
				result = resultList.get(0);
			endTransaction();
		}catch(RuntimeException e){
			System.out.println(e);
		}
		return result;
	}

	public int update(Gift gift){
		int result = 0;
		try{
			beginTransaction();
			String sqlString = "update Gift set stock = :stock, score = :score where gift = :gift";
			result = session.createQuery(sqlString)
							.setParameter("stock", gift.getStock())
							.setParameter("score", gift.getScore())
							.setParameter("gift", gift.getGift())
							.executeUpdate();
			if(result == 0){
				session.save(gift);
				result++;
			}
			System.out.print(result);
			endTransaction();
		}catch(RuntimeException e){
			System.out.println(e);
		}
		return result;
	}
	
	public void Close_Connect(){
		
/*		try {
			sessionFactory.close();
		} catch (RuntimeException e) {
			// TODO: handle exception
		}*/
	}
}

