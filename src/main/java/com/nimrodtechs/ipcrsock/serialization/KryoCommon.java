package com.nimrodtechs.ipcrsock.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.unsafe.UnsafeInput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import org.hibernate.collection.internal.*;
public class KryoCommon {

    public ThreadLocal<KryoInfo> getKryoThreadLocal() {
        return kryoThreadLocal;
    }

    private final ThreadLocal<KryoInfo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setReferences(true);
        // OR Register your classes here, for better performance and versioning
        // kryo.register(YourClass.class);
        kryo.register(BigDecimal.class);
        kryo.register(Date.class);
        kryo.register(Class.class, new ClassSerializer());
        kryo.register(HashMap.class);
        kryo.register(HashSet.class);
        kryo.register(Boolean[].class);
        kryo.register(Double[].class);
        kryo.register(Float[].class);
        kryo.register(Integer[].class);
        kryo.register(Long[].class);
        kryo.register(Short[].class);
        kryo.register(String[].class);
        kryo.register(Date[].class);
        kryo.register(BigDecimal[].class);
        kryo.register(BigInteger[].class);
        kryo.register(Class[].class);
        kryo.register(Object[].class);
        kryo.register(ArrayList.class);
        kryo.register(TreeMap.class);
        kryo.register(boolean[].class);
        kryo.register(double[].class);
        kryo.register(float[].class);
        kryo.register(int[].class);
        kryo.register(long[].class);
        kryo.register(short[].class);
        kryo.register(byte[].class);
        kryo.register(TreeSet.class);
        try {
            kryo.addDefaultSerializer(PersistentIdentifierBag.class, new FieldSerializer(kryo, PersistentIdentifierBag.class));
            kryo.addDefaultSerializer(PersistentBag.class, new FieldSerializer(kryo, PersistentBag.class));
            kryo.addDefaultSerializer(PersistentList.class, new FieldSerializer(kryo, PersistentList.class));
            kryo.addDefaultSerializer(PersistentSet.class, new FieldSerializer(kryo, PersistentSet.class));
            kryo.addDefaultSerializer(PersistentMap.class, new FieldSerializer(kryo, PersistentMap.class));
            kryo.addDefaultSerializer(PersistentSortedMap.class, new FieldSerializer(kryo, PersistentSortedMap.class));
            kryo.addDefaultSerializer(PersistentSortedSet.class, new FieldSerializer(kryo, PersistentSortedSet.class));
        } catch (NoClassDefFoundError e) {
            //Don't care ...just means hibernate entities containing collection classes can't be deserialized

        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        return new KryoInfo(kryo,outputStream,
                new UnsafeOutput(outputStream),
                new UnsafeInput());
    });


}
