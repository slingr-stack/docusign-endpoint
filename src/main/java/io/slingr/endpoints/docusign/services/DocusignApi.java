package io.slingr.endpoints.docusign.services;

import io.slingr.endpoints.docusign.utils.Converter;
import io.slingr.endpoints.exceptions.EndpointException;
import io.slingr.endpoints.exceptions.ErrorCode;
import io.slingr.endpoints.services.Files;
import io.slingr.endpoints.services.rest.RestClient;
import io.slingr.endpoints.utils.Json;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * <p>Created by lefunes on 30/06/15.
 */
public class DocusignApi extends RestClient {

    private static final Logger logger = LoggerFactory.getLogger(DocusignApi.class);

    private final String accountId;
    private final Files endpointFiles;

    DocusignApi(Files endpointFiles, String baseUrl, String authentication, String accountId, boolean debug) throws EndpointException {
        super(baseUrl);
        this.accountId = accountId;
        this.setDebug(debug);

        this.setupDefaultHeader(DocusignServiceApi.AUTH_HEADER, authentication);
        this.endpointFiles = endpointFiles;
    }

    public String getAccountId() {
        return accountId;
    }

    public Json getAccountInformation(){
        WebTarget target = getApiTarget();
        return get(target);
    }

    public Json requestSignatureFromDocument(Json data) throws EndpointException {
        final Map<String, InputStream> inputStreamsList = new HashMap<>();
        try {
            final MultiPart content = new MultiPart();
            createNewEnvelopeContent(data, inputStreamsList, content);

            logger.debug("Calling API");
            final WebTarget target = getApiTarget().path("envelopes");
            final Json response = post(target, content);
            logger.info(String.format("Response received [%s]", response.toString()));
            return response;
        } catch (Exception ex){
            logger.warn(String.format("Exception when request signature from document [%s]", ex instanceof EndpointException ? ((EndpointException) ex).toJson(false) : ex.getMessage()), ex);
            throw convertException(ex);
        } finally {
            closeInputStreams(inputStreamsList);
        }
    }

    public Json updateEnvelope(Json data) throws EndpointException {
        final Map<String, InputStream> inputStreamsList = new HashMap<>();
        try {
            logger.debug("Converting message from application to docusign");
            final Json json = Converter.fromSlingrToDocusign(endpointFiles, data, null, true, false);
            logger.debug("Message converted");

            final String envelopeId = data.string("envelopeId");
            if(StringUtils.isBlank(envelopeId)){
                throw EndpointException.permanent(ErrorCode.ARGUMENT, "Empty envelope id.");
            }

            logger.debug("Calling API");
            final WebTarget target = getApiTarget().path("envelopes").path(envelopeId)
                    .queryParam("advanced_update", true)
                    .queryParam("resend_envelope", data.bool("resendEnvelope", false));
            final Json response = put(target, json);
            response.set("envelopeId", envelopeId);
            logger.info(String.format("Response received [%s]", response.toString()));
            return response;
        } catch (Exception ex){
            logger.warn(String.format("Exception when update envelope [%s]", ex instanceof EndpointException ? ((EndpointException) ex).toJson(false) : ex.getMessage()), ex);
            throw convertException(ex);
        } finally {
            closeInputStreams(inputStreamsList);
        }
    }

    private void closeInputStreams(Map<String, InputStream> inputStreamsList) {
        try {
            Executors.newSingleThreadExecutor().execute(() -> {
                for (String fileId : inputStreamsList.keySet()) {
                    logger.debug(String.format("Closing local file [%s] streams...", fileId));
                    final InputStream inputStream = inputStreamsList.get(fileId);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                            logger.debug(String.format("Closed stream for local file [%s]", fileId));
                        } catch (Exception ex) {
                            logger.warn(String.format("Exception when try to close input stream for file [%s]: %s", fileId, ex.getMessage()));
                        }
                    }
                }
            });
        } catch (RejectedExecutionException ree){
            logger.info(String.format("Exception when try to start thread to close input streams: %s", ree.getMessage()));
        }
    }

    private void createNewEnvelopeContent(Json data, Map<String, InputStream> inputStreamsList, MultiPart content) throws FileNotFoundException {
        final List<Json> files = new ArrayList<>();
        logger.debug("Converting message from application to docusign");
        final Json json = Converter.fromSlingrToDocusign(endpointFiles, data, files, false, true);
        logger.debug("Message converted");

        final String requestString = json.toString();
        if (debug) {
            logger.info(String.format("Sending request signature from document [%s]", requestString));
        }

        final BodyPart jsonBodyPart = new BodyPart(requestString, MediaType.APPLICATION_JSON_TYPE);
        jsonBodyPart.setContentDisposition(ContentDisposition.type("form-data").build());
        content.bodyPart(jsonBodyPart);

        for (Json file : files) {
            logger.debug(String.format("Adding file %s", file.string("fileName")));
            final InputStream is = new FileInputStream(file.string("file"));
            inputStreamsList.put(file.string("fileName"), is);

            final BodyPart filePart = new BodyPart(is, (MediaType) file.object("mediaType"));
            filePart.setContentDisposition(ContentDisposition.type(String.format("file; filename=%s; documentId=%s;", file.string("fileName"), file.string("documentId"))).build());

            content.bodyPart(filePart);
            logger.debug(String.format("File %s added", file.string("fileName")));
        }
    }

    private EndpointException convertException(Exception exception) {
        return EndpointException.parseHTTPExceptions(exception, "message", "errorCode");
    }
}
