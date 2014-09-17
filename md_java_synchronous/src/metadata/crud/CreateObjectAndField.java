package metadata.crud;
import com.sforce.soap.metadata.SaveResult;
import com.sforce.soap.metadata.CustomField;
import com.sforce.soap.metadata.CustomObject;
import com.sforce.soap.metadata.DeploymentStatus;
import com.sforce.soap.metadata.FieldType;
import com.sforce.soap.metadata.Metadata;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.SharingModel;
import connection.ConnectionProvider;

public class CreateObjectAndField {
	public MetadataConnection metadataConnection;
	public boolean isTest = false;

	public void runCreate() throws Exception {
		metadataConnection = ConnectionProvider.getMetadataConnection();
		System.out.println("After successfully loggin in ... ");
		// Custom objects and fields must have __c suffix in the full name.
		final String uniqueObjectName = "YourCustomObject__c"; // Api name for the custom object to be created
		createCustomObjectSync(uniqueObjectName);
	} // END private void runCreate() throws Exception

	private void createCustomObjectSync(final String uniqueName) throws Exception {
		final String label = "Your Custom Object Label";
		CustomObject co = new CustomObject();
		co.setFullName(uniqueName);
		co.setDeploymentStatus(DeploymentStatus.Deployed);
		co.setDescription("Created by Sanjay");
		co.setEnableActivities(true);
		co.setLabel(label);
		co.setPluralLabel(label + "s");
		co.setSharingModel(SharingModel.ReadWrite);
		// The name field appears in page layouts, related lists, and elsewhere.
		CustomField nf = new CustomField();
		nf.setType(FieldType.Text);
		nf.setDescription("The custom object identifier on page layouts, related lists etc");
		nf.setLabel(label);
		nf.setFullName(uniqueName);
		co.setNameField(nf); // setting name filed is necessary
		SaveResult[] results = null;
		if(!isTest){
			results = metadataConnection.createMetadata(new Metadata[] { co }); // creating the custom object, upsertMetadata() can also be used instead.
			for (SaveResult r : results) {
				if (r.isSuccess()) {
					System.out.println("Success: Created component : " + r.getFullName());
				} else {
					System.out.println("Warning: Errors were encountered while creating : "+ r.getFullName());
					for (com.sforce.soap.metadata.Error e : r.getErrors()) {
						System.out.println("Error message: " + e.getMessage());
						System.out.println("Status code: " + e.getStatusCode());
					}
				}
			}
		}
	} // END private void createCustomObjectSync(final String uniqueName) throws Exception	
}