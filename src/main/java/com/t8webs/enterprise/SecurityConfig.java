package com.t8webs.enterprise;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf()
                .disable()
                .antMatcher("/**")
                .authorizeRequests()
                .antMatchers( "/", "/*.js", "/*.css", "/dashboard", "/assets/T8WEBSICON.png")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2Login().permitAll().defaultSuccessUrl("/dashboard")
                .and()
                .logout().logoutSuccessUrl("/")
                .and()
                .exceptionHandling().authenticationEntryPoint(new Http403ForbiddenEntryPoint());
////        httpSecurity.csrf().disable().antMatcher("/**").authorizeRequests()
////                .antMatchers("/").permitAll()
////                .anyRequest().authenticated()
////                .and()
////                .oauth2Login().permitAll()
////                .and()
////                .logout().logoutSuccessUrl("/").clearAuthentication(true).invalidateHttpSession(true)
////                .deleteCookies("JSESSIONID");
    }
}