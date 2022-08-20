package com.t8webs.enterprise;

import com.t8webs.enterprise.dao.DbQuery;
import com.t8webs.enterprise.dao.AvailableServer.IAvailableServerDAO;
import com.t8webs.enterprise.dto.Server;
import com.t8webs.enterprise.service.IServerService;
import com.t8webs.enterprise.utils.DnsUtil.IDnsUtil;
import com.t8webs.enterprise.utils.ProxmoxUtil.IProxmoxUtil;
import com.t8webs.enterprise.utils.UserServerUtil.IUserServerUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;

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
    IDnsUtil domainUtil;
    @Autowired
    IUserServerUtil clientServerUtil;

    @Test
    void contextLoads() {
    }

    @Test
    void duplicateServerNames_ReturnsBeginStatus() {
        Server.CreationStatus serverStatus1 = serverService.addServer("user1", "testServer");
        Server.CreationStatus serverStatus2 = serverService.addServer("user2", "testServer");
        Assert.assertTrue(serverStatus1 == Server.CreationStatus.COMPLETED && serverStatus2 == Server.CreationStatus.BEGIN);
    }

    @Test
    void noAvailableServers_ReturnsVerifiedNameStatus() {
        ArrayList<Server> assignedServers = new ArrayList<>();
        try {
            for (int i=1; i<11; i++) {
                Server server = clientServerUtil.assignUserServer("100201287479296569425", "server"+i);
                if (server.isFound()) {
                    assignedServers.add(server);
                }
            }
        } catch (DbQuery.IntegrityConstraintViolationException e) {
            e.printStackTrace();
        }

        Server.CreationStatus serverStatus = serverService.addServer("100201287479296569425", "uat08");

        for (Server assignedServer: assignedServers) {
            clientServerUtil.unassignUserServer(assignedServer);
        }

        Assert.assertTrue(serverStatus == Server.CreationStatus.VERIFIED_NAME);
    }
}
