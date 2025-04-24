package br.com.cmabreu.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class RESTService {


	private HttpHeaders getHeaders(){
		HttpHeaders headers = new HttpHeaders();
		headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36");
		//headers.add("accept-encoding", "gzip, deflate, br");
		headers.add("accept-language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7");
		headers.add("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		headers.add("cache-control", "max-age=0");
		return headers;
	}
	
	public String doRequestGetWithAuthKey( String url, String authKey ) {
		HttpHeaders headers = this.getHeaders();
		headers.add("Authentication", authKey);
		return this.doRequest(url, headers);
	}

	public String doRequestGet( String url ) {
		return this.doRequest(url, this.getHeaders() );
	}	
	
	
	private String doRequest( String url, HttpHeaders headers ) {
		String responseBody = "";
		RestTemplate restTemplate = new RestTemplate( /* authService.getFactory() */ );

		
		HttpEntity<String> request = new HttpEntity<String>(headers);

		try {
			ResponseEntity<String> response = restTemplate.exchange(
			        url,
			        HttpMethod.GET,
			        request,
			        String.class
			);			
			
			if (response.getStatusCode() == HttpStatus.OK) {
				responseBody = response.getBody().toString();
			} else {
			    System.out.println( "Request Failed " + response.getStatusCode() );
			}			
			
		} catch (HttpClientErrorException e) {
		    responseBody = e.getResponseBodyAsString();
		} catch ( Exception ex) {
			return ex.getMessage();
		}
		return responseBody;
	}
	
	
}