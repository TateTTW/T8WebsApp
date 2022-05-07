package com.t8webs.enterprise;

import com.t8webs.enterprise.dao.IAvailableServerDAO;
import com.t8webs.enterprise.service.IServerService;
import com.t8webs.enterprise.utils.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.sql.SQLException;

@SpringBootTest
@ActiveProfiles("test")
class T8WebsApplicationTests {

    @Autowired
    IServerService serverService;
    @Autowired
    IAvailableServerDAO serverDAO;
    @Autowired
    IProxmoxUtil proxmoxUtil;
    @Autowired
    IDomainUtil domainUtil;

    @Test
    void contextLoads() {
    }

    @Test
    void testVMdata() throws ClassNotFoundException, SQLException, IOException {
        proxmoxUtil.getVmData(120, ProxmoxUtil.TimeFrame.HOUR);
    }
}
