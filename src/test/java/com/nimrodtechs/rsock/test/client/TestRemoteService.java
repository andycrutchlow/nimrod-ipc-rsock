package com.nimrodtechs.rsock.test.client;

import com.nimrodtechs.ipcrsock.annotation.RemoteMethod;

public interface TestRemoteService {

    @RemoteMethod(processName = "server1", methodName = "getMarketData1")
    String getMarketData1(String param);
}
