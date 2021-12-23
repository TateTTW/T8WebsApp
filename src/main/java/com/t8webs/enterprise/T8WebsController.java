package com.t8webs.enterprise;

import com.t8webs.enterprise.dto.UserAccount;
import com.t8webs.enterprise.service.IServerService;
import com.t8webs.enterprise.service.IUserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@Controller
public class T8WebsController {
    @Autowired
    IUserAccountService userAccountService;
    @Autowired
    IServerService serverService;

    @RequestMapping("/")
    public String index(HttpServletResponse response) {
        return "start";
    }

    /**
     * Create a new user account record from the user account data provided.
     *
     * Returns one of the following status codes:
     * 201: successfully created a user account.
     * 409: unable to create a user account, because username already exists in the database.
     * 500: SQL Database error occurred.
     *
     * @param userAccount a JSON representation of a UserAccount object
     * @return a valid user token for session authentication
     */
    @PostMapping(value="/signUp", consumes="application/json", produces="application/json")
    public ResponseEntity signUpUser(@RequestBody UserAccount userAccount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            if (userAccountService.userAccountExists(userAccount)) {
                return new ResponseEntity(headers, HttpStatus.CONFLICT);
            }

            userAccount = userAccountService.createUserAccount(userAccount);

        } catch (Exception e) {
            return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (!userAccount.isFound() || userAccount.getToken() == null || userAccount.getToken().isEmpty()) {
            return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(userAccount.getToken(), headers, HttpStatus.CREATED);
    }

    @PutMapping(value="/assignServer", consumes="application/json", produces="application/json")
    public ResponseEntity assignServer(@RequestParam(value="serverName") String servername, @RequestParam(value="username") String username, @RequestParam(value="token") String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            // authenticate request
            if (isTokenInvalid(username, token)) {
                return new ResponseEntity(headers, HttpStatus.UNAUTHORIZED);
            }

            UserAccount userAccount = userAccountService.fetchUserAccount(username);

            if (!serverService.assignUserServer(userAccount, servername)) {
                return new ResponseEntity(headers, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(headers, HttpStatus.OK);
    }

    private boolean isTokenInvalid(String username, String token) throws SQLException, IOException, ClassNotFoundException {
        return !userAccountService.isTokenValid(userAccountService.fetchUserAccount(username), token);
    }
}
