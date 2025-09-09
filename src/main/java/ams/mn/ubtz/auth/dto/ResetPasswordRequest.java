package ams.mn.ubtz.auth.dto;

public class ResetPasswordRequest {
	
    private String adminUname;
    private String targetUname;
    private String newPassword;

    // Getter & Setter
    public String getAdminUname() {
        return adminUname;
    }

    public void setAdminUname(String adminUname) {
        this.adminUname = adminUname;
    }

    public String getTargetUname() {
        return targetUname;
    }

    public void setTargetUname(String targetUname) {
        this.targetUname = targetUname;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
