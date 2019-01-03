package com.xiaobai.codec.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

public class TestSubscribeReqProto {

    private static byte[] encode(SubscribeReqProto.SubscribeReq req) {
        return req.toByteArray();
    }

    private static SubscribeReqProto.SubscribeReq decode(byte[] body) throws InvalidProtocolBufferException {
        return SubscribeReqProto.SubscribeReq.parseFrom(body);
    }

    private static SubscribeReqProto.SubscribeReq createSubScribeReq() {
        SubscribeReqProto.SubscribeReq.Builder builder = SubscribeReqProto.SubscribeReq.newBuilder();
        builder.setSubReqID(1);
        builder.setUserName("xiaobai");
        builder.setProductName("Java Supior");
        List<String> address = new ArrayList<>();
        address.add("Shenyang YUEXING");
        address.add("Jinzhou HUAGUANG");
        address.add("Beijing HAIDIAN");
        builder.addAllAddress(address);
        return builder.build();
    }

    public static void main(String[] args) throws InvalidProtocolBufferException {
        SubscribeReqProto.SubscribeReq req = createSubScribeReq();
        System.out.println("Before encoding: ");
        System.out.println(req.toString());
        SubscribeReqProto.SubscribeReq req2 = decode(encode(req));
        System.out.println("After encoding and decoding: ");
        System.out.println(req2.toString());
        System.out.println("Assert enqual: --> " + req2.equals(req));
    }
}
