package dao;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.ConnectPersonScoreInfo;

/**
 * ScoreIncreaseRecord_Dao 连接数据库新增积分信息表的服务dao
 * @author LinLi
 * @version 1.0.0
 *
 */
public class ConnectPersonScoreInfo_Dao {
	private static Logger logger = LogManager.getLogger(PayRecord_Dao.class);
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	
	public ConnectPersonScoreInfo_Dao(SessionFactory wFactory) {
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
	
	/**
	 * 根据agentId获取指定位置、指定个数的info，用于前端分页显示
	 * @param agentId
	 * @param offset
	 * @param pagesize
	 * @return
	 */
	public List<ConnectPersonScoreInfo> getInfoByAgentId(String agentId, int offset, int pagesize){
		try{
			beginTransaction();
			String sqlString = "from ConnectPersonScoreInfo where agent = :agentId";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<ConnectPersonScoreInfo> infos = session.createQuery(sqlString)
					.setParameter("agentId", agentId)
					.setFirstResult(offset)
					.setMaxResults(pagesize)
					.list();
			endTransaction();
			return infos;
		}catch(RuntimeException e){
			logger.error(e);
			logger.error("ConnectPersonScoreInfo_Dao中执行根据用户名获取Score信息失败");
			return null;
		}
	}
	
	/**
	 * 根据agentId获取全部的info，用于生成Excel
	 * @param agentId
	 * @return
	 */
	public List<ConnectPersonScoreInfo> getInfoByAgentId(String agentId){
		try{
			beginTransaction();
			String sqlString = "from ConnectPersonScoreInfo where agent = :agentId";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<ConnectPersonScoreInfo> infos = session.createQuery(sqlString)
					.setParameter("agentId", agentId)
					.list();
			endTransaction();
			return infos;
		}catch(RuntimeException e){
			logger.error(e);
			logger.error("ConnectPersonScoreInfo_Dao中执行根据用户名获取Score信息失败");
			return null;
		}
	}

	/**
	 * 获取指定位置及指定个数的info，用于前端分页显示
	 * @param offset
	 * @param pagesize
	 * @return
	 */
	public List<ConnectPersonScoreInfo> getAllInfo(int offset, int pagesize){
		try{
			beginTransaction();
			String sqlString = "from ConnectPersonScoreInfo";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<ConnectPersonScoreInfo> infos = session.createQuery(sqlString)
					.setFirstResult(offset)
					.setMaxResults(pagesize)
					.list();
			endTransaction();
			return infos;
		}catch(RuntimeException e){
			logger.error(e);
			logger.error("ConnectPersonScoreInfo_Dao中执行获取全部Score信息失败");
			return null;
		}
	}
	
	/**
	 * 获取全部的Info，用于生成Excel
	 * @return
	 */

	public List<ConnectPersonScoreInfo> getAllInfo(){
		try{
			beginTransaction();
			String sqlString = "from ConnectPersonScoreInfo";
			@SuppressWarnings({ "unchecked", "deprecation" })
			List<ConnectPersonScoreInfo> infos = session.createQuery(sqlString).list();
			endTransaction();
			return infos;
		}catch(RuntimeException e){
			logger.error(e);
			logger.error("ConnectPersonScoreInfo_Dao中执行获取全部Score信息失败");
			return null;
		}
	}
	
	public int getAllNum(){
		try{
			beginTransaction();
			String sqlString = "select count(*) from ConnectPersonScoreInfo";
			@SuppressWarnings({ "deprecation" })
			int num =((Number)session.createQuery(sqlString).uniqueResult()).intValue();
			endTransaction();
			return num;
		}catch(RuntimeException e){
			logger.error(e);
			logger.error("ConnectPersonScoreInfo_Dao中获取总的记录数失败");
			return -1;
		}
	}
	
	public int getNumByAgentId(String agentId)
	{
		try{
			beginTransaction();
			String sqlString = "select count(*) from ConnectPersonScoreInfo where agent = :agentId";
			@SuppressWarnings({"deprecation" })
			int num =((Number)session.createQuery(sqlString)
									.setParameter("agentId", agentId)
									.uniqueResult()).intValue();
			endTransaction();
			return num;
		}catch(RuntimeException e){
			logger.error(e);
			logger.error("ConnectPersonScoreInfo_Dao中获取总的记录数失败");
			return -1;
		}
	}
	
	public ConnectPersonScoreInfo getConnectPersonScoreInfo(String username)
	{
		try{
			beginTransaction();
			String sqlString = "from ConnectPersonScoreInfo where username = :username";
			@SuppressWarnings({"deprecation", "unchecked" })
			List<ConnectPersonScoreInfo> connectPersonScoreInfos =session.createQuery(sqlString)
									.setParameter("username", username).list();
			endTransaction();
			if(connectPersonScoreInfos.size() == 1)
				return connectPersonScoreInfos.get(0);
			else 
				return null;
		}catch(RuntimeException e){
			logger.error(e);
			logger.error("ConnectPersonScoreInfo_Dao中获取用户信息失败");
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

