package teamcity.api.requests;

import teamcity.api.enums.Endpoint;
import teamcity.api.requests.unchecked.UncheckedBase;
import io.restassured.specification.RequestSpecification;

import java.util.EnumMap;

public class UncheckedRequests {

    private final EnumMap<Endpoint, UncheckedBase> requests = new EnumMap<>(Endpoint.class);

    public UncheckedRequests(RequestSpecification spec){
        for(var endpoint: Endpoint.values()){
            requests.put(endpoint, new UncheckedBase(spec, endpoint));
        }
    }

    public UncheckedBase getRequest(Endpoint endpoint){
        return requests.get(endpoint);
    }
}
