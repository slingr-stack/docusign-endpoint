package io.slingr.endpoints.docusign.services;

import io.slingr.endpoints.exceptions.EndpointException;
import io.slingr.endpoints.services.Files;
import io.slingr.endpoints.services.rest.RestClient;
import io.slingr.endpoints.utils.Json;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.WebTarget;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * <p>Created by lefunes on 06/29/15.
 */
public class DocusignServiceApi extends RestClient {

    private static final Logger logger = LoggerFactory.getLogger(DocusignServiceApi.class);

    private static final String BASE_URL = "https://%s.docusign.net/restapi/v2";
    private static final long MAX_VALID_API = 6L * 60 * 60 * 1000; // 6 hours

    public static final String AUTH_HEADER = "X-DocuSign-Authentication";

    private final Files endpointFiles;
    private final String authentication;
    private DocusignApi api = null;
    private final ReentrantLock lockGenerateApi = new ReentrantLock();
    private long lastApiGeneration = 0L;

    public DocusignServiceApi(Files endpointFiles, String email, String password, String integratorKey, boolean demoIntegrationKey, boolean debug) throws EndpointException {
        super(String.format(BASE_URL, demoIntegrationKey ? "demo" : "www"));
        this.setDebug(debug);

        this.authentication = String.format("<DocuSignCredentials><Username>%s</Username><Password>%s</Password><IntegratorKey>%s</IntegratorKey></DocuSignCredentials>", email, password, integratorKey);
        setupDefaultHeader(AUTH_HEADER, this.authentication);

        this.endpointFiles = endpointFiles;
    }

    public String getAuthentication(){
        return authentication;
    }

    public Json getLoginInformation(){
        WebTarget target = getApiTarget().path("/login_information");
        return get(target);
    }

    public DocusignApi generateApi(){
        try {
            final Json info = getLoginInformation();
            if(info != null) {
                final List<Json> loginAccounts = info.jsons("loginAccounts");
                if(loginAccounts != null && !loginAccounts.isEmpty()) {
                    final String baseUrl = loginAccounts.get(0).string("baseUrl");
                    if (StringUtils.isNotBlank(baseUrl)) {
                        return new DocusignApi(endpointFiles, baseUrl, this.authentication, loginAccounts.get(0).string("accountId"), this.debug);
                    }
                }
            }
        } catch (Exception ex){
            logger.warn(String.format("Error when try to generate api client: %s", ex instanceof EndpointException ? ((EndpointException) ex).toJson(false) : ex.getMessage()), ex);
            throw ex;
        }
        return null;
    }

    public DocusignApi getApi(){
        logger.debug("Get api: Starting");
        DocusignApi response = null;
        lockGenerateApi.lock();
        try {
            if (api == null || (System.currentTimeMillis() - lastApiGeneration > MAX_VALID_API)) {
                api = generateApi();
                lastApiGeneration = System.currentTimeMillis();

                logger.debug(String.format("Get api: generated new api [%s]", api.getAccountId()));
            }
            response = api;
        } finally {
            lockGenerateApi.unlock();
        }
        logger.debug(String.format("Get api: api to use [%s]", response != null ? response.getAccountId() : "-"));
        return response;
    }

    /**
     * This retrieves the current API request logging setting for the user and remaining log entries.
     *
     * @throws EndpointException
     */
    public Json requestLoggingSettings() throws EndpointException {
        try {
            if (debug) {
                logger.info("Sending request to get the logging settings");
            }

            final WebTarget target = getApiTarget().path("/diagnostics/settings");
            return get(target);
        } catch (Exception ex){
            throw convertException(ex);
        }
    }

    private EndpointException convertException(Exception exception) {
        return EndpointException.parseHTTPExceptions(exception, "message", "errorCode");
    }
}
