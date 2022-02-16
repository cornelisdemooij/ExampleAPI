package com.example.exampleapi.config

import com.example.exampleapi.account.AccountEndpoint
import com.example.exampleapi.auth.AuthenticationEndpoint
import com.example.exampleapi.authority.AuthorityEndpoint
import org.glassfish.jersey.server.ResourceConfig
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import javax.ws.rs.ApplicationPath
import org.springframework.web.multipart.commons.CommonsMultipartResolver
import org.glassfish.jersey.media.multipart.MultiPartFeature

@Component
@ApplicationPath("/api")
final class JerseyConfig: ResourceConfig() {
    init {
        register(AuthenticationEndpoint::class.java)

        register(AccountEndpoint::class.java)
        register(AuthorityEndpoint::class.java)

        register(MultiPartFeature::class.java)
    }

    @Bean(name = ["multipartResolver"])
    fun multipartResolver(): CommonsMultipartResolver {
        return CommonsMultipartResolver()
    }
}