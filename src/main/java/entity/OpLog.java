package entity;
// Generated 2016-12-26 13:35:33 by Hibernate Tools 5.1.0.Beta1

/**
 * OpLog generated by hbm2java
 */
public class OpLog implements java.io.Serializable {

	private Integer id;
	private String time;
	private String usertype;
	private String username;
	private String content;
	private String result;

	public OpLog() {
	}

	public OpLog(String time, String usertype, String username, String content, String result) {
		this.time = time;
		this.usertype = usertype;
		this.username = username;
		this.content = content;
		this.result = result;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTime() {
		return this.time;
	}

	public void setTime(String time) {
		this.time = time;
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

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getResult() {
		return this.result;
	}

	public void setResult(String result) {
		this.result = result;
	}

}
