/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crypto;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author lcv
 */
public class Cryptor {
   protected String  _cryptoKey,
        _charset="ISO-8859-1";
    protected final String _atoms="abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";


   public Cryptor(String k) {
       setCryptoKey(k);
   }

   public void setCryptoKey(String k) {
        _cryptoKey="";
        for (int i=0; i<16; i++)
            _cryptoKey += k.charAt(i%k.length()); 
   }
   
   public String getCryptoKey() {
       return _cryptoKey;
   }
   
   public String getCharSet() {
       return this._charset;
   }
   
   public String enCrypt(String text) {
       String res= "";
        Key aesKey;
        Cipher cipher;
        try {
            aesKey = new SecretKeySpec(_cryptoKey.getBytes(_charset), "AES");
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(text.getBytes(_charset));
            res = new String(encrypted, _charset);
        } catch (Exception ex) {
            System.err.println("DBA.encrypt "+ex.toString());
        }
        return res;
    }

    public String deCrypt(String text) {
        String res= "";
        Key aesKey;
        Cipher cipher;
        try {
            aesKey = new SecretKeySpec(_cryptoKey.getBytes(_charset), "AES");
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] encrypted = text.getBytes(_charset);
            res = new String(cipher.doFinal(encrypted), _charset);
        } catch (Exception ex) {
            System.err.println("DBA.decrypt: "+ex.toString());
        }
        return res;
    }
  
}