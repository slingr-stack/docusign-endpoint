package io.slingr.endpoints.docusign.entities;

import io.slingr.endpoints.utils.Json;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Created by lefunes on 08/07/15.
 */
public class EnvelopeInformation {

    private Envelope EnvelopeStatus;
    private List<Document> DocumentPDFs;

    public EnvelopeInformation() {
    }

    public EnvelopeInformation(Envelope envelope, List<Document> documents) {
        this.EnvelopeStatus = envelope;
        this.DocumentPDFs = documents;
    }

    public Envelope getEnvelope() {
        return EnvelopeStatus;
    }

    public void setEnvelope(Envelope envelope) {
        this.EnvelopeStatus = envelope;
    }

    public List<Document> getDocuments() {
        return DocumentPDFs;
    }

    public void setDocuments(List<Document> documents) {
        this.DocumentPDFs = documents;
    }

    public Json toJson(){
        final Json json = Json.map().set("includeDocuments", false);
        if(this.EnvelopeStatus != null) {
            json.merge(this.EnvelopeStatus.toJson());

            if(this.DocumentPDFs != null && !this.DocumentPDFs.isEmpty()){
                final List<Json> documents = json.jsons("documents");
                if(documents != null && !documents.isEmpty()) {
                    for (Document document : DocumentPDFs) {
                        if(StringUtils.isNotBlank(document.getName())) {
                            for (Json document1 : documents) {
                                if (StringUtils.equals(document.getName(), document1.string("name"))) {
                                    document1.merge(document.toJson());
                                    if(StringUtils.isNotBlank(document1.string("file"))){
                                        json.set("includeDocuments", true);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }

                final List<Map> documentList = new ArrayList<>();
                for (Json document : documents) {
                    documentList.add(document.toMap());
                }
                json.set("documents", documentList);
            }
        }
        return json;
    }
}
