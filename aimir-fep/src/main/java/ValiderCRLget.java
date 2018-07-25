import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import common.LDAPUtil;

public class ValiderCRLget {

    public static void main(String[] args) {
        byte[] certData = null;
        try {
            // hes_penta.der
            certData = java.nio.file.Files.readAllBytes( java.nio.file.Paths.get(args[0]) );
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        byte[] rtnCRLdata = LDAPUtil.getCRL(certData);
        
        OutputStream out = null;
        try {
            // crl file
            out = new FileOutputStream(args[1]);
            out.write( rtnCRLdata );
        }
        catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
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
        
        System.out.println("CRL DATA updated !!");
    }

}
