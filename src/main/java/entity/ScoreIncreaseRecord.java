package entity;

import java.sql.Timestamp;

public class ScoreIncreaseRecord implements java.io.Serializable{
	
	/**
	 * 
	 */
	private int id;
	private Timestamp time;
	private int increaseScore;
	private String hander;
	private String description;
	private byte status;
	private String username;
	
	public ScoreIncreaseRecord(){
	}
	
	public ScoreIncreaseRecord(int id, Timestamp time, int increaseScore, String hander, String description, byte status, String username){
		this.id = id;
		this.time = time;
		this.increaseScore = increaseScore;
		this.hander = hander;
		this.description = description;
		this.status = status;
		this.username = username;
	}
	
	public int getId(){
		return this.id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public Timestamp getTime(){
		return this.time;
	}
	
	public void setTime(Timestamp time){
		this.time = time;
	}
	
	public int getIncreaseScore(){
		return this.increaseScore;
	}
	
	public void setIncreaseScore(int increaseScore){
		this.increaseScore = increaseScore;
	}
	
	public String getHander(){
		return this.hander;
	}
	
	public void setHander(String hander){
		this.hander = hander;
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
	
	public String getUsername(){
		return this.username;
	}
	
	public void setUsername(String username){
		this.username = username;
	}

}
