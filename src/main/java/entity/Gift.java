package entity;

import java.io.Serializable;

public class Gift implements Serializable{
	
	private int id;
	private String gift;
	private int stock;
	private int score;
	
	public Gift(){
		
	}
	
	public Gift(String gift, int stock, int score){
		this.gift = gift;
		this.stock = stock;
		this.score = score;
	}
	
	public Gift(int id, String gift, int stock, int score){
		this.id = id;
		this.gift = gift;
		this.stock = stock;
		this.score = score;
	}
	
	public int getId(){
		return this.id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public String getGift(){
		return this.gift;
	}
	
	public void setGift(String gift){
		this.gift = gift;
	}
	
	public int getStock(){
		return this.stock;
	}
	
	public void setStock(int stock){
		this.stock = stock;
	}
	
	public int getScore(){
		return this.score;
	}
	
	public void setScore(int score){
		this.score = score;
	}
}
