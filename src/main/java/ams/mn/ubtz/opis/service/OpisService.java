package ams.mn.ubtz.opis.service;

import java.util.List;

import ams.mn.ubtz.opis.entity.Opis;

public interface OpisService {
	
	List<Object> opisList(String fid);
	List<Object> getSelect(String oid);
	void opisInsert(Opis obj);
	void opisUpdate(Opis obj);

}
