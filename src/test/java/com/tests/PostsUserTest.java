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
 *then extract the userId of search user
 *
 * 3-navigate to https://jsonplaceholder.typicode.com/posts+userId
 *
 */

public class PostsUserTest {
    SearchUserService callUser = new SearchUserService();
    PostService postService=new PostService();
    Response response;

    @DisplayName(" Post UserId Test")
    @Test
    @Tag("smoke")
    public void getPostsTest() {
        response=callUser.SearchUserName("Samantha");
        int IdOfSearchUser= SearchUserService.id;
        System.out.println("IdOfSearchUser: "+IdOfSearchUser);
        response=postService.extractUserIdWhenPosts(IdOfSearchUser);
        int userIdOfSearchUser=PostService.userId;
        response= postService.createPostWithUserId(userIdOfSearchUser);
        Assertions.assertTrue(response.statusCode() == 200);
        //Assert.assertEquals(userIdOfSearchUser,response.jsonPath().getInt("id[0]"));

    }
}
