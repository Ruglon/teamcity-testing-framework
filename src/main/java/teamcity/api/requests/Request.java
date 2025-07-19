package teamcity.api.requests;

import io.restassured.specification.RequestSpecification;
import teamcity.api.enums.Endpoint;

public class Request {

    /**
     * Request - это класс, описывающий меняющиеся параметры запроса, такие как:
     *  спецификация, эндпоинт (relative URL, model)
     */

    protected final RequestSpecification spec;
    protected final Endpoint endpoint;

    public Request(RequestSpecification spec, Endpoint endpoint){
        this.spec = spec;
        this.endpoint = endpoint;
    }
}
