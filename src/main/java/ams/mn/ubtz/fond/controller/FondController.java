package ams.mn.ubtz.fond.controller;

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
import ams.mn.ubtz.fond.service.FondService;

@RestController
@RequestMapping("/fond")

public class FondController {
	
	@Autowired
    private FondService fondService;

    @GetMapping("/list")
    public ResponseEntity<List<Object>> getAll() {
        
    	List<Object> list = fondService.getAllUsers();
    	return new ResponseEntity<>(list,HttpStatus.OK);
    }
    
    
    @PostMapping("/insert")
    public String insertFond(@RequestBody Fond obj) throws SQLException{
    	fondService.fondInsert(obj);;
        return "Fond created successfully!";
    }
    
    @PostMapping("/update")
    public String updateFond(@RequestBody Fond obj) throws SQLException{
    	fondService.fondUpdate(obj);;
        return "Fond updated successfully!";
    }
    
    @GetMapping("/select")
    public List<Object> getSelect(@RequestParam("fid") String fid) {
        return fondService.getSelect(fid);
    }
    
    @GetMapping("/getfid")
    public ResponseEntity<List<Object>> getFid(@RequestParam("fkod") String fkod) {
        
    	List<Object> list = fondService.getFid(fkod);
    	return new ResponseEntity<>(list,HttpStatus.OK);
    }


}
