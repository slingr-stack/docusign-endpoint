package io.slingr.endpoints.docusign.entities;

import io.slingr.endpoints.utils.Json;

/**
 * Created by lefunes on 16/07/15.
 */
public class TabStatus {
    private String TabType;
    private String Status;
    private String XPosition;
    private String YPosition;
    private String TabLabel;
    private String TabName;
    private String TabValue;
    private String DocumentID;
    private String PageNumber;
    private String OriginalValue;
    private String CustomTabType;
    private String ValidationPattern;
    private String RoleName;

    public TabStatus() {
    }

    public TabStatus(String tabType, String status, String xPosition, String yPosition, String tabLabel, String tabName, String tabValue, String documentID, String pageNumber, String originalValue, String customTabType, String validationPattern, String roleName) {
        this.TabType = tabType;
        this.Status = status;
        this.XPosition = xPosition;
        this.YPosition = yPosition;
        this.TabLabel = tabLabel;
        this.TabName = tabName;
        this.TabValue = tabValue;
        this.DocumentID = documentID;
        this.PageNumber = pageNumber;
        this.OriginalValue = originalValue;
        this.CustomTabType = customTabType;
        this.ValidationPattern = validationPattern;
        this.RoleName = roleName;
    }

    public String getTabType() {
        return TabType;
    }

    public void setTabType(String tabType) {
        TabType = tabType;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getXPosition() {
        return XPosition;
    }

    public void setXPosition(String xPosition) {
        this.XPosition = xPosition;
    }

    public String getYPosition() {
        return YPosition;
    }

    public void setYPosition(String yPosition) {
        this.YPosition = yPosition;
    }

    public String getTabLabel() {
        return TabLabel;
    }

    public void setTabLabel(String tabLabel) {
        TabLabel = tabLabel;
    }

    public String getTabName() {
        return TabName;
    }

    public void setTabName(String tabName) {
        TabName = tabName;
    }

    public String getDocumentID() {
        return DocumentID;
    }

    public void setDocumentID(String documentID) {
        DocumentID = documentID;
    }

    public String getPageNumber() {
        return PageNumber;
    }

    public void setPageNumber(String pageNumber) {
        PageNumber = pageNumber;
    }

    public String getTabValue() {
        return TabValue;
    }

    public void setTabValue(String tabValue) {
        TabValue = tabValue;
    }

    public String getOriginalValue() {
        return OriginalValue;
    }

    public void setOriginalValue(String originalValue) {
        OriginalValue = originalValue;
    }

    public String getCustomTabType() {
        return CustomTabType;
    }

    public void setCustomTabType(String customTabType) {
        CustomTabType = customTabType;
    }

    public String getValidationPattern() {
        return ValidationPattern;
    }

    public void setValidationPattern(String validationPattern) {
        ValidationPattern = validationPattern;
    }

    public String getRoleName() {
        return RoleName;
    }

    public void setRoleName(String roleName) {
        RoleName = roleName;
    }

    public Json toJson(){
        final Json json = Json.map()
                .setIfNotEmpty("tabType", this.TabType)
                .setIfNotEmpty("tabName", this.TabName)
                .setIfNotEmpty("tabValue", this.TabValue)
                .setIfNotEmpty("tabLabel", this.TabLabel)
                .setIfNotEmpty("status", this.Status)
                .setIfNotEmpty("xPosition", this.XPosition)
                .setIfNotEmpty("yPosition", this.YPosition)
                .setIfNotEmpty("documentID", this.DocumentID)
                .setIfNotEmpty("pageNumber", this.PageNumber)
                .setIfNotEmpty("originalValue", this.OriginalValue)
                .setIfNotEmpty("customTabType", this.CustomTabType)
                .setIfNotEmpty("validationPattern", this.ValidationPattern)
                .setIfNotEmpty("roleName", this.RoleName)
                ;

        return json;
    }
}
