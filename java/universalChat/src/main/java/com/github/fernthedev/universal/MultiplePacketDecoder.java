package com.github.fernthedev.universal;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiplePacketDecoder extends MessageToMessageDecoder<ByteBuf> {
    private List<SinglePacketDecoder> protobufDecoderList;

    public MultiplePacketDecoder(SinglePacketDecoder protobufDecoder) {
        protobufDecoderList.add(protobufDecoder);

    }

    public MultiplePacketDecoder(SinglePacketDecoder... protobufDecoders) {
        protobufDecoderList.addAll(Arrays.asList(protobufDecoders));
    }

    public MultiplePacketDecoder(List<SinglePacketDecoder> singlePacketDecoders) {
        protobufDecoderList = singlePacketDecoders;
    }

    /**
     * Decode from one message to an other. This method will be called for each written message that can be handled
     * by this decoder.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link MessageToMessageDecoder} belongs to
     * @param msg the message to decode to an other one
     * @param out the {@link List} to which decoded messages should be added
     * @throws Exception is thrown if an error occurs
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {

        boolean continueLoop = true;
        for(SinglePacketDecoder singlePacketDecoder: protobufDecoderList) {
            List<Object> newOut = new ArrayList<>(out);
            singlePacketDecoder.decode(this,ctx,msg,newOut);

            for(Object object : newOut) {
                if(!(object instanceof GeneratedMessageV3)) continue;
                GeneratedMessageV3 message = (GeneratedMessageV3) object;
                if(singlePacketDecoder.thisPrototype.getAllFields().size() != message.getAllFields().size()) {

                    out.addAll(newOut);
                    continueLoop = false;
                    break;
                }else{
                    break;
                }
            }

            if(!continueLoop) {
                break;
            }

        }
    }

}
