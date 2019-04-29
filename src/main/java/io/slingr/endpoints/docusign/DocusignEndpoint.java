package io.slingr.endpoints.docusign;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import io.slingr.endpoints.Endpoint;
import io.slingr.endpoints.docusign.entities.*;
import io.slingr.endpoints.docusign.services.DocusignApi;
import io.slingr.endpoints.docusign.services.DocusignServiceApi;
import io.slingr.endpoints.docusign.utils.Converter;
import io.slingr.endpoints.exceptions.EndpointException;
import io.slingr.endpoints.exceptions.ErrorCode;
import io.slingr.endpoints.framework.annotations.EndpointFunction;
import io.slingr.endpoints.framework.annotations.EndpointProperty;
import io.slingr.endpoints.framework.annotations.EndpointWebService;
import io.slingr.endpoints.framework.annotations.SlingrEndpoint;
import io.slingr.endpoints.services.rest.RestMethod;
import io.slingr.endpoints.utils.Json;
import io.slingr.endpoints.utils.Strings;
import io.slingr.endpoints.ws.exchange.WebServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>DocuSign endpoint
 *
 * <p>Created by lefunes on 06/29/15.
 */
@SlingrEndpoint(name = "docusign")
public class DocusignEndpoint extends Endpoint {

    private static final Logger logger = LoggerFactory.getLogger(DocusignEndpoint.class);

    private DocusignServiceApi service = null;

    private XStream xStream = generateXStreamConfiguration();

    @EndpointProperty
    private String email;

    @EndpointProperty
    private String password;

    @EndpointProperty
    private String integratorKey;

    @EndpointProperty(defaultValue = "false")
    private boolean demoIntegrationKey;

    @Override
    public void endpointStarted() {
        this.service = new DocusignServiceApi(files(), email, password, integratorKey, demoIntegrationKey, properties().isDebug());

        logger.info(String.format("Configured endpoint: email [%s] password [%s] integration key [%s] demo account [%s]", email, Strings.maskToken(password), integratorKey, demoIntegrationKey));
    }

    @EndpointFunction(name = "requestSignatureFromDocument")
    public Json requestSignatureFromDocument(Json body){
        final DocusignApi api = service.getApi();
        if(api == null){
            throw EndpointException.permanent(ErrorCode.API, "Exception when try to request signature from documents: empty api client.");
        }
        final Json response;
        try {
            response = api.requestSignatureFromDocument(body);
        } catch (Exception ex){
            logger.warn(String.format("Exception when try to request signature from documents: %s", ex instanceof EndpointException ? ((EndpointException) ex).toJson(false) : ex.getMessage()), ex);
            throw ex;
        }
        return response;
    }

    @EndpointFunction(name = "updateEnvelope")
    public Json updateEnvelope(Json body){
        final DocusignApi api = service.getApi();
        if(api == null){
            throw EndpointException.permanent(ErrorCode.API, "Exception when try to updateEnvelope: empty api client.");
        }
        final Json response;
        try {
            response = api.updateEnvelope(body);
        } catch (Exception ex){
            logger.warn(String.format("Exception when try to updateEnvelope: %s", ex instanceof EndpointException ? ((EndpointException) ex).toJson(false) : ex.getMessage()), ex);
            throw ex;
        }
        return response;
    }

    @EndpointWebService(methods = RestMethod.POST)
    public void webhooks(String request){
        if(request == null){
            return;
        }

        if(demoIntegrationKey){
            logger.info(String.format("New event arrived [%s]", request.replaceAll("[\\n\\t\\r]+", "")));
        }

        final EnvelopeInformation notification = (EnvelopeInformation) xStream.fromXML(request);

        final Json jsonNotification = notification.toJson();
        final boolean isEnvelopeEvent = jsonNotification.is("includeDocuments");

        jsonNotification.remove("includeDocuments");

        final Json event = Converter.fromDocusignToSlingr(files(), jsonNotification);
        events().send(isEnvelopeEvent ? "envelopeStatusChanged" : "recipientStatusChanged", event);
    }

    @EndpointWebService(methods = {RestMethod.GET, RestMethod.HEAD})
    public void exposeWebhookUri(WebServiceRequest request){
        // do nothing
    }

    private static XStream generateXStreamConfiguration(){
        XStream xStream = new XStream() {
            @Override
            protected MapperWrapper wrapMapper(MapperWrapper next) {
                return new MapperWrapper(next) {
                    @Override
                    public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                        return definedIn != Object.class && super.shouldSerializeMember(definedIn, fieldName);
                    }
                };
            }
        };
        xStream.alias("DocuSignEnvelopeInformation", EnvelopeInformation.class);
        xStream.alias("EnvelopeStatus", Envelope.class);
        xStream.alias("RecipientStatus", Recipient.class);
        xStream.alias("DocumentStatus", Document.class);
        xStream.alias("DocumentPDF", Document.class);
        xStream.alias("TabStatus", TabStatus.class);

        XStream.setupDefaultSecurity(xStream);
        xStream.allowTypesByWildcard(new String[] {
                "io.slingr.endpoints.docusign.entities.**"
        });

        return xStream;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getIntegratorKey() {
        return integratorKey;
    }

    public boolean isDemoIntegrationKey() {
        return demoIntegrationKey;
    }
}
