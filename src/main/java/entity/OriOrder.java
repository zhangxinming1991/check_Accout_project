package entity;
// Generated 2016-12-17 23:35:50 by Hibernate Tools 5.1.0.Beta1

/**
 * OriOrder generated by hbm2java
 */
public class OriOrder implements java.io.Serializable {

	private OriOrderId id;
	private String orderNum;
	private Double input;
	private Double debt;
	private Double total;
	private String state;
	private String updateTime;
	private String remark;
	private String client;
	private String owner;
	private String connectBank;
	private String customid;
	private String productTime;
	private String ownerProduct;
	private String customname;
	private String customphone;
	private String customweixin;
	private String asname;
	private String asphone;
	private String asemail;
	private String province;
	private int num;

	public OriOrder() {
	}

	public OriOrder(OriOrderId id, String orderNum, int num) {
		this.id = id;
		this.orderNum = orderNum;
		this.num = num;
	}

	public OriOrder(OriOrderId id, String orderNum, Double input, Double debt, Double total, String state,
			String updateTime, String remark, String client, String owner, String connectBank, String customid,
			String productTime, String ownerProduct, String customname, String customphone, String customweixin,
			String asname, String asphone, String asemail, String province, int num) {
		this.id = id;
		this.orderNum = orderNum;
		this.input = input;
		this.debt = debt;
		this.total = total;
		this.state = state;
		this.updateTime = updateTime;
		this.remark = remark;
		this.client = client;
		this.owner = owner;
		this.connectBank = connectBank;
		this.customid = customid;
		this.productTime = productTime;
		this.ownerProduct = ownerProduct;
		this.customname = customname;
		this.customphone = customphone;
		this.customweixin = customweixin;
		this.asname = asname;
		this.asphone = asphone;
		this.asemail = asemail;
		this.province = province;
		this.num = num;
	}

	public OriOrderId getId() {
		return this.id;
	}

	public void setId(OriOrderId id) {
		this.id = id;
	}

	public String getOrderNum() {
		return this.orderNum;
	}

	public void setOrderNum(String orderNum) {
		this.orderNum = orderNum;
	}

	public Double getInput() {
		return this.input;
	}

	public void setInput(Double input) {
		this.input = input;
	}

	public Double getDebt() {
		return this.debt;
	}

	public void setDebt(Double debt) {
		this.debt = debt;
	}

	public Double getTotal() {
		return this.total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getClient() {
		return this.client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getOwner() {
		return this.owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getConnectBank() {
		return this.connectBank;
	}

	public void setConnectBank(String connectBank) {
		this.connectBank = connectBank;
	}

	public String getCustomid() {
		return this.customid;
	}

	public void setCustomid(String customid) {
		this.customid = customid;
	}

	public String getProductTime() {
		return this.productTime;
	}

	public void setProductTime(String productTime) {
		this.productTime = productTime;
	}

	public String getOwnerProduct() {
		return this.ownerProduct;
	}

	public void setOwnerProduct(String ownerProduct) {
		this.ownerProduct = ownerProduct;
	}

	public String getCustomname() {
		return this.customname;
	}

	public void setCustomname(String customname) {
		this.customname = customname;
	}

	public String getCustomphone() {
		return this.customphone;
	}

	public void setCustomphone(String customphone) {
		this.customphone = customphone;
	}

	public String getCustomweixin() {
		return this.customweixin;
	}

	public void setCustomweixin(String customweixin) {
		this.customweixin = customweixin;
	}

	public String getAsname() {
		return this.asname;
	}

	public void setAsname(String asname) {
		this.asname = asname;
	}

	public String getAsphone() {
		return this.asphone;
	}

	public void setAsphone(String asphone) {
		this.asphone = asphone;
	}

	public String getAsemail() {
		return this.asemail;
	}

	public void setAsemail(String asemail) {
		this.asemail = asemail;
	}

	public String getProvince() {
		return this.province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public int getNum() {
		return this.num;
	}

	public void setNum(int num) {
		this.num = num;
	}

}
