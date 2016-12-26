package dao;

import java.io.Serializable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import entity.Agent;
import entity.Assistance;
import entity.BankInput;

/**
 * Assistance_Dao �������ݿ������Ա��dao
 * @author zhangxinming
 * @version 1.0.0
 */
public class Assistance_Dao {
	
	private static Logger logger = LogManager.getLogger(Assistance_Dao.class);
	
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public Assistance_Dao(SessionFactory wFactory) {
	//	sessionFactory = new Configuration().configure().buildSessionFactory();
		sessionFactory = wFactory;
	}
	
	protected void beginTransaction(){
	/*	session = sessionFactory.openSession();
		transaction = session.beginTransaction();*/
		session = sessionFactory.openSession();
		transaction = session.beginTransaction();
	}
	
	protected void endTransaction() {
		transaction.commit();
		session.close();
	}
	
	public int add(Assistance in_assis){
		try {
			beginTransaction();
			session.save(in_assis);
			endTransaction();	
			return 0;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("���Assistance��" + in_assis.getWorkId() + "ʧ��" + e);
			return -1;
		}
	}
	
	public boolean delete(Assistance de_assis){
		try {
			beginTransaction();
			session.delete(de_assis);
			endTransaction();		
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("ɾ��Assistance:" + de_assis.getWorkId() + "ʧ��" + e);
			return false;
		}
	}
	
