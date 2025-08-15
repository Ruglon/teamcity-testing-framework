package teamcity.api.spec;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

public class ValidationResponseSpecifications {

    private static final String PROJECT_NAME_ALREADY_EXISTS = "Project with this name already exists: %s";
    private static final String PROJECT_NAME_CANNOT_BE_EMPTY = "Project name cannot be empty: %s";
    private static final String PROJECT_ID_MUST_NOT_BE_EMPTY = "Project ID must not be empty: %s";
    private static final String INVALID_PROJECT_ID = "Invalid project ID: %s";
    private static final String SOURCE_PROJECT_NOT_FOUND = "Source project not found: %s";
    private static final String SOURCE_PROJECT_LOCATOR_EMPTY = "Source project locator cannot be empty";
    private static final String ACCESS_DENIED = "Access denied: insufficient permissions";



    public static ResponseSpecification checkProjectValidationError(String validationMessage, String projectInfo) {
        ResponseSpecBuilder responseSpecBuilder = new ResponseSpecBuilder();
        responseSpecBuilder.expectStatusCode(HttpStatus.SC_BAD_REQUEST);
        responseSpecBuilder.expectBody(Matchers.containsString(validationMessage.formatted(projectInfo)));
        return responseSpecBuilder.build();
    }

    public static ResponseSpecification checkProjectNameAlreadyExists(String projectName) {
        return checkProjectValidationError(PROJECT_NAME_ALREADY_EXISTS, projectName);
    }

    public static ResponseSpecification checkProjectNameCannotBeEmpty(String projectName) {
        return checkProjectValidationError(PROJECT_NAME_CANNOT_BE_EMPTY, projectName);
    }

    public static ResponseSpecification checkProjectIdMustNotBeEmpty(String projectName) {
        return checkProjectValidationError(PROJECT_ID_MUST_NOT_BE_EMPTY, projectName);
    }

    public static ResponseSpecification checkInvalidProjectId(String projectId) {
        return checkProjectValidationError(INVALID_PROJECT_ID, projectId);
    }

    public static ResponseSpecification checkSourceProjectNotFound(String sourceProjectId) {
        return checkProjectValidationError(SOURCE_PROJECT_NOT_FOUND, sourceProjectId);
    }

    public static ResponseSpecification checkSourceProjectLocatorEmpty() {
        ResponseSpecBuilder responseSpecBuilder = new ResponseSpecBuilder();
        responseSpecBuilder.expectStatusCode(HttpStatus.SC_BAD_REQUEST);
        responseSpecBuilder.expectBody(Matchers.containsString(SOURCE_PROJECT_LOCATOR_EMPTY));
        return responseSpecBuilder.build();
    }

    public static ResponseSpecification checkAccessDenied() {
        ResponseSpecBuilder responseSpecBuilder = new ResponseSpecBuilder();
        responseSpecBuilder.expectStatusCode(HttpStatus.SC_FORBIDDEN);
        responseSpecBuilder.expectBody(Matchers.containsString(ACCESS_DENIED));
        return responseSpecBuilder.build();
    }
}
