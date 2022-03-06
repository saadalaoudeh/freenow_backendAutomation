package settings;

import exceptions.AzureInfoNotFound;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateAzureTesPlan {
    protected static String AZURE_BASE_URL;
    protected static String PAT;
    protected static int PLAN_ID;

    public UpdateAzureTesPlan(String baseUrl, String pat, int planId) {
        AZURE_BASE_URL = baseUrl;
        PAT = pat;
        PLAN_ID = planId;
    }

    public HashMap<String, Object> createTestRuns(String specName) {
        RequestSpecification requestSpecification = RestAssured
                .given()
                .header("Authorization", "Basic " + PAT)
                .header("Content-Type", "application/json")
                .baseUri(AZURE_BASE_URL)
                .pathParam("test", "test")
                .pathParam("runs", "runs")
                .queryParams("api-version", "6.1-preview.3")
                .relaxedHTTPSValidation()
                .log()
                .ifValidationFails();

        Map<String, Object> bodyParameters = new HashMap<>() {
            {
                put("name", specName);
                put("isAutomated", true);
                put("plan", new HashMap<>() {{
                    put("id", PLAN_ID);
                }});
            }
        };

        JSONObject queryParamJson = new JSONObject(bodyParameters);
        Response createTestRunRes = requestSpecification
                .body(queryParamJson.toString())
                .when()
                .post("{test}/{runs}")
                .then()
                .extract()
                .response();
        createTestRunRes
                .then()
                .log()
                .ifValidationFails()
                .statusCode(200);

        var results = new HashMap<String, Object>();
        results.put("testRunId", createTestRunRes.getBody().jsonPath().get("id"));
        results.put("testUrl", createTestRunRes.getBody().jsonPath().get("url"));
        results.put("testName", bodyParameters.get("name").toString());
        return results;
    }

    public static HashMap<Integer, HashMap<String, Object>> setTestCases(int testCaseId, int suitId) throws AzureInfoNotFound {

        RequestSpecification requestSpecification = RestAssured
                .given()
                .header("Authorization", "Basic " + PAT)
                .headers("Content-Type", "application/json")
                .pathParams("suitId", suitId)
                .baseUri(AZURE_BASE_URL)
                .pathParam("planId", PLAN_ID)
                .pathParams("testplan", "testplan")
                .pathParams("Plans", "Plans")
                .pathParams("suites", "suites")
                .pathParams("TestCase", "TestCase")
                .log()
                .ifValidationFails();

        Response suitResponse = requestSpecification
                .that()
                .get("/{testplan}/{Plans}/{planId}/{suites}/{suitId}/{TestCase}")
                .then()
                .extract()
                .response();
        suitResponse.then().log().ifValidationFails().statusCode(200);
        var results = new HashMap<Integer, HashMap<String, Object>>();
        var value = suitResponse.getBody().jsonPath().getList("value");
        for (Object workItem : value) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> workItemMap = (HashMap<String, Object>) (workItem instanceof HashMap ? workItem : null);
            assert workItemMap != null;
            @SuppressWarnings("unchecked")
            HashMap<String, Object> workItemDetail = (HashMap<String, Object>) (workItemMap.get("workItem") instanceof HashMap
                    ? workItemMap.get("workItem")
                    : null);
            @SuppressWarnings("unchecked")
            List<HashMap<String, Object>> pointAssignments = (List<HashMap<String, Object>>) workItemMap.get("pointAssignments");

            HashMap<String, Object> points = new HashMap<>();
            for (HashMap<String, Object> pointAssignment : pointAssignments) {
                points.put("id", pointAssignment.get("id"));
            }
            assert workItemDetail != null;
            @SuppressWarnings("unchecked")
            ArrayList<HashMap<String, Object>> workItemFields = (ArrayList<HashMap<String, Object>>) workItemDetail.get("workItemFields");
            String testCaseRevision = null;

            for (HashMap<String, Object> fields : workItemFields) {
                if (fields.get("System.Rev") != null)
                    testCaseRevision = fields.get("System.Rev").toString();
            }
            final String rev = testCaseRevision;

            results.put(Integer.parseInt(workItemDetail.get("id").toString()),
                    new HashMap<>() {{
                        put("name", workItemDetail.get("name").toString());
                        put("testCaseRevision", rev);
                        put("testPoint", points);
                        put("startTime", LocalDateTime.now());

                    }});
        }
        if (!results.containsKey(testCaseId)) {
            throw new AzureInfoNotFound("Test case id: " + testCaseId + " does not exists");
        }
        return results;
    }

    public void updateTestRun(String speckName, String test_run_id) {
        RequestSpecification requestSpecification = RestAssured
                .given()
                .header("Authorization", "Basic " + PAT)
                .header("Content-Type", "application/json")
                .baseUri(AZURE_BASE_URL)
                .pathParam("test", "test")
                .pathParam("runs", "runs")
                .pathParams("runId", test_run_id)
                .queryParams("api-version", "5.0")
                .relaxedHTTPSValidation()
                .log()
                .ifValidationFails();
        HashMap<String, String> bodyParameter = new HashMap<>() {{
            put("state", "Completed");
            put("postProcessState", "Complete");
        }};
        JSONObject bodParameterJson = new JSONObject(bodyParameter);
        Response response = requestSpecification.body(bodParameterJson.toString())
                .when()
                .patch("{test}/{runs}/{runId}")
                .then()
                .extract()
                .response();
        response.then().log().ifValidationFails().statusCode(200);
    }

    public void createTestResultToAzure(String specName,
                                        int testCaseId,
                                        String testStatus,
                                        String errorMessage,
                                        HashMap<String, HashMap<Object, Object>> testInfo) {

        String computerName = null;
        try {
            computerName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            System.out.println("Hostname can not be resolved");
        }
        final String computerName_f = computerName;
        @SuppressWarnings("unchecked")
        var testCases = (HashMap<String, Object>) testInfo.get(specName).get(testCaseId);
        var testPoint = (HashMap<String, Object>) testCases.get("testPoint");

        LocalDateTime toDateTime = LocalDateTime.of(2014, 9, 10, 6, 40, 45);

        var testStartTime = testCases.get("startTime");
        long startMin = ((LocalDateTime) testStartTime).until(toDateTime, ChronoUnit.MILLIS);
        long endMin = LocalDateTime.now().until(toDateTime, ChronoUnit.MILLIS);

        long testDuration = startMin - endMin;

        Map<String, Object> bodyParameter = new HashMap<>();
        bodyParameter.put("testCaseTitle", testCases.get("name").toString());
        bodyParameter.put("startedDate", testCases.get("startTime").toString());
        if (errorMessage != null)
            bodyParameter.put("errorMessage", errorMessage);
        bodyParameter.put("endedDate", LocalDateTime.now().toString());
        bodyParameter.put("durationInMs", testDuration);
        bodyParameter.put("testCaseTitle", testCases.get("name").toString());
        bodyParameter.put("computerName", computerName_f);
        bodyParameter.put("outcome", testStatus);
        bodyParameter.put("testCaseRevision", testCases.get("testCaseRevision").toString());
        bodyParameter.put("testPoint", testPoint);
        bodyParameter.put("testCase", new HashMap<String, Object>() {{
            put("id", testCaseId);
            put("name", testCases.get("name").toString());
        }});
        bodyParameter.put("testRun", new HashMap<String, Object>() {{
            put("id", testInfo.get(specName).get("testRunId").toString());
            put("name", testInfo.get(specName).get("testName").toString());
            put("url", testInfo.get(specName).get("testUrl").toString());
        }});
        bodyParameter.put("state", "Completed");

        RequestSpecification requestSpecification = RestAssured
                .given()
                .header("Authorization", "Basic " + PAT)
                .header("Content-Type", "application/json")
                .baseUri(AZURE_BASE_URL)
                .pathParam("test", "test")
                .pathParam("runs", "runs")
                .pathParams("runID", testInfo.get(specName).get("testRunId").toString())
                .pathParams("result", "results")
                .queryParams("api-version", "6.1-preview.6")
                .relaxedHTTPSValidation()
                .log()
                .ifValidationFails();

        JSONObject bodyParamJson = new JSONObject(bodyParameter);
        JSONArray bodyParamArray = new JSONArray();
        bodyParamArray.add(bodyParamJson);

        requestSpecification.that()
                .body(bodyParamArray.toString())
                .when()
                .post("{test}/{runs}/{runID}/{result}")
                .then()
                .log()
                .ifValidationFails()
                .statusCode(200);
    }

}
