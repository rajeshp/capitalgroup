package com.adobe.ReprocessJobAVSCreation.impl;

import com.day.cq.dam.api.Asset;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * <code>ActiveJobsService</code> wraps access to Scene7 Active jobs request
 */
public class AssetStatusService{
	/** default log */
	private final Logger log = LoggerFactory.getLogger(getClass());
	private String path;
	private ResourceResolver resolver;
	private Node jcrContent;

	public AssetStatusService(String path, ResourceResolver resolver, Node jcrContent) {
		this.path = path;
		this.resolver = resolver;
		this.jcrContent = jcrContent;
	}

	/**
	 * @param jobHandle
	 * @return true if active job with this jobHandle or orginalName is active
	 */
	public Boolean isAssetPublished(String path, ResourceResolver resolver, Node jcrContent) {
		String scene7FileStatus="";
		
	    try {
	    	scene7FileStatus = getAssetPublishStatus( path, resolver, jcrContent);
		} catch (RepositoryException e) {
			log.error(e.getMessage(), e);
		}

		if (scene7FileStatus != null && "PublishComplete".equalsIgnoreCase(scene7FileStatus))
		{
		log.info("Current Scene 7 Transaction Status : " + scene7FileStatus);
		return true;
		}

		if (scene7FileStatus != null && !"PublishComplete".equalsIgnoreCase(scene7FileStatus))
			log.info("Current Scene 7 Transaction Status : " + scene7FileStatus);
		
		if (scene7FileStatus == null)
			log.error("isAssetPublished failed, invalid service response");

		return false;
	}

	protected String getAssetPublishStatus(String path, ResourceResolver resolver, Node jcrContent) throws RepositoryException {
				
	    	Asset asset = resolver.getResource(path).adaptTo(Asset.class);
	        String scene7FileStatus = asset.getMetadataValue(Scene7Constants.PN_S7_FILE_STATUS);

	        return scene7FileStatus;
	}
}
