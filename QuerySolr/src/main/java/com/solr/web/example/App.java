package com.solr.web.example;

import static spark.Spark.port;
import static spark.Spark.post;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import spark.Request;
import spark.Response;

public class App {
	
	private static Map<String,String> domainIntelligence = new HashMap<String, String>();
	
	public static void main(String[] args) {
		
		domainIntelligence.put("security", " OR knox OR kerberos");
		
		port(11223);
		post("/query",(request,response) -> querySolr(request,response));
	}

	private static String querySolr(Request request, Response response) {
		
		response.type("application/json");
		
		JSONObject responseObj = new JSONObject();
		int docCount = 0;
		String httpResult = null;
		JSONParser parser = new JSONParser();
		JSONObject json =null;
		String queryString = "";
		try {
			
			json = (JSONObject) parser.parse(String.valueOf(request.queryParams().toArray()[0]));
			boolean useIntelligence = false;
			try {
				useIntelligence =(""+json.get("intelligence")).equalsIgnoreCase("true") ? true : false;
			}catch(Exception e) {}
			
			queryString = ""+json.get("queryString");
			
			if(useIntelligence) {
				if(domainIntelligence.containsKey(queryString)) {
					queryString = queryString+domainIntelligence.get(queryString);
				}
			}
			
			
			queryString = URLEncoder.encode(queryString);
			
		}catch(Exception e) {
			
			e.printStackTrace();
		}
		
		String scoreURL="http://c1161-redacted-.com:8886/solr/ocr1/select?q="+queryString+"&fl=id,score";
		String contentURL="http://c1161-redacted-.com:8886/solr/ocr1/select?q="+queryString;
		
		
		//http://10.42.17.20:8886/solr/pdfs/
		
		
		Map<String,String> solrResults = new HashMap<String,String>();
		try{
			/*
			---------------------------------------------------------------------------------------------
												get document score
			---------------------------------------------------------------------------------------------
			*/
			httpResult = sendGet(scoreURL);
			json = (JSONObject) parser.parse(httpResult);
			JSONArray docs = (JSONArray) ((JSONObject) json.get("response")).get("docs");
			Iterator<JSONObject> it = docs.iterator();
			while (it.hasNext()) {
				JSONObject jobj = it.next();
				solrResults.put(""+jobj.get("id"),""+jobj.get("score"));
				
			}
			
			/*
			---------------------------------------------------------------------------------------------
												get document content
			---------------------------------------------------------------------------------------------
			*/
			httpResult = sendGet(contentURL);
			
			json = (JSONObject) parser.parse(httpResult);
			docs = (JSONArray) ((JSONObject) json.get("response")).get("docs");
			
			it = docs.iterator();
			
			
			/*
			---------------------------------------------------------------------------------------------
									 generate resultant JSON with score and content
			---------------------------------------------------------------------------------------------
			*/
			
			while (it.hasNext()) {
				
				JSONObject jobj = it.next();
				String id = ""+jobj.get("id");
				if(solrResults.containsKey(id)) {
					
					JSONObject resultWithScore = new JSONObject();
					
					resultWithScore.put("id",id);
					resultWithScore.put("score",solrResults.get(id));
					resultWithScore.put("content",""+jobj.get("content"));
					resultWithScore.put("resource",""+jobj.get("resourcename"));
					resultWithScore.put("type",""+jobj.get("content_type"));
					resultWithScore.put("version",""+jobj.get("_version_"));
					docCount+=1;
					responseObj.put(String.valueOf(docCount), resultWithScore);
					
					
				}
				
				
			}
			
			responseObj.put("docCount", String.valueOf(docCount));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			// create a response code with error message
		}
		return responseObj.toString();
		
		
	}
	
	private static String sendGet(String url) throws Exception {
		
		System.out.println(url);
		CloseableHttpClient client = HttpClients.createDefault();
		String result = null;
		HttpGet request = new HttpGet(url);
		try (CloseableHttpResponse response = client.execute(request)) {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity);
			}
		}
		return result;
	}

}
