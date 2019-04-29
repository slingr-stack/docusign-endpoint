package io.slingr.endpoints.docusign.entities;

import io.slingr.endpoints.utils.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Created by lefunes on 08/07/15.
 */
public class Envelope {

    private String EnvelopeID;
    private String Subject;
    private String UserName;
    private String Email;
    private String Status;
    private String TimeGenerated;
    private String Created;
    private String Sent;
    private String Delivered;
    private String Signed;
    private String Completed;
    private String Declined;
    private String DeclineReason;
    private List<Recipient> RecipientStatuses;
    private List<Document> DocumentStatuses;

    public Envelope() {
    }

    public Envelope(String envelopeId, String subject, String username, String email, String status, String statusDateTime, String created, String sent, String delivered, String signed, String completed, String declined, String declineReason, List<Recipient> recipients, List<Document> documents) {
        this.EnvelopeID = envelopeId;
        this.Subject = subject;
        this.UserName = username;
        this.Email = email;
        this.Status = status;
        this.TimeGenerated = statusDateTime;
        this.Created = created;
        this.Sent = sent;
        this.Delivered = delivered;
        this.Signed = signed;
        this.Completed = completed;
        this.Declined = declined;
        this.DeclineReason = declineReason;
        this.RecipientStatuses = recipients;
        this.DocumentStatuses = documents;
    }

    public String getEnvelopeId() {
        return EnvelopeID;
    }

    public void setEnvelopeId(String envelopeId) {
        this.EnvelopeID = envelopeId;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        this.Subject = subject;
    }

    public String getUsername() {
        return UserName;
    }

    public void setUsername(String username) {
        this.UserName = username;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        this.Status = status;
    }

    public String getStatusDateTime() {
        return TimeGenerated;
    }

    public void setStatusDateTime(String statusDateTime) {
        this.TimeGenerated = statusDateTime;
    }

    public String getCreated() {
        return Created;
    }

    public void setCreated(String created) {
        this.Created = created;
    }

    public String getSent() {
        return Sent;
    }

    public void setSent(String sent) {
        this.Sent = sent;
    }

    public String getDelivered() {
        return Delivered;
    }

    public void setDelivered(String delivered) {
        this.Delivered = delivered;
    }

    public String getSigned() {
        return Signed;
    }

    public void setSigned(String signed) {
        this.Signed = signed;
    }

    public String getCompleted() {
        return Completed;
    }

    public void setCompleted(String completed) {
        this.Completed = completed;
    }

    public String getDeclined() {
        return Declined;
    }

    public void setDeclined(String declined) {
        this.Declined = declined;
    }

    public String getDeclineReason() {
        return DeclineReason;
    }

    public void setDeclineReason(String declineReason) {
        this.DeclineReason = declineReason;
    }

    public List<Recipient> getRecipients() {
        return RecipientStatuses;
    }

    public void setRecipients(List<Recipient> recipients) {
        this.RecipientStatuses = recipients;
    }

    public List<Document> getDocuments() {
        return DocumentStatuses;
    }

    public void setDocuments(List<Document> documents) {
        this.DocumentStatuses = documents;
    }

    public Json toJson(){
        Json json = Json.map()
                .setIfNotEmpty("envelopeId", this.EnvelopeID)
                .setIfNotEmpty("subject", this.Subject)
                .setIfNotEmpty("username", this.UserName)
                .setIfNotEmpty("email", this.Email)
                .setIfNotEmpty("status", this.Status)
                .setIfNotEmpty("statusDateTime", this.TimeGenerated)
                .setIfNotEmpty("created", this.Created)
                .setIfNotEmpty("sent", this.Sent)
                .setIfNotEmpty("delivered", this.Delivered)
                .setIfNotEmpty("signed", this.Signed)
                .setIfNotEmpty("completed", this.Completed)
                .setIfNotEmpty("declined", this.Declined)
                .setIfNotEmpty("declineReason", this.DeclineReason)
                ;
        if(this.RecipientStatuses != null && !this.RecipientStatuses.isEmpty()){
            final List<Map> recipients = new ArrayList<>();
            for (Recipient recipient : RecipientStatuses) {
                recipients.add(recipient.toJson().toMap());
            }
            json.setIfNotEmpty("recipients", recipients);
        }
        if(this.DocumentStatuses != null && !this.DocumentStatuses.isEmpty()){
            final List<Map> documents = new ArrayList<>();
            for (Document document : DocumentStatuses) {
                documents.add(document.toJson().toMap());
            }
            json.setIfNotEmpty("documents", documents);
        }

        return json;
    }
}
