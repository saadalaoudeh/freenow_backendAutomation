package settings;

import annotations.AzureTestCaseId;
import annotations.AzureTestPlanSuitId;
import exceptions.AzureInfoNotFound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;

public class TestSettings implements TestExecutionExceptionHandler, BeforeTestExecutionCallback, BeforeAllCallback, AfterAllCallback, AfterTestExecutionCallback {

    private static final String AZURE_BASE_URL = "";
    private static final String PAT = "";
    private static final int PLAN_ID = 7101;
    private static final HashMap<String, HashMap<Object, Object>> TEST_INFO = new HashMap<>();
    private static Long TEST_TIME;
    private static final String START_TIME = "Start Time";
    private final Boolean isResultPostToAzure = Boolean.valueOf(System.getProperty("isPost"));
    private static final Logger Log = LogManager.getLogger(TestSettings.class);
    private UpdateAzureTesPlan azureTesPlan;

    public TestSettings() {
        azureTesPlan = new UpdateAzureTesPlan(AZURE_BASE_URL, PAT, PLAN_ID);
    }

    public static void trace(String message) {
        Log.trace(message);
    }

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        // if an error occurred when test execution, TEST_STATUS update for azure runs update
        String className = extensionContext.getTestClass().get().getName();
        String methodName = extensionContext.getTestMethod().get().getName();
        error(throwable.getMessage());
        if (isResultPostToAzure) {
            TEST_INFO.get(className).put(
                    "testStatus", new HashMap<>() {{
                        put(methodName, throwable.getMessage());
                    }}
            );
        }
        throw throwable;
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {

        TEST_INFO.put(extensionContext.getTestClass().get().getName(), new HashMap<>());

        if (isResultPostToAzure) {
            String className = extensionContext.getTestClass().get().getName();

            var result = azureTesPlan.createTestRuns(className);
            TEST_INFO.get(className).putAll(result);
        }

    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws AzureInfoNotFound {

        // if isResultPostToAzure is true, post request to azur for create test result each test
        String errMsg;
        String methodName = extensionContext.getTestMethod().get().getName();
        String className = extensionContext.getTestClass().get().getName();
        String displayName = extensionContext.getDisplayName();
        endLog(displayName, className);

        var testStatus = (HashMap<String, String>) TEST_INFO.get(className).get("testStatus");

        if (testStatus != null)
            errMsg = testStatus.get(methodName);
        else errMsg = null;
//        errMsg = TEST_STATUS.get(className);
        if (isResultPostToAzure) {
            //getting azure test case id from AzureTestCaseId annotations value
            Method method = extensionContext.getTestMethod().get();
            int testCaseId;
            try {
                AzureTestCaseId testCase = method.getAnnotation(AzureTestCaseId.class);
                testCaseId = testCase.value();
            } catch (NullPointerException e) {
                throw new AzureInfoNotFound("please set azure test plan test case id");
            }
            // ----- //
            //set test stattus
            String outcome;

            if (errMsg == null)
                outcome = "Passed";
            else {
                outcome = "Failed";
            }
            // -- //
            // post request to azure api for creating test result of test case
            azureTesPlan.createTestResultToAzure(className, testCaseId, outcome, errMsg, TEST_INFO);
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        // post request to azure api for update test run status to complete
        String className = extensionContext.getTestClass().get().getName();
        if (isResultPostToAzure) {
            String runId = TEST_INFO.get(className).get("testRunId").toString();
            azureTesPlan.updateTestRun(className, runId);
        }
    }

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws AzureInfoNotFound {
        String className = extensionContext.getTestClass().get().getName();
        ThreadContext.put("className", className);
        String displayName = extensionContext.getDisplayName();
        final String traceId = UUID.randomUUID().toString();
        try {
            TEST_INFO.get(className).put(displayName,
                    new HashMap<String, String>() {{
                        put("traceId", traceId);
                    }});
        } catch (NullPointerException e) {
            className = extensionContext.getTestClass().get().getName();
            displayName = extensionContext.getDisplayName();
            TEST_INFO.get(className).put(displayName,
                    new HashMap<String, String>() {{
                        put("traceId", traceId);
                    }});
        }


        ThreadContext.put("traceId", traceId);
        Class<?> testClass = extensionContext.getTestClass().get();
        startLog(displayName, className);

        //if isResultPostToAzure is true, getting all testcaseId belonging the test suit
        if (isResultPostToAzure) {

            AzureTestPlanSuitId azureTestPlanSuitId = testClass.getAnnotation(AzureTestPlanSuitId.class);
            int suitId = azureTestPlanSuitId.value();
            Method method = extensionContext.getTestMethod().get();
            AzureTestCaseId azureTestCaseId = method.getAnnotation(AzureTestCaseId.class);
            try {
                int testCaseId = azureTestCaseId.value();
                var result = azureTesPlan.setTestCases(testCaseId, suitId);
                TEST_INFO.get(className).putAll(result);
            } catch (NullPointerException e) {
                new AzureInfoNotFound("please set azure test case Id");
            }

        }
    }

    public static void startLog(String displayName, String className) {
        Thread.currentThread().setName(displayName);
        int index = displayName.lastIndexOf(".") + 1;
        var traceId = (HashMap<String, String>) TEST_INFO.get(className).get(displayName);
        info("---------------Test is Starting..." + displayName.substring(index) + traceId + " ---------------");
    }

    //We can use it when ending tests
    public static void endLog(String displayName, String className) {
        int index = displayName.lastIndexOf(".") + 1;
        var traceId = (HashMap<String, String>) TEST_INFO.get(className).get(displayName);
        info("---------------Test is Ending..." + displayName.substring(index) + traceId + "---------------\n");

    }

    //Info Level Logs
    public static void info(String message) {
        Log.info(message);
    }

    //Warn Level Logs
    public static void warn(String message) {
        Log.warn(message);
    }

    //Error Level Logs
    public static void error(String message) {
        Log.error(message);
    }

    //Fatal Level Logs
    public static void fatal(String message) {
        Log.fatal(message);
    }

    //Debug Level Logs
    public static void debug(String message) {
        Log.debug(message);
    }
}
