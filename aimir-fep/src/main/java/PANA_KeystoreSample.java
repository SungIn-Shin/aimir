import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * @author sjlee
 *
 */
public class PANA_KeystoreSample {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
		
        // arg0 : keyfile path and name
        // arg1 : keyfile password
        // arg2 : key alias
        // arg3 : certfile path and name
        // arg4 : keystore path and name
        try {
            // Read Private from Keyfile
            PrivateKey priKey = null;
            // String keyFile = "/home/aimir/aimiramm/penta/ECDSA/pana/hes_pana.key"; // Keyfile path
            String keyFile = args[0]; // Keyfile path
            RandomAccessFile keyfile = new RandomAccessFile(new File(keyFile), "r");
            byte[] keydata = new byte[(int)keyfile.length()];
            keyfile.readFully(keydata);
            keyfile.close();
            keyfile = null;
			
            PrivateKeyInfo priv = PrivateKeyInfo.getInstance(keydata);
	        
            byte[] keyBytes = priv.getEncoded();
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("EC", "BC"); 
            priKey = kf.generatePrivate(spec);
	         
            //------------------------------------------------------------------------
            
            // Import private key into Keystore

            // Load the keystore
            // String KeystoreFilename = "/home/aimir/aimiramm/penta/ECDSA/aimir_keystore.jks"; // keystore file path 
            String KeystoreFilename = args[4]; // keystore file path 
            String keyStorePassword = "aimiramm";
            // String keyStorePassword = args[1];
            KeyStore keyStore = KeyStore.getInstance("jks");
            FileInputStream keyStoreInputStream = new FileInputStream(KeystoreFilename);
            keyStore.load(keyStoreInputStream, keyStorePassword.toCharArray());
            keyStoreInputStream.close();

            // import cert
            // String certfile = "/home/aimir/aimiramm/penta/ECDSA/pana/hes_pana.der"; // cert file path
            String certfile = args[3]; // cert file path
            FileInputStream certificateStream = new FileInputStream(certfile);

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            java.security.cert.Certificate[] chain = {};
            chain = certificateFactory.generateCertificates(certificateStream).toArray(chain);
            certificateStream.close();

            // import prikey
            // String privateKeyEntryPassword = "aimiramm"; // keystore
            String privateKeyEntryPassword = args[1];
            // String entryAlias = "panaaimirkey";
            String entryAlias = args[2];
            keyStore.setEntry(entryAlias, new KeyStore.PrivateKeyEntry(priKey, chain),
                    new KeyStore.PasswordProtection(privateKeyEntryPassword.toCharArray()));

            // Write out the keystore
            FileOutputStream keyStoreOutputStream = new FileOutputStream(KeystoreFilename);
            keyStore.store(keyStoreOutputStream, keyStorePassword.toCharArray());
            keyStoreOutputStream.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }	
}

 
