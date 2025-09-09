package ams.mn.ubtz.fond.service;

import java.util.List;

import ams.mn.ubtz.fond.entity.Fond;

public interface FondService {
	
	List<Object> getAllUsers();
	List<Object> getSelect(String fid);
	List<Object> getFid(String fkod);
	void fondInsert(Fond obj);
	void fondUpdate(Fond obj);
	
}
