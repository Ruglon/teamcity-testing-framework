package teamcity.api;

import org.testng.annotations.Test;
import teamcity.api.models.Project;

import static teamcity.api.enums.Endpoint.PROJECTS;
import static teamcity.api.generators.TestDataGenerator.generate;

@Test(groups = {"Regression"})
public class PositiveProjectTests extends BaseApiTest{

    @Test(description = "User should be able to create a project with correct data", groups = {"Positive", "Project"})
    public void userCreatesProjectWithCorrectDataTest() {
        var project = generate(Project.class);
        var createdProject = superUserCheckedRequests.<Project>getRequest(PROJECTS).create(project);
        softy.assertEquals(createdProject, testData.getProject());

        softy.assertEquals(createdProject, project, "Project must contain correct data");
    }

    @Test(description = "User should be able to create a project if id includes repeating symbols", groups = {"Positive", "Project"})
    public void userCreatesProjectWithRepeatingSymbolsIdTest() {
        var project = generate(Project.class);
        project.setId("aaaa_" + project.getId());
        var createdProject = superUserCheckedRequests.<Project>getRequest(PROJECTS).create(project);
        softy.assertEquals(createdProject, testData.getProject());

        softy.assertEquals(createdProject, project, "Project must match");
    }

    @Test(description = "User should be able to create a project if id has 225 symbols", groups = {"Positive", "Project"})
    public void userCreatesProjectWithLongIdTest() {
        var project = generate(Project.class);
        String longId = "a".repeat(225);
        project.setId(longId);
        var createdProject = superUserCheckedRequests.<Project>getRequest(PROJECTS).create(project);
        softy.assertEquals(createdProject, testData.getProject());

        softy.assertEquals(createdProject, project, "Project must match");
    }

    @Test(description = "User should be able to create a project if id includes latin letters, digits", groups = {"Positive", "Project"})
    public void userCreatesProjectWithLatinAndDigitsIdTest() {
        var project = generate(Project.class);
        project.setId("Project123_" + project.getId());
        var createdProject = superUserCheckedRequests.<Project>getRequest(PROJECTS).create(project);
        softy.assertEquals(createdProject, testData.getProject());

        softy.assertEquals(createdProject, project, "Project must able to include id includes latin letters, digits");
    }

    @Test(description = "User should be able to create a project if id includes 1 valid symbol", groups = {"Positive", "Project"})
    public void userCreatesProjectWithSingleSymbolIdTest() {
        var project = generate(Project.class);
        project.setId("a");
        var createdProject = superUserCheckedRequests.<Project>getRequest(PROJECTS).create(project);
        softy.assertEquals(createdProject, testData.getProject());

        softy.assertEquals(createdProject, project, "Project must has id with 1 symbol");
    }

    @Test(description = "User should be able to create a project if name has more than 225 symbols", groups = {"Positive", "Project"})
    public void userCreatesProjectWithLongNameTest() {
        var project = generate(Project.class);
        String longName = "Project_".repeat(32) + "x";
        project.setName(longName);
        var createdProject = superUserCheckedRequests.<Project>getRequest(PROJECTS).create(project);
        softy.assertEquals(createdProject, testData.getProject());

        softy.assertEquals(createdProject, project, "Project with name length more than 225 symbols should be created");
    }

    @Test(description = "User should be able to create a project if name has cyrillic symbols", groups = {"Positive", "Project"})
    public void userCreatesProjectWithCyrillicNameTest() {
        var project = generate(Project.class);
        project.setName("Проект_" + project.getName());
        var createdProject = superUserCheckedRequests.<Project>getRequest(PROJECTS).create(project);
        softy.assertEquals(createdProject, testData.getProject());

        softy.assertEquals(createdProject, project, "Project with  cyrillic symbols should be created");
    }

    @Test(description = "User should be able to create a project with 'copyAllAssociatedSettings' false", groups = {"Positive", "Project"})
    public void userCreatesProjectWithCopyAllAssociatedSettingsFalseTest() {
        var project = generate(Project.class);
        project.setCopyAllAssociatedSettings(false);
        var createdProject = superUserCheckedRequests.<Project>getRequest(PROJECTS).create(project);
        softy.assertEquals(createdProject, testData.getProject());

        softy.assertEquals(createdProject, project, "Project with 'copyAllAssociatedSettings' false must be created");
    }

    @Test(description = "User should be able to create a copy of a project", groups = {"Positive", "Project"})
    public void userCreatesProjectCopyTest() {
        var originalProject = generate(Project.class);
        superUserCheckedRequests.<Project>getRequest(PROJECTS).create(originalProject);
        var copyProject = Project.builder()
                .id(originalProject.getId() + "_Copy")
                .name(originalProject.getName() + "_Copy")
                .parentProject(Project.ProjectLocator.builder().locator("id:_Root").build())
                .sourceProject(Project.ProjectLocator.builder().locator("id:" + originalProject.getId()).build())
                .copyAllAssociatedSettings(true)
                .build();
        var createdCopy = superUserCheckedRequests.<Project>getRequest(PROJECTS).create(copyProject);

        softy.assertEquals(createdCopy, copyProject, "Copy project must match");
    }
}