	public Assistance findById(Class<Assistance> cla,Serializable id){
		logger.info("findById" + "id=" + id);
		try {
			session = sessionFactory.openSession();
			Assistance find_Assis = (Assistance) session.get(cla, id);
			session.close();
			return find_Assis;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + "id= " + id + "���Ҳ�����Աʧ��" + e);
			return null;
		}
	}
	
	public boolean update(Assistance assistance){
		try {
			beginTransaction();
			session.update(assistance);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����Assistance:" + assistance.getWorkId() + "ʧ��" + e);
			return false;
		}
	}
	
	public List<Assistance> GetTotalTbByElement(String filed1,Object value1){
		try {
					session = sessionFactory.openSession();
		String hql_select_all = "from Assistance where " + filed1 + " = :value1";
		List<Assistance> assistances =   (List<Assistance>) session.createQuery(hql_select_all)
				.setParameter("value1", value1)
				.list();
		session.close();
		return assistances;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + filed1 + "=" + value1 + "��ȡ����Assistance��" + "ʧ��" + e);
			return null;
		}
	}
	
	public List<Assistance> GetTotalTbByElement_ByPage(String filed1,Object value1,int offset,int pagesize){
		try {
					session = sessionFactory.openSession();
		String hql_select_all = "from Assistance where " + filed1 + " = :value1";
		List<Assistance> assistances =   (List<Assistance>) session.createQuery(hql_select_all)
				.setParameter("value1", value1)
				.setFirstResult(offset)
				.setMaxResults(pagesize)
				.list();
		session.close();
		return assistances;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + filed1 + "=" + value1 + "��ȡ����Assistance��" + "ʧ��" + e);
			return null;
		}
	}
	
	public List<Assistance> GetTotalTbByElement_ByPage_ByUserType(String filed1,Object value1,int offset,int pagesize, String usertype){
		try {
					session = sessionFactory.openSession();
		String hql_select_all = "from Assistance where usertype = :usertype and " + filed1 + " = :value1";
		List<Assistance> assistances =   (List<Assistance>) session.createQuery(hql_select_all)
				.setParameter("usertype", usertype)
				.setParameter("value1", value1)
				.setFirstResult(offset)
				.setMaxResults(pagesize)
				.list();
		session.close();
		return assistances;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + filed1 + "=" + value1 + "��ȡ����Assistance��" + "ʧ��" + e);
			return null;
		}
	}
	
	public int GetTotalTbByElement_Num_ByPage(String filed1,Object value1){
		int num = 0;
		try {
			session = sessionFactory.openSession();
			String hql_select_all = "select count(*) from Assistance where " + filed1 + " = :value1";
		//	String hql_select_all = "select count(*) from Assistance";
			Query query =   session.createQuery(hql_select_all)
					.setParameter("value1", value1);
			//int size = query.list().size();
			num = ((Long)query.uniqueResult()).intValue();
			session.close();
			return num;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("��ѯ�м�¼��ʧ��" + e);
			return -1;
		}
	}
	
	public int GetTotalTbByElement_Num_ByPage_ByUserType(String filed1,Object value1, String usertype){
		int num = 0;
		try {
			session = sessionFactory.openSession();
			String hql_select_all = "select count(*) from Assistance where usertype = :usertype and " + filed1 + " = :value1";
		//	String hql_select_all = "select count(*) from Assistance";
			Query query =   session.createQuery(hql_select_all)
					.setParameter("usertype", usertype)
					.setParameter("value1", value1);
			//int size = query.list().size();
			num = ((Long)query.uniqueResult()).intValue();
			session.close();
			return num;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("��ѯ�м�¼��ʧ��" + e);
			return -1;
		}
	}
	
	public List<Assistance> GetTotalTbByElement_ByPage_And(String filed1,Object value1,String filed2,Object value2,int offset,int pagesize){
		try {
					session = sessionFactory.openSession();
		String hql_select_all = "select assis from Assistance assis where " + filed1 + " = :value1" + " or " + filed2 + " = :value2";
		List<Assistance> assistances =   (List<Assistance>) session.createQuery(hql_select_all)
				.setParameter("value1", value1)
				.setParameter("value2", value2)
				.setFirstResult(offset)
				.setMaxResults(pagesize)
				.list();
		session.close();
		return assistances;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + filed1 + "=" + value1 + "��ȡ����Assistance��" + "ʧ��" + e);
			return null;
		}
	}
	
	public List<Assistance> GetTotalTbByElement_ByPage_ByUserType_And(String filed1,Object value1,String filed2,Object value2,int offset,int pagesize, String usertype){
		try {
					session = sessionFactory.openSession();
		String hql_select_all = "select assis from Assistance assis where usertype = :usertype and " + filed1 + " = :value1" + " or " + filed2 + " = :value2";
		List<Assistance> assistances =   (List<Assistance>) session.createQuery(hql_select_all)
				.setParameter("usertype", usertype)
				.setParameter("value1", value1)
				.setParameter("value2", value2)
				.setFirstResult(offset)
				.setMaxResults(pagesize)
				.list();
		session.close();
		return assistances;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("����" + filed1 + "=" + value1 + "��ȡ����Assistance��" + "ʧ��" + e);
			return null;
		}
	}
	public int GetTotalTbByElement_Num_ByPage_And(String filed1,Object value1,String filed2,Object value2){
		int num = 0;
		try {
			session = sessionFactory.openSession();
			String hql_select_all = "select count(*) from Assistance where " + filed1 + " = :value1" + " or " + filed2 + " = :value2";
		//	String hql_select_all = "select count(*) from Assistance";
			Query query =   session.createQuery(hql_select_all)
					.setParameter("value1", value1)
					.setParameter("value2", value2);
			//int size = query.list().size();
			num = ((Long)query.uniqueResult()).intValue();
			session.close();
			return num;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("��ѯ�м�¼��ʧ��" + e);
			return -1;
		}
	}
	
	public int GetTotalTbByElement_Num_ByPage_ByUserType_And(String filed1,Object value1,String filed2,Object value2, String usertype){
		int num = 0;
		try {
			session = sessionFactory.openSession();
			String hql_select_all = "select count(*) from Assistance where usertype = :usertype and " + filed1 + " = :value1" + " or " + filed2 + " = :value2";
		//	String hql_select_all = "select count(*) from Assistance";
			Query query =   session.createQuery(hql_select_all)
					.setParameter("usertype", usertype)
					.setParameter("value1", value1)
					.setParameter("value2", value2);
			//int size = query.list().size();
			num = ((Long)query.uniqueResult()).intValue();
			session.close();
			return num;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("��ѯ�м�¼��ʧ��" + e);
			return -1;
		}
	}
	
	/*��ȡ�������ݱ�*/
	public int GetTotalTb_Num_ByElement(){
		int num = 0;
		try {
				session = sessionFactory.openSession();
				String hql_select_all = "select count(*) from Assistance";
				Query query =   session.createQuery(hql_select_all);
				num = ((Long)query.uniqueResult()).intValue();
				session.close();
				return num;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.error("��ȡ������ʧ��" + e);
			return -1;
		}
		
	}
	
	public boolean checkExistBu(String agentId, int flag){
		boolean result = false;
		try {
			beginTransaction();
			String sqlString = "select count(*) from Assistance where agentid = :agentId and flag = :flag";
			Query query =   session.createQuery(sqlString).setParameter("agentId", agentId).setParameter("flag", flag);
			int num = ((Long)query.uniqueResult()).intValue();
			if(num != 0)
				result = true;
			endTransaction();
		} catch (RuntimeException e) {
			logger.error("�������̲���״̬ʧ��");
			// TODO: handle exception
		}
		return result;
	}
	
	public void Close_Connect(){
		
	/*	try {
			sessionFactory.close();
		} catch (RuntimeException e) {
			// TODO: handle exception
		}*/
	}

}
