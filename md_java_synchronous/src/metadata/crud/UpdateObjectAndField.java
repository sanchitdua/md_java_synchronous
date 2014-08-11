package metadata.crud;
import com.sforce.soap.metadata.ReadResult;
import com.sforce.soap.metadata.SaveResult;
import com.sforce.soap.metadata.CustomField;
import com.sforce.soap.metadata.CustomObject;
import com.sforce.soap.metadata.DeploymentStatus;
import com.sforce.soap.metadata.FieldType;
import com.sforce.soap.metadata.Metadata;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.SharingModel;

import connection.ConnectionProvider;

public class UpdateObjectAndField {
	private MetadataConnection metadataConnection;

	public static void main(String... str) throws Exception{
		UpdateObjectAndField uof = new UpdateObjectAndField();
		uof.runUpdate();
	}
	
	private void runUpdate() throws Exception {
		metadataConnection = ConnectionProvider.getMetadataConnection();
		System.out.println("After successfully loggin in ... ");
        // Custom objects and fields must have __c suffix in the full name.
        final String uniqueObjectName = "YourCustomObject__c"; // object to be updated.
        updateCustomObjectSync(uniqueObjectName);
    }
	
	private void updateCustomObjectSync(final String uniqueName) throws Exception {
        final String label = "Your Custom Object Label";
        CustomObject co = new CustomObject();
        co.setFullName(uniqueName);
        co.setDeploymentStatus(DeploymentStatus.Deployed);
        co.setDescription("this is Updated by Sanjay");
        co.setEnableActivities(true);
        co.setLabel(label);
        co.setPluralLabel(label + "s");
        co.setSharingModel(SharingModel.ReadWrite); // manually passing (NOT changing) the appropriate sharing model info.==> needed
        // you can also get Sharing model info by using method given in the end of this class.
        CustomField nf = new CustomField(); //specifying name field
        nf.setType(FieldType.Text);
        nf.setDescription("The custom object identifier on page layouts, related lists etc");
        nf.setLabel(label);
        nf.setFullName(uniqueName);
        co.setNameField(nf);
       
        SaveResult[] results = metadataConnection.updateMetadata(new Metadata[] { co }); // updating the specified object
        for (SaveResult r : results) {
            if (r.isSuccess()) {
                System.out.println("Success: Updated component " + r.getFullName());
            } else {
                System.out.println("Warning: Errors were encountered while updating: "+ r.getFullName());
                for (com.sforce.soap.metadata.Error e : r.getErrors()) {
                    System.out.println("Error message: " + e.getMessage());
                    System.out.println("Status code: " + e.getStatusCode());
                }
            }
        }
    } // END private void updateCustomObjectSync(final String uniqueName)
	
	
	//This method is not used anywhere.. only for reference.
	public static void readCustomObjectSync() {
		try {
			MetadataConnection mConnection = ConnectionProvider.getMetadataConnection(); // getting metadata connection
			ReadResult readResult = mConnection.readMetadata("CustomObject", new String[] {	"MyCustomObject__c", "MyCustomObject2__c" });
			Metadata[] mdInfo = readResult.getRecords();
			System.out.println("Number of component info returned: "+ mdInfo.length);
			for (Metadata md : mdInfo) {
				if (md != null) {
					CustomObject obj = (CustomObject) md;
					System.out.println("Custom object full name: "	+ obj.getFullName());
					System.out.println("Label: " + obj.getLabel());
					System.out.println("Number of custom fields: "	+ obj.getFields().length);
					System.out.println("Sharing model: "+ obj.getSharingModel()); // to get sharing model information
				} else {
					System.out.println("Empty metadata.");
				}
			}
		} catch (Exception ce) {
			ce.printStackTrace();
		}
	}
}