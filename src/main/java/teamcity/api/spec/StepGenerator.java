package teamcity.api.spec;

import teamcity.api.models.Step;

import java.util.Map;

public class StepGenerator {

    public static Step generateSimpleRunner(String content) {
        return Step.builder()
                .name("Simple Runner Step")
                .type("simpleRunner")
                .parameters(Map.of(
                        "use.custom.script", "true",
                        "script.content", content
                ))
                .build();
    }

}
