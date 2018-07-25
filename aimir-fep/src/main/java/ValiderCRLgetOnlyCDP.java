import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import common.LDAPConnector;

public class ValiderCRLgetOnlyCDP {

    public static void main(String[] args) {
        // String crldp = "ldap://prodradius.aimir.int:389/cn=7VwpJA2MR-Sx8rXWhzGJXw,ou=EAPofValider,o=Valider,c=no";
        String crldp = "ldap://prodradius.aimir.int:389/cn=dp_vabb3UOPQA6Eg5TydV2Jegp0,ou=AuthofNURI,o=NURI,c=KR";
        byte[] crl_ldap = null;
	    
        OutputStream out = null;
        try {
            crl_ldap = LDAPConnector.getCrlFromCrldp(crldp);
            // ../soria_certs/nuri_soria_pana.crl
            out = new FileOutputStream(args[0]);
            out.write( crl_ldap );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (Exception e) {}
            }
        }
        System.out.println("PANA CRL DATA updated !!");
    }
}
