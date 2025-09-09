package ams.mn.ubtz.doc.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DocumentLog {
	
	private int npid;
	private String type;
	private int uid;
	private LocalDateTime logDate;

	public DocumentLog(int npid, String type, int uid, LocalDateTime logDate) {
		super();
		this.npid = npid;
		this.type = type;
		this.uid = uid;
		this.logDate = logDate;
	}
	
	public int getNpid() {
		return npid;
	}
	public void setNpid(int npid) {
		this.npid = npid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public LocalDateTime getLogDate() {
		return logDate;
	}
	public void setLogDate(LocalDateTime logDate) {
		this.logDate = logDate;
	}
	
	

}
