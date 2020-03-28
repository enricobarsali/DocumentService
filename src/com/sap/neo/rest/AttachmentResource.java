package com.sap.neo.rest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;

import com.sap.neo.ds.DocDerviceUtils;



@Path("/attachment")
public class AttachmentResource {
	
	private String folder = "GoSignDocuments";
	private String uniqueName = "GoSign";
	private String secretKey = "e35f3f29-7595-498a-bca5-d99af4e47561";
    
    @GET
    @Path("ping")
    public String getServerTime() {
        return "received ping on "+new Date().toString();
    }
    
    @SuppressWarnings("finally")
	@GET
    @Produces({MediaType.APPLICATION_JSON})  //add MediaType.APPLICATION_XML if you want XML as well (don't forget @XmlRootElement)
    public Attachment getMessage(@QueryParam("CMISID") String CMISID) throws Exception{

		Attachment a = new Attachment();
    	Session openCmisSession = new DocDerviceUtils().getSession(uniqueName, secretKey);
		try{
			if (openCmisSession != null) {
				Document doc = (Document) openCmisSession.getObject(CMISID);
				ContentStream content = doc.getContentStream();
				InputStream stream = content.getStream();

				int bufferSize = 1024;
				char[] buffer = new char[bufferSize];
				final StringBuilder out = new StringBuilder();
				Reader in = new InputStreamReader(stream, StandardCharsets.UTF_8);
				int charsRead;
				while((charsRead = in.read(buffer, 0, buffer.length)) > 0) {
				    out.append(buffer, 0, charsRead);
				}
				
				String pipedCustomProperties = doc.getDescription();
				String[] customProperties = pipedCustomProperties.split("\\|");
				a.setExternalId(customProperties[0]);
				a.setDocVersion(customProperties[1]);
				a.setAttachType(customProperties[2]);
				a.setTitle(customProperties[3]);
				a.setParent_externalId(customProperties[4]);
				a.setParent_docVersion(customProperties[5]);
				
				a.setOriginalFname(content.getFileName()); //"originalFname": "1000001127.PDF",
				a.setMimeType(content.getMimeType());
				a.setBytes(out.toString());
				a.setDocVersion("01");
			} else {
				a.setBytes("ERROR");
				a.setDocVersion("99");
			}
		}catch(Exception e){
			a.setBytes(e.getMessage().toString());
			a.setDocVersion("99");
		}finally{
	    	return a;
		}
    }
    
    @SuppressWarnings("finally")
	@POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})    
    @Path("/post")
    public ResponseBody postMessage(Attachment m) throws Exception{
    	Session openCmisSession = new DocDerviceUtils().getSession(uniqueName, secretKey);
    	String message = "";
    	String CMISID = "";
    	String status = "S";
		Folder root = openCmisSession.getRootFolder();
		// create a new folder
		try {
			Map<String, String> newFolderProps = new HashMap<String, String>();
			newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			newFolderProps.put(PropertyIds.NAME, folder);
			root.createFolder(newFolderProps);
			message = message + "Folder "+ folder + " created in root." + "\r\n";
		} catch (CmisNameConstraintViolationException e) {
			message = message + "Folder "+ folder + " already exists." + "\r\n";
		}
		// create a new file in the folder
		List<Tree<FileableCmisObject>> folderTree = root.getFolderTree(1);
		Tree<FileableCmisObject> treeo = folderTree.get(0);
		Folder f = (Folder)treeo.getItem();
		try {
   	
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
	
//    		"originalFname": "1000001127.PDF",
			properties.put(PropertyIds.NAME, m.getOriginalFname()); //"originalFname": "1000001127.PDF",

//	        "externalId": "APS1000001127",
//	        "docVersion": "91",
//	        "attachType": "PDF",
//	        "title": "1000001127.PDF",
//    		"parent_externalId": "APS1000001127",
//	        "parent_docVersion": "91"
			StringBuilder sb = new StringBuilder();
			sb.append(m.getExternalId()); sb.append("|");
			sb.append(m.getDocVersion()); sb.append("|");
			sb.append(m.getAttachType()); sb.append("|");
			sb.append(m.getTitle()); sb.append("|");
			sb.append(m.getParent_externalId()); sb.append("|");
			sb.append(m.getParent_docVersion());
			
			properties.put(PropertyIds.DESCRIPTION, sb.toString());

            //https://chemistry.apache.org/docs/cmis-samples/samples/properties/index.html#setting-properties
            // add two secondary types
            List<String> secondaryTypes = new ArrayList<String>();
            secondaryTypes.add("custom:externalId");
            secondaryTypes.add("custom:docVersion");
            secondaryTypes.add("custom:attachType");
            secondaryTypes.add("custom:title");
            secondaryTypes.add("custom:mimeType");
            secondaryTypes.add("custom:parent_externalId");
            secondaryTypes.add("custom:parent_docVersion");

            properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, secondaryTypes);

            // set secondary type properties
            properties.put("externalId", m.getExternalId());
            properties.put("docVersion", m.getDocVersion());
            properties.put("attachType", m.getAttachType());
            properties.put("title", m.getTitle());
            properties.put("mimeType", m.getMimeType());
            properties.put("parent_externalId", m.getParent_externalId());
            properties.put("parent_docVersion", m.getParent_docVersion());
			
			
			byte[] encoded;
			encoded = m.getBytes().getBytes("UTF-8"); //"bytes": "JVBERi0xLj.......
			
			InputStream inputStream = new ByteArrayInputStream(encoded);
			ContentStream contentStream1 = openCmisSession.getObjectFactory()
					.createContentStream(m.getOriginalFname(), -1, m.getMimeType(), inputStream);//"mimeType": "application/pdf",
	
			Document cmisDocument = root.createDocument(properties, contentStream1, VersioningState.NONE);
			inputStream.close();
	
			CMISID = cmisDocument.getId();
			message = message + "File " + m.getOriginalFname() + " stored with ID: " + CMISID;
	    	
		} catch (CmisNameConstraintViolationException e) {
			status = "E";
			message = message + "File "+ m.getOriginalFname() + " already exists in the folder "+ f.getName() + ".\n";
		} catch (UnsupportedEncodingException e1) {
			status = "E";
			message = message + "UnsupportedEncodingException: " + e1.getMessage().toString() + "\r\n";
		} catch (Exception e1) {
			status = "E";
			message = message + "Exception: " + e1.getMessage().toString() + "\r\n";
		}finally{
	    	ResponseBody r = new ResponseBody();
			r.setStatus(status);
			r.setKey(CMISID);
	    	r.setMessage(message);
			return r;			
		}
    }
      
}