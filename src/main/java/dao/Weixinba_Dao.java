package dao;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import entity.WeixinBindAssistance;

public class Weixinba_Dao {
private static Logger logger = LogManager.getLogger(Agent_Dao.class);
private static Logger logger_error = LogManager.getLogger("error");
	protected SessionFactory sessionFactory;
	protected Session session;
	protected Transaction transaction;
	
	public Weixinba_Dao(SessionFactory wFactory) {
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
	
	public boolean add(WeixinBindAssistance in_assistance){
		try {
			beginTransaction();
			session.save(in_assistance);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			//System.out.println("save failed");
			logger_error.error("添加微信联系人" +in_assistance.getUsername() + "失败" + e);
			return false;
		}
	}
	
	public void delete(WeixinBindAssistance de_assistance){
		try {
			beginTransaction();
			session.delete(de_assistance);
			endTransaction();			
		} catch (RuntimeException e) {
			// TODO: handle exception
			//System.out.println("delete failed");
			logger_error.error("删除微信联系人" + de_assistance.getUsername() + "失败" + e);
		}
	}
	
	public WeixinBindAssistance findById(Class<WeixinBindAssistance> cla,Serializable id){
		try {
			session = sessionFactory.openSession();
			WeixinBindAssistance find_agent = (WeixinBindAssistance) session.get(cla, id);
			session.close();
			return find_agent;	
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger_error.error("根据id" + "=" + id + "查找微信联系人失败" + e);
			return null;
		}
	}
	
	public boolean update(WeixinBindAssistance assistance){
		try {
			beginTransaction();
			session.update(assistance);
			endTransaction();	
			return true;
		} catch (RuntimeException e) {
			// TODO: handle exception
			logger.info("更新微信联系人" + assistance.getUsername()+ "失败" + e);
			return false;
		}
	}
}
