package ams.mn.ubtz.opis.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ams.mn.ubtz.fond.entity.Fond;
import ams.mn.ubtz.opis.entity.Opis;
import ams.mn.ubtz.opis.service.OpisService;

@RestController
@RequestMapping("/opis")
public class OpisController {
	
	@Autowired
	private OpisService service;
	
	@GetMapping("/list")
	public ResponseEntity<List<Object>> getOpisList(@RequestParam("fid") String fid){
		
		List<Object> list= service.opisList(fid);
		
		return new ResponseEntity<>(list, HttpStatus.OK);
	} 
	
	@GetMapping("/select")
	public ResponseEntity<List<Object>> getSelectOpis(@RequestParam("oid") String oid){
		
		List<Object> list = service.getSelect(oid);
		return new ResponseEntity<>(list,HttpStatus.OK);
	}
	
	@PostMapping("/insert")
	public String insertOpis(@RequestBody Opis obj) throws SQLException{
	    	service.opisInsert(obj);
	        return "Opis created successfully!";
	}
	
	@PostMapping("/update")
	public String update(@RequestBody Opis obj) throws SQLException{
	    	service.opisUpdate(obj);
	        return "Opis updated successfully!";
	}
	
	

}
