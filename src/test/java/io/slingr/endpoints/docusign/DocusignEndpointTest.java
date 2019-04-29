package io.slingr.endpoints.docusign;

import io.slingr.endpoints.services.rest.RestMethod;
import io.slingr.endpoints.utils.FilesUtils;
import io.slingr.endpoints.utils.Json;
import io.slingr.endpoints.utils.tests.EndpointTests;
import io.slingr.endpoints.utils.tests.EndpointsServicesMock;
import io.slingr.endpoints.ws.exchange.WebServiceResponse;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.*;

/**
 * <p>Created by lefunes on 06/29/15.
 */
@Ignore("For dev proposes")
public class DocusignEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(DocusignEndpointTest.class);

    private static EndpointTests test;

    @BeforeClass
    public static void init() throws Exception {
        test = EndpointTests.start(new io.slingr.endpoints.docusign.Runner(), "test.properties");
    }

    @Test
    public void testWebhooks() throws Exception {
        WebServiceResponse data;
        Json event, response;
        List<EndpointsServicesMock.FileMock> files;

        test.clearReceivedEvents();
        assertTrue(test.getReceivedEvents().isEmpty());
        test.clearFiles();
        assertTrue(test.getFiles().isEmpty());

        data = test.executeWebServices(RestMethod.GET, "");
        assertNotNull(data);
        assertTrue(test.getReceivedEvents().isEmpty());
        assertTrue(test.getFiles().isEmpty());

        data = test.executeWebServices(RestMethod.HEAD, "");
        assertNotNull(data);
        assertTrue(test.getReceivedEvents().isEmpty());
        assertTrue(test.getFiles().isEmpty());

        data = test.executeWebServices(RestMethod.POST, "", FilesUtils.readInternalFile("webhook/signerComplete.xml"));
        assertNotNull(data);
        assertFalse(test.getReceivedEvents().isEmpty());
        assertTrue(test.getFiles().isEmpty());

        assertEquals(1, test.getReceivedEvents().size());
        event = test.getReceivedEvents().get(0);
        assertNotNull(event);
        assertEquals("recipientStatusChanged", event.string("event"));

        response = event.json("data");
        assertNotNull(response);
        assertEquals("Luis Funes", response.string("username"));
        assertEquals("ssssssssssss", response.string("subject"));
        assertEquals("2015-07-06T11:38:16.2229309", response.string("statusDateTime"));
        assertEquals("Completed", response.string("status"));
        assertEquals("2015-07-06T06:44:57.077", response.string("signed"));
        assertEquals("2015-07-06T06:39:57.417", response.string("sent"));
        assertEquals("638bc6e3-f0b2-441e-bb99-fdec08caf3e6", response.string("envelopeId"));
        assertEquals("lefunes@slingr.io", response.string("email"));
        assertEquals("2015-07-06T06:44:34.927", response.string("delivered"));
        assertEquals("2015-07-06T06:37:56.687", response.string("created"));
        assertEquals("2015-07-06T06:44:57.077", response.string("completed"));
        assertNull(response.string("declined"));
        assertNull(response.string("declineReason"));
        assertFalse(test.getReceivedEvents().isEmpty());

        List<Json> documents = response.jsons("documents");
        assertEquals(2, documents.size());
        assertEquals("1", documents.get(0).string("documentId"));
        assertEquals("test_doc_1.pdf", documents.get(0).string("fileName"));
        assertEquals("application/pdf", documents.get(0).string("contentType"));
        assertNull(documents.get(0).string("fileId"));
        assertEquals("2", documents.get(1).string("documentId"));
        assertEquals("test_doc_2.pdf", documents.get(1).string("fileName"));
        assertEquals("application/pdf", documents.get(1).string("contentType"));
        assertNull(documents.get(1).string("fileId"));

        List<Json> recipients = response.jsons("recipients");
        assertEquals(3, recipients.size());
        for (Json recipient : recipients) {
            assertEquals("Completed", recipient.string("status"));
            assertEquals("Signer", recipient.string("type"));
            assertNull(recipient.string("declined"));
            assertNull(recipient.string("declineReason"));
        }
        assertEquals("2015-07-06T06:43:07.59", recipients.get(0).string("delivered"));
        assertEquals("lefunes@slingr.io", recipients.get(0).string("email"));
        assertEquals("Le Funes", recipients.get(0).string("name"));
        assertEquals("ea7bbdd9-b89b-45ca-b27a-2e236a791d8b", recipients.get(0).string("recipientId"));
        assertEquals("2015-07-06T06:39:56.967", recipients.get(0).string("sent"));
        assertEquals("2015-07-06T06:43:16.203", recipients.get(0).string("signed"));
        assertEquals("2015-07-06T06:40:38.24", recipients.get(1).string("delivered"));
        assertEquals("lefunes+2@slingr.io", recipients.get(1).string("email"));
        assertEquals("LEF 2", recipients.get(1).string("name"));
        assertEquals("019417be-3d35-4b1a-8325-8dd490c089ee", recipients.get(1).string("recipientId"));
        assertEquals("2015-07-06T06:39:57.153", recipients.get(1).string("sent"));
        assertEquals("2015-07-06T06:40:47.91", recipients.get(1).string("signed"));
        assertEquals("2015-07-06T06:44:34.847", recipients.get(2).string("delivered"));
        assertEquals("lefunes+3@slingr.io", recipients.get(2).string("email"));
        assertEquals("Luis E. Funes 3", recipients.get(2).string("name"));
        assertEquals("3198a88f-d91d-4ecb-a3bf-972d9de909f1", recipients.get(2).string("recipientId"));
        assertEquals("2015-07-06T06:39:57.323", recipients.get(2).string("sent"));
        assertEquals("2015-07-06T06:44:57.047", recipients.get(2).string("signed"));
        assertNull(response.bool("includeDocuments"));

        test.clearReceivedEvents();
        assertTrue(test.getReceivedEvents().isEmpty());
        test.clearFiles();
        assertTrue(test.getFiles().isEmpty());

        data = test.executeWebServices(RestMethod.POST, "", FilesUtils.readInternalFile("webhook/envelopeCompleted.xml"));
        assertNotNull(data);
        assertFalse(test.getReceivedEvents().isEmpty());
        assertFalse(test.getFiles().isEmpty());

        assertEquals(1, test.getReceivedEvents().size());
        event = test.getReceivedEvents().get(0);
        assertNotNull(event);
        assertEquals("envelopeStatusChanged", event.string("event"));

        response = event.json("data");
        assertNotNull(response);
        documents = response.jsons("documents");
        assertEquals(2, documents.size());
        assertEquals("1", documents.get(0).string("documentId"));
        assertEquals("test_doc_1.pdf", documents.get(0).string("fileName"));
        assertEquals("application/pdf", documents.get(0).string("contentType"));
        assertNotNull(documents.get(0).string("fileId"));
        assertEquals("2", documents.get(1).string("documentId"));
        assertEquals("test_doc_2.pdf", documents.get(1).string("fileName"));
        assertEquals("application/pdf", documents.get(1).string("contentType"));
        assertNotNull(documents.get(1).string("fileId"));

        files = test.getFiles();
        assertEquals(2, files.size());
        assertEquals("test_doc_1.pdf", files.get(0).getFileName());
        assertEquals("test_doc_2.pdf", files.get(1).getFileName());

        test.clearReceivedEvents();
        assertTrue(test.getReceivedEvents().isEmpty());
        test.clearFiles();
        assertTrue(test.getFiles().isEmpty());

        data = test.executeWebServices(RestMethod.POST, "", FilesUtils.readInternalFile("webhook/signerDeclined.xml"));
        assertNotNull(data);

        assertFalse(test.getReceivedEvents().isEmpty());
        assertTrue(test.getFiles().isEmpty());

        assertEquals(1, test.getReceivedEvents().size());
        event = test.getReceivedEvents().get(0);
        assertNotNull(event);
        assertEquals("recipientStatusChanged", event.string("event"));

        response = event.json("data");
        assertNotNull(response);
        documents = response.jsons("documents");
        assertEquals(3, documents.size());
        assertEquals("1", documents.get(0).string("documentId"));
        assertEquals("test_doc_1.pdf", documents.get(0).string("fileName"));
        assertEquals("application/pdf", documents.get(0).string("contentType"));
        assertNull(documents.get(0).string("fileId"));
        assertEquals("2", documents.get(1).string("documentId"));
        assertEquals("test_doc_2.pdf", documents.get(1).string("fileName"));
        assertEquals("application/pdf", documents.get(1).string("contentType"));
        assertNull(documents.get(1).string("fileId"));
        assertEquals("3", documents.get(2).string("documentId"));
        assertEquals("test_doc_3.pdf", documents.get(2).string("fileName"));
        assertEquals("application/pdf", documents.get(2).string("contentType"));
        assertNull(documents.get(2).string("fileId"));

        test.clearReceivedEvents();
        assertTrue(test.getReceivedEvents().isEmpty());
        test.clearFiles();
        assertTrue(test.getFiles().isEmpty());

        logger.info("-- END");
    }
}
