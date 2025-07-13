package me.meiallu.sayori;

@FunctionalInterface
public interface Action {

    void execute(Request request, Response response);
}