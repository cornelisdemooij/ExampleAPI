package com.example.exampleapi.mail

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.client.googleapis.auth.oauth2.*
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.Base64
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.Message
import org.springframework.beans.factory.annotation.*
import org.springframework.stereotype.Service
import java.io.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.GeneralSecurityException
import java.util.*
import javax.json.Json
import javax.json.stream.JsonParser
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.*

@Service
class MailService(
    @Value("\${mail.refreshToken}") val refreshToken: String // From the Google OAuth Playground.
) {
    private val applicationName = "ExampleAPI"
    private val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()

    // Load client secrets.
    private val credentialsFilePath = "/client_secret.json" // From the Google API Console.
    private val `in` = MailService::class.java.getResourceAsStream(credentialsFilePath)
        ?: throw FileNotFoundException("Resource not found: $credentialsFilePath")
    private val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(jsonFactory, InputStreamReader(`in`))

    val refreshUrl = "https://www.googleapis.com/oauth2/v4/token" // From https://developers.google.com/identity/protocols/OAuth2WebServer#offline

    @Throws(IOException::class, GeneralSecurityException::class)
    fun sendMail(to: String?, subject: String?, bodyText: String?): Message? {
        // Build a new authorized API client service.
        val netHttpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val accessToken = requestAccessToken()
        val credential = GoogleCredential().setAccessToken(accessToken)
        val service: Gmail = Gmail.Builder(netHttpTransport, jsonFactory, credential)
            .setApplicationName(applicationName)
            .build()

        // Send the email from info@example.com:
        return try {
            val userId = "info@example.com"
            val from = "Example Info <$userId>"
            val emailContent: MimeMessage = createEmail(to, from, subject, bodyText)
            val message: Message = createMessageWithEmail(emailContent)
            service.users().messages().send(userId, message).execute()
        } catch (e: Exception) {
            println(e.message)
            throw MailException("Could not send email.");
        }
    }

    fun requestAccessToken(): String {
        val values: HashMap<String, String> = HashMap()
        values["grant_type"] = "refresh_token"
        values["client_id"] = clientSecrets.details.clientId
        values["client_secret"] = clientSecrets.details.clientSecret
        values["refresh_token"] = refreshToken
        val requestBody = ObjectMapper().writeValueAsString(values)
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(refreshUrl))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build()

        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        val parser = Json.createParser(StringReader(response.body()))
        val event = parser.next()
        return if (event == JsonParser.Event.START_OBJECT) {
            parser.getObject().getString("access_token")
        } else {
            ""
        }
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to email address of the receiver
     * @param from email address of the sender, the mailbox account
     * @param subject subject of the email
     * @param bodyText body text of the email
     * @return the MimeMessage to be used to send email
     * @throws MessagingException
     */
    @Throws(MessagingException::class)
    fun createEmail(to: String?, from: String?, subject: String?, bodyText: String?): MimeMessage {
        val props = Properties()
        val session: Session = Session.getDefaultInstance(props, null)
        val email = MimeMessage(session)
        email.setFrom(InternetAddress(from))
        email.addRecipient(javax.mail.Message.RecipientType.TO, InternetAddress(to))
        email.subject = subject
        email.setText(bodyText, "UTF-8", "html")
        return email
    }

    /**
     * Create a message from an email.
     *
     * @param emailContent Email to be set to raw of message
     * @return a message containing a base64url encoded email
     * @throws IOException
     * @throws MessagingException
     */
    @Throws(MessagingException::class, IOException::class)
    fun createMessageWithEmail(emailContent: MimeMessage): Message {
        val buffer = ByteArrayOutputStream()
        emailContent.writeTo(buffer)
        val bytes = buffer.toByteArray()
        val encodedEmail: String = Base64.encodeBase64URLSafeString(bytes)
        val message = Message()
        message.raw = encodedEmail
        return message
    }
}
