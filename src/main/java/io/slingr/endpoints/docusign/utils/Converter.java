package io.slingr.endpoints.docusign.utils;

import io.slingr.endpoints.exceptions.EndpointException;
import io.slingr.endpoints.exceptions.ErrorCode;
import io.slingr.endpoints.services.Files;
import io.slingr.endpoints.services.exchange.Parameter;
import io.slingr.endpoints.services.rest.DownloadedFile;
import io.slingr.endpoints.utils.FilesUtils;
import io.slingr.endpoints.utils.Json;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * <p>Converts data between application and Docusign
 *
 * <p>Created by lefunes on 23/09/15.
 */
public class Converter {

    private static final Logger logger = LoggerFactory.getLogger(Converter.class);

    public static Json fromDocusignToSlingr(Files endpointFiles, Json notification){
        if(notification == null){
            notification = Json.map();
        }

        final List<Json> documents = new ArrayList<>();
        if(notification.contains("documents")){
            for (Json document : notification.jsons("documents")) {
                final String fileName = document.string("name");
                final String contentType = document.string("contentType");

                final Json d = Json.map()
                        .setIfNotEmpty("documentId", document.string("documentId"))
                        .setIfNotEmpty("contentType", contentType)
                        .setIfNotEmpty("fileName", fileName);

                if(document.contains("file") && StringUtils.isNotBlank(document.string("file"))){
                    final boolean b64 = document.bool("base64", false);
                    final Json file = endpointFiles.upload(fileName, document.string("file"), contentType, b64);

                    d.setIfNotEmpty("fileId", file.string("fileId"))
                            .setIfNotEmpty("fileName", file.string("fileName"))
                            .setIfNotEmpty("contentType", file.string("contentType"));
                }

                documents.add(d);
            }
        }
        notification.setIfNotNull("documents", documents);

        return notification;
    }

    public static Json fromSlingrToDocusign(Files endpointFiles, Json data, List<Json> files, boolean isPartialUpdate, boolean includeDocuments){
        if(data == null || data.isEmpty()){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "Invalid data.");
        }

        String status = data.string("status");
        if(StringUtils.isBlank(status)){
            status = "sent";
        }

        final String subject = data.string("subject");
        if(StringUtils.isBlank(subject) && "sent".equals(status)){
            throw EndpointException.permanent(ErrorCode.ARGUMENT, "Empty subject. It is a mandatory field.");
        }

        final Json json = Json.map()
                .set("status", status)
                .set("emailSubject", subject)
                .setIfNotEmpty("emailBlurb", data.string("message"))
                .setIfNotEmpty("voidedReason", data.string("voidedReason"));

        final List<Json> recipients = data.jsons("signers");

        final List<Map<String, Object>> signers = new ArrayList<>();
        if(recipients != null && !recipients.isEmpty()) {
            int id = 1;
            for (Json recipient : recipients) {
                final String email = recipient.string("email");
                final String name = recipient.string("name");

                if(StringUtils.isNotBlank(email) && StringUtils.isNotBlank(name)){
                    Integer routingOrder = recipient.integer("routingOrder");
                    if(routingOrder == null || routingOrder < 1){
                        routingOrder = 1;
                    }

                    final Json s = Json.map()
                            .set("email", email)
                            .set("name", name)
                            .set("recipientId", id)
                            .set("routingOrder", routingOrder);

                    s.setIfNotEmpty("tabs", recipient.objectsMap("tabs"));

                    signers.add(s.toMap());
                    id++;
                } else {
                    logger.warn(String.format("requestSignatureFromDocument: invalid signer definition: email [%s] name [%s]", email, name));
                }
            }
        }
        if(isPartialUpdate){
            if(!signers.isEmpty()){
                json.set("recipients", Json.map().set("signers", signers).toMap());
            }
        } else {
            if (signers.isEmpty()) {
                throw EndpointException.permanent(ErrorCode.ARGUMENT, "There is no defined signer.");
            }
            json.set("recipients", Json.map().set("signers", signers).toMap());
        }

        if(includeDocuments) {
            final List<Json> docs = data.jsons("documents");
            final List<Json> documents = processDocuments(endpointFiles, docs, files);
            if (isPartialUpdate) {
                if (!documents.isEmpty()) {
                    json.set("documents", documents);
                }
            } else {
                if (documents.isEmpty()) {
                    throw EndpointException.permanent(ErrorCode.ARGUMENT, "There is no document to send.");
                }
                json.set("documents", documents);
            }
        }

