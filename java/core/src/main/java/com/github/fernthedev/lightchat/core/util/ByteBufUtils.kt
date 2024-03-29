package com.github.fernthedev.lightchat.core.util

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled


fun ByteBuf.asBytesArrayFast(): ByteArray {
    return asBytesArrayFastOffset().first
}

fun ByteBuf.asBytesArrayFastOffset(): Pair<ByteArray, Int> {
    val array: ByteArray
    val offset: Int
    val length: Int = this.readableBytes()
    if (this.hasArray()) {
        array = this.array()
        offset = this.arrayOffset() + this.readerIndex()
    } else {
        array = ByteBufUtil.getBytes(this, this.readerIndex(), length, false)
        offset = 0
    }

    return array to offset
}

fun <T> ByteArray.toTempByteBuf(invoke: (byteBuf: ByteBuf) -> T): T {
    val buf = Unpooled.wrappedBuffer(this)
    val r = invoke(buf)
    buf.release()

    return r
}