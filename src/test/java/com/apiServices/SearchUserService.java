package com.apiServices;
import com.pojos.User;
import com.tests.TestBase;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.List;

public class SearchUserService extends TestBase {

    public static int id;
    public  static List<User> AllUsers;



    public Response SearchUserName(String name) {
        Response response = spec().log().all().when().get(BASEURI+"/users")
                .then().log().all().extract().response();

        id = response.path("find {it.username =='"+name+"'}.id");
        JsonPath jp=response.jsonPath();
        AllUsers=jp.getList("",User.class);
        for (User allUser : SearchUserService.AllUsers) {
            System.out.println(allUser.getUsername());
        }
        return response;
    }





}
