package com.tests;

import annotations.AzureTestCaseId;
import annotations.AzureTestPlanSuitId;
import com.apiServices.CommentsService;
import com.apiServices.PostService;
import com.apiServices.SearchUserService;
import com.utilities.Helpers;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import settings.TestSettings;

import java.util.List;
import java.util.stream.Collectors;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(TestSettings.class)
@AzureTestPlanSuitId(17458)
public class EmailCommentsTest {

    Response response;
    SearchUserService callUser = new SearchUserService();
    PostService postService=new PostService();
    CommentsService commentsService=new CommentsService();

    @DisplayName("Verify Emails on Comments")
    @Test
    @Tag("smoke")
    @AzureTestCaseId(13442)
    public void validateEmail() {

        response=callUser.SearchUserName("Samantha");
        int IdOfSearchUser= SearchUserService.id;
        System.out.println("IdOfSearchUser: "+IdOfSearchUser);
        response=postService.extractUserIdWhenPosts(IdOfSearchUser);
        int userIdOfSearchUser=PostService.userId;
        response= commentsService.Comments(userIdOfSearchUser);
        response.jsonPath().getList("email")
                .stream()
                .parallel()
                .forEach(email->{
                    Assertions.assertTrue(Helpers.isValidEmailAddress(String.valueOf(email)));
                });

        List sample=response.jsonPath().getList("email")
                .stream()
                .parallel()
                .filter(email->!Helpers.isValidEmailAddress(String.valueOf(email))).collect(Collectors.toList());
        Assertions.assertTrue(sample.size()==0,sample.toString());
    }


}
