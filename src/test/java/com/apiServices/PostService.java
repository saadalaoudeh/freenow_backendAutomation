package com.apiServices;

import com.tests.TestBase;
import io.restassured.response.Response;

public class PostService extends TestBase {

    public static int userId;

    public Response extractUserIdWhenPosts(int id) {
        Response response = spec().log().all()
                .when().get(BASEURI+"/posts")
                .then().log().all().extract().response();

        userId = response.path("find {it.id =="+id+"}.userId");
        System.out.println("USERID:"+userId);
        return response;
    }

    public Response getPostWithUserId(int userId) {
        Response response = spec().log().all().when().get(BASEURI+"/posts/"+userId)
                .then().log().all().extract().response();
        return response;
    }
}
