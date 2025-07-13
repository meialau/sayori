package me.meiallu.sayori;

@FunctionalInterface
public interface MiddlewareFunction {

    boolean execute(Request request, Response response);
}