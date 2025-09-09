package ams.mn.ubtz.doc.service;

import ams.mn.ubtz.doc.service.impl.DocumentSyncServiceImpl.SyncResult;

public interface DocumentSyncService {

	SyncResult syncAllFromRemote();
	void syncAllFromRemoteTname();
	int syncAllFromRemoteDelo();
	
	//void syncFilesFromRemote();
	
	
}
