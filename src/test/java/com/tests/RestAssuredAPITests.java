package com.tests;

import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static com.base.Base.DeSerializeFromFileToObject;
import static com.base.Base.SerializeToFile;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class RestAssuredAPITests {


    final static String BASEURI = "http://restapi.demoqa.com/utilities/weather/city";


    @Test
    public void getWeatherDetails () {

        // Specify the base URL to the RESTful web service
        RestAssured.baseURI = BASEURI;

        // Get the RequestSpecification of the request that you want to sent
        // to the server. The server is specified by the BaseURI that we have
        // specified in the above step.
        RequestSpecification httpRequest = RestAssured.given();

        // Make a request to the server by specifying the method Type and the method URL.
        // This will return the Response from the server. Store the response in a variable.
        Response response = httpRequest.request(Method.GET, "/Hyderabad");

        // Now let us print the body of the message to see what response
        // we have recieved from the server
        String responseBody = response.getBody().asString();
        System.out.println("Response Body is =>  " + responseBody);
        Assert.assertTrue(responseBody.contains("Hyderabad"));


        int responseCode = response.getStatusCode();
        System.out.println("Response Code is =>  " + responseCode);

        Assert.assertEquals(responseCode, 200);

    }

    @Test
    public void getWeatherDetails_BDDStyle() {

        when().
                get(BASEURI+ "/hyderabad").
        then().
                assertThat().
                statusCode(200).
                body("Humidity", endsWith("Percent"));

    }

    @Test
    public void test_NumberOfCircuitsFor2017Season_ShouldBe20() {

        given().
        when().
                get("http://ergast.com/api/f1/2017/circuits.json").
        then().
                assertThat().
                body("MRData.CircuitTable.Circuits.circuitId",hasSize(20));
    }

    @Test
    public void test_ResponseHeaderData_ShouldBeCorrect() {

        given().
        when().
                get("http://ergast.com/api/f1/2017/circuits.json").
        then().
                assertThat().statusCode(200).
                and().

                contentType(ContentType.JSON).

                and().
                header("Content-Length",equalTo("4551"));
    }

    @Test
    public void verifyMD5CheckSum() {

        given().
                param("text", "test").

        when().
                get("http://md5.jsontest.com").
        then().
                assertThat().
                body("md5", equalTo("098f6bcd4621d373cade4e832627b4f6"));

    }

    @Test
    public void test_NumberOfCircuits_ShouldBe20_Parameterized() {

        String season = "2017";
        int numberOfRaces = 20;

        given().
                pathParam("season", season).

        when().
                get("http://ergast.com/api/f1/{season}/circuits.json").

        then().
                assertThat().
                body("MRData.CircuitTable.Circuits.circuitId", hasSize(numberOfRaces));

    }

    @DataProvider(name="seasonsAndNumberOfRaces")
    public Object[][] createTestDataRecords() {
        return new Object[][] {
                {"2017",20},
                {"2016",21},
                {"1966",9}
        };
    }

    @Test(dataProvider="seasonsAndNumberOfRaces")
    public void test_NumberOfCircuits_ShouldBe_DataDriven(String season, int numberOfRaces) {

        given().
                pathParam("raceSeason",season).
        when().
                get("http://ergast.com/api/f1/{raceSeason}/circuits.json").
        then().
                assertThat().
                body("MRData.CircuitTable.Circuits.circuitId",hasSize(numberOfRaces));
    }


    @Test
    public void test_APIWithBasicAuthentication_ShouldBeGivenAccess() {

        given().
                auth().
                preemptive().
                basic("username", "password").
        when().
                get("http://path.to/basic/secured/api").
        then().
                assertThat().
                statusCode(200);
    }

    /*@Test(priority = -1)
    public void test_APIWithOAuth2Authentication_ShouldBeGivenAccess() {

        try {
            String authTokenGMailAPI = "908193927340-ie6adqgqn4phi6qjn6gp2vvqnrghsfsl.apps.googleusercontent.com";
            String clientSecret = "d4m1_H-L0ZEk3cMynRrud0wv";

            given().
                    pathParam("userId", "akashsood59@gmail.com").
                    auth().
                    oauth2(authTokenGMailAPI).
                    contentType(ContentType.JSON).
                    accept(ContentType.JSON).
            when().
                    get("https://www.googleapis.com/gmail/v1/users/{userId}/profile").
            then().
                    assertThat().
                    statusCode(200);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }*/


    @Test
    public void test_ScenarioRetrieveFirstCircuitFor2017SeasonAndGetCountry_ShouldBeAustralia() {

        // First, retrieve the circuit ID for the first circuit of the 2017 season
        String circuitId = given().
                when().
                get("http://ergast.com/api/f1/2017/circuits.json").
                then().
                extract().
                path("MRData.CircuitTable.Circuits.circuitId[0]");

        // Then, retrieve the information known for that circuit and verify it is located in Australia
        given().log().all().
                pathParam("circuitId",circuitId).
                when().
                get("http://ergast.com/api/f1/circuits/{circuitId}.json").
                then().
                assertThat().
                log().status().
                body("MRData.CircuitTable.Circuits.Location[0].country",equalTo("Australia"));
    }

    /**
     * To create a reusable ResponseSpecification that checks the aforementioned status code and content type
     */


    ResponseSpecification checkStatusCodeAndContentType =
            new ResponseSpecBuilder().
                    expectStatusCode(200).
                    expectContentType(ContentType.JSON).
                    build();

    @Test
    public void test_NumberOfCircuits_ShouldBe20_UsingResponseSpec() {

        given().log().all()
                .when().
                get("http://ergast.com/api/f1/2017/circuits.json").
                then().
                assertThat().
                spec(checkStatusCodeAndContentType).
                and().
                body("MRData.CircuitTable.Circuits.circuitId",hasSize(20));
    }

    @Test
    public void RegistrationSuccessful() {

        JSONObject requestParams = new JSONObject();
        requestParams.put("FirstName", "Akash2012"); // Cast
        requestParams.put("LastName", "Sood153");
        requestParams.put("UserName", "akash566492");
        requestParams.put("Password", "passwor24455222");
        requestParams.put("Email", "akash.sood.6659@gmail.com");

        Response response = given().
                        accept(ContentType.JSON).
                        contentType(ContentType.JSON).
                        body(requestParams).
                        when().
                        post("http://restapi.demoqa.com/customer/register");

        System.out.println(response.asString());
        JsonPath jsonPathEvaluator = response.jsonPath();

        if(response.getStatusCode()==201)
        {
            RegistrationSuccessfulResponse successResponse = response.body().as(RegistrationSuccessfulResponse.class);
            Assert.assertEquals(jsonPathEvaluator.get("SuccessCode"), successResponse.SuccessCode);
            Assert.assertEquals(jsonPathEvaluator.get("Message"), successResponse.Message);
        }

        else if (response.getStatusCode() == 200)
        {
            RegistrationFailureResponse failResponse = response.body().as(RegistrationFailureResponse.class);
            Assert.assertEquals(jsonPathEvaluator.get("FaultId"), failResponse.FaultId);
            Assert.assertEquals(jsonPathEvaluator.get("fault"), failResponse.fault);
        }

    }

    @Test
    public void serializationExample() {
        Rectangle rect = new Rectangle(18, 78);
        SerializeToFile(rect, "rectSerialized");

        Rectangle deSerializedRect = (Rectangle) DeSerializeFromFileToObject("rectSerialized");
        System.out.println("Rect area is " + deSerializedRect.Area());
    }

    @Test
    public void RegistrationUnSuccessful() {

        JSONObject requestParams = new JSONObject();
        requestParams.put("FirstName", "Akash2012"); // Cast
        requestParams.put("LastName", "Sood153");
        requestParams.put("UserName", "akash566492");
        requestParams.put("Password", "passwor24455222");
        requestParams.put("Email", "akash.sood.6659@gmail.com");

        Response response = given().
                accept(ContentType.JSON).
                contentType(ContentType.JSON).
                body(requestParams).
                when().
                get("http://restapi.demoqa.com/customer/register");

        System.out.println(response.asString());
        JsonPath jsonPathEvaluator = response.jsonPath();
        RegistrationFailureResponse failResponse = response.body().as(RegistrationFailureResponse.class);
        Assert.assertEquals(response.getStatusCode(), 405);
        Assert.assertEquals(jsonPathEvaluator.get("FaultId"), failResponse.FaultId);
        Assert.assertEquals(jsonPathEvaluator.get("fault"), failResponse.fault);
    }

}
