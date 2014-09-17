package metadata.crud;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.sforce.soap.metadata.*;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import connection.ConnectionProvider;

public class CreateRecordTypes {
	public MetadataConnection mConnection;
	public PartnerConnection pConnection;
	public boolean isTest= false;

	public void creatingRecordTypes() throws Exception{
		mConnection = ConnectionProvider.getMetadataConnection(); // getting metadata connection
		pConnection = ConnectionProvider.getPartnerConnection(); // getting partner connection
		System.out.println("logged in salesforce..");
		CustomObject customObject = new CustomObject();
		customObject.setFullName("MyCustomObject__c"); // Object on which Record Types are to be created.
		Set< String> recordTypes = new HashSet< String>();
		recordTypes.add("Super111");
		recordTypes.add("Employee111");

		System.out.println("\nFor the sObject: "+ customObject.getFullName()+""+" Record Type to be made are: "+ recordTypes);
		ListMetadataQuery lmq = new ListMetadataQuery();
		lmq.setType("Profile");
		double asOfVersion = 31.0;
		FileProperties[] lmr = mConnection.listMetadata(new ListMetadataQuery[] {lmq}, asOfVersion);
		String profileFullName = "";

		if(lmr != null){
			for(FileProperties n: lmr){
				if(pConnection.getUserInfo().getProfileId().equalsIgnoreCase(n.getId())){
					profileFullName = n.getFullName(); // <-- Profile Full Name
					break;
				}
			}
		}

		try {
			RecordType[] rType = new RecordType[recordTypes.size()];
			List<String> recordTypesList = new ArrayList<String>();
			recordTypesList.addAll(recordTypes);
			com.sforce.soap.metadata.ProfileRecordTypeVisibility[] prtv = new com.sforce.soap.metadata.ProfileRecordTypeVisibility[recordTypes.size()];
			int counter = 0 ;

			for(int i=0; i<recordTypes.size(); i++){
				// Instantiating the Metadata type "RecordType" for setting some mandatory fields - (Name, Label, Active)
				rType[i] = new RecordType();

				rType[i].setLabel(recordTypesList.get(i)); // <-- setting the label name of Record Type
				if( recordTypesList.get(i).trim().contains(" ") ) {
					rType[i].setFullName(customObject.getFullName()+"."+recordTypesList.get(i).trim().replaceAll(" ", "_")); // <-- If there are any white spaces in the Record Type name from Configuration (replacing the white spaces with underscores _ )
				} else
					rType[i].setFullName(customObject.getFullName()+"."+recordTypesList.get(i));
				rType[i].setActive(true); // <-- Making the record type Active

				// Instantiating the Metadata type ProfileRecordTypeVisibility
				prtv[i] = new ProfileRecordTypeVisibility();
				if( recordTypesList.get(i).contains(" ") ) {
					prtv[i].setRecordType(""+customObject.getFullName()+"."+recordTypesList.get(i).trim().replaceAll(" ", "_"));
				} else
					prtv[i].setRecordType(""+customObject.getFullName()+"."+recordTypesList.get(i));

				if(counter==0){
					prtv[i].setDefault(true); // <-- If it is the first record type then setting it "Default"
				} else
					prtv[i].setDefault(false);
				prtv[i].setVisible(true); // <-- Visibility over Profile
				counter++;
			}

			Profile pr = new Profile();
			pr.setRecordTypeVisibilities(prtv); // <-- Assigning the Record Type visiblities from above
			UpsertResult[] results =null;
			if(!isTest){
				results = mConnection.upsertMetadata(rType); // upserting Record types
				for (UpsertResult r : results) {
					if (r.isSuccess()) {
						System.out.println("Success: Created Record Type " + r.getFullName());
					} else {
						System.out.println("Warning: Errors were encountered while creating: "+ r.getFullName());
						for (com.sforce.soap.metadata.Error e : r.getErrors()) {
							System.out.println("Error: Error in creating RecordTypes");
							System.out.println("Error message: " + e.getMessage());
							System.out.println("Status code: " + e.getStatusCode());
						}
					}
				}
			}
			System.out.println("\n\tUpdating the Current User's profile to the get the Reocord Types Visible ...");
			pr.setFullName(profileFullName);
			UpsertResult[] sr =null;
			if(!isTest){
				sr = mConnection.upsertMetadata(new Metadata[]{pr}); // updating current profile
				for (UpsertResult r : sr) {
					if (r.isSuccess()) {
						System.out.println("Success: Updated Profile " + r.getFullName());
					} else {
						System.out.println("Warning: Errors were encountered while updating Profile : "+ r.getFullName());
						for (com.sforce.soap.metadata.Error e : r.getErrors()) {
							System.out.println("Error: Error in updating Profile");
							System.out.println("Error message: " + e.getMessage());
							System.out.println("Status code: " + e.getStatusCode());
						}
					}
				}
			}
			System.out.println("\n\tUpdating the Custom object to the get the Reocord Types Visible ...");
			String label ="Custom Label";
			CustomObject cust = new CustomObject();
			cust.setFullName("YourCustomObject__c");
			cust.setDeploymentStatus(DeploymentStatus.Deployed);
			cust.setDescription("Created by Sanjay");
			cust.setEnableActivities(true);
			cust.setLabel(label);
			cust.setPluralLabel(label+"s");
			cust.setSharingModel(SharingModel.ReadWrite);
			CustomField nf = new CustomField();
			nf.setType(FieldType.Text);
			nf.setDescription("The custom object identifier on page layouts, related lists etc");
			nf.setLabel(label+"field");
			nf.setFullName("Name");
			cust.setNameField(nf);
			if(!isTest)
				sr = mConnection.upsertMetadata(new Metadata[]{cust}); // updating object
			if(sr!=null)
				for (UpsertResult r: sr) {
					if (r.isSuccess()) {
						System.out.println("Success: Updated object " + r.getFullName());
					} else {
						System.out.println("Warning: Errors were encountered while updating object : "+ r.getFullName());
						for (com.sforce.soap.metadata.Error e : r.getErrors()) {
							System.out.println("Error: Error in updating Object");
							System.out.println("Error message: " + e.getMessage());
							System.out.println("Status code: " + e.getStatusCode());
						}
					}
				}
			System.out.println("Record types created.");
		} catch (ConnectionException ce) {
			ce.printStackTrace();
		}
	}// END public void CreatingRecordTypes()
}