        return json;
    }

    private static List<Json> processDocuments(Files endpointFiles, List<Json> docs, List<Json> files) throws EndpointException {
        final List<Json> documents = new ArrayList<>();
        if (files == null) {
            files = new ArrayList<>();
        }

        if (docs != null && !docs.isEmpty()) {
            int id = 1;
            final Random rnd = new Random();
            for (Json document : docs) {
                final String name = document.string("fileName");

                if (StringUtils.isBlank(name)) {
                    logger.warn(String.format("requestSignatureFromDocument: empty document name [%s]", name));
                    throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("File [%s] - document name is empty", id));
                } else {
                    final String fileId = document.string("fileId");
                    if (StringUtils.isBlank(fileId)) {
                        logger.warn("requestSignatureFromDocument: invalid document id");
                        throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("File [%s] - fileId is empty", id));
                    } else {
                        final Json d = Json.map()
                                .set("name", name)
                                .set("documentId", id)
                                .set("order", id);

                        final Json f = Json.map()
                                .set("fileName", name)
                                .set("documentId", id);

                        String[] parts = name.split("\\.");
                        if (parts.length < 2) {
                            logger.warn(String.format("requestSignatureFromDocument: the file name does not include the extension [%s]", name));
                            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("File [%s] - error when try to read the file extension [%s] - file [%s]", id, name, fileId));
                        } else {
                            d.set("fileExtension", parts[parts.length - 1]);
                            f.set("name", parts[0]);
                        }

                        InputStream inputStream = null;
                        OutputStream outputStream = null;
                        byte[] bytes = new byte[1024];
                        int read;

                        try {
                            // download the file
                            logger.debug(String.format("Reading file [%s]...", fileId));
                            final String tmpFileName = String.format("%s-%s", fileId, Integer.toHexString(rnd.nextInt()));
                            final File temp = File.createTempFile(tmpFileName, null);
                            outputStream = new FileOutputStream(temp);

                            final DownloadedFile file = endpointFiles.download(fileId);
                            inputStream = file.getFile();

                            boolean contentNotNull = false;
                            if (inputStream != null) {
                                while ((read = inputStream.read(bytes)) != -1) {
                                    outputStream.write(bytes, 0, read);
                                    if (read > 0) {
                                        contentNotNull = true;
                                    }
                                }
                            }
                            if (!contentNotNull) {
                                throw EndpointException.retryable(ErrorCode.CLIENT, String.format("The file [%s] can be retrieved from the application", fileId));
                            }
                            try {
                                outputStream.flush();
                            } catch (Exception ex) {
                                logger.warn(String.format("Exception when try to flush output stream for file [%s]: %s", fileId, ex.getMessage()));
                            }
                            logger.debug(String.format("File [%s] saved locally", fileId));

                            f.setIfNotNull("file", temp.getAbsolutePath());

                            final Json fileMetadata = endpointFiles.metadata(fileId);
                            f.setIfNotEmpty("mediaType", FilesUtils.getMediaType(fileMetadata != null ?
                                    fileMetadata.string(Parameter.CONTENT_TYPE) :
                                    file.getHeaders() != null ?
                                            file.getHeaders().string(Parameter.CONTENT_TYPE) :
                                            null
                                    , name));

                            files.add(f);
                            documents.add(d);
                            id++;
                        } catch (Exception ex) {
                            logger.warn(String.format("Exception when try to read the file [%s] - exception: %s", fileId, ex.getMessage()), ex);
                            throw EndpointException.permanent(ErrorCode.ARGUMENT, String.format("File [%s] - error when try to read the file [%s]", id, fileId));
                        } finally {
                            logger.debug(String.format("Closing file [%s] streams...", fileId));
                            if (outputStream != null) {
                                try {
                                    outputStream.flush();
                                    outputStream.close();
                                } catch (Exception ex) {
                                    logger.warn(String.format("Exception when try to close output stream for file [%s]: %s", fileId, ex.getMessage()));
                                }
                            }
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (Exception ex) {
                                    logger.warn(String.format("Exception when try to close input stream for file [%s]: %s", fileId, ex.getMessage()));
                                }
                            }
                        }
                    }
                }
            }
        }
        return documents;
    }
}
