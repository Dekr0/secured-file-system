package com.ece.sfs.injection;


import com.ece.sfs.prompt.ShellPrompt;
import org.jline.terminal.Terminal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringShellConfig {

    @Bean
    public ShellPrompt shellPrompt(Terminal terminal) {
        return new ShellPrompt(terminal);
    }
}
