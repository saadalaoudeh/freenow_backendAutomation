package com.tests;

import com.apiServices.SearchUserService;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * navigate to https://jsonplaceholder.typicode.com/users
 * then extract the id of search user and verify that
 * Id of search user matches the actual value.
 */

public class b_IdTest {

    SearchUserService callUser = new SearchUserService();
    Response response;

    @DisplayName("Id Test")
    @Test
    @Tag("smoke")
    public void searchForId() {
        response=callUser.SearchUserName("Samantha");
        int IdOfSearchUser= SearchUserService.id;
        System.out.println("IdOfSearchUser: "+IdOfSearchUser);
        Assertions.assertTrue(response.statusCode() == 200);
        Assert.assertEquals(IdOfSearchUser,3);
    }

}
