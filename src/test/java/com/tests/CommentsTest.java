package com.tests;

import annotations.AzureTestCaseId;
import annotations.AzureTestPlanSuitId;
import com.apiServices.CommentsService;
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


public class CommentsTest {

    SearchUserService callUser = new SearchUserService();
    PostService postService=new PostService();
    CommentsService commentsService=new CommentsService();
    Response response;

    @DisplayName("Comments Test")
    @Test
    @Tag("smoke")

    public void getCommentsTest() {
        response=callUser.SearchUserName("Samantha");
        int IdOfSearchUser= SearchUserService.id;
        response=postService.extractUserIdWhenPosts(IdOfSearchUser);
        int userIdOfSearchUser=PostService.userId;
        response= commentsService.Comments(userIdOfSearchUser);
        Assertions.assertTrue(response.statusCode() == 200);
    }
}
