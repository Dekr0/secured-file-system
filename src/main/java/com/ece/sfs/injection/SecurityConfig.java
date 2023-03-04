package com.ece.sfs.injection;

import com.ece.sfs.group.UserGroupManager;
import com.ece.sfs.io.Cryptography;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.*;
import java.util.List;


@Configuration
public class SecurityConfig {

    @Bean
    @Scope("singleton")
    public DaoAuthenticationProvider authProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }

    @Bean
    @Scope("singleton")
    public AuthenticationManager authManager(DaoAuthenticationProvider authProvider) {
        return new ProviderManager(
                List.of(authProvider)
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    @Scope("singleton")
    public InMemoryUserDetailsManager userManager(GrantedAuthority adminAuthority, UserGroupManager userGroupManager) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        manager.createUser(
                new User("root",
                        passwordEncoder().encode("Root1234!@"),
                        List.of(adminAuthority)
                ));

        userGroupManager.addUserToGroup("root", "Admins");


        return manager;
    }

    @Bean
    public KeyGenerator keyGenerator() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("ChaCha20");

        keyGenerator.init(256, SecureRandom.getInstanceStrong());

        return keyGenerator;
    }

    @Bean
    public SecretKey secretKey(KeyGenerator keyGenerator) {
        return keyGenerator.generateKey();
    }

    @Bean
    public byte[] nonce() {
        byte[] nonce = new byte[12];

        new SecureRandom().nextBytes(nonce);

        return nonce;
    }

    @Bean
    public Cryptography cryptography(byte[] nonce, SecretKey secretKey) {
        return new Cryptography(secretKey, nonce);
    }
}
