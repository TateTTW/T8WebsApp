package com.t8webs.enterprise.utils.EmailUtil;

import com.t8webs.enterprise.dto.User;

public interface IEmailUtil {
    void notifyAccessRequest(User user);
}
