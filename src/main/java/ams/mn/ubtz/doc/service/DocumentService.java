package ams.mn.ubtz.doc.service;

import java.util.List;
import java.util.Map;

import ams.mn.ubtz.doc.entity.Document;
import ams.mn.ubtz.doc.entity.DocumentLog;
import java.util.Date;

public interface DocumentService {
	
	List<Object> docList(String pid);
	List<Object> docSelect(int npid);
	void docInsert(Document obj);
	void docUpdate(Document obj);
	void docDelete(int npid,int uid);
	void docLogInsert(DocumentLog obj);
	List<Object> tname(int tid);
	//List<Object> getFidOid(String fkod, String okod);
	List<Object> docListDown();
	
	
	List<Map<String, Object>> getPagedData(int page, int size);
	List<Map<String, Object>> getTnameData(int page, int size);
	List<Map<String, Object>> getUnitData(int page, int size);
	
	List<Object> docListReport(Date fromDate, Date toDate, Integer uid);
	
	
	
}
