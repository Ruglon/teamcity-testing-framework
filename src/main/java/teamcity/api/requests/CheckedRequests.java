package teamcity.api.requests;

import io.restassured.specification.RequestSpecification;
import teamcity.api.enums.Endpoint;
import teamcity.api.models.BaseModel;
import teamcity.api.requests.checked.CheckedBase;

import java.util.EnumMap;

public class CheckedRequests {
    private final EnumMap<Endpoint, CheckedBase> requests = new EnumMap<>(Endpoint.class);

    public CheckedRequests(RequestSpecification spec){
        for(var endpoint: Endpoint.values()){
            requests.put(endpoint, new CheckedBase(spec, endpoint));
        }
    }

    public <T extends BaseModel> CheckedBase<T> getRequest(Endpoint endpoint){
        return requests.get(endpoint);
    }
}

