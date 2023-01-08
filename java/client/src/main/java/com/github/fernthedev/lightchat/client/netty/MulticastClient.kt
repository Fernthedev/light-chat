package com.github.fernthedev.lightchat.client.netty

import com.github.fernthedev.lightchat.core.MulticastData
import com.github.fernthedev.lightchat.core.StaticHandler.isDebug
import com.github.fernthedev.lightchat.core.StaticHandler.multicastAddress
import com.github.fernthedev.lightchat.core.exceptions.DebugChainedException
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import java.io.IOException
import java.io.StringReader
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.SocketTimeoutException

class MulticastClient {
    private val serversAddress: MutableList<MulticastData> = ArrayList()
    private val addressServerAddressMap: MutableMap<String, MulticastData> = HashMap()
    fun checkServers(amount: Int) {
        try {
            MulticastSocket(4446).use { socket ->
                val group = InetAddress.getByName(multicastAddress)
                socket.soTimeout = 2000
                socket.joinGroup(group)
                var packet: DatagramPacket
                for (i in 0 until amount) {
                    val buf = ByteArray(256)
                    packet = DatagramPacket(buf, buf.size)
                    socket.receive(packet)
                    var received = String(packet.data)
                    if (received == "") {
                        continue
                    }
                    received = received.replace(" ".toRegex(), "")
                    parseData(packet, received)
                }
                socket.leaveGroup(group)
            }
        } catch (ignored: SocketTimeoutException) {
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: DebugChainedException) {
            e.printStackTrace()
        }
    }

    private fun parseData(packet: DatagramPacket, received: String) {
        try {
            JsonReader(StringReader(received)).use { reader ->
                reader.isLenient = true
                val data = Gson().fromJson<MulticastData>(reader, MulticastData::class.java)
                val address = packet.address.hostAddress
                data.address = address
                if (!addressServerAddressMap.containsKey(address)) {
                    serversAddress.add(data)
                    addressServerAddressMap[address] = data
                }
            }
        } catch (e: Exception) {
            if (isDebug()) {
                throw DebugChainedException(e, "Unable to read packet")
            }
        }
    }

    fun getServersAddress(): List<MulticastData> {
        return serversAddress
    }
}