package com.t8webs.enterprise;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.service.IServerService;
import com.t8webs.enterprise.service.IUserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;

@RestController
public class T8WebsController {
    @Autowired
    IUserAccountService userAccountService;
    @Autowired
    IServerService serverService;

    private ObjectMapper mapper = new ObjectMapper();

    @GetMapping(value="/user", produces="application/json")
    public ResponseEntity getUser(@AuthenticationPrincipal OAuth2User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("name", (String) user.getAttribute("given_name"));
        jsonNode.put("picture", (String) user.getAttribute("picture"));

        return new ResponseEntity(jsonNode, headers, HttpStatus.OK);
    }

    @GetMapping(value="/addServer", produces="application/json")
    public ResponseEntity addServer(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="serverName") String serverName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {

            //UserAccount userAccount = userAccountService.fetchUserAccount(user.getAttribute("email"));

//            if (!serverService.assignUserServer(userAccount, servername)) {
//                return new ResponseEntity(headers, HttpStatus.BAD_REQUEST);
//            }

        } catch (Exception e) {
            return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(headers, HttpStatus.OK);
    }

    private boolean isTokenInvalid(String username, String token) throws SQLException, IOException, ClassNotFoundException {
        return !userAccountService.isTokenValid(userAccountService.fetchUserAccount(username), token);
    }
}
