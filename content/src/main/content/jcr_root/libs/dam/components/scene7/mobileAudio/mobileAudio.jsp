<%--
  Copyright 1997-2008 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

--%><%@page session="false" 
            import="java.net.URLEncoder,
                    java.util.Locale,
                    java.util.ResourceBundle, 
                    com.day.cq.dam.api.Asset,
                    com.day.cq.i18n.I18n, 
                    com.day.cq.wcm.api.WCMMode,
                    com.day.cq.wcm.api.components.DropTarget"
%><%@include file="/libs/foundation/global.jsp"%><%
%><%
    if((WCMMode.fromRequest(request) == WCMMode.EDIT) || (WCMMode.fromRequest(request) == WCMMode.DESIGN) ) {
    %><cq:includeClientLib categories="cq.dam.scene7" /><%                
    }

    Locale pageLocale = currentPage.getLanguage(true);
    ResourceBundle resourceBundle = slingRequest.getResourceBundle(pageLocale);
    I18n i18n = new I18n(resourceBundle);

    String width = properties.get("width",currentStyle.get("defaultWidth", "-1"));    
    String height = properties.get("height",currentStyle.get("defaultHeight", "-1")); 

    String fileReference = properties.get("fileReference",String.class);
    if ((fileReference == null) || (resourceResolver.getResource(fileReference) == null)) {
        %><div class="<%= DropTarget.CSS_CLASS_PREFIX + "video" + (WCMMode.fromRequest(request) == WCMMode.EDIT ? " cq-video-placeholder" : "") %>"></div><%
    } else {
        Asset asset = resourceResolver.getResource(fileReference).adaptTo(Asset.class);
        if(asset != null) {
            String id = asset.getMetadataValue("dam:scene7ID");
            String publishStatus = ( !("".equals(asset.getMetadataValue("dam:scene7FileStatus"))) ?  asset.getMetadataValue("dam:scene7FileStatus"):"PublishComplete");

            // calculate some default values according to the correct aspect ratio
            long origWidth = (!"".equals(asset.getMetadataValue("tiff:ImageWidth")) ? (long)Double.parseDouble(asset.getMetadataValue("tiff:ImageWidth")) : 500);
            long origHeight = (!"".equals(asset.getMetadataValue("tiff:ImageLength")) ? (long)Double.parseDouble(asset.getMetadataValue("tiff:ImageLength")) : 500);
            int widthInt = Integer.parseInt(width);
            int heightInt = Integer.parseInt(height);
            if(widthInt < 0 && heightInt < 0) {
                width = "200";
                height = "200";
            }else if(widthInt < 0) {
                width = Long.toString(heightInt * origWidth / origHeight);
            }else if(heightInt < 0) {
                height = Long.toString(widthInt * origHeight / origWidth);
            }
           
            // if the asset is not yet set _or_ the upload/publish has not yet completed
            if ("".equals(id) || !"PublishComplete".equals(publishStatus)) {
                //asset not uploaded

                if((WCMMode.fromRequest(request) == WCMMode.EDIT) || (WCMMode.fromRequest(request) == WCMMode.DESIGN) ) {
                    String uploadText = i18n.get("Publish to Scene7", "Scene7 upload link text");                                       
                    if(!"PublishComplete".equals(publishStatus))               
                        uploadText = i18n.get("Check Scene7 Publish Status", "Check Publish Status link text");

                %>
                <div class="not-published" style="position:relative;width: <%= width %>px; height: <%= height %>px;">
                    <img src="<%= fileReference %>/jcr:content/renditions/cq5dam.thumbnail.48.48.png"/>
                    <div class="overlay" style="width: <%= width %>px; height: <%= height %>px;"></div>
                    <div style="width: <%= width %>px; height: <%= height %>px;">
                        <a class="publish-button" id="<%=resource.getPath().replace("/", "-")%>-publishLink" onclick="CQ.scene7.triggerWorkflowFromViewer('<%=resource.getPath().replace("/", "-")%>', '<%=fileReference%>');return false;"><%=uploadText %></a>
                    </div>
                </div>
                <%
                } else {
                %>
                    <img src="<%= fileReference %>" width="<%= width %>" height="<%= height %>"/>
                <%
                }
            } else {
                //embed
//Object md = asset.getMetadata();
//System.out.println("md:" + md);
                String assetValue = asset.getMetadataValue("dam:scene7Folder")+"/"+asset.getMetadataValue("dam:scene7Name");
                
                // Had problems with using reg exp in context of CQ component - so trying this instead...
                int idx = assetValue.lastIndexOf(".");
                boolean found = (idx == assetValue.length()-4);
                if (!found) {
                    assetValue += ".mp3"; // Assume mp4 if no extension found
                }
                
                String company = assetValue.split("/")[0];
                String viewerUrl = asset.getMetadataValue("dam:scene7Domain") + "skins/Scene7SharedAssets/desktopViewers-AS3/GenericVideo.swf?company=" + company + "&locale=en&contentRoot=" + asset.getMetadataValue("dam:scene7Domain") + "skins/&videoserverurl=/e2/&serverUrl=" + asset.getMetadataValue("dam:scene7Domain") + "is/image/&stageSize=" + width + "," + height + "&cache=off";
                String directUrl = asset.getMetadataValue("dam:scene7Domain") + "is/content/" + assetValue;
                %>
<%
out.println(directUrl);


%>

<br>

<audio controls>
<source src="<%= directUrl %>" >
</audio>

<%} 

}


}%>