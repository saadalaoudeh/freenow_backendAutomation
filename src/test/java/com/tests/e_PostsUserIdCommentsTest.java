package com.tests;

import com.apiServices.CommentsService;
import com.apiServices.PostService;
import com.apiServices.SearchUserService;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class e_PostsUserIdCommentsTest {

    SearchUserService callUser = new SearchUserService();
    PostService postService=new PostService();
    CommentsService commentsService=new CommentsService();
    Response response;

    @DisplayName(" Comments Test")
    @Test
    @Tag("smoke")
    public void createCommentsTest() {
        response=callUser.SearchUserName("Samantha");
        int IdOfSearchUser= SearchUserService.id;
        System.out.println("ıdofsearchUser: "+IdOfSearchUser);
        response=postService.extractUserIdWhenPosts(IdOfSearchUser);
        int userIdOfSearchUser=PostService.userId;
        response= commentsService.Comments(userIdOfSearchUser);
        Assertions.assertTrue(response.statusCode() == 200);




    }
}
