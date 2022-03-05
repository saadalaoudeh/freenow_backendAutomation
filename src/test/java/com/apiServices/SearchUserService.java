package com.apiServices;

import com.tests.TestBase;

import io.restassured.response.Response;

import java.util.List;

public class SearchUserService extends TestBase {
    public static int id;

    public Response SearchUserName(String name) {
        Response response = spec().log().all()
                .when().get(BASEURI+"/users")
                .then().log().all().extract().response();

        id = response.path("find {it.username =='"+name+"'}.id");
        return response;
    }
}
