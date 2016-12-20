package entity;
// Generated 2016-12-17 23:35:50 by Hibernate Tools 5.1.0.Beta1

/**
 * Assistance generated by hbm2java
 */
public class Assistance implements java.io.Serializable {

	private String workId;
	private String sourceSet;
	private String name;
	private String phone;
	private String email;
	private String password;
	private String usertype;
	private String username;
	private String agentid;
	private Integer flag;
	private Double lastLogTime;
	private Boolean logLock;
	private Integer logNum;
	private Double timepoint;
	private String verifyCode;
	private String resetId;

	public Assistance() {
	}

	public Assistance(String workId, String name, String phone, String email, String password, String usertype) {
		this.workId = workId;
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.password = password;
		this.usertype = usertype;
	}

	public Assistance(String workId, String sourceSet, String name, String phone, String email, String password,
			String usertype, String username, String agentid, Integer flag, Double lastLogTime, Boolean logLock,
			Integer logNum, Double timepoint, String verifyCode, String resetId) {
		this.workId = workId;
		this.sourceSet = sourceSet;
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.password = password;
		this.usertype = usertype;
		this.username = username;
		this.agentid = agentid;
		this.flag = flag;
		this.lastLogTime = lastLogTime;
		this.logLock = logLock;
		this.logNum = logNum;
		this.timepoint = timepoint;
		this.verifyCode = verifyCode;
		this.resetId = resetId;
	}

	public String getWorkId() {
		return this.workId;
	}

	public void setWorkId(String workId) {
		this.workId = workId;
	}

	public String getSourceSet() {
		return this.sourceSet;
	}

	public void setSourceSet(String sourceSet) {
		this.sourceSet = sourceSet;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsertype() {
		return this.usertype;
	}

	public void setUsertype(String usertype) {
		this.usertype = usertype;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAgentid() {
		return this.agentid;
	}

	public void setAgentid(String agentid) {
		this.agentid = agentid;
	}

	public Integer getFlag() {
		return this.flag;
	}

	public void setFlag(Integer flag) {
		this.flag = flag;
	}

	public Double getLastLogTime() {
		return this.lastLogTime;
	}

	public void setLastLogTime(Double lastLogTime) {
		this.lastLogTime = lastLogTime;
	}

	public Boolean getLogLock() {
		return this.logLock;
	}

	public void setLogLock(Boolean logLock) {
		this.logLock = logLock;
	}

	public Integer getLogNum() {
		return this.logNum;
	}

	public void setLogNum(Integer logNum) {
		this.logNum = logNum;
	}

	public Double getTimepoint() {
		return this.timepoint;
	}

	public void setTimepoint(Double timepoint) {
		this.timepoint = timepoint;
	}

	public String getVerifyCode() {
		return this.verifyCode;
	}

	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}

	public String getResetId() {
		return this.resetId;
	}

	public void setResetId(String resetId) {
		this.resetId = resetId;
	}

}
