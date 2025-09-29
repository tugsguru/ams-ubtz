package ams.mn.ubtz.doc.service;

import ams.mn.ubtz.doc.service.impl.DocumentSyncServiceImpl.SyncResult;

public interface DocumentSyncService {

	SyncResult syncAllFromRemoteDoc();
	void syncAllFromRemoteTname();
	int syncAllFromRemoteDelo();
	
	//void syncFilesFromRemote();
	
	
}
