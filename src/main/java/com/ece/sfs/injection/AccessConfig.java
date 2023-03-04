package com.ece.sfs.injection;

import com.ece.sfs.access.AccessManager;
import com.ece.sfs.access.AccessRight;
import com.ece.sfs.access.AccessRightList;
import com.ece.sfs.group.UserGroupManager;
import com.ece.sfs.core.Directory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;


@Configuration
@ComponentScan(basePackageClasses = Directory.class)
public class AccessConfig {

    @Bean
    @Scope("singleton")
    public Directory root() {
        Directory root = new Directory(
                "/",
                "/",
                null,
                Calendar.getInstance().getTime(),
                UUID.randomUUID(),
                new HashMap<>()
        );

        root.addComponent(
                new Directory(
                        "home",
                        "home",
                        root,
                        Calendar.getInstance().getTime(),
                        UUID.randomUUID(),
                        new HashMap<>()
                )
        );


        return root;
    }

    @Bean
    @Scope("singleton")
    @Qualifier("adminAuthority")
    public GrantedAuthority adminAuthority() {
        return new SimpleGrantedAuthority("Admin");
    }

    @Bean
    @Scope("singleton")
    @Qualifier("userAuthority")
    public GrantedAuthority userAuthority() {
        return new SimpleGrantedAuthority("User");
    }

    @Bean
    @Scope("singleton")
    public UserGroupManager userGroupManager(GrantedAuthority adminAuthority, GrantedAuthority userAuthority) {
        UserGroupManager manager = new UserGroupManager(new ArrayList<>());

        manager.createGroup("Users", Collections.singletonList(userAuthority));
        manager.createGroup("Admins", Collections.singletonList(adminAuthority));

        return manager;
    }

    @Bean
    @Scope("singleton")
    public AccessManager accessRightListManager(Directory root, UserGroupManager userGroupManager) {
        AccessManager manager = new AccessManager(userGroupManager);

        manager.addAccessRightList(
                root.getUUID(),
                new AccessRightList(
                        AccessRight.defaultAR("root"),
                        AccessRight.defaultAR("Admins")
                )
        );

        manager.addAccessRightList(
                root.getComponent("home").getLeft().getUUID(),
                new AccessRightList(
                        AccessRight.defaultAR("root"),
                        AccessRight.defaultRead("Users")
                )
        );

        manager.addGroupAccessRight(
                "Admin",
                root.getComponent("home").getLeft().getUUID(),
                AccessRight.defaultAR()
        );

        return manager;
    }
}
