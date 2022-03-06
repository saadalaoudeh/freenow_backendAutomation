package com.tests;

import annotations.AzureTestCaseId;
import annotations.AzureTestPlanSuitId;
import com.apiServices.PostService;
import com.apiServices.SearchUserService;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import settings.TestSettings;


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
 @Execution(ExecutionMode.CONCURRENT)
 @ExtendWith(TestSettings.class)
 @AzureTestPlanSuitId(17458)

 public class PostsUserTest {
    SearchUserService callUser = new SearchUserService();
    PostService postService=new PostService();
    Response response;

    @DisplayName(" Post UserId Test")
    @Test
    @Tag("smoke")
    @AzureTestCaseId(13441)

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
