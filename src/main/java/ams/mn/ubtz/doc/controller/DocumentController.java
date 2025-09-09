package ams.mn.ubtz.doc.controller;

import java.util.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ams.mn.ubtz.doc.entity.Document;
import ams.mn.ubtz.doc.entity.DocumentLog;
import ams.mn.ubtz.doc.service.DocumentService;

@RestController
@RequestMapping("/doc")
public class DocumentController {
	
	@Autowired
	private DocumentService service;
	
	@RequestMapping("/list")
	public ResponseEntity<List<Object>> listDoc(@RequestParam String pid){
		
		List<Object> list=service.docList(pid);
		return new ResponseEntity<>(list,HttpStatus.OK);
	}
	
	/*@RequestMapping("/api/list")
	public ResponseEntity<List<Object>> listDocDown(){
		
		List<Object> list=service.docListDown();
		return new ResponseEntity<>(list,HttpStatus.OK);
	}*/
	
	@RequestMapping("/api/list")
    public ResponseEntity<List<Map<String, Object>>> getList(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "1000") int size) {
        List<Map<String, Object>> data = service.getPagedData(page, size);
        return ResponseEntity.ok(data);
    }
	
	@RequestMapping("/api/report")
	public ResponseEntity<List<Object>> getReport(
		    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
		    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
		    @RequestParam(required = false) Integer uid
		) {
		    List<Object> result = service.docListReport(fromDate, toDate, uid);
		    return ResponseEntity.ok(result);
		}
	
	@RequestMapping("/api/tnamelist")
    public ResponseEntity<List<Map<String, Object>>> getTnameList(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "1000") int size) {
        List<Map<String, Object>> data = service.getTnameData(page, size);
        return ResponseEntity.ok(data);
    }
	
	@RequestMapping("/api/delolist")
    public ResponseEntity<List<Map<String, Object>>> getUnitList(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "1000") int size) {
        List<Map<String, Object>> data = service.getUnitData(page, size);
        return ResponseEntity.ok(data);
    }
	
	@RequestMapping("/select")
	public ResponseEntity<List<Object>> select(@RequestParam int npid){
		
		List<Object> list=service.docSelect(npid);
		return new ResponseEntity<>(list,HttpStatus.OK);
	}
	
	@RequestMapping("/tname")
	public ResponseEntity<List<Object>> tname(@RequestParam int tid){
		
		List<Object> list=service.tname(tid);
		return new ResponseEntity<>(list,HttpStatus.OK);
	}
	
	@PostMapping("/insert")
	public String insertDoc(@RequestBody Document obj) throws SQLException {
		service.docInsert(obj);
		return "Баримт амжилттай хадгалагдлаа!";
	}
	
	@PostMapping("/insertlog")
	public String insertDocLog(@RequestBody DocumentLog obj) throws SQLException {
		service.docLogInsert(obj);
		return "Баримтын лог амжилттай хадгалагдлаа!";
	}
	
	@PostMapping("/update")
	public String updateDoc(@RequestBody Document obj) throws SQLException {
		service.docUpdate(obj);
		return "Баримт амжилттай хадгалагдлаа! /засах/";
	}
	
	
	
	@PostMapping("/delete")
	public ResponseEntity<String> delDoc(@RequestParam int npid, @RequestParam int uid) {
	    try {
	        service.docDelete(npid, uid);
	        return ResponseEntity.ok("Баримт амжилттай устгагдлаа!");
	    } catch (Exception e) {
	        e.printStackTrace(); // log file руу бичихэд тохиромжтой
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body("Устгах үед өгөгдлийн сантай холбоотой алдаа гарлаа.");
	    }
	}

}
