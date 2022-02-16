package com.example.exampleapi.authority

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestBody
import java.io.StringReader
import java.util.*
import javax.json.Json
import javax.json.JsonObject
import javax.json.stream.JsonParser
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("authority")
@Component
class AuthorityEndpoint(
    @Autowired val authorityService: AuthorityService
) {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    fun postAuthority(@RequestBody requestBody: String): Response {
        val parser = Json.createParser(StringReader(requestBody))
        val event = parser.next()
        return if (event == JsonParser.Event.START_OBJECT) {
            val json: JsonObject = parser.getObject()
            val authority = Authority(json)
            val result: Boolean = authorityService.save(authority)
            if (result) {
                Response.accepted(result).build()
            } else {
                Response.status(424).header("reason", "Failed dependency: authority could not be saved.").build()
            }
        } else if (event == JsonParser.Event.START_ARRAY) {
            val jsonArray = parser.array
            var result = true
            for (i in jsonArray.indices) {
                val json: JsonObject = jsonArray.getJsonObject(i)
                val authority = Authority(json)
                val authorityResult: Boolean = authorityService.save(authority)
                // Note: do not combine the following line with previous line, because we want later authorities
                // to still be saved even when an earlier authority was not saved successfully.
                result = result && authorityResult
            }
            if (result) {
                Response.accepted(result).build()
            } else {
                Response.status(206).header("reason", "Partial success: not all authorities were saved.").build()
            }
        } else {
            Response
                .status(422)
                .header("reason", "Unprocessable Entity: authority data should be a JSON object or JSON array.")
                .build()
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAuthorities(@QueryParam("email") email: String?, @QueryParam("role") role: String?): Response {
        return if (email != null && role != null) {
            val optionalAuthority: Optional<Authority> = authorityService.findByEmailAndRole(email, role)
            Response.ok(optionalAuthority).build()
        } else if (email != null) {
            val authorities: Iterable<Authority> = authorityService.findByEmail(email)
            Response.ok(authorities).build()
        } else if (role != null) {
            val authorities: Iterable<Authority> = authorityService.findByRole(role)
            Response.ok(authorities).build()
        } else {
            val authorities: Iterable<Authority> = authorityService.findAll()
            Response.ok(authorities).build()
        }
    }

    @GET
    @Path("{email}/{role}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getByEmailAndRole(@PathParam("email") email: String, @PathParam("role") role: String): Response {
        val optionalAuthority: Optional<Authority> = authorityService.findByEmailAndRole(email, role)
        return Response.ok(optionalAuthority).build()
    }

    @PUT
    @Path("{oldEmail}/{oldRole}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    fun update(
        @PathParam("oldEmail") oldEmail: String,
        @PathParam("oldRole") oldRole: String,
        @RequestBody requestBody: String
    ): Response {
        return if (requestBody.isEmpty()) {
            Response.status(422).header("reason", "Unprocessable Entity: missing request body.").build()
        } else {
            val parser = Json.createParser(StringReader(requestBody))
            val event = parser.next()
            if (event == JsonParser.Event.START_OBJECT) {
                val json = parser.getObject()
                val authority = Authority(json)
                val result: Boolean = authorityService.update(authority, oldEmail, oldRole)
                if (result) {
                    Response.ok(result).build()
                } else {
                    Response.noContent().build()
                }
            } else {
                Response.status(422, "Unprocessable Entity: account data should be a JSON object or JSON array.")
                    .build()
            }
        }
    }

    @DELETE
    @Path("{email}/{role}")
    @Produces(MediaType.TEXT_PLAIN)
    fun deleteByEmailAndRole(@PathParam("email") email: String, @PathParam("email") role: String): Response {
        val optionalAuthority: Optional<Authority> = authorityService.findByEmailAndRole(email, role)
        return if (optionalAuthority.isPresent) {
            val authority: Authority = optionalAuthority.get()
            val result: Boolean = authorityService.delete(authority)
            Response.ok(result).build()
        } else {
            Response.noContent().build()
        }
    }
}