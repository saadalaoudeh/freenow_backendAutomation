package com.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.utilities.ReadProperties;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONArray;

import static io.restassured.RestAssured.given;

public class TestBase {
    /**
     * <<<MANAGE THE ENVIRONMENT AND USER HERE>>>
     * To change environment need to change BASEURI; like test, staging, hotfix, prod
     *
     */
    public static String BASEURI = new ReadProperties().get("base_URI");

    public RequestSpecification spec() {
        RequestSpecification spec = given().log().all()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON);
        return spec;
    }
}
