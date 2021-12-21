package com.t8webs.enterprise.service;

import com.mashape.unirest.http.exceptions.UnirestException;

public interface IDomainService {

    /**
     * @param domainName to remove
     * @return
     * @throws UnirestException
     */
    boolean removeDomain(String domainName) throws UnirestException;


    /**
     * @param domainName to create
     * @return
     * @throws UnirestException
     */
    boolean createDomain(String domainName) throws UnirestException;
}
