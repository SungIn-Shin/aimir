import common.LDAPConnector;
import util.Base64;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class ValiderPANACertVerifier {
    
    public static void main(String[] args) {
       
        try {
        //Read PANA Device Certificate
        String inputFileName = "";

        if(args.length == 2) {
            inputFileName = args[0];    
        } else {
            inputFileName = "../TempClientCert/client_pana.cert";
        }

        Scanner sc = new Scanner(new File(inputFileName));
        String strClientCert ="";
            
        while(sc.hasNextLine()) {
                
            String line = sc.nextLine();
                
            strClientCert += line.replace("-----BEGIN CERTIFICATE-----", "").replace("-----END CERTIFICATE-----", "")+"\n";     
        }
        sc.close();
        
        String b64_cert = strClientCert;
        Security.addProvider(new BouncyCastleProvider());

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
        
        byte[] bin_cert = Base64.decode(b64_cert, 0);
        InputStream certInputStream = new ByteArrayInputStream(bin_cert);
        X509Certificate cert = (X509Certificate)certFactory.generateCertificate(certInputStream);
        certInputStream.close();
            
        // String crlFilePath = "../PANACRL/nuri_pana.crl";
        String crlFilePath = args[1];
        InputStream crlInputStream = new FileInputStream(crlFilePath); // ?????ㅽ?몃┝ ???

            X509CRL crl = (X509CRL)certFactory.generateCRL(crlInputStream);
            crlInputStream.close();
            
            X509CRLEntry crl_entry = crl.getRevokedCertificate(cert.getSerialNumber());
            String str_crl_result = "";
            if (crl_entry == null)
            str_crl_result = "0";//"vaild";
            else
            str_crl_result = "1";//"revoked";
            
            System.out.println(str_crl_result);
                
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

