package com.nimrodtechs.ipcrsock.serialization;

import com.esotericsoftware.kryo.io.Input;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KryoDecoderOld implements Decoder<Object> {

    private static KryoCommon kryoCommon;
    List<MimeType> mimeTypes = new ArrayList<>();

    public KryoDecoderOld(KryoCommon kryoCommon) {
        this.kryoCommon = kryoCommon;
        mimeTypes.add(MimeType.valueOf("application/x-kryo"));
    }

    public static <T> T deserialize(byte[] b, Class<T> c) {
        if (String.class.equals(c)) {
            return (T) new String(b);
        }
        KryoInfo kryoInfo = kryoCommon.getKryoThreadLocal().get();
        kryoInfo.getInput().reset();
        try (Input input = new Input(b)) {
            return (T)kryoInfo.getKryo().readClassAndObject(input);
        }
    }

    @Override
    public boolean canDecode(ResolvableType elementType, MimeType mimeType) {
        return MimeType.valueOf("application/x-kryo").equals(mimeType);
    }
    @Override
    public Object decode(DataBuffer dataBuffer, ResolvableType targetType, MimeType mimeType, Map<String, Object> hints) {
        byte[] bytes = new byte[dataBuffer.readableByteCount()];
        dataBuffer.read(bytes);
        KryoInfo kryoInfo = kryoCommon.getKryoThreadLocal().get();
        //kryoInfo.getInput().setBuffer(bytes);
        try (Input input = new Input(new ByteArrayInputStream(bytes))) {
            return kryoInfo.getKryo().readClassAndObject(input);
        }
    }

    @Override
    public List<MimeType> getDecodableMimeTypes() {
        return mimeTypes;
    }

    @Override
    public List<MimeType> getDecodableMimeTypes(ResolvableType targetType) {
        return Decoder.super.getDecodableMimeTypes(targetType);
    }

    @Override
    public Flux<Object> decode(Publisher<DataBuffer> dataBuffers, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
        return Flux.from(dataBuffers)
                .map(dataBuffer -> {
                    KryoInfo kryoInfo = kryoCommon.getKryoThreadLocal().get();
                    kryoInfo.getOutput().reset();
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    try (Input input = new Input(new ByteArrayInputStream(bytes))) {
                        return kryoInfo.getKryo().readClassAndObject(input);
                    }
                });
    }

    @Override
    public Mono<Object> decodeToMono(Publisher<DataBuffer> inputStream, ResolvableType elementType, MimeType mimeType, Map<String, Object> hints) {
        return null;
    }
}

