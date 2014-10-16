package com.adobe.ReprocessJobAVSCreation.impl;

import com.adobe.ReprocessJobAVSCreation.TriggerScene7AssetSetService;
import com.day.cq.dam.api.Asset;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@Component(
		metatype = true,
		label = "%cq.scene7.service.name",
		description = "%cq.scene7.service.description"
		)
@Service
public class TriggerScene7AssetSetServiceImpl extends Scene7APIClient implements TriggerScene7AssetSetService {

	/** default log */
	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final String SPS = ".sps";
	private static final String IPS = ".ips";

	// Timeout if this job for some reason does not clear the queue, generally S7 will timeout the upload job after 30 minutes,
	// we are more aggressive in the workflow
	private final static int ACTIVE_JOB_TIMEOUT = 15*60*1000;
	// poll interval between checks that the upload/publish jobs have cleared active job queue in s7
	private final static long ACTIVE_JOB_WAITING_INTERVAL = 10*1000;

	private static HashMap<String, String> domains = new HashMap<String, String>();
	static {
		domains.put("northamerica-enterprise" + SPS, "https://s7sps1apissl.scene7.com");
		domains.put("northamerica-enterprise" + IPS, "http://s7ips1.scene7.com");
		domains.put("northamerica-smallmedium" + SPS, "https://s7sps2apissl.scene7.com");
		domains.put("northamerica-smallmedium" + IPS, "http://s7ips2.scene7.com");
		domains.put("europe" + SPS, "https://s7sps3apissl.scene7.com");
		domains.put("europe" + IPS, "http://s7ips3.scene7.com");
		domains.put("asia" + SPS, "http://s7sps5api.scene7.com");
		domains.put("asia" + IPS, "http://s7ips5.scene7.com");
	}

	
	public String triggerAssetSetCreation(String path, String email, String password, String region, String userHandle,
			String companyHandle, String rootPath, ResourceResolver resolver) {
		HttpClient client = new HttpClient();

		Node jcrContent = resolver.getResource(path + "/jcr:content/metadata").adaptTo(Node.class);

        long thumbnailTimeInMillis=0;



		String result = "failed";

		try {

            if(jcrContent.hasProperty("thumbnailTime"))
            {
                log.info("Thumbnail Time in String : "+ jcrContent.getProperty("thumbnailTime").getString());
                thumbnailTimeInMillis =  Integer.parseInt(jcrContent.getProperty("thumbnailTime").getString()) * 1000;
            }


			String reprocessFilename = getReprocessFilename(path,resolver,jcrContent);
	        String reprocessJobname = "Reprocess_"+reprocessFilename;
	        
	        String publishStatus = waitOnPublishComplete( path,resolver,jcrContent);

            log.info("********Reprocess Publish status = "+publishStatus);
	        
	        if( "success".equalsIgnoreCase(publishStatus)){
	        	String assetHandle = getAssetHandle(path,resolver,jcrContent);
		        result = submitReprocessJob(email, password, region, companyHandle, reprocessJobname, userHandle, assetHandle, thumbnailTimeInMillis);
	        }
	    
		}catch (Exception e) {
			log.error("Submit scene7 Reprocess Job  for asset (" + path + ") failed", e);
		}
		return result;

	}
	
	private synchronized String waitOnPublishComplete(String path, ResourceResolver resolver, Node jcrContent) {

		AssetStatusService assetStatusService = new AssetStatusService(path, resolver, jcrContent);
		long start = System.currentTimeMillis();
		while (!assetStatusService.isAssetPublished(path, resolver, jcrContent)) {
			try {
				if (System.currentTimeMillis() - start > ACTIVE_JOB_TIMEOUT) {
					log.warn("Reprocess Job : Timed out waiting for active Scene7 Publish job complete");
					return "failed";
				}
				this.wait(ACTIVE_JOB_WAITING_INTERVAL);
			} catch (InterruptedException e) {
				log.error("Reprocess Job : error waiting for Scene7 Publish job to complete", e);
				return "failed";
			}
		}
		log.info("Reprocess Job : wait for Scene7 Publish job : completed");
		return "success";
	}

    private String getReprocessFilename(String path, ResourceResolver resolver, Node jcrContent) throws RepositoryException
    {
        Asset asset = resolver.getResource(path).adaptTo(Asset.class);
        String reprocessFilename = asset.getMetadataValue(Scene7Constants.PN_S7_UPLOAD_FILENAME);
        if ("".equals(reprocessFilename))
        {
            SimpleDateFormat ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
            String now = ISO8601Local.format(new Date());

            reprocessFilename = now+"_"+asset.getName();

        }
        return reprocessFilename;
    }
    
    protected String getAssetHandle(String path, ResourceResolver resolver, Node jcrContent) throws RepositoryException {
    	Asset asset = resolver.getResource(path).adaptTo(Asset.class);
        String assetId = asset.getMetadataValue(Scene7Constants.PN_S7_ASSET_ID);
        
        return assetId;
	}

