package ams.mn.ubtz.auth.model;

public class User {
    private int uid;
    private String uname;
    private String pass;
    private int permission;
    private String wname;

    // Getters & Setters
    public int getUid() {
        return uid;
    }
    public void setUid(int uid) {
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

    public int getPermission() {
        return permission;
    }
    public void setPermission(int permission) {
        this.permission = permission;
    }

    public String getWname() {
        return wname;
    }
    public void setWname(String wname) {
        this.wname = wname;
    }
}
