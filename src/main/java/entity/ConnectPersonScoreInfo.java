package entity;

import java.io.Serializable;

import org.apache.xml.resolver.apps.resolver;
public class ConnectPersonScoreInfo implements Serializable{

	private String username;
	private String phone;
	private String weiXin;
	private String company;
	private String realName;
	private String registerWay;
	private String agent;
	private String companyId;
	private String email;
	private String cardId;
	private String agentName;
	private Integer score;
	private Integer exchangedScore;
	private Integer exchangingScore;
	
	public ConnectPersonScoreInfo(){
		
	}
	
	public ConnectPersonScoreInfo(String username, String phone, String weiXin, String company, String realName, String registerWay,
														String agent, String companyId, String email, String cardId, String agentName, 
														Integer score, Integer exchanged_score, Integer exchanging_score){
		this.username = username;
		this.phone = phone;
		this.weiXin = weiXin;
		this.company = company;
		this.realName = realName;
		this.registerWay = registerWay;
		this.agent = agent;
		this.companyId = companyId;
		this.email = email;
		this.cardId = cardId;
		this.agentName = agentName;
		this.score = score;
		this.exchangedScore = exchanged_score;
		this.exchangingScore = exchanging_score;
	}
	
	public String getUsername(){
		return this.username;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public String getPhone(){
		return this.phone;
	}
	
	public void setPhone(String phone){
		this.phone = phone;
	}
	
	public String getWeiXin(){
		return this.weiXin;
	}
	
	public void setWeiXin(String weiXin){
		this.weiXin = weiXin;
	}
	
	public String getCompany(){
		return this.company;
	}
	
	public void setCompany(String company){
		this.company = company;
	}
	
	public String getRealName(){
		return this.realName;
	}
	
	public void setRealName(String realName){
		this.realName = realName;
	}
	
	public String getRegisterWay(){
		return this.registerWay;
	}
	
	public void setRegisterWay(String registerWay){
		this.registerWay = registerWay;
	}
	
	public String getAgent(){
		return this.agent;
	}
	
	public void setAgent(String agent){
		this.agent = agent;
	}
	
	public String getAgentName(){
		return this.agentName;
	}
	
	public void setAgentName(String agentName){
		this.agentName = agentName;
	}
	
	public String getCompanyId(){
		return this.companyId;
	}
	
	public void setCompanyId(String companyId){
		this.companyId = companyId;
	}
	
	public String getEmail(){
		return this.email;
	}
	
	public void setEmail(String email){
		this.email = email;
	}
	
	public String getCardId(){
		return this.cardId;
	}
	
	public void setCardId(String cardId){
		this.cardId = cardId;
	}
	
	public Integer getScore(){
		return this.score;
	}
	
	public void setScore(Integer score){
		this.score = score;
	}
	
	public Integer getExchangedScore(){
		return this.exchangedScore;
	}
	
	public void setExchangedScore(Integer exchangedScore){
		this.exchangedScore = exchangedScore;
	}
	
	public Integer getExchangingScore(){
		return this.exchangingScore;
	}
	
	public void setExchangingScore(Integer exchangingScore){
		this.exchangingScore = exchangingScore;
	}
}
