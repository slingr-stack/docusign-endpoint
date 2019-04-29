package io.slingr.endpoints.docusign.entities;

import io.slingr.endpoints.utils.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Created by lefunes on 08/07/15.
 */
public class Recipient {
    private String RecipientId;
    private String Type;
    private String UserName;
    private String Email;
    private String Status;
    private Integer RoutingOrder;
    private String Sent;
    private String Delivered;
    private String Signed;
    private String Declined;
    private String DeclineReason;
    private List<TabStatus> TabStatuses;

    public Recipient() {
    }

    public Recipient(String recipientId, String type, String name, String email, String status, Integer routingOrder, String sent, String delivered, String signed, String declined, String declineReason, List<TabStatus> tabStatuses) {
        this.RecipientId = recipientId;
        this.Type = type;
        this.UserName = name;
        this.Email = email;
        this.Status = status;
        this.RoutingOrder = routingOrder;
        this.Sent = sent;
        this.Delivered = delivered;
        this.Signed = signed;
        this.Declined = declined;
        this.DeclineReason = declineReason;
        this.TabStatuses = tabStatuses;
    }

    public String getRecipientId() {
        return RecipientId;
    }

    public void setRecipientId(String recipientId) {
        this.RecipientId = recipientId;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        this.Type = type;
    }

    public String getName() {
        return UserName;
    }

    public void setName(String name) {
        this.UserName = name;
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

    public Integer getRoutingOrder() {
        return RoutingOrder;
    }

    public void setRoutingOrder(Integer routingOrder) {
        this.RoutingOrder = routingOrder;
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

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public List<TabStatus> getTabStatuses() {
        return TabStatuses;
    }

    public void setTabStatuses(List<TabStatus> tabStatuses) {
        this.TabStatuses = tabStatuses;
    }

    public Json toJson(){
        final Json json = Json.map()
                .setIfNotEmpty("recipientId", this.RecipientId)
                .setIfNotEmpty("type", this.Type)
                .setIfNotEmpty("name", this.UserName)
                .setIfNotEmpty("email", this.Email)
                .setIfNotEmpty("status", this.Status)
                .setIfNotEmpty("routingOrder", this.RoutingOrder)
                .setIfNotEmpty("sent", this.Sent)
                .setIfNotEmpty("delivered", this.Delivered)
                .setIfNotEmpty("signed", this.Signed)
                .setIfNotEmpty("declined", this.Declined)
                .setIfNotEmpty("declineReason", this.DeclineReason)
                ;

        if(this.TabStatuses != null && !this.TabStatuses.isEmpty()){
            final List<Map> tabStatuses = new ArrayList<>();
            for (TabStatus tabStatus : TabStatuses) {
                tabStatuses.add(tabStatus.toJson().toMap());
            }
            json.setIfNotEmpty("tabStatuses", tabStatuses);
        }

        return json;
    }
}
