package ru.hawoline.messenger.model

class ChatMessage(val id: String, var text: String, val fromId: String, val toId: String, val timeStamp: Long){
    constructor(): this("", "", "", "", -1)
}
