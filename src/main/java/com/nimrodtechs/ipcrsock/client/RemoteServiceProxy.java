package com.nimrodtechs.ipcrsock.client;

import com.nimrodtechs.ipcrsock.annotation.RemoteMethod;
import com.nimrodtechs.ipcrsock.common.NimrodRmiException;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class RemoteServiceProxy {

    private final RemoteServerService remoteServerService;

    public RemoteServiceProxy(RemoteServerService remoteServerService) {
        this.remoteServerService = remoteServerService;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class<?>[]{serviceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // Check if the method has the @RemoteMethod annotation
                        if (method.isAnnotationPresent(RemoteMethod.class)) {
                            RemoteMethod annotation = method.getAnnotation(RemoteMethod.class);

                            String processName = annotation.processName();
                            String methodName = annotation.methodName();

                            try {
                                return remoteServerService.executeRmiMethod(
                                        method.getReturnType(),
                                        processName,
                                        methodName,
                                        args
                                );
                            } catch (NimrodRmiException e) {
                                if (e.getMessage() == null || !e.getMessage().equals(NimrodRmiException.EMPTY_RESPONSE)) {
                                    throw new RuntimeException("Error invoking remote method", e);
                                }
                            }
                        }

                        // For non-annotated methods, use default behavior
                        return method.invoke(proxy, args);
                    }
                }
        );
    }
}
