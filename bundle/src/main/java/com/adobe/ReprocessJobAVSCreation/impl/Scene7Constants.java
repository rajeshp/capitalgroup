package com.adobe.ReprocessJobAVSCreation.impl;


/**
 * defines constants, such as node types and
 * property names for all things scene7.
 *             
 */
public class Scene7Constants {
 
	private Scene7Constants() {
        // static access only
    }
 
    // ----------------------------------------------< s7 asset properties >

    public static final String PN_S7_NAME 			 = "dam:scene7Name";			// S7 generated asset Name
    public static final String PN_S7_UPLOAD_FILENAME = "dam:scene7UploadFileName";	// Generated Upload filename (also used for subsequent re-upload)
    public static final String PN_S7_TYPE 		= "dam:scene7Type";					// S7 asset type 
    public static final String PN_S7_ASSET_ID 	= "dam:scene7ID";					// S7 Asset handle
    public static final String PN_S7_COMPANY_ID = "dam:scene7CompanyID";			// S7 Company Handle
    public static final String PN_S7_FILE	 	= "dam:scene7File";					// S7 Asset specifier for Viewer components
    public static final String PN_S7_FILE_STATUS= "dam:scene7FileStatus";			// S7 upload/publish status (see values below)
    public static final String PN_S7_FOLDER 	= "dam:scene7Folder";				// S7 Folder in which the asset resides
    public static final String PN_S7_DOMAIN 	= "dam:scene7Domain";				// S7 Server
    public static final String PN_S7_PATH 		= "dam:scene7Path";					// S7 Publish Server asset path
    public static final String PN_S7_UPLOAD_TIMESTAMP = "dam:scene7UploadTimeStamp";// local upload complete timestamp
    public static final String PN_S7_LAST_MODIFIED    = "dam:scene7LastModified";	// S7 LastModified timestamp
    public static final String PN_S7_API_SERVER 	  = "dam:scene7APIServer";		// S7 WebServices server
       
    // ----------------------------------------------< PN_S7_FILE_STATUS prop values >
    
    public static final String PV_S7_UPLOAD_START 		= "UploadStart";
    public static final String PV_S7_PUBLISH_QUEUED 	= "PublishQueued";
    public static final String PV_S7_PUBLISH_START 		= "PublishStart";
    public static final String PV_S7_PUBLISH_COMPLETE 	= "PublishComplete";
    public static final String PV_S7_PUBLISH_FAILED 	= "PublishFailed"; 
    public static final String PV_S7_UPLOAD_FAILED 		= "UploadFailed";
  

}
