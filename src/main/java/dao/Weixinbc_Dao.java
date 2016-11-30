package dao;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.PayRecord;
import entity.WeixinBindConnectPerson;

public class Weixinbc_Dao {
	private static Logger logger = LogManager.getLogger(Agent_Dao.class);
	private static Logger logger_error = LogManager.getLogger("error");
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public Weixinbc_Dao(SessionFactory wFactory) {
	//	sessionFactory = new Configuration().configure().buildSessionFactory();
		sessionFactory = wFactory;
	}
	
	protected void beginTransaction(){
	/*	session = sessionFactory.openSession();
		transaction = session.beginTransaction();*/
		try {
				session = sessionFactory.openSession();
				transaction = session.beginTransaction();
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("beginTransaction failed:" + e);
		}

	}
	
	protected void endTransaction() {
		transaction.commit();
		session.close();
	}
	
	public boolean add(WeixinBindConnectPerson in_assistance){
		try {
			beginTransaction();
			session.save(in_assistance);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			//System.out.println("save failed");
			logger_error.error("添加对账联系人" +in_assistance.getUsername() + "失败" + e);
			return false;
		}
	}
	
	public boolean delete(WeixinBindConnectPerson de_assistance){
		try {
			beginTransaction();
			session.delete(de_assistance);
			endTransaction();
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			//System.out.println("delete failed");
			logger_error.error("删除对账联系人" + de_assistance.getUsername() + "失败" + e);
			return false;
		}
	}
	
	public WeixinBindConnectPerson findById(Class<WeixinBindConnectPerson> cla,Serializable id){
		try {
			session = sessionFactory.openSession();
			WeixinBindConnectPerson find_assistance = (WeixinBindConnectPerson) session.get(cla, id);
			session.close();
			return find_assistance;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("根据id" + "=" + id + "查找对账联系人失败" + e);
			return null;
		}
	}
	
	public boolean update(WeixinBindConnectPerson connectp){
		try {
			beginTransaction();
			session.update(connectp);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("更新对账联系人" + connectp.getUsername()+ "失败" + e);
			return false;
		}
	}
	
	/*根据指定字段进行查找，字段类型为字符串类型*/
	public java.util.List<WeixinBindConnectPerson> FindBySpeElement_S(String filed,String value){
		String fdclient_hql = "select connectp from WeixinBindConnectPerson connectp where " +  filed + " = :value";
		try {
			
			session = sessionFactory.openSession();
			java.util.List<WeixinBindConnectPerson> wxbcp = session.createQuery(fdclient_hql).setParameter("value", value).list();
			session.close();
	
			return wxbcp;
		
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("根据" + filed + "=" + value + "查找微信联系人失败" + e);
			return null;
		}
	}
}
