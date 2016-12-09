package dao;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.ConnectPersonScoreInfo;

/**
 * ScoreIncreaseRecord_Dao �������ݿ�����������Ϣ��ķ���dao
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
	 * ����agentId��ȡָ��λ�á�ָ��������info������ǰ�˷�ҳ��ʾ
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
			logger.error("ConnectPersonScoreInfo_Dao��ִ�и����û�����ȡScore��Ϣʧ��");
			return null;
		}
	}
	
	/**
	 * ����agentId��ȡȫ����info����������Excel
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
			logger.error("ConnectPersonScoreInfo_Dao��ִ�и����û�����ȡScore��Ϣʧ��");
			return null;
		}
	}

	/**
	 * ��ȡָ��λ�ü�ָ��������info������ǰ�˷�ҳ��ʾ
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
			logger.error("ConnectPersonScoreInfo_Dao��ִ�л�ȡȫ��Score��Ϣʧ��");
			return null;
		}
	}
	
	/**
	 * ��ȡȫ����Info����������Excel
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
			logger.error("ConnectPersonScoreInfo_Dao��ִ�л�ȡȫ��Score��Ϣʧ��");
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
			logger.error("ConnectPersonScoreInfo_Dao�л�ȡ�ܵļ�¼��ʧ��");
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
			logger.error("ConnectPersonScoreInfo_Dao�л�ȡ�ܵļ�¼��ʧ��");
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
			logger.error("ConnectPersonScoreInfo_Dao�л�ȡ�û���Ϣʧ��");
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

