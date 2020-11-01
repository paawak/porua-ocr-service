package com.swayam.ocr.porua.tesseract.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private GoogleAuthenticationFilter googleAuthenticationFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
	http.cors().and().addFilterAfter(googleAuthenticationFilter, BasicAuthenticationFilter.class)
		.authorizeRequests(a -> a.antMatchers("/", "/error", "/webjars/**").permitAll().anyRequest().authenticated())
		.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
	corsConfiguration.addAllowedOrigin("http://localhost:3000");
	corsConfiguration.addAllowedMethod("*");
	source.registerCorsConfiguration("/**/**", corsConfiguration);
	return source;
    }

    // @Bean
    // public JwtDecoder jwtDecoder() {
    // return JwtDecoders.fromOidcIssuerLocation("https://accounts.google.com");
    // }

}
