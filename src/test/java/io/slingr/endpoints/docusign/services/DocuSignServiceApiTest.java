package io.slingr.endpoints.docusign.services;

import io.slingr.endpoints.docusign.DocusignEndpoint;
import io.slingr.endpoints.docusign.utils.Converter;
import io.slingr.endpoints.utils.Json;
import io.slingr.endpoints.utils.tests.EndpointTests;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * <p>Created by lefunes on 06/30/15.
 */
@Ignore("For dev proposes")
public class DocuSignServiceApiTest {

    private static final Logger logger = LoggerFactory.getLogger(DocuSignServiceApiTest.class);

    private static EndpointTests test;
    private static DocusignEndpoint endpoint;
    private static DocusignServiceApi service;

    @BeforeClass
    public static void init() throws Exception {
        test = EndpointTests.start(new io.slingr.endpoints.docusign.Runner(), "test.properties");
        endpoint = (DocusignEndpoint) test.getEndpoint();

        service = new DocusignServiceApi(endpoint.files(), endpoint.getEmail(), endpoint.getPassword(), endpoint.getIntegratorKey(), endpoint.isDemoIntegrationKey(), true);

        assertNotNull(service);
        assertNotNull(service.getAuthentication());
    }

    @Test
    public void testGetLoginInformation() throws Exception {
        Json loginInfo = service.getLoginInformation();
        assertNotNull(loginInfo);

        logger.info("-- END");
    }

    @Test
    public void testGenerateApi() throws Exception {
        DocusignApi api = service.generateApi();
        assertNotNull(api);

        Json accountInfo = api.getAccountInformation();
        assertNotNull(accountInfo);

        assertEquals("idea2 Tests", accountInfo.string("accountName"));
        assertEquals("2baecd04-e627-4437-94fb-08e312392d64", accountInfo.string("accountIdGuid"));
        assertEquals("DEVCENTER_DEMO_APRIL2013", accountInfo.string("planName"));

        logger.info("-- END");
    }

    @Test
    public void testGetApi() throws Exception {
        DocusignApi api1 = service.getApi();
        assertNotNull(api1);
        DocusignApi api2 = service.getApi();
        assertNotNull(api2);
        DocusignApi api3 = service.getApi();
        assertNotNull(api3);
        DocusignApi api4 = service.getApi();
        assertNotNull(api4);

        assertEquals(api1, api2);
        assertEquals(api3, api4);
        assertEquals(api1, api4);

        logger.info("-- END");
    }

