package dao;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import entity.Gift;
import entity.LogisticInfo;
import entity.ScoreExchangeRecord;

/**
 * ScoreIncreaseRecord_Dao 连接数据库新增积分信息表的服务dao
 * @author LinLi
 * @version 1.0.0
 *
 */
public class ScoreExchangeRecord_Dao {
	private static Logger logger = LogManager.getLogger(ScoreExchangeRecord_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	
	public ScoreExchangeRecord_Dao(SessionFactory wFactory) {
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
	
	public void add(ScoreExchangeRecord in_ser){
		try {
			beginTransaction();
			session.save(in_ser);
			Gift gift = session.get(Gift.class, in_ser.getExchangeType());
			gift.setStock(gift.getStock() - 1);
			session.update(gift);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			System.out.println("save failed");
		}
	}
	// 通过用户名查询兑换记录
	public List<ScoreExchangeRecord> getInfoByUsername(String username, int num){
		try{
			beginTransaction();
			String sqlString = "from ScoreExchangeRecord where username = :username order by applica_time desc";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<ScoreExchangeRecord> infos = session.createQuery(sqlString)
					.setParameter("username", username)
					.setMaxResults(num)
					.list();
			endTransaction();
			return infos;
		}catch(RuntimeException e){
			System.out.println("getInfoByUsername failed");
			return null;
		}
	}
	
	public List<ScoreExchangeRecord> getInfoByUsername(String username){
		try{
			beginTransaction();
			String sqlString = "from ScoreExchangeRecord where username = :username order by applica_time desc";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<ScoreExchangeRecord> infos = session.createQuery(sqlString)
					.setParameter("username", username)
					.list();
			endTransaction();
			return infos;
		}catch(RuntimeException e){
			System.out.println("getInfoByUsername failed");
			return null;
		}
	}
	
	public List<ScoreExchangeRecord> getInfos(int offset, int pagesize){
		try{
			beginTransaction();
			String sqlString = "from ScoreExchangeRecord  order by applica_time desc";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<ScoreExchangeRecord> infos = session.createQuery(sqlString)
					.setFirstResult(offset)
					.setMaxResults(pagesize)
					.list();
			endTransaction();
			return infos;
		}catch(RuntimeException e){
			System.out.println("getInfoByUsername failed");
			return null;
		}
	}
	
	public List<ScoreExchangeRecord> getInfos(){
		try{
			beginTransaction();
			String sqlString = "from ScoreExchangeRecord  order by applica_time desc";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<ScoreExchangeRecord> infos = session.createQuery(sqlString)
					.list();
			endTransaction();
			return infos;
		}catch(RuntimeException e){
			System.out.println("getInfoByUsername failed");
			return null;
		}
	}
	
	public List<ScoreExchangeRecord> getInfosByAgentId(String agentId, int offset, int pagesize){
		try{
			beginTransaction();
			String sqlString ="select S.username, S.exchangeScore, S.exchangeType, S.status, S.applicaTime, "
					+ "S. finishTime, S.randKey, S.description, S.hander from ScoreExchangeRecord  S, ConnectPersonScoreInfo  C "
					+ "where S.username = C.username and C.agent = :agentId order by applica_time desc";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<ScoreExchangeRecord> infos = session.createQuery(sqlString)
					.setParameter("agentId", agentId)
					.setFirstResult(offset)
					.setMaxResults(pagesize)
					.list();
			System.out.print(infos.toString());
			endTransaction();
			return infos;
		}catch(RuntimeException e){
			System.out.println("getInfosByAgentId failed");
			System.out.println(e);
			return null;
		}
	}
	
	public List<ScoreExchangeRecord> getInfosByAgentId(String agentId){
		try{
			beginTransaction();
			String sqlString = "from ScoreExchangeRecord  S, ConnectPersonScoreInfo  C "
					+ "where S.username = C.username and C.agent = :agentId order by applica_time desc";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<ScoreExchangeRecord> infos = session.createQuery(sqlString)
					.setParameter("agentId", agentId)
					.list();
			endTransaction();
			return infos;
		}catch(RuntimeException e){
			System.out.println("getInfoByUsername failed");
			System.out.println(e);
			return null;
		}
	}
	
	public int getNum()
	{
		try{
			beginTransaction();
			String sqlString = "select count(*) from ScoreExchangeRecord";
			@SuppressWarnings({"deprecation" })
			int num =((Number)session.createQuery(sqlString)
									.uniqueResult()).intValue();
			endTransaction();
			return num;
		}catch(RuntimeException e){
			logger.error(e);
			logger.error("ScoreExchangedRecord_Dao中获取总的记录数失败");
			return -1;
		}
	}
	
	public int getNumByAgentId(String agentId)
	{
		try{
			beginTransaction();
			String sqlString = "select count(*)  from ScoreExchangeRecord  S, ConnectPersonScoreInfo  C "
					+ "where S.username = C.username and C.agent = :agentId";
			@SuppressWarnings({"deprecation" })
			int num =((Number)session.createQuery(sqlString)
									.setParameter("agentId", agentId)
									.uniqueResult()).intValue();
			endTransaction();
			return num;
		}catch(RuntimeException e){
			logger.error(e);
			logger.error("ScoreExchangedRecord_Dao中获取总的记录数失败");
			return -1;
		}
	}
	
	/*
	 * 根据用户名分组，查询已兑换或正在兑换的所有积分
	 * status：1: 正在兑换 ，2：兑换成功
	 */
	public List<ScoreExchangeRecord> getScoresByUsernameByGroup(byte status, int offset, int pagesize){
		try{
			beginTransaction();
			String sqlString = "select username, sum(exchangeScore) from ScoreExchangeRecord where status = :status group by username";
			@SuppressWarnings("unchecked")
			List<ScoreExchangeRecord> resultList = session.createQuery(sqlString)
																.setParameter("status", status)
																.setFirstResult(offset)
																.setMaxResults(pagesize).list();
			endTransaction();
			return resultList;
		}catch(RuntimeException e){
			System.out.println(e);
			return null;
		}
	}
	
	public int updateStatus(String randKey, byte status)
	{
		try{
			beginTransaction();
			String sqlString = "update ScoreExchangeRecord set status = :status where randKey = :randKey";
			Query query = session.createQuery(sqlString);
			query.setParameter("status", status);
			query.setParameter("randKey", randKey);
			int result = query.executeUpdate();
			endTransaction();
			return result;
		}catch(RuntimeException e){
			System.out.println("update status  failed");
			return 0;
		}
	}

	public int updateStatus(String randKey, byte status, String hander)
	{
		try{
			beginTransaction();
			String sqlString = "update ScoreExchangeRecord set status = :status, hander = :hander  where randKey = :randKey";
			Query query = session.createQuery(sqlString);
			query.setParameter("hander", hander);
			query.setParameter("status", status);
			query.setParameter("randKey", randKey);
			int result = query.executeUpdate();
			endTransaction();
			return result;
		}catch(RuntimeException e){
			System.out.println("update status  failed");
			return 0;
		}
	}
	
	public ScoreExchangeRecord getInfoByRandKey(String randKey){
		ScoreExchangeRecord scoreExchangeRecord = null;
		try{
			beginTransaction();
			String sqlString = "from ScoreExchangeRecord where randKey = :randKey";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<ScoreExchangeRecord> resultList = session.createQuery(sqlString)
																	.setParameter("randKey", randKey)
																	.list();
			scoreExchangeRecord = resultList.isEmpty() ? null : resultList.get(0);
			endTransaction();
		}catch(RuntimeException e){
			System.out.println(e);
		}
		return scoreExchangeRecord;
	}
	
	
	public void Close_Connect(){
		
/*		try {
			sessionFactory.close();
		} catch (RuntimeException e) {
			// TODO: handle exception
		}*/
	}
}

