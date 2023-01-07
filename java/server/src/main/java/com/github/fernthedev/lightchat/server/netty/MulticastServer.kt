package com.github.fernthedev.lightchat.server.netty

import com.github.fernthedev.lightchat.core.MulticastData
import com.github.fernthedev.lightchat.core.StaticHandler
import com.github.fernthedev.lightchat.server.Server
import com.google.gson.Gson
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress

class MulticastServer//        setRun(true);    //    @Synchronized
//    private void setRun(boolean run) {
//        this.run = run;
//    }
    (name: String?, private val server: Server, private val multicastAddress: String) : QuoteServerThread(name) {
    @Volatile
    private var run = true
    fun stopMulticast() {
        run = false
        //        setRun(false);
    }

    override fun run() {
        while (moreQuotes && run) {
            try {
                var buf: ByteArray
                // don't wait for request...just send a quote
                val dataSend = MulticastData(
                    server.port,
                    StaticHandler.VERSION_DATA.variablesJSON!!
                        .version,
                    StaticHandler.VERSION_DATA.variablesJSON!!.minVersion,
                    server.playerHandler.uuidMap.size
                )
                buf = Gson().toJson(dataSend).toByteArray()
                val group = InetAddress.getByName(multicastAddress)
                var packet: DatagramPacket
                packet = DatagramPacket(buf, buf.size, group, 4446)
                socket!!.send(packet)
                try {
                    sleep((Math.random() * 200).toLong())
                } catch (ignored: InterruptedException) {
                    currentThread().interrupt()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                moreQuotes = false
            }
            try {
                sleep(15)
            } catch (e: InterruptedException) {
                currentThread().interrupt()
            }
        }
        server.logger.info("Closing MultiCast Server")
        socket!!.close()
    }
}