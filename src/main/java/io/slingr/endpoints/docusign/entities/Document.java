package io.slingr.endpoints.docusign.entities;

import io.slingr.endpoints.utils.EmailUtils;
import io.slingr.endpoints.utils.Json;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by lefunes on 08/07/15.
 */
public class Document {
    private String ID;
    private String Name;
    private String PDFBytes;
    private String test;

    public Document() {
    }

    public Document(String documentId, String name, String file, String test) {
        this.ID = documentId;
        this.Name = name;
        this.PDFBytes = file;
        this.test = test;
    }

    public String getDocumentId() {
        return ID;
    }

    public void setDocumentId(String documentId) {
        this.ID = documentId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getFile() {
        return PDFBytes;
    }

    public void setFile(String file) {
        this.PDFBytes = file;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public Json toJson(){
        Json json = Json.map()
                .setIfNotEmpty("documentId", this.ID)
                .setIfNotEmpty("name", this.Name);

        if(StringUtils.isNotBlank(this.Name)){
            json.setIfNotEmpty("contentType", EmailUtils.getContentType(null, this.Name));
        }
        if(StringUtils.isNotBlank(this.PDFBytes)){
            json.set("base64", true).set("file", this.PDFBytes);
        }
        if(StringUtils.isNotBlank(this.test)){
            json.set("test", this.test.equalsIgnoreCase("true"));
        }
        return json;
    }
}
