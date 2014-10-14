package com.adobe.ReprocessJobAVSCreation;

import org.apache.sling.api.resource.ResourceResolver;

/**
 * The <code>Scene7Service</code> provides methods for interacting with the
 * official Scene7 API.
 */
public interface TriggerScene7AssetSetService{

	/**
	 * Uploads a file at a given repository path to the Scene7 server.
	 * 
	 * @param path
	 *            Path to the file in the repository
	 * @param email
	 *            E-Mail address
	 * @param password
	 *            Password
	 * @param region
	 *            Region of the Scene7 service
	 * @param userHandle
	 *            Unique user handle
	 * @param companyHandle
	 *            Unique company handle
	 * @param rootPath
	 *            Scene7 root path to upload the file to
	 * @param resolver
	 *            Sling ResourceResolver
	 * @return String 'success' if upload finished successfully or a
	 *         representation of the job handle if upload failed.
	 */
	String triggerAssetSetCreation(String path, String email, String password, String region, String userHandle, String companyHandle, String rootPath, ResourceResolver resolver);
	
}
