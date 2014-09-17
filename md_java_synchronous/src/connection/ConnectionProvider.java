package connection;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
public class ConnectionProvider {
	private static final Properties props;
	static{
		props = new Properties();
		try{
			props.load(new FileInputStream("resources/credentials.properties"));
		}catch(IOException ie) {
			ie.printStackTrace();
		}
	}
	private static final String username = props.getProperty("username");
	private static final String password = props.getProperty("password");
	private static final String token = props.getProperty("token");
	private static final String endpoint = props.getProperty("EnterpriseEndPoint");
	private static final String pendpoint = props.getProperty("PartnerEndPoint");
	public static MetadataConnection getMetadataConnection() throws Exception{
		MetadataConnection returnVal = null;
		metadata.MetadataLoginUtil log = new metadata.MetadataLoginUtil();
		returnVal = log.login(username,password+token,endpoint);
		return returnVal;
	}
	public static PartnerConnection getPartnerConnection(){
		PartnerConnection returnVal = null;
		partner.PartnerLoginUtil log = new partner.PartnerLoginUtil();
		returnVal = log.login(username, password+token,pendpoint);
		return returnVal;
	}
}