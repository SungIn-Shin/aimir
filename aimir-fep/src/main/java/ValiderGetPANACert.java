import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import common.LDAPConnector;

public class ValiderGetPANACert {
    
    public static void main(String[] args) {
       
        try {
            //Read PANA Device Certificate
            String outputFileName = "";

            if(args.length == 2) {
                outputFileName = args[0];
            } else {
                outputFileName = "../PANACRL/nuri_pana.crl";
            }   

        Security.addProvider(new BouncyCastleProvider());

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
            
        // String crldp = "ldap://prodradius.aimir.int:389/cn=dp_vabb3UOPQA6Eg5TydV2Jegp0,ou=AuthofNURI,o=NURI,c=KR";
        String crldp = args[1];
        byte[] crl_ldap = null;     
            crl_ldap = LDAPConnector.getCrlFromCrldp(crldp);
            InputStream crlInputStream = new ByteArrayInputStream(crl_ldap);
        
             X509CRL crl = (X509CRL)certFactory.generateCRL(crlInputStream);
             crlInputStream.close();
             new java.io.FileOutputStream(outputFileName).write( crl_ldap );
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

