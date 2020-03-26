package edu.asu.sbs.config;

import edu.asu.sbs.security.jwt.JWTConfigurer;
import edu.asu.sbs.security.jwt.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomSuccessHandler successHandler;

    private final TokenProvider tokenProvider;
    private final CorsFilter corsFilter;
    private final SecurityProblemSupport securityProblemSupport;

    public SecurityConfiguration(TokenProvider tokenProvider, CorsFilter corsFilter, SecurityProblemSupport securityProblemSupport) {
        this.tokenProvider = tokenProvider;
        this.corsFilter = corsFilter;
        this.securityProblemSupport = securityProblemSupport;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Override
//    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
////        auth.userDetailsService(userDetailsService);
//        auth.inMemoryAuthentication()
//        .withUser("user1").password(passwordEncoder().encode("user1Pass")).roles("USER")
//        .and()
//        .withUser("user2").password(passwordEncoder().encode("user2Pass")).roles("USER")
//        .and()
//        .withUser("admin").password(passwordEncoder().encode("adminPass")).roles(UserType.ADMIN_ROLE);
//    }
//
//    @Bean
//    public AuthenticationSuccessHandler myAuthenticationSuccessHandler(){
//        return new CustomSuccessHandler();
//    }

    @Override
    public void configure(WebSecurity webSecurity) {
        webSecurity.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/app/**/*.{js,html}")
                .antMatchers("/content/**")
                .antMatchers("/swagger-ui.html")
                .antMatchers("/test/**");
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf()
                .disable()
                .antMatcher("/**")
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                .and()
                    .headers()
                    .contentSecurityPolicy("default-src 'self'; frame-src 'self' data:; script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:")
                .and()
                    .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .and()
                    .featurePolicy("geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'")
                .and()
                    .frameOptions()
                    .deny()
//                .and()
//                    .sessionManagement()
//                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .authorizeRequests()
                    .antMatchers("/api/v1/user/reset-password/init", "/api/v1/user/reset-password/finish", "/api/v1/user/signup", "/api/v1/user/test", "/api/v1/user/login", "/api/v1/user/authenticate", "/api/v1/user/activate", "/api/v1/user/register", "/api/account/reset-password/init", "/api/account/password-reset/finish", "/swagger-ui.html", "/css/main.css","/webjars/bootstrap/4.4.1-1/css/bootstrap.min.css","/webjars/bootstrap/4.4.1-1/js/bootstrap.min.js","/webjars/jquery/3.4.1/jquery.min.js", "/js/passwordValidation.js").permitAll()
                .antMatchers().permitAll()
                .anyRequest().authenticated()
                .and()
//                .httpBasic()
                .formLogin()
                .loginPage("/api/v1/user/login").successHandler(successHandler)
//                .loginProcessingUrl("/process_login")
//                .defaultSuccessUrl("/api/v1/user/authenticate")
//                .and()
//                .authorizeRequests()
//                    .anyRequest().authenticated();
                .and()
                    .apply(securityConfigurerAdapter())
                .and()
                .logout()
                .logoutUrl("/logout")
                .invalidateHttpSession(false)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/api/v1/user/login");

    }

    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer(tokenProvider);
    }

}
