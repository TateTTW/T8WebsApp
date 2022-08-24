package com.t8webs.enterprise;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.t8webs.enterprise.dao.User.IUserDAO;
import com.t8webs.enterprise.dto.User;
import com.t8webs.enterprise.service.IServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class T8WebsAdminController {

    @Autowired
    IServerService serverService;
    @Autowired
    IUserDAO userDAO;

    @PatchMapping(value="/grantAccess", produces="application/json")
    public ResponseEntity grantAccess(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="userId") String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!userDAO.isAdmin(user.getAttribute("sub"))) {
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }

        if (userDAO.isApproved(userId)) {
            return new ResponseEntity(headers, HttpStatus.BAD_REQUEST);
        }

        if (userDAO.grantAccess(userId)) {
            return new ResponseEntity(headers, HttpStatus.OK);
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PatchMapping(value="/revokeAccess", produces="application/json")
    public ResponseEntity revokeAccess(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="userId") String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!userDAO.isAdmin(user.getAttribute("sub"))) {
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }

        if (!userDAO.isApproved(userId)) {
            return new ResponseEntity(headers, HttpStatus.BAD_REQUEST);
        }

        if (userDAO.revokeAccess(userId)) {
            return new ResponseEntity(headers, HttpStatus.OK);
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(value="/allUsers", produces="application/json")
    public ResponseEntity getAllUsers(@AuthenticationPrincipal OAuth2User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!userDAO.isAdmin(user.getAttribute("sub"))) {
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }

        List<User> users = userDAO.getAllUsers();
        return new ResponseEntity(users, headers, HttpStatus.OK);
    }

    @GetMapping(value="/allServers", produces="application/json")
    public ResponseEntity getAllServers(@AuthenticationPrincipal OAuth2User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!userDAO.isAdmin(user.getAttribute("sub"))) {
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }

        ArrayNode servers = serverService.getAllServers();
        return new ResponseEntity(servers, headers, HttpStatus.OK);
    }

    @DeleteMapping(value="/forceDeleteServer", produces="application/json")
    public ResponseEntity forceDeleteServer(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="vmid") int vmid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!userDAO.isAdmin(user.getAttribute("sub"))) {
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }

        if (serverService.forceDeleteVM(vmid)) {
            return new ResponseEntity(headers, HttpStatus.OK);
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
