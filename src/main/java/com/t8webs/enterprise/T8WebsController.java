package com.t8webs.enterprise;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.service.IServerService;
import com.t8webs.enterprise.service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;

@RestController
public class T8WebsController {

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
            serverService.assignUserServer(user.getAttribute("email"), serverName);
        } catch (Exception e) {
            return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(headers, HttpStatus.OK);
    }

    @PostMapping("/deployBuild")
    public ResponseEntity uploadFile(@AuthenticationPrincipal OAuth2User user, @RequestParam("buildFile") MultipartFile buildFile, @RequestParam(value="vmid") int vmid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            serverService.deployBuild(user.getAttribute("email"), vmid, buildFile);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return new ResponseEntity(headers, HttpStatus.OK);
    }
}
