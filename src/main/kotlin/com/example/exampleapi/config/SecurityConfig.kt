package com.example.exampleapi.config

import com.example.exampleapi.auth.CustomUserDetailsService
import com.example.exampleapi.auth.TokenAuthenticationFilter
import com.example.exampleapi.auth.TokenHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.*
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.*
import org.springframework.security.config.annotation.web.configuration.*
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.cors.*
import java.lang.Exception
import javax.sql.DataSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Autowired val jwtUserDetailsService: CustomUserDetailsService,
    @Autowired val dataSource: DataSource,
    @Autowired val tokenHelper: TokenHelper
) : WebSecurityConfigurerAdapter() {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Autowired
    @Throws(Exception::class)
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService<UserDetailsService?>(jwtUserDetailsService)
            .passwordEncoder(passwordEncoder())
    }

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.jdbcAuthentication()
            .dataSource(dataSource)
            .usersByUsernameQuery(
                "select email, hashedPassword, enabled "
                        + "from accounts "
                        + "where email = ?"
            )
            .authoritiesByUsernameQuery(
                ("select email, role "
                        + "from authorities "
                        + "where email = ?")
            )
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.cors()
            .and()
            .authorizeRequests()
            .antMatchers(HttpMethod.POST, "/api/account").permitAll()
            .antMatchers(HttpMethod.POST, "/api/account/request-password-reset").permitAll()
            .antMatchers(HttpMethod.POST, "/api/account/set-password").permitAll()
            .antMatchers(HttpMethod.POST, "/api/account/reset-password").permitAll()
            .antMatchers(HttpMethod.POST, "/api/account/login").permitAll()
            .antMatchers(HttpMethod.GET, "/api/account").permitAll()
            .antMatchers(HttpMethod.GET, "/api/account/does-username-exist/**").permitAll()
            .antMatchers(HttpMethod.PUT, "/api/account").permitAll()
            .antMatchers(HttpMethod.DELETE, "/api/account/**").permitAll()
            .antMatchers(HttpMethod.POST, "/api/account/transfer").permitAll()
            .antMatchers(HttpMethod.PUT, "/api/account/transfer/**").permitAll()
            .antMatchers("/api/auth/**").permitAll()
            .antMatchers("/api/authority").denyAll()
            .anyRequest().authenticated()
            .and().csrf().disable()
            .addFilterBefore(TokenAuthenticationFilter(tokenHelper, jwtUserDetailsService), BasicAuthenticationFilter::class.java)
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(
            "https://example.com",
            "https://www.example.com",
            "https://example.com",
            "https://www.example.com",
            "https://example.gr",
            "https://www.example.gr",
            "http://localhost/", // Only needed for local testing, e.g. from Postman. Better to exclude from production builds.
            "http://127.0.0.1/", // Only needed for local testing, e.g. from Postman. Better to exclude from production builds.
            "http://localhost:8080", // Only needed for connecting from a local frontend. Better to exclude from production builds.
            "http://localhost:8081", // Only needed for connecting from a local frontend. Better to exclude from production builds.
            "http://127.0.0.1:8080", // Only needed for connecting from a local frontend. Better to exclude from production builds.
            "http://127.0.0.1:8081", // Only needed for connecting from a local frontend. Better to exclude from production builds.
            "http://192.168.1.160:8080", // Needed for connecting over local network, get IP with ipconfig in terminal, update also server.address in application.properties.
            "http://192.168.1.160:8081"  // Needed for connecting over local network, get IP with ipconfig in terminal, update also server.address in application.properties.
        )
        configuration.allowedMethods = listOf("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH", "HEAD")
        configuration.allowedHeaders = listOf("X-Requested-With", "Origin", "Content-Type", "Accept", "Authorization")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
