package functions

import com.google.common.testing.TestLogHandler
import com.google.common.truth.Truth
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.cloudevents.core.builder.CloudEventBuilder
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.net.URI
import java.util.logging.Logger

@RunWith(JUnit4::class)
class LogCloudEventTest {
    companion object {
        private val logger = Logger.getLogger(LogCloudEvent::class.java.name)
        private val logHandler = TestLogHandler()

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            logger.addHandler(logHandler)
        }
    }

    @Test
    @Throws(Exception::class)
    fun functionsLogCloudEvent_shouldLogCloudEvent() {
        // Build a CloudEvent Log Entry
        val protoPayload = JsonObject()
        val authInfo = JsonObject()
        val email = "test@gmail.com"
        authInfo.addProperty("principalEmail", email)
        protoPayload.add("authenticationInfo", authInfo)
        protoPayload.addProperty("resourceName", "test resource")
        protoPayload.addProperty("methodName", "test method")
        val encodedData = JsonObject()
        encodedData.add("protoPayload", protoPayload)
        encodedData.addProperty("name", "test name")
        val event = CloudEventBuilder.v1()
            .withId("0")
            .withSubject("test subject")
            .withType("google.cloud.audit.log.v1.written")
            .withSource(URI.create("https://example.com"))
            .withData(Gson().toJson(encodedData).toByteArray())
            .build()
        LogCloudEvent().accept(event)
        Truth.assertThat("Event Subject: " + event.subject).isEqualTo(
            logHandler.storedLogRecords[1].message
        )
        Truth.assertThat("Authenticated User: $email").isEqualTo(
            logHandler.storedLogRecords[4].message
        )
    }
}