package com.github.fernthedev.terminal.core.packets

import com.github.fernthedev.lightchat.core.data.LightCandidate
import com.github.fernthedev.lightchat.core.packets.PacketJSON
import com.github.fernthedev.lightchat.core.packets.PacketInfo

@PacketInfo(name = "AUTO_COMPLETE_PACKET")
class AutoCompletePacket(@JvmField var words: List<String>) : PacketJSON() {
    var candidateList: List<LightCandidate> = ArrayList()

    override fun toString(): String {
        return "AutoCompletePacket(candidateList=" + candidateList + ", words=" + words + ")"
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is AutoCompletePacket) return false
        val other = o
        if (!other.canEqual(this as Any)) return false
        if (!super.equals(o)) return false
        val `this$candidateList`: Any = candidateList
        val `other$candidateList`: Any = other.candidateList
        if (if (`this$candidateList` == null) `other$candidateList` != null else `this$candidateList` != `other$candidateList`) return false
        val `this$words`: Any = words
        val `other$words`: Any = other.words
        return if (if (`this$words` == null) `other$words` != null else `this$words` != `other$words`) false else true
    }

    protected fun canEqual(other: Any?): Boolean {
        return other is AutoCompletePacket
    }

    override fun hashCode(): Int {
        val PRIME = 59
        var result = super.hashCode()
        val `$candidateList`: Any = candidateList
        result = result * PRIME + (`$candidateList`?.hashCode() ?: 43)
        val `$words`: Any = words
        result = result * PRIME + (`$words`?.hashCode() ?: 43)
        return result
    }
}