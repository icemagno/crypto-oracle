package br.com.cmabreu.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {

    @GetMapping( value="/getdata", produces= MediaType.APPLICATION_JSON_VALUE )
    public ResponseEntity<String> getData( ) {
    	return new ResponseEntity<String>( "" , HttpStatus.OK);
    }

   
}
