package com.t8webs.enterprise;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.t8webs.enterprise.dto.Server;
import com.t8webs.enterprise.service.IServerService;
import com.t8webs.enterprise.utils.ProxmoxUtil;
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

    @PostMapping(value="/addServer", produces="application/json")
    public ResponseEntity addServer(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="serverName") String serverName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            Server.CreationStatus status = serverService.addServer(user.getAttribute("email"), serverName.trim());
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

        try {
            if(serverService.deleteVM(user.getAttribute("email"), vmid)){
                return new ResponseEntity(headers, HttpStatus.OK);
            }
        } catch (ProxmoxUtil.InvalidVmStateException e) {
            e.printStackTrace();
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/renameServer")
    public ResponseEntity renameServer(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="vmid") int vmid, @RequestParam(value="name") String serverName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if(serverService.renameServer(user.getAttribute("email"), vmid, serverName)){
            return new ResponseEntity(headers, HttpStatus.OK);
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/deployBuild")
    public ResponseEntity uploadFile(@AuthenticationPrincipal OAuth2User user, @RequestParam("buildFile") MultipartFile buildFile, @RequestParam(value="vmid") int vmid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            if(serverService.deployBuild(user.getAttribute("email"), vmid, buildFile)){
                return new ResponseEntity(headers, HttpStatus.OK);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping(value="/servers", produces="application/json")
    public ResponseEntity getUserServers(@AuthenticationPrincipal OAuth2User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {

            ArrayNode results = mapper.createArrayNode();

            ObjectNode attribute = mapper.createObjectNode();
            attribute.put("type", 0);

            ObjectNode serversNode = mapper.createObjectNode();
            serversNode.put("id", "0");
            serversNode.put("name", "Servers");
            serversNode.put("expanded", true);
            serversNode.put("hasAttributes", attribute);

            ArrayNode serversArray = serverService.getUserServers(user.getAttribute("email"));
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

        try {
            if(serverService.startVM(user.getAttribute("email"), vmid)){
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

        try {
            if(serverService.shutdownVM(user.getAttribute("email"), vmid)){
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

        try {
            if(serverService.rebootVM(user.getAttribute("email"), vmid)){
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
        responseNode.put("status", serverService.getVmStatus(user.getAttribute("email"), vmid));
        return new ResponseEntity(responseNode, headers, HttpStatus.OK);
    }

    @GetMapping(value="/serverData", produces="application/json")
    public ResponseEntity getServerData(@AuthenticationPrincipal OAuth2User user, @RequestParam(value="vmid") int vmid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject jsonObject = serverService.getVmData(user.getAttribute("email"), vmid, ProxmoxUtil.TimeFrame.HOUR);
        ObjectNode objectNode = mapper.valueToTree(jsonObject.toMap());

        return new ResponseEntity(objectNode, headers, HttpStatus.OK);
    }
}
