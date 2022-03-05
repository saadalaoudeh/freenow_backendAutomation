package com.tests;

import com.apiServices.PostService;
import com.apiServices.SearchUserService;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 *1- navigate to https://jsonplaceholder.typicode.com/users
 * then extract the id of search user.
 *
 * 2-navigate to https://jsonplaceholder.typicode.com/posts with the parameter id of search user
 *then extract the userId of search user and verify that
 *  userId matches the actual value
 */

public class c_UserIdTest {
    SearchUserService callUser = new SearchUserService();
    PostService postService=new PostService();
    Response response;

    @DisplayName("UserId Test")
    @Test
    @Tag("smoke")
    public void searchForUserId() {
        response=callUser.SearchUserName("Samantha");
        int IdOfSearchUser= SearchUserService.id;
        System.out.println("idofsearchUser: "+IdOfSearchUser);
        response=postService.extractUserIdWhenPosts(IdOfSearchUser);
        int userIdOfSearchUser=PostService.userId;
        System.out.println("userIdOfSearchUser :"+userIdOfSearchUser);
        Assertions.assertTrue(response.statusCode() == 200);
        Assert.assertEquals(userIdOfSearchUser,1);
    }
}