    @Test
    public void checkRequestSendDocument() throws Exception {
        Json request;
        Json response;
        List<Json> signers;
        Json signer;
        Json tabs;
        List<Json> userTabs;
        Json userTab;
        List<Json> documents;
        List<Json> files;

        // example 001
        request = getRequest("001_send_document");

        assertEquals("Test message to request-signature from document", request.string("message"));
        assertEquals("Test message", request.string("subject"));

        signers = request.jsons("signers");
        assertNotNull(signers);
        assertEquals(3, signers.size());

        signer = signers.get(0);
        assertEquals("test@crm.slingr.io", signer.string("email"));
        assertEquals("Slingr Test Account", signer.string("name"));
        assertEquals((Integer) 2, signer.integer("routingOrder"));
        assertNull(signer.objectsMap("tabs"));

        signer = signers.get(1);
        assertEquals("test+2@crm.slingr.io", signer.string("email"));
        assertEquals("Slingr Test Account - 2", signer.string("name"));
        assertNull(signer.integer("routingOrder"));

        tabs = signer.json("tabs");
        assertNotNull(tabs);
        assertEquals(2, tabs.size());

        userTabs = tabs.jsons("signHereTabs");
        assertNotNull(userTabs);
        assertEquals(1, userTabs.size());

        userTab = userTabs.get(0);
        assertEquals("100", userTab.string("xPosition"));
        assertEquals("100", userTab.string("yPosition"));
        assertEquals("1", userTab.string("documentId"));
        assertEquals("1", userTab.string("pageNumber"));

        userTabs = tabs.jsons("initialHereTabs");
        assertNotNull(userTabs);
        assertEquals(1, userTabs.size());

        userTab = userTabs.get(0);
        assertEquals("200", userTab.string("xPosition"));
        assertEquals("100", userTab.string("yPosition"));
        assertEquals("1", userTab.string("documentId"));
        assertEquals("1", userTab.string("pageNumber"));

        signer = signers.get(2);
        assertEquals("test+3@crm.slingr.io", signer.string("email"));
        assertEquals("Slingr Test Account - 3", signer.string("name"));
        assertEquals((Integer) 3, signer.integer("routingOrder"));
        assertNull(signer.objectsMap("tabs"));

        documents = request.jsons("documents");
        assertNotNull(documents);
        assertEquals(10, documents.size());

        assertEquals("document_01.txt", documents.get(0).string("fileName"));
        assertEquals("document_02.pdf", documents.get(1).string("fileName"));
        assertEquals("document_03.rtf", documents.get(2).string("fileName"));
        assertEquals("document_04.xls", documents.get(3).string("fileName"));
        assertEquals("document_05.xlsx", documents.get(4).string("fileName"));
        assertEquals("document_06.csv", documents.get(5).string("fileName"));
        assertEquals("document_07.html", documents.get(6).string("fileName"));
        assertEquals("document_08.doc", documents.get(7).string("fileName"));
        assertEquals("document_09.docx", documents.get(8).string("fileName"));
        assertEquals("document_10.txt", documents.get(9).string("fileName"));

        files = new ArrayList<>();
        response = Converter.fromSlingrToDocusign(endpoint.files(), request, files, true, true);
        assertNotNull(response);

        assertEquals("sent", response.string("status"));
        assertEquals("Test message", response.string("emailSubject"));
        assertEquals("Test message to request-signature from document", response.string("emailBlurb"));

        signer = response.json("recipients");
        assertNotNull(signer);
        assertEquals(1, signer.size());

        signers = signer.jsons("signers");
        assertNotNull(signers);
        assertEquals(3, signers.size());

        signer = signers.get(0);
        assertEquals("test@crm.slingr.io", signer.string("email"));
        assertEquals("Slingr Test Account", signer.string("name"));
        assertEquals((Integer) 2, signer.integer("routingOrder"));
        assertEquals((Integer) 1, signer.integer("recipientId"));
        assertNull(signer.objectsMap("tabs"));

        signer = signers.get(1);
        assertEquals("test+2@crm.slingr.io", signer.string("email"));
        assertEquals("Slingr Test Account - 2", signer.string("name"));
        assertEquals((Integer) 1, signer.integer("routingOrder"));
        assertEquals((Integer) 2, signer.integer("recipientId"));

        tabs = signer.json("tabs");
        assertNotNull(tabs);
        assertEquals(2, tabs.size());

        userTabs = tabs.jsons("signHereTabs");
        assertNotNull(userTabs);
        assertEquals(1, userTabs.size());

        userTab = userTabs.get(0);
        assertEquals("100", userTab.string("xPosition"));
        assertEquals("100", userTab.string("yPosition"));
        assertEquals("1", userTab.string("documentId"));
        assertEquals("1", userTab.string("pageNumber"));

        userTabs = tabs.jsons("initialHereTabs");
        assertNotNull(userTabs);
        assertEquals(1, userTabs.size());

        userTab = userTabs.get(0);
        assertEquals("200", userTab.string("xPosition"));
        assertEquals("100", userTab.string("yPosition"));
        assertEquals("1", userTab.string("documentId"));
        assertEquals("1", userTab.string("pageNumber"));

        signer = signers.get(2);
        assertEquals("test+3@crm.slingr.io", signer.string("email"));
        assertEquals("Slingr Test Account - 3", signer.string("name"));
        assertEquals((Integer) 3, signer.integer("routingOrder"));
        assertEquals((Integer) 3, signer.integer("recipientId"));
        assertNull(signer.objectsMap("tabs"));

        documents = response.jsons("documents");
        assertNotNull(documents);
        assertEquals(10, documents.size());

        assertNotNull(files);
        assertEquals(10, files.size());

        testDocument(documents, files, 0, "document_01", "txt", "1", "1", "text/plain");
        testDocument(documents, files, 1, "document_02", "pdf", "2", "2", "application/pdf");
        testDocument(documents, files, 2, "document_03", "rtf", "3", "3", "application/rtf");
        testDocument(documents, files, 3, "document_04", "xls", "4", "4", "application/octet-stream");
        testDocument(documents, files, 4, "document_05", "xlsx", "5", "5", "application/octet-stream");
        testDocument(documents, files, 5, "document_06", "csv", "6", "6", "application/octet-stream");
        testDocument(documents, files, 6, "document_07", "html", "7", "7", "text/html");
        testDocument(documents, files, 7, "document_08", "doc", "8", "8", "application/octet-stream");
        testDocument(documents, files, 8, "document_09", "docx", "9", "9", "application/octet-stream");
        testDocument(documents, files, 9, "document_10", "txt", "10", "10", "text/plain");

        logger.info("-- END");
    }

    @Test
    @Ignore("For dev purposes only")
    public void testSendDocument() throws Exception {
        privateSend("001_send_document_emails");
        logger.info("-- END");
    }

    @Test
    @Ignore("For dev purposes only")
    public void testSendDocument2() throws Exception {
        privateSend("003_send_simple_document");
        logger.info("-- END");
    }

