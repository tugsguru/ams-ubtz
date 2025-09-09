package ams.mn.ubtz.auth.dto;

public class RegisterRequest {
    private String uname;
    private String pass;
    private String wname;

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

