package com.t8webs.enterprise;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.dao.DbQuery;
import com.t8webs.enterprise.dao.User.IUserDAO;
import com.t8webs.enterprise.dto.Server;
import com.t8webs.enterprise.dto.User;
import com.t8webs.enterprise.service.IServerService;
import com.t8webs.enterprise.utils.ProxmoxUtil.ProxmoxUtil;
import kong.unirest.json.JSONObject;
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

@RestController
public class T8WebsController {

    @Autowired
    IServerService serverService;
    @Autowired
    IUserDAO userDAO;

    private ObjectMapper mapper = new ObjectMapper();

    @GetMapping(value="/user", produces="application/json")
    public ResponseEntity getUser(@AuthenticationPrincipal OAuth2User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String userId = user.getAttribute("sub");
            String name = user.getAttribute("given_name");
            String email = user.getAttribute("email");
            String picture = user.getAttribute("picture");

            User appUser = userDAO.getUserById(userId);

            if (!appUser.isFound()) {
                appUser = new User();
                appUser.setUserId(userId);
                appUser.setName(name);
                appUser.setEmail(email);
                userDAO.save(appUser);
            } else if (!(appUser.getName().equals(name) && appUser.getEmail().equals(email))) {
                appUser.setName(name);
                appUser.setEmail(email);
                userDAO.update(appUser);
            }

            ObjectNode jsonNode = mapper.createObjectNode();
            jsonNode.put("status", appUser.getStatus().name());
            jsonNode.put("name", name);
            jsonNode.put("picture", picture);

            return new ResponseEntity(jsonNode, headers, HttpStatus.OK);

        } catch (DbQuery.IntegrityConstraintViolationException e) {
            e.printStackTrace();
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PatchMapping(value="/requestAccess", produces="application/json")
    public ResponseEntity requestAccess(@AuthenticationPrincipal OAuth2User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        User appUser = userDAO.getUserById(user.getAttribute("sub"));

        if (appUser.getStatus() != User.Status.NONE) {
            return new ResponseEntity(headers, HttpStatus.BAD_REQUEST);
        }

        if (userDAO.requestAccess(user.getAttribute("sub"))) {
            return new ResponseEntity(headers, HttpStatus.OK);
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(value="/addServer", produces="application/json")
    public ResponseEntity addServer(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="serverName") String serverName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!userDAO.isApproved(user.getAttribute("sub"))) {
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }

        try {
            Server.CreationStatus status = serverService.addServer(user.getAttribute("sub"), serverName.trim());
            if (status == Server.CreationStatus.COMPLETED) {
                return new ResponseEntity(headers, HttpStatus.OK);
            } else if (status == Server.CreationStatus.BEGIN) {
                return new ResponseEntity(headers, HttpStatus.CONFLICT);
            } else if (status == Server.CreationStatus.VERIFIED_NAME) {
                return new ResponseEntity(headers, HttpStatus.NO_CONTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/deleteServer")
    public ResponseEntity deleteServer(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="vmid") int vmid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!userDAO.isApproved(user.getAttribute("sub"))) {
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }

        if (serverService.deleteVM(user.getAttribute("sub"), vmid)) {
            return new ResponseEntity(headers, HttpStatus.OK);
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/renameServer")
    public ResponseEntity renameServer(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="vmid") int vmid, @RequestParam(value="name") String serverName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!userDAO.isApproved(user.getAttribute("sub"))) {
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }

        if (serverService.renameServer(user.getAttribute("sub"), vmid, serverName)) {
            return new ResponseEntity(headers, HttpStatus.OK);
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/deployBuild")
    public ResponseEntity uploadFile(@AuthenticationPrincipal OAuth2User user, @RequestParam("buildFile") MultipartFile buildFile, @RequestParam(value="vmid") int vmid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!userDAO.isApproved(user.getAttribute("sub"))) {
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }

        try {
            if (serverService.deployBuild(user.getAttribute("sub"), vmid, buildFile)) {
                return new ResponseEntity(headers, HttpStatus.OK);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(value="/tree", produces="application/json")
    public ResponseEntity getTree(@AuthenticationPrincipal OAuth2User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {

            ArrayNode results = mapper.createArrayNode();

            if (userDAO.isAdmin(user.getAttribute("sub"))) {
                ObjectNode adminNode = getAdminTreeNode();
                results.add(adminNode);
            }

            ObjectNode attr = mapper.createObjectNode();
            attr.put("type", 4);

            ObjectNode serversNode = mapper.createObjectNode();
            serversNode.put("id", "4");
            serversNode.put("name", "Servers");
            serversNode.put("expanded", true);
            serversNode.put("hasAttributes", attr);

            ArrayNode serversArray = serverService.getUserServers(user.getAttribute("sub"));
            serversNode.put("subChild", serversArray);
            results.add(serversNode);

            return new ResponseEntity(results, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/startServer")
    public ResponseEntity startServer(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="vmid") int vmid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!userDAO.isApproved(user.getAttribute("sub"))) {
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }

        try {
            if (serverService.startVM(user.getAttribute("sub"), vmid)) {
                ObjectNode responseNode = mapper.createObjectNode();
                responseNode.put("status", "running");

                return new ResponseEntity(responseNode, headers, HttpStatus.OK);
            }
        } catch (ProxmoxUtil.InvalidVmStateException e) {
            e.printStackTrace();
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PatchMapping("/stopServer")
    public ResponseEntity stopServer(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="vmid") int vmid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!userDAO.isApproved(user.getAttribute("sub"))) {
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }

        try {
            if (serverService.shutdownVM(user.getAttribute("sub"), vmid)) {
                ObjectNode responseNode = mapper.createObjectNode();
                responseNode.put("status", "stopped");

                return new ResponseEntity(responseNode, headers, HttpStatus.OK);
            }
        } catch (ProxmoxUtil.InvalidVmStateException e) {
            e.printStackTrace();
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PatchMapping("/rebootServer")
    public ResponseEntity rebootServer(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="vmid") int vmid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (!userDAO.isApproved(user.getAttribute("sub"))) {
            return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
        }

        try {
            if (serverService.rebootVM(user.getAttribute("sub"), vmid)) {
                ObjectNode responseNode = mapper.createObjectNode();
                responseNode.put("status", "running");

                return new ResponseEntity(responseNode, headers, HttpStatus.OK);
            }
        } catch (ProxmoxUtil.InvalidVmStateException e) {
            e.printStackTrace();
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(value="/serverStatus", produces="application/json")
    public ResponseEntity getServerStatus(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="vmid") int vmid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode responseNode = mapper.createObjectNode();
        responseNode.put("status", serverService.getVmStatus(user.getAttribute("sub"), vmid));
        return new ResponseEntity(responseNode, headers, HttpStatus.OK);
    }

    @GetMapping(value="/serverData", produces="application/json")
    public ResponseEntity getServerData(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="vmid") int vmid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject jsonObject = serverService.getVmData(user.getAttribute("sub"), vmid, ProxmoxUtil.TimeFrame.HOUR);
        ObjectNode objectNode = mapper.valueToTree(jsonObject.toMap());

        return new ResponseEntity(objectNode, headers, HttpStatus.OK);
    }

    private ObjectNode getAdminTreeNode() {
        ObjectNode adminNode = mapper.createObjectNode();
        adminNode.put("id", "1");
        adminNode.put("name", "Administration");
        adminNode.put("expanded", true);

        ObjectNode adminAttr = mapper.createObjectNode();
        adminAttr.put("type", 1);
        adminNode.put("hasAttributes", adminAttr);

        ArrayNode adminSubNode = mapper.createArrayNode();
        ObjectNode usersNode = mapper.createObjectNode();
        usersNode.put("id", "2");
        usersNode.put("name", "Users");

        ObjectNode usersAttr = mapper.createObjectNode();
        usersAttr.put("type", 2);
        usersNode.put("hasAttributes", usersAttr);

        ObjectNode serversNode = mapper.createObjectNode();
        serversNode.put("id", "3");
        serversNode.put("name", "Servers");

        ObjectNode serversAttr = mapper.createObjectNode();
        serversAttr.put("type", 3);
        serversNode.put("hasAttributes", serversAttr);

        adminSubNode.add(usersNode);
        adminSubNode.add(serversNode);

        adminNode.put("subChild", adminSubNode);

        return adminNode;
    }
}
