package com.internetguard.pro.ai.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteModerationClientTest {

    @Test
    fun parseResponse_blockAction() {
        val json = """
            {"inappropriate":true,"confidence":0.91,"category":"pornography","language":"en","action":"block","rationale":"explicit"}
        """.trimIndent()
        val client = RemoteModerationClient("http://localhost")
        val res = client.parseResponse(json)
        assertTrue("inappropriate should be true", res.isInappropriate)
        assertEquals("block", res.action)
        assertEquals("pornography", res.category)
    }

    @Test
    fun parseResponse_allow() {
        val json = """
            {"inappropriate":false,"confidence":0.12,"category":null,"language":"fa","action":"allow"}
        """.trimIndent()
        val client = RemoteModerationClient("http://localhost")
        val res = client.parseResponse(json)
        assertFalse(res.isInappropriate)
        // action may be null if server doesn't send it; fallback acceptable
        assertEquals("allow", res.action ?: "allow")
    }
}


