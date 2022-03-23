package functions

import com.google.cloud.functions.CloudEventsFunction
import com.google.gson.Gson
import com.google.gson.JsonObject
import functions.LogCloudEvent
import io.cloudevents.CloudEvent
import java.nio.charset.StandardCharsets
import java.util.logging.Logger

class LogCloudEvent : CloudEventsFunction {
    override fun accept(event: CloudEvent) {
        // Print out details from the CloudEvent
        // The type of event related to the originating occurrence
        logger.info("Event Type: " + event.type)
        // The subject of the event in the context of the event producer
        logger.info("Event Subject: " + event.subject)
        if (event.data != null) {
            // Extract data from CloudEvent wrapper
            val cloudEventData = String(event.data!!.toBytes(), StandardCharsets.UTF_8)
            val gson = Gson()
            // Convert data into a JSON object
            val eventData = gson.fromJson(cloudEventData, JsonObject::class.java)

            // Extract Cloud Audit Log data from protoPayload
            // https://cloud.google.com/logging/docs/audit#audit_log_entry_structure
            val payload = eventData.getAsJsonObject("protoPayload")
            logger.info("API Method: " + payload["methodName"].asString)
            logger.info("Resource name: " + payload["resourceName"].asString)
            val auth = payload.getAsJsonObject("authenticationInfo")
            if (auth != null) {
                // The email address of the authenticated user 
                // (or service account on behalf of third party principal) making the request
                logger.info("Authenticated User: " + auth["principalEmail"].asString)
            }
        }
    }

    companion object {
        private val logger = Logger.getLogger(LogCloudEvent::class.java.name)
    }
}