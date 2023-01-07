package com.github.fernthedev.lightchat.server.netty

import java.io.BufferedReader
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.util.*

open class QuoteServerThread @JvmOverloads constructor(name: String? = "QuoteServerThread") : Thread(name) {
    protected var socket: DatagramSocket? = null
    protected var `in`: BufferedReader? = null
    protected var moreQuotes = true

    init {
        socket = DatagramSocket(4445)

        /*try {
            in = new BufferedReader(new FileReader("one-liners.txt"));
        } catch (FileNotFoundException e) {
            System.err.println("Could not open quote file. Serving time instead.");
        }*/
    }

    override fun run() {
        while (moreQuotes) {
            try {
                var buf = ByteArray(256)

                // receive request
                var packet = DatagramPacket(buf, buf.size)
                socket!!.receive(packet)

                // figure out response
                var dString: String? = null
                dString = if (`in` == null) Date().toString() else nextQuote
                buf = dString.toByteArray()

                // send the response to the client at "address" and "port"
                val address = packet.address
                val port = packet.port
                packet = DatagramPacket(buf, buf.size, address, port)
                socket!!.send(packet)
            } catch (e: IOException) {
                e.printStackTrace()
                moreQuotes = false
            }
        }
        socket!!.close()
    }

    protected val nextQuote: String
        protected get() {
            var returnValue: String
            try {
                if (`in`!!.readLine().also { returnValue = it } == null) {
                    `in`!!.close()
                    moreQuotes = false
                    returnValue = "No more quotes. Goodbye."
                }
            } catch (e: IOException) {
                returnValue = "IOException occurred in server."
            }
            return returnValue
        }
}