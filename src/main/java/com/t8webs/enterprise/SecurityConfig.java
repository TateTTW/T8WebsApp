package com.t8webs.enterprise;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;

import javax.servlet.http.Cookie;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable().antMatcher("/**").authorizeRequests()
//                .antMatchers("/").permitAll()
                .anyRequest().authenticated()
                .and()
                .oauth2Login().permitAll()
                .and()
                .logout().logoutSuccessUrl("/").clearAuthentication(true).invalidateHttpSession(true)
                .deleteCookies("JSESSIONID");
    }
}