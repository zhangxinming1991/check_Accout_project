package entity;
// Generated 2016-12-26 13:35:33 by Hibernate Tools 5.1.0.Beta1

/**
 * PayRecordHistory generated by hbm2java
 */
public class PayRecordHistory implements java.io.Serializable {

	private int id;
	private String payer;
	private double payMoney;
	private String payWay;
	private String payAccount;
	private String vicePayer;
	private String receiver;
	private String connPerson;
	private String linkCer;
	private Integer bankinputId;
	private String owner;
	private Character checkResult;
	private Boolean pass;
	private Boolean isconnect;
	private String manyPay;
	private String uploadTime;
	private String contractNum;
	private String caid;
	private Boolean freeback;
	private String actualPayer;
	private String paymentNature;
	private String actualPayTime;

	public PayRecordHistory() {
	}

	public PayRecordHistory(int id, String payer, double payMoney, String payWay) {
		this.id = id;
		this.payer = payer;
		this.payMoney = payMoney;
		this.payWay = payWay;
	}

	public PayRecordHistory(int id, String payer, double payMoney, String payWay, String payAccount, String vicePayer,
			String receiver, String connPerson, String linkCer, Integer bankinputId, String owner,
			Character checkResult, Boolean pass, Boolean isconnect, String manyPay, String uploadTime,
			String contractNum, String caid, Boolean freeback, String actualPayer, String paymentNature,
			String actualPayTime) {
		this.id = id;
		this.payer = payer;
		this.payMoney = payMoney;
		this.payWay = payWay;
		this.payAccount = payAccount;
		this.vicePayer = vicePayer;
		this.receiver = receiver;
		this.connPerson = connPerson;
		this.linkCer = linkCer;
		this.bankinputId = bankinputId;
		this.owner = owner;
		this.checkResult = checkResult;
		this.pass = pass;
		this.isconnect = isconnect;
		this.manyPay = manyPay;
		this.uploadTime = uploadTime;
		this.contractNum = contractNum;
		this.caid = caid;
		this.freeback = freeback;
		this.actualPayer = actualPayer;
		this.paymentNature = paymentNature;
		this.actualPayTime = actualPayTime;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPayer() {
		return this.payer;
	}

	public void setPayer(String payer) {
		this.payer = payer;
	}

	public double getPayMoney() {
		return this.payMoney;
	}

	public void setPayMoney(double payMoney) {
		this.payMoney = payMoney;
	}

	public String getPayWay() {
		return this.payWay;
	}

	public void setPayWay(String payWay) {
		this.payWay = payWay;
	}

	public String getPayAccount() {
		return this.payAccount;
	}

	public void setPayAccount(String payAccount) {
		this.payAccount = payAccount;
	}

	public String getVicePayer() {
		return this.vicePayer;
	}

	public void setVicePayer(String vicePayer) {
		this.vicePayer = vicePayer;
	}

	public String getReceiver() {
		return this.receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getConnPerson() {
		return this.connPerson;
	}

	public void setConnPerson(String connPerson) {
		this.connPerson = connPerson;
	}

	public String getLinkCer() {
		return this.linkCer;
	}

	public void setLinkCer(String linkCer) {
		this.linkCer = linkCer;
	}

	public Integer getBankinputId() {
		return this.bankinputId;
	}

	public void setBankinputId(Integer bankinputId) {
		this.bankinputId = bankinputId;
	}

	public String getOwner() {
		return this.owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Character getCheckResult() {
		return this.checkResult;
	}

	public void setCheckResult(Character checkResult) {
		this.checkResult = checkResult;
	}

	public Boolean getPass() {
		return this.pass;
	}

	public void setPass(Boolean pass) {
		this.pass = pass;
	}

	public Boolean getIsconnect() {
		return this.isconnect;
	}

	public void setIsconnect(Boolean isconnect) {
		this.isconnect = isconnect;
	}

	public String getManyPay() {
		return this.manyPay;
	}

	public void setManyPay(String manyPay) {
		this.manyPay = manyPay;
	}

	public String getUploadTime() {
		return this.uploadTime;
	}

	public void setUploadTime(String uploadTime) {
		this.uploadTime = uploadTime;
	}

	public String getContractNum() {
		return this.contractNum;
	}

	public void setContractNum(String contractNum) {
		this.contractNum = contractNum;
	}

	public String getCaid() {
		return this.caid;
	}

	public void setCaid(String caid) {
		this.caid = caid;
	}

	public Boolean getFreeback() {
		return this.freeback;
	}

	public void setFreeback(Boolean freeback) {
		this.freeback = freeback;
	}

	public String getActualPayer() {
		return this.actualPayer;
	}

	public void setActualPayer(String actualPayer) {
		this.actualPayer = actualPayer;
	}

	public String getPaymentNature() {
		return this.paymentNature;
	}

	public void setPaymentNature(String paymentNature) {
		this.paymentNature = paymentNature;
	}

	public String getActualPayTime() {
		return this.actualPayTime;
	}

	public void setActualPayTime(String actualPayTime) {
		this.actualPayTime = actualPayTime;
	}

}
