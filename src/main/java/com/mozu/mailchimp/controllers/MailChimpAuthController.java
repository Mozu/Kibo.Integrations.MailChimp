/**
 * 
 */
package com.mozu.mailchimp.controllers;


import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

/**
 * Super class to all controllers. Provides inherited functionality for HMAC
 * authentication for now.
 * 
 * @author Akshay
 * 
 */
@Controller
@Scope("session")
public class MailChimpAuthController {

	protected ResponseEntity<String> responseEntity(String respBody,
			HttpStatus httpStatus) {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Content-Type", "application/json; charset=utf-8");
		return new ResponseEntity<String>(respBody, responseHeaders, httpStatus);
	}

}
