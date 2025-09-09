package ams.mn.ubtz.user.model;

public class Users {

	private Long uid;
    private String uname;
    private String pass;
    private String wname;

	public Users(Long uid, String uname, String pass, String wname) {
		super();
		this.uid = uid;
		this.uname = uname;
		this.pass = pass;
		this.wname = wname;
	}

	public Long getUid() {
		return uid;
	}

	public void setUid(Long uid) {
		this.uid = uid;
	}

	public String getUname() {
		return uname;
	}

	public void setUname(String uname) {
		this.uname = uname;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getWname() {
		return wname;
	}

	public void setWname(String wname) {
		this.wname = wname;
	}
   
   
    
}
