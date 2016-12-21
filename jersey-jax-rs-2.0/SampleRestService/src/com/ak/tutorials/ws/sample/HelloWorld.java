package com.ak.tutorials.ws.sample;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/HelloWorld")
public class HelloWorld {
	
	
	@GET
	   @Path("/getHello")
	   @Produces(MediaType.APPLICATION_JSON)
	   public String getHello(){
	      return "{\"key\":\"hello world\"}";
	   }
	
	@POST
	 @Path("/getName")
	 @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	   public String getName(String data){
	      return "{\"name\":\""+data+"\"}";
	   }
}
