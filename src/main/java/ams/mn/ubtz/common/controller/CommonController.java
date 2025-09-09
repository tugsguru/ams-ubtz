package ams.mn.ubtz.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ams.mn.ubtz.common.service.LoginLogService;

@RestController
@RequestMapping("/login")
public class CommonController {
	
	@Autowired
	private LoginLogService service;
	
	@RequestMapping("/log")
	public ResponseEntity<List<Object>> listLoginLog(){
		
		List<Object> list=service.loginLogList();
		return new ResponseEntity<>(list,HttpStatus.OK);
	}

}
