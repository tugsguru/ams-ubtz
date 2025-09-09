package ams.mn.ubtz.delo.service;

import java.util.List;

import ams.mn.ubtz.delo.entity.Delo;

public interface DeloService {
	
	List<Object> deloList(String oid);
	List<Object> deloSelect(String lid);
	void deloInsert(Delo obj);
	void deloUpdate(Delo obj);
	
	List<Object> getFidOid(String fkod, String okod);
	
}
