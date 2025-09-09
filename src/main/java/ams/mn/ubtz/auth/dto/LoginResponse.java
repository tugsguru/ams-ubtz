package ams.mn.ubtz.auth.dto;

public class LoginResponse {
    private int uid;
    private String uname;
    private String wname;
    private int permission;
    private boolean success;

    // Constructor
    public LoginResponse(int uid, String uname, String wname, int permission, boolean success) {
        this.uid = uid;
        this.uname = uname;
        this.wname = wname;
        this.permission = permission;
        this.success = success;
    }

    // Getters and setters
    public int getUid() { return uid; }
    public void setUid(int uid) { this.uid = uid; }

    public String getUname() { return uname; }
    public void setUname(String uname) { this.uname = uname; }


    public String getWname() { return wname; }
    public void setWname(String wname) { this.wname = wname; }

    public int getPermission() { return permission; }
    public void setPermission(int permission) { this.permission = permission; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}

