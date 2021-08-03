package com.help.howtosecurity.service

import org.springframework.stereotype.Service

@Service
class DummyService {

    fun dummyCall(from: String): String {
        return "Dummy Call from $from"
    }


}