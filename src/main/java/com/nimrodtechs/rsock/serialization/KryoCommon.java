package com.nimrodtechs.rsock.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.unsafe.UnsafeInput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;

import java.io.ByteArrayOutputStream;

public class KryoCommon {

    public ThreadLocal<KryoInfo> getKryoThreadLocal() {
        return kryoThreadLocal;
    }

    private final ThreadLocal<KryoInfo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        // OR Register your classes here, for better performance and versioning
        // kryo.register(YourClass.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        return new KryoInfo(kryo,outputStream,
                new UnsafeOutput(outputStream),
                new UnsafeInput());
    });


}
