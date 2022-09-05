package com.t8webs.enterprise.utils.EmailUtil;

import com.t8webs.enterprise.dto.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class EmailUtilStub implements IEmailUtil {
    @Override
    public void notifyAccessRequest(User user) {

    }
}