	private String submitReprocessJob( String email, String password, String region, String companyHandle, String jobName, String userHandle, String assetHandle, long thumbnailTimeInMillis) {
		HttpClient client = new HttpClient();
		String appSettingsTypeHandle = getApplicationPropertyHandle( email, password, region);
		if (appSettingsTypeHandle == null) {
			return "failed";
		}
		
		try {

            log.info("*********ThumbnailTimeInMillis = "+thumbnailTimeInMillis);

			PostMethod request = new PostMethod(domains.get(region + SPS) + "/scene7/services/IpsApiService");
			request.addRequestHeader("SOAPAction", "submitJob");
			RequestEntity entity = new StringRequestEntity(getRequestBody("submitJob", email, password,
				"<companyHandle>" + companyHandle + "</companyHandle>" +
				"<userHandle>" + userHandle + "</userHandle>" +
				"<jobName>" + jobName + "</jobName>" +
				"<locale>en-US</locale>" +
					
				  "<reprocessAssetsJob>" + 
				    "<assetHandleArray>" +
				      "<items>"+ assetHandle +"</items>" +
				    "</assetHandleArray>" +
				    "<readyForPublish>true</readyForPublish>" +
				    "<createMask>false</createMask>" +
				    "<mediaOptions>" +
				      "<videoEncodingPresetsArray>" +
				        "<items>ps|2521985</items>" +
				        "<items>ps|2521986</items>" +
				        "<items>ps|2520964</items>" +
				        "<items>ps|2520962</items>" +
				        "<items>ps|2521970</items>" +
				        "<items>ps|2522962</items>" +
				        "<items>ps|2521975</items>" +
				        "<items>ps|2522964</items>" +
				        "<items>ps|2521974</items>" +
				        "<items>ps|2520963</items>" +
				        "<items>ps|2521972</items>" +
				        "<items>ps|2522961</items>" +
				        "<items>ps|2521969</items>" +
				        "<items>ps|2521967</items>" +
				        "<items>ps|2521973</items>" +
				        "<items>ps|2522963</items>" +
				        "<items>ps|2521968</items>" +
				        "<items>ps|2521971</items>" +
				        "<items>ps|13335</items>" +
				        "<items>ps|2521982</items>" +
				        "<items>ps|2521977</items>" +
				        "<items>ps|2522967</items>" +
				        "<items>ps|2522971</items>" +
				        "<items>ps|2521976</items>" +
				        "<items>ps|2522973</items>" +
				        "<items>ps|2521978</items>" +
				        "<items>ps|2520965</items>" +
				        "<items>ps|2522974</items>" +
				        "<items>ps|2521981</items>" +
				        "<items>ps|2521979</items>" +
				        "<items>ps|2521983</items>" +
				        "<items>ps|2522965</items>" +
				        "<items>ps|2522972</items>" +
				        "<items>ps|2522969</items>" +
				        "<items>ps|2521980</items>" +
				        "<items>ps|2522968</items>" +
				        "<items>ps|2522966</items>" +
				        "<items>ps|2521984</items>" +
				        "<items>ps|2522970</items>" +
				      "</videoEncodingPresetsArray>" +
				    " <thumbnailOptions> <thumbnailTime>"+thumbnailTimeInMillis+"</thumbnailTime> </thumbnailOptions>  </mediaOptions>" +
				    "<emailSetting>All</emailSetting>" +
				    "<excludeMasterVideoFromAVS>true</excludeMasterVideoFromAVS>"+
				  "</reprocessAssetsJob>"
					),
					CONTENTTYPE, CHARSET);
			request.setRequestEntity(entity);

			client.executeMethod(request);
			log.info(request.getResponseBodyAsString());
			Document document = getResponseDOM(request.getResponseBodyAsStream());
			
			if (document != null) {
				NodeList list = document.getElementsByTagName("jobHandle");
				if (list.getLength() > 0 && list.item(0).getFirstChild() != null) {
					return "success";
				}
			}
			
		} catch (HttpException e) {
			log.error("Error retrieving Scene7 published servers" + e.getMessage());
		} catch (IOException e) {
			log.error("Error retrieving Scene7 published servers" + e.getMessage());
		}
		return "failed";

	}
	
	private String getApplicationPropertyHandle( String email, String password, String region) {
		HttpClient client = new HttpClient();
		try {
			PostMethod request = new PostMethod(domains.get(region + SPS) + "/scene7/services/IpsApiService");
			request.addRequestHeader("SOAPAction", "getPropertySetTypes");
			RequestEntity entity = new StringRequestEntity(getRequestBody("getPropertySetTypes", email, password, ""), CONTENTTYPE, CHARSET);
			request.setRequestEntity(entity);

			client.executeMethod(request);
			log.info(request.getResponseBodyAsString());
			Document document = getResponseDOM(request.getResponseBodyAsStream());

			if (document != null) {
				NodeList list = document.getElementsByTagName("items");
				for (int i = 0; i < list.getLength(); i++) {
					NodeList namesNodes = ((Element) list.item(i)).getElementsByTagName("name");
					if (namesNodes.getLength() > 0 && "Adobe.SPS.ApplicationSetting".equals(namesNodes.item(0).getFirstChild().getNodeValue())){
						NodeList typeHandleNodes = ((Element) list.item(i)).getElementsByTagName("typeHandle");
						if (typeHandleNodes.getLength() > 0 && typeHandleNodes.item(0).getFirstChild() != null)
							return typeHandleNodes.item(0).getFirstChild().getNodeValue();
					}
				}
			}
		} catch (HttpException e) {
			log.error("Error SPS application setting"+e);
		} catch (IOException e) {
			log.error("Error SPS application setting"+e);
		}
		return null;
	}


    private int getThumbnailTime(String assetPath)
    {

        int thumbnailTimeInMillis = 0;

        log.info("Asset Path = "+assetPath);

        return thumbnailTimeInMillis;
    }


}