    @Test
    public void checkReviewSendDocumentWithTabs() throws Exception {
        final Json userRequest = getRequest("002_tabs");

        List<Json> userSigners = userRequest.jsons("signers");

        Json userTabs = userSigners.get(0).json("tabs");
        assertNotNull(userTabs);
        assertEquals(4, userTabs.size());

        List<Json> userSignHereTabs = userTabs.jsons("signHereTabs");
        assertNotNull(userSignHereTabs);
        assertEquals(3, userSignHereTabs.size());

        assertEquals("100", userSignHereTabs.get(0).string("xPosition"));
        assertEquals("100", userSignHereTabs.get(0).string("yPosition"));
        assertEquals("1", userSignHereTabs.get(0).string("documentId"));
        assertEquals("1", userSignHereTabs.get(0).string("pageNumber"));

        assertEquals("100", userSignHereTabs.get(1).string("xPosition"));
        assertEquals("300", userSignHereTabs.get(1).string("yPosition"));
        assertEquals("1", userSignHereTabs.get(1).string("documentId"));
        assertEquals("1", userSignHereTabs.get(1).string("pageNumber"));

        assertEquals("EXAMPLE #1", userSignHereTabs.get(2).string("anchorString"));
        assertEquals("1", userSignHereTabs.get(2).string("anchorXOffset"));
        assertEquals("0", userSignHereTabs.get(2).string("anchorYOffset"));
        assertEquals("false", userSignHereTabs.get(2).string("anchorIgnoreIfNotPresent"));
        assertEquals("inches", userSignHereTabs.get(2).string("anchorUnits"));

        List<Json> userInitialHereTabs = userTabs.jsons("initialHereTabs");
        assertNotNull(userInitialHereTabs);
        assertEquals(1, userInitialHereTabs.size());

        assertEquals("200", userInitialHereTabs.get(0).string("xPosition"));
        assertEquals("100", userInitialHereTabs.get(0).string("yPosition"));
        assertEquals("1", userInitialHereTabs.get(0).string("documentId"));
        assertEquals("1", userInitialHereTabs.get(0).string("pageNumber"));

        Json request = Converter.fromSlingrToDocusign(endpoint.files(), userRequest, null, true, true);
        assertNotNull(request);

        Json recipients = request.json("recipients");
        assertNotNull(recipients);
        assertEquals(1, recipients.size());

        List<Json> signers = recipients.jsons("signers");
        assertNotNull(signers);
        assertEquals(1, signers.size());

        Json tabs = signers.get(0).json("tabs");
        assertNotNull(tabs);
        assertEquals(4, tabs.size());

        List<Json> signHereTabs = tabs.jsons("signHereTabs");
        assertNotNull(signHereTabs);
        assertEquals(3, signHereTabs.size());

        assertEquals("100", signHereTabs.get(0).string("xPosition"));
        assertEquals("100", signHereTabs.get(0).string("yPosition"));
        assertEquals("1", signHereTabs.get(0).string("documentId"));
        assertEquals("1", signHereTabs.get(0).string("pageNumber"));

        assertEquals("100", signHereTabs.get(1).string("xPosition"));
        assertEquals("300", signHereTabs.get(1).string("yPosition"));
        assertEquals("1", signHereTabs.get(1).string("documentId"));
        assertEquals("1", signHereTabs.get(1).string("pageNumber"));

        assertEquals("EXAMPLE #1", signHereTabs.get(2).string("anchorString"));
        assertEquals("1", signHereTabs.get(2).string("anchorXOffset"));
        assertEquals("0", signHereTabs.get(2).string("anchorYOffset"));
        assertEquals("false", signHereTabs.get(2).string("anchorIgnoreIfNotPresent"));
        assertEquals("inches", signHereTabs.get(2).string("anchorUnits"));

        List<Json> initialHereTabs = tabs.jsons("initialHereTabs");
        assertNotNull(initialHereTabs);
        assertEquals(1, initialHereTabs.size());

        assertEquals("200", initialHereTabs.get(0).string("xPosition"));
        assertEquals("100", initialHereTabs.get(0).string("yPosition"));
        assertEquals("1", initialHereTabs.get(0).string("documentId"));
        assertEquals("1", initialHereTabs.get(0).string("pageNumber"));

        logger.info("-- END");
    }

    @Test
    @Ignore("For dev purposes only")
    public void checkSendDocumentWithTabs() throws Exception {
        privateSend("002_tabs_email");
        logger.info("-- END");
    }

    private void privateSend(String file) throws IOException {
        final DocusignApi api = service.getApi();
        assertNotNull(api);

        final Json request = getRequest(file);

        final Json response = api.requestSignatureFromDocument(request);
        assertNotNull(response);

        assertNotNull(response.string("envelopeId"));
        assertNotNull(response.string("uri"));
        assertEquals("sent", response.string("status"));
    }

    private Json getRequest(String name) throws IOException {
        final Json request = Json.fromInternalFile(String.format("requests/%s.json", name));
        assertNotNull(request);

        return request;
    }

    private void testDocument(List<Json> documents, List<Json> files, int index, String name, String extension, String docId, String order, String contentType) {
        final Json document = documents.get(index);
        assertEquals(String.format("%s.%s", name, extension), document.string("name"));
        assertEquals(extension, document.string("fileExtension"));
        assertEquals(docId, document.string("documentId"));
        assertEquals(order, document.string("order"));

        final Json file = files.get(index);
        assertEquals(String.format("%s.%s", name, extension), file.string("fileName"));
        assertEquals(name, file.string("name"));
        assertNotNull(file.string("file"));
        assertEquals(docId, file.string("documentId"));
        assertEquals(contentType, file.string("mediaType"));
    }
}