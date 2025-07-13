package me.meiallu.sayori;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@SuppressWarnings("unused")
public class Mapping {

    @Getter
    @Setter
    private String path;
    @Setter
    private Action responseFunction;
    @Getter
    private Set<Request.Type> allowedTypes;

    public void execute(Request request, Response response) {
        responseFunction.execute(request, response);
    }

    public boolean allowType(Request.Type type) {
        return allowedTypes.add(type);
    }

    public boolean disallowType(Request.Type type) {
        return allowedTypes.remove(type);
    }

    public Mapping(String path, Request.Type type, Action responseFunction) {
        this.path = path;
        this.responseFunction = responseFunction;
        this.allowedTypes = Set.of(type);
    }
}
