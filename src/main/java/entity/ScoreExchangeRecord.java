package entity;

import java.io.Serializable;
import java.sql.Timestamp;

public class ScoreExchangeRecord  implements Serializable{

	private int id;
	private String username;
	private int exchangeScore;
	private byte exchangeType;
	private byte status;
	private Timestamp applicaTime;
	private Timestamp finishTime;
	private String serialNumber;
	private String randKey;
	private String description;
	private String hander;
	
	public ScoreExchangeRecord(){
		
	}
	
	public ScoreExchangeRecord( String username, int exchangeScore, byte exchangeType, byte status, 
			Timestamp applicaTime, String randKey, String description){
		this.username = username;
		this.exchangeScore = exchangeScore;
		this.exchangeType = exchangeType;
		this.status = status;
		this.applicaTime = applicaTime;
		this.randKey = randKey;
		this.description = description;
	}
	
	public ScoreExchangeRecord(int id, String username, int exchangeScore, byte exchangeType, byte status, 
			Timestamp applicaTime, Timestamp finishTime, String serialNumber, String randKey, String description, String hander){
		this.id = id;
		this.username = username;
		this.exchangeScore = exchangeScore;
		this.exchangeType = exchangeType;
		this.status = status;
		this.applicaTime = applicaTime;
		this.finishTime = finishTime;
		this.serialNumber = serialNumber;
		this.randKey = randKey;
		this.description = description;
		this.hander = hander;
	}
	
	public int getId(){
		return this.id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public int getExchangeScore(){
		return this.exchangeScore;
	}
	
	public void setExchangeScore(int exchangeScore){
		this.exchangeScore = exchangeScore;
	}
	
	public byte getExchangeType(){
		return this.exchangeType;
	}
	
	public void setExchangeType(byte exchangeType){
		this.exchangeType = exchangeType;
	}
	
	public Timestamp getApplicaTime(){
		return this.applicaTime;
	}
	
	public void setApplicaTime(Timestamp applicaTime){
		this.applicaTime = applicaTime;
	}
	
	public Timestamp getFinishTime(){
		return this.finishTime;
	}
	
	public void setFinishTime(Timestamp finishTime){
		this.finishTime = finishTime;
	}
	
	public String getSerialNumber(){
		return serialNumber;
	}
	
	public void setSerialNumber(String serialNumber){
		this.serialNumber = serialNumber;
	}
	
	public String getRandKey(){
		return this.randKey;
	}
	
	public void setRandKey(String randKey){
		this.randKey = randKey;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
	public byte getStatus(){
		return this.status;
	}
	
	public void setStatus(byte status){
		this.status = status;
	}
	
	public String getHander(){
		return this.hander;
	}
	
	public void setHander(String hander){
		this.hander = hander;
	}
}
