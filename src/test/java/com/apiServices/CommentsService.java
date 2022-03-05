package com.apiServices;

import com.tests.TestBase;
import io.restassured.response.Response;

public class CommentsService extends TestBase {

    public Response Comments(int  userId){
        Response response=spec().log().all()
                    .when()
                    .get(BASEURI+"/post/"+userId+"/comments")
                    .then().log().all().extract().response();
            return response;
        }






}
