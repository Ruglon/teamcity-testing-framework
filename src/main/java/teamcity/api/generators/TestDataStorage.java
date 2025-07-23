package teamcity.api.generators;

import teamcity.api.enums.Endpoint;
import teamcity.api.models.BaseModel;
import teamcity.api.requests.unchecked.UncheckedBase;
import teamcity.api.spec.Specifications;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TestDataStorage {

    private static TestDataStorage testDataStorage;
    private final EnumMap<Endpoint, Set<String>> createdEntitiesMap;

    private TestDataStorage(){
        createdEntitiesMap = new EnumMap<>(Endpoint.class);
    };

    public static TestDataStorage getStorage(){
        if(testDataStorage == null){
            testDataStorage = new TestDataStorage();
        }
        return testDataStorage;
    }

    public void addCreatedEntity(Endpoint endpoint, String id){
        if(id != null){
            createdEntitiesMap.computeIfAbsent(endpoint, key -> new HashSet<>()).add(id);
        }
    }

    public void addCreatedEntity(Endpoint endpoint, BaseModel baseModel){
        addCreatedEntity(endpoint, getEntityIdOrLocator(baseModel));
    }

    private String getEntityIdOrLocator(BaseModel baseModel){
        try{
            var idField = baseModel.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            var idFieldValue = Objects.toString(idField.get(baseModel), null);
            idField.setAccessible(false);
            return idFieldValue;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            try {
                var locatorField = baseModel.getClass().getDeclaredField("locator");
                locatorField.setAccessible(true);
                var locatorFieldValue = Objects.toString(locatorField.get(baseModel), null);
                locatorField.setAccessible(false);
                return locatorFieldValue;
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                throw new IllegalStateException("Cannot get id or locator of entity", e);
            }
        }
    }



    public void deleteCreatedEntities() {
        createdEntitiesMap.forEach(((endpoint, ids) ->
                        ids.forEach(id ->
                                new UncheckedBase(Specifications.superUserAuth(), endpoint).delete(id)
                        )
                )
        );

        createdEntitiesMap.clear();
    }


}

