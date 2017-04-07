/**
 * 
 */
package com.wurrly.controllers;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.jsoniter.any.Any;
import com.typesafe.config.Config;
import com.wurrly.models.User;
import com.wurrly.server.ServerRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * User API
 */
@Api(tags="users")
@Path("/api/users")
@Produces(("application/json")) 
@Consumes(("application/json")) 
@Singleton
public class Users  
{
	 

	@Inject
	@Named("es.index.name")
	protected String esIndexName;
	
	@Inject
	protected Config configuration;
	
	private static Logger log = LoggerFactory.getLogger(Users.class.getCanonicalName());
 

	public Users()
	{

	}

	@GET
	@Path("/{userId}/type")
	@ApiOperation(value = "Find users by id with type", httpMethod = "GET", response = User.class)
	public Any userType(
	                    @ApiParam(hidden=true)final ServerRequest serverRequest, @PathParam("userId") final Long userId, 
	                    @QueryParam("optionalQueryString")  Optional<String> optionalQueryString, 
	                    @QueryParam("optionalLong")  Optional<Long> optionalLong, 
	                    @QueryParam("longValue")   Long  longValue, 
	                    @QueryParam("dateValue") @ApiParam(format="date")  Optional<Date>  dateValue, 
	                    @QueryParam("numbers")    List<Integer>  numbers, 

	                      @QueryParam("queryFromString") UUID queryFromString, 
	                    @ApiParam(defaultValue="01234567-9ABC-DEF0-1234-56789ABCDEF0", format="uuid") @QueryParam("optionalQueryFromString") Optional<UUID> optionalQueryFromString, 
	                    @ApiParam(defaultValue="01234567-9ABC-DEF0-1234-56789ABCDEF0") @HeaderParam("headerFromString") Optional<UUID> headerFromString,
	                    @HeaderParam("headerString") String headerString,
	                    @HeaderParam("optionalHeaderString") Optional<String> optionalHeaderString,
	                    @QueryParam("queryEnum") User.UserType queryEnum, 
	                    @QueryParam("optionalQueryEnum") Optional<User.UserType> optionalQueryEnum
	                    
	                    )
	{
//		
 log.debug("optionalQueryFromString: " + optionalQueryFromString);
 log.debug("queryFromString: " + queryFromString);
 log.debug("optionalQueryString: " + optionalQueryString);
 log.debug("headerFromString: " + headerFromString);
 log.debug("headerString: " + headerString);
 log.debug("optionalHeaderString: " + optionalHeaderString);
 log.debug("queryEnum: " + queryEnum);
 log.debug("optionalQueryEnum: " + optionalQueryEnum);
 log.debug("userId: " + userId);
 log.debug("numbers: " + numbers);


//				
				return Any.wrap(new User(232343L));

	}
	
	@POST
	@Path("/form/{userId}")
 	@Consumes("*/*")
	@ApiOperation(value = "Post a complex form",   httpMethod = "POST", response = User.class)
	public Any userForm(@ApiParam(hidden=true) final ServerRequest serverRequest, 
	                    @ApiParam(name="userId",required=true) @PathParam("userId") final Long userId,
	                    @ApiParam(name="context",required=false) @QueryParam("context") Optional<String> context, 
	                    @ApiParam(name="type",required=true) @QueryParam("type") User.UserType type, 
	                    ByteBuffer testFile
	                    )
	{
//		
// 	log.debug("esIndexName: " + esIndexName);
// 	log.debug("configuration: " + configuration);

 	log.debug("testFile: " + testFile);
//
//				
				return Any.wrap(new User(userId,type));

	}
	 
	 
	@GET
	@Path("/{userId}")
	@ApiOperation(value = "Find users by id",   httpMethod = "GET", response = User.class)
	public Any user(@ApiParam(hidden=true)final ServerRequest serverRequest, 
	                @ApiParam(name="userId", required=true) @PathParam("userId") final Long userId, 
	                @ApiParam(name="context", required=false) @QueryParam("context") Optional<String> context
	                )
	{
//		
// 	log.debug("esIndexName: " + esIndexName);
// 	log.debug("configuration: " + configuration);

//		log.debug("context: " + context);
//
//				
				return Any.wrap(new User(userId));

	}
 
	 
	@POST
	@Path("/")
	//@Consumes("multipart/form-data")
//	@ApiImplicitParams({ @ApiImplicitParam(dataType = "com.wurrly.models.User", name = "user", paramType = "body", required = false, allowMultiple = false) })
	@ApiOperation(value = "Create a user",   httpMethod = "POST", response = User.class)
	public Any createUser(@ApiParam(hidden=true)final ServerRequest serverRequest,  @QueryParam("context") Optional<String> context, final User user  )
	{
//		
 
//	log.debug("context: " + context); 
//	log.debug("request: " + serverRequest); 
//	log.debug("file: " + user); 
		
		 return Any.wrap(new User(34L));
 
		 

	}
	
	@PUT
	@Path("/username")
	@Consumes(("application/json")) 
//	@ApiImplicitParams({ @ApiImplicitParam(dataType = "com.wurrly.models.User", name = "user", paramType = "body", required = false, allowMultiple = false) })
	@ApiOperation(value = "Update a user's name",   httpMethod = "PUT", response = User.class)
	public Any updateUsername(@ApiParam(hidden=true)final ServerRequest serverRequest,  @QueryParam("context") Optional<String> context, final User user  )
	{
//		
	log.debug("esIndexName: " + esIndexName); 
	log.debug("context: " + context); 
	log.debug("request: " + serverRequest); 
	log.debug("file: " + user); 

 
				return  Any.wrap(user);

	}



}
