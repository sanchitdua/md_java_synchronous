package metadata.crud;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.metadata.*;
import connection.ConnectionProvider;

public class CreateOrUpdateLayouts {
	public static void main(String... str) throws Exception{
		CreateOrUpdateLayouts coul = new CreateOrUpdateLayouts();
		coul.updateLayoutItems();
	}
	
	public void updateLayoutItems() throws Exception {
		// Fields to be added on Layout
		Set<String> fields = new HashSet<String>();
		fields.add("City__c");
		fields.add("ContactNo__c");
		fields.add("Email_Id__c");
		fields.add("Last_Name__c");
		MetadataConnection mConnection = ConnectionProvider.getMetadataConnection(); // getting metadata connection
		PartnerConnection pConnection = ConnectionProvider.getPartnerConnection(); // getting partner connection
		System.out.println("Logged in...");
		updateLayout(mConnection, null, "MyCustomObject__c", "Custom _section_name", fields, true, pConnection);
	} // END public void updateLayoutItems()

	public static void updateLayout(MetadataConnection mConnection, String[] recordTypeTest, String sObjName, String secName, Set<String> f, boolean isNameAutoNumber, PartnerConnection pConnection){
		try{
			if(f!= null){
				boolean nameSet = false;
				com.sforce.soap.partner.DescribeSObjectResult[] dsrArray = null;
				boolean isNameAuto = false;
				if(pConnection != null)
					dsrArray = pConnection.describeSObjects(new String[] { sObjName }); // lablename, apiname, all of the child components as well / listMetadata ==> Type :: 
				boolean isNameIncluded = false;
				com.sforce.soap.partner.DescribeSObjectResult dsr = null;
				if(dsrArray != null)
					if(dsrArray.length >0)
						dsr = dsrArray[0];
				// Here, we're checking if the Name Standard field is autonumber or not. 
				// If the name field is autonumber then its treated as readonly field which is not required to set on the edit layout while if the name std field is of type Text then its mandatory to set on the layout.
				if(dsr != null)
					for (int i = 0; i < dsr.getFields().length; i++) {
						com.sforce.soap.partner.Field field = dsr.getFields()[i];
						if(!field.getCustom()){
							if(field.getNameField()){
								if(!field.isAutoNumber()){
									nameSet = true;
								}
								if(field.isAutoNumber())
									isNameAuto = true;
							}
						}
					}
				String sectionName = ""+secName;

				// The set coming as argument
				Set<String> fields = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
				fields.addAll(f);
				Set<String> allFieldsAlreadyonLayout = new HashSet<String>();
				com.sforce.soap.partner.DescribeLayoutResult dlResult = null;
				if(recordTypeTest==null)
					dlResult = pConnection.describeLayout(""+sObjName,null, null);
				else
					dlResult = pConnection.describeLayout(""+sObjName,null, recordTypeTest);
				Map<String, LayoutSection> sectionNameWithLayoutSection = new LinkedHashMap<String, LayoutSection>();
				List<LayoutSection> lsList = new ArrayList<LayoutSection>();
				// collecting every section's items so that we can compare it with the upcoming section.
				// also the styling should be maintained;
				// i think it would be difficult to maintain the previous 
				// things on the section as it is if we are trying to add some more items in it.
				Map<String, Set<String>> sectionWithItems = new TreeMap<String, Set<String>>(String.CASE_INSENSITIVE_ORDER);

				for(com.sforce.soap.partner.DescribeLayout lay: dlResult.getLayouts()){
					boolean isChecked = false;
					for(com.sforce.soap.partner.DescribeLayoutSection dls: lay.getEditLayoutSections()){
						Set<String> items = new LinkedHashSet<String>();
						String sectionHeading = dls.getHeading();
						if(sectionName.equalsIgnoreCase(sectionHeading)){
							sectionName = sectionHeading;
						}
						List<LayoutItem> leftLayoutItemList = new ArrayList<LayoutItem>();
						List<LayoutItem> rightLayoutItemList = new ArrayList<LayoutItem>();
						for(com.sforce.soap.partner.DescribeLayoutRow dlr: dls.getLayoutRows()){
							com.sforce.soap.partner.DescribeLayoutItem[] dli = dlr.getLayoutItems();
							if(dli[0].getLayoutComponents() != null && dli[0].getLayoutComponents().length >0){
								LayoutItem li = new LayoutItem();
								if(dli[0].getLayoutComponents()[0].getValue() == null){
									li.setEmptySpace(true);
									leftLayoutItemList.add(li);
								}
								if(dli[0].getLayoutComponents()[0].getValue() != null ){
									if(!fields.contains(dli[0].getLayoutComponents()[0].getValue()) || dli[0].getLayoutComponents()[0].getValue().equalsIgnoreCase("Name")){
										li.setField(dli[0].getLayoutComponents()[0].getValue());
										if(dli[0].getLayoutComponents()[0].getValue().equalsIgnoreCase("CreatedBy") || dli[0].getLayoutComponents()[0].getValue().equalsIgnoreCase("LastModifiedBy") || dli[0].getLayoutComponents()[0].getValue().equalsIgnoreCase("Owner"))
											li.setBehavior(UiBehavior.Readonly);
										else if(dli[0].getLayoutComponents()[0].getValue().equalsIgnoreCase("Name") && isNameAuto && !nameSet){
											li.setBehavior(UiBehavior.Readonly);
											isNameIncluded = true;
										}
										else if(dli[0].getLayoutComponents()[0].getValue().equalsIgnoreCase("Name") && !isNameAuto && nameSet){
											li.setBehavior(UiBehavior.Required);
											isNameIncluded = true;
										}
										else
											li.setBehavior(UiBehavior.Edit);
										allFieldsAlreadyonLayout.add(dli[0].getLayoutComponents()[0].getValue());
										leftLayoutItemList.add(li);
										items.add(dli[0].getLayoutComponents()[0].getValue());
									}
								}
							}	
							if(dli[1].getLayoutComponents() != null && dli[1].getLayoutComponents().length >0){
								LayoutItem li = new LayoutItem();
								if(dli[1].getLayoutComponents()[0].getValue() == null){
									li.setEmptySpace(true);
									rightLayoutItemList.add(li);
								}
								if(dli[1].getLayoutComponents()[0].getValue() != null){
									if(!fields.contains(dli[1].getLayoutComponents()[0].getValue()) || dli[1].getLayoutComponents()[0].getValue().equalsIgnoreCase("Name")){
										li.setField(dli[1].getLayoutComponents()[0].getValue());
										if(dli[1].getLayoutComponents()!= null){
											if(dli[1].getLayoutComponents() != null){
												if(dli[1].getLayoutComponents()[0]!=null){
													if(dli[1].getLayoutComponents()[0].getValue()!= null){
														if(dli[1].getLayoutComponents()[0].getValue().equalsIgnoreCase("CreatedBy") || dli[1].getLayoutComponents()[0].getValue().equalsIgnoreCase("LastModifiedBy") || dli[1].getLayoutComponents()[0].getValue().equalsIgnoreCase("Owner"))
															li.setBehavior(UiBehavior.Readonly);
														else if(dli[1].getLayoutComponents()[0].getValue().equalsIgnoreCase("Name") && isNameAuto && !nameSet && !isNameIncluded)
															li.setBehavior(UiBehavior.Readonly);
														else if(dli[1].getLayoutComponents()[0].getValue().equalsIgnoreCase("Name") && !isNameAuto && nameSet && !isNameIncluded)
															li.setBehavior(UiBehavior.Required);
														else
															li.setBehavior(UiBehavior.Edit);
													}
												}
											}
										}
										allFieldsAlreadyonLayout.add(dli[1].getLayoutComponents()[0].getValue());
										rightLayoutItemList.add(li);
										items.add(dli[1].getLayoutComponents()[0].getValue());
									}
								}
							}							
						}
						// here put the map information.
						LayoutColumn lColumn1 = new LayoutColumn();
						lColumn1.setLayoutItems(leftLayoutItemList.toArray(new LayoutItem[leftLayoutItemList.size()]));
						LayoutColumn lColumn2 = new LayoutColumn();
						lColumn2.setLayoutItems(rightLayoutItemList.toArray(new LayoutItem[rightLayoutItemList.size()]));
						LayoutSection ls = new LayoutSection();
						ls.setLabel(""+sectionHeading);
						ls.setCustomLabel(true);
						ls.setDetailHeading(true);
						ls.setEditHeading(true); // to enable collapse and expand functionality
						ls.setStyle(LayoutSectionStyle.TwoColumnsLeftToRight);
						ls.setLayoutColumns(new LayoutColumn[]{lColumn1, lColumn2});
						lsList.add(ls);
						sectionNameWithLayoutSection.put(sectionHeading, ls);
						sectionWithItems.put(sectionHeading, items);

						// If the section already exists ==> Evaluation of upcoming fields so that we may compare the duplicacy and the space required according to the number of items (i.e odd or even)
						if(sectionWithItems.containsKey(sectionName) && !isChecked){
							Set<String> existingFields=  sectionWithItems.get(sectionName);
							if(existingFields.size()>0) // To get rid of the duplicacy that can occur while updating the Layout Section
								fields.removeAll(existingFields);
							existingFields.clear();
							isChecked = true;
							if(!fields.contains("Name")){
								List<LayoutItem> leftLayoutItemsList = new ArrayList<LayoutItem>();
								List<LayoutItem> rightLayoutItemsList = new ArrayList<LayoutItem>();
								leftLayoutItemsList.addAll(leftLayoutItemList);
								rightLayoutItemsList.addAll(rightLayoutItemList);
								List<String> totalList = new ArrayList<String>();
								totalList.addAll(fields);
								int midVal = 0;
								if(totalList.size()%2==0)
									midVal = totalList.size()/2;
								else
									midVal = totalList.size()/2+1;
								List<String> subListOne = totalList.subList(0, midVal);
								List<String> subListTwo = totalList.subList(midVal, totalList.size());
								for(String str: subListOne){
									LayoutItem li = new LayoutItem();
									li.setField(str);
									leftLayoutItemsList.add(li);
								}
								for(String str: subListTwo){
									LayoutItem li = new LayoutItem();
									li.setField(str);
									rightLayoutItemsList.add(li);
								}
								LayoutColumn lColumn12 = new LayoutColumn();
								LayoutColumn lColumn22 = new LayoutColumn();
								lColumn12.setLayoutItems(leftLayoutItemsList.toArray(new LayoutItem[leftLayoutItemsList.size()]));
								lColumn22.setLayoutItems(rightLayoutItemsList.toArray(new LayoutItem[rightLayoutItemsList.size()]));
								leftLayoutItemsList.clear();
								rightLayoutItemsList.clear();
								LayoutSection ls1 = new LayoutSection();
								ls1.setLabel(""+sectionName);
								ls1.setCustomLabel(true);
								ls1.setDetailHeading(true);
								ls1.setEditHeading(true);
								ls1.setStyle(LayoutSectionStyle.TwoColumnsLeftToRight);
								ls1.setLayoutColumns(new LayoutColumn[]{lColumn12, lColumn22});
								sectionNameWithLayoutSection.put(sectionName, ls1);
							}
						}
					}
				} // END for

				/********************<start>*Manipulations*</start>********************************/
				// If the section needs to be created // it will also execute when there are no Layout Items in the existing section.
				if(!sectionWithItems.containsKey(sectionName)){
					if(true){

						List<LayoutItem> leftLayoutItemsList = new ArrayList<LayoutItem>();
						List<LayoutItem> rightLayoutItemsList = new ArrayList<LayoutItem>();
						List<String> newItems = new ArrayList<String>();
						if(fields.contains("Name"))
							fields.remove("Name");
						newItems.addAll(fields); // upcoming
						int midVal = 0;
						if(newItems.size()%2==0)
							midVal = newItems.size()/2;
						else
							midVal = newItems.size()/2+1;
						List<String> subListOne = newItems.subList(0, midVal);
						List<String> subListTwo = newItems.subList(midVal, newItems.size());
						for(String str: subListOne){
							LayoutItem li = new LayoutItem();
							li.setField(str);
							li.setBehavior(UiBehavior.Edit);
							leftLayoutItemsList.add(li);
						}
						for(String str: subListTwo){
							LayoutItem li = new LayoutItem();
							li.setField(str);
							rightLayoutItemsList.add(li);
						}
						LayoutColumn lColumn21 = new LayoutColumn();
						LayoutColumn lColumn22 = new LayoutColumn();
						lColumn21.setLayoutItems(leftLayoutItemsList.toArray(new LayoutItem[leftLayoutItemsList.size()]));
						lColumn22.setLayoutItems(rightLayoutItemsList.toArray(new LayoutItem[rightLayoutItemsList.size()]));
						leftLayoutItemsList.clear();
						rightLayoutItemsList.clear();
						LayoutSection ls1 = new LayoutSection();
						ls1.setLabel(""+sectionName);
						ls1.setCustomLabel(true);
						ls1.setDetailHeading(true);
						ls1.setEditHeading(true);
						ls1.setStyle(LayoutSectionStyle.TwoColumnsLeftToRight);
						ls1.setLayoutColumns(new LayoutColumn[]{lColumn21, lColumn22});
						sectionNameWithLayoutSection.put(sectionName, ls1);
					}

				} // END if(!sectionWithItems.containsKey(sectionName))

				/*********************<end>*Manipulations*</end>******************************/
				//				System.out.println("before setting the layout sectionNameWithLayoutSection.keySet is: "+sectionNameWithLayoutSection.keySet());
				ListMetadataQuery lmq = new ListMetadataQuery();
				lmq.setType("Layout");
				String layoutName = "";
				FileProperties[] lmr = mConnection.listMetadata(new ListMetadataQuery[] {lmq}, 31.0);
				for(FileProperties fp: lmr){
					if(fp.getFullName().split("\\-")[0].equalsIgnoreCase(""+sObjName)){
						layoutName = fp.getFullName();
					}
				}
				Layout lay1 = new Layout();
				lay1.setFullName(""+layoutName);
				lay1.setEmailDefault(true);
				lay1.setLayoutSections(sectionNameWithLayoutSection.values().toArray(new LayoutSection[sectionNameWithLayoutSection.size()]));
				sectionNameWithLayoutSection.clear();
				SaveResult[] sr = mConnection.updateMetadata(new Metadata[]{lay1}); // updating the layout
				for (SaveResult r : sr) {
		            if (r.isSuccess()) {
		                System.out.println("Success: Updated layout " + r.getFullName());
		            } else {
		                System.out.println("Warning: Errors were encountered while creating: "+ r.getFullName());
		                for (com.sforce.soap.metadata.Error e : r.getErrors()) {
		                	System.out.println("Error: Error in updating Profile");
		                    
		                    System.out.println("Error message: " + e.getMessage());
		                    System.out.println("Status code: " + e.getStatusCode());
		                }
		            }
		        }
				dsrArray = null;
				fields.clear();
				fields = null;
				dlResult = null;
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	} // END udpateLayout()
}