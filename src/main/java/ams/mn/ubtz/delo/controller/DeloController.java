package ams.mn.ubtz.delo.controller;

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

import ams.mn.ubtz.delo.entity.Delo;
import ams.mn.ubtz.delo.service.DeloService;
import ams.mn.ubtz.opis.entity.Opis;

@RestController
@RequestMapping("/delo")
public class DeloController {
	
	@Autowired
	private DeloService service;
	
	@GetMapping("/list")
	public ResponseEntity<List<Object>> deloList(@RequestParam String oid){
		List<Object> list=service.deloList(oid);
		return new ResponseEntity<>(list,HttpStatus.OK);
	}
	
	@GetMapping("/select")
	public ResponseEntity<List<Object>> deloSelect(@RequestParam String lid){
		List<Object> list=service.deloSelect(lid);
		return new ResponseEntity<>(list,HttpStatus.OK);
	}
	
	@GetMapping("/fidoid")
	public ResponseEntity<List<Object>> deloList(@RequestParam String fkod,@RequestParam String okod){
		List<Object> list=service.getFidOid(fkod,okod);
		return new ResponseEntity<>(list,HttpStatus.OK);
	}
	
	@PostMapping("/insert")
	public String insertOpis(@RequestBody Delo obj) throws SQLException{
	    	service.deloInsert(obj);
	        return "Delo created successfully!";
	}
	
	@PostMapping("/update")
	public String update(@RequestBody Delo obj) throws SQLException {
		service.deloUpdate(obj);
		return "Delo updates successfully!";
	}
	
}
