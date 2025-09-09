package ams.mn.ubtz.doc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ams.mn.ubtz.doc.service.DocumentSyncService;
import ams.mn.ubtz.doc.service.FileSyncService;
import ams.mn.ubtz.doc.service.impl.DocumentSyncServiceImpl.SyncResult;

@RestController
@RequestMapping("/api/sync")
public class DocumentSyncController {

    @Autowired
    private DocumentSyncService documentSyncService;
    
    @Autowired
    private FileSyncService fileSyncService;

    @GetMapping("/doc/start")
    public ResponseEntity<String> sync() {
        SyncResult result = documentSyncService.syncAllFromRemote();
        return ResponseEntity.ok(
            " Шинээр нэмэгдсэн: " + result.getInserted() +
            ", Шинэчлэгдсэн: " + result.getUpdated()
        );
    }
    
    @GetMapping("/tname/start")
    public ResponseEntity<String> syncName() {
        documentSyncService.syncAllFromRemoteTname();
        return ResponseEntity.ok("Таталт, хадгалалт дууслаа.");
    }
    
    @GetMapping("/delo/start")
    public ResponseEntity<String> syncDelo() {
    	int totalSaved = documentSyncService.syncAllFromRemoteDelo();
        return ResponseEntity.ok(""+ totalSaved);
    }
    
    /*@GetMapping("/download/start")
    public ResponseEntity<String> syncFileDownload() {
    	fileSyncService.syncFilesFromRemote();
        return ResponseEntity.ok("Таталт, хадгалалт дууслаа.");
    }*/
    
    //download start
    @GetMapping("/download/start")
    public ResponseEntity<String> syncFileDownload() {
    	ams.mn.ubtz.doc.service.impl.FileSyncServiceImpl.SyncResult result = fileSyncService.syncFilesFromRemote();
    	return ResponseEntity.ok(
                " Татагдсан файл: " + result.getTotal()+
                ", Нийт хэмжээ: " + result.getFormattedTotalSize()
            );
    }
    
    
}
