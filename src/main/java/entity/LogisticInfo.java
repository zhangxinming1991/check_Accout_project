package entity;

import java.io.Serializable;
public class LogisticInfo implements Serializable{

	private int id;
	private String randKey;
	private String user;
	private String phone;
	private String address;
	private String logisticCompany;
	private String logisticNumber;
	
	public LogisticInfo(){
		
	}
	
	public LogisticInfo(String randKey, String user, String phone, String address, String logisticCompany, String logisticNumber){
		this.randKey = randKey;
		this.user = user;
		this.phone = phone;
		this.address = address;
		this.logisticCompany = logisticCompany;
		this.logisticNumber = logisticNumber;
	}
	
	public LogisticInfo(int id, String randKey, String user, String phone, String address, String logisticCompany, String logisticNumber){
		this.id = id;
		this.randKey = randKey;
		this.user = user;
		this.phone = phone;
		this.address = address;
		this.logisticCompany = logisticCompany;
		this.logisticNumber = logisticNumber;
	}
	
	public int getId(){
		return this.id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public String getRandKey(){
		return this.randKey;
	}
	
	public void setRandKey(String randKey){
		this.randKey = randKey;
	}
	
	public String getUser(){
		return this.user;
	}
	
	public void setUser(String user){
		this.user = user;
	}
	
	public String getPhone(){
		return this.phone;
	}
	
	public void setPhone(String phone){
		this.phone = phone;
	}
	
	public String getAddress(){
		return this.address;
	}
	
	public void setAddress(String address){
		this.address = address;
	}
	
	public String getLogisticCompany(){
		return this.logisticCompany;
	}
	
	public void setLogisticCompany(String logisticCompany){
		this.logisticCompany = logisticCompany;
	}
	
	public String getLogisticNumber(){
		return this.logisticNumber;
	}
	
	public void setLogisticNumber(String logisticNumber){
		this.logisticNumber = logisticNumber;
	}
}
