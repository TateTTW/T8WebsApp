package com.t8webs.enterprise;

import com.t8webs.enterprise.dao.IServerDAO;
import com.t8webs.enterprise.service.IServerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.security.SecureRandom;
import java.util.Base64;

@SpringBootTest
@ActiveProfiles("test")
class T8WebsApplicationTests {

    @Autowired
    IServerService serverService;
    @Autowired
    IServerDAO serverDAO;

    final String TEST_USERNAME = generateUsername();
    final String TEST_USER_PASSWORD = "testPassword";

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    @Test
    void contextLoads() {
    }

    private static String generateUsername() {
        byte[] randomBytes = new byte[10];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

//    @Test
//    void userRequestsServer_ServerIsAssigned() throws SQLException, IOException, ClassNotFoundException, UnirestException {
//        UserAccount userAccount = new UserAccount();
//        userAccount.setUsername(TEST_USERNAME);
//        userAccount.setPassword(TEST_USER_PASSWORD);
//        userAccount.setEmail("testUser@testSite.com");
//
//        userAccountService.createUserAccount(userAccount);
//
//        boolean assigned = serverService.assignUserServer(userAccount, "alex");
//
//        Assert.isTrue(assigned, "Server service failed to assign server.");
//
//        Server server = serverDAO.fetch("alex");
//
//        Assert.isTrue(server.isFound(), "Server assignment was not found.");
//        Assert.isTrue(server.getUsername().equals(TEST_USERNAME), "Username was not added to server assignment.");
//    }

}
