package com.nimrodtechs.ipcrsock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RemoteMethod {
    String processName(); // Process name on the server
    String methodName();  // Method to invoke on the server
}
