/**
 * 
 */
package io.sinistral.proteus.openapi.test.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.LongStream;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author jbauer
 */
@RunWith(OpenAPIDefaultServer.class)
public class TestOpenAPIControllerEndpoints
{
 
	private File file = null;
	
	private Set<Long> idSet = new HashSet<>();

	@Before
	public void setUp()
	{
		try
		{
 			byte[] bytes  = new byte[8388608];
			Random random = new Random(); 
			random.nextBytes(bytes);

			file = Files.createTempFile("test-asset", ".mp4").toFile();
			
			LongStream.range(1L,10L).forEach( l -> {
				
				idSet.add(l);
			});
			 

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	@Test
	public void testOpenAPIDocs()
	{
		when().get("openapi.yaml").then().statusCode(200);
	}

	@Test
	public void testPojoType()
	{
		when().get("tests/types/pojo").then().statusCode(200).body("id", equalTo(100),"name", equalTo("John Doe"));
	}

	@Test
	public void testMoneyType()
	{
		when().get("tests/types/money").then().statusCode(200).body("amount", equalTo(123.23f),"currency", equalTo("USD"));
	}


	@Test
	public void testSecurityRequirementEndpoint()
	{
		when().get("tests/secure/resource").then().statusCode(200);
	}

	@Test
	public void testJsonViewEndpoint()
	{
		when().get("tests/response/jsonview").then().statusCode(200);
	}
	
	@After
	public void tearDown()
	{
		try
		{
 			if(file.exists())
 			{
 				file.delete();
 			}

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
