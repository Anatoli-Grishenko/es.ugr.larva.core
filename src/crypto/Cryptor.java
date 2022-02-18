/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crypto;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
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


    /**
     * @brief Basic constructor
     * @param k The key to be used (must be 16 chars)
     */
   public Cryptor(String k) {
       setCryptoKey(k);
   }

   public Cryptor(String cs, String k) {
       setCryptoKey(k);
       setCharSet(cs);
   }

   /**
    * @brief Changes the key ot be used
    * @param k The key (16chars)
    */
   public void setCryptoKey(String k) {
        _cryptoKey="";
        for (int i=0; i<16; i++)
            _cryptoKey += k.charAt(i%k.length()); 
   }
   
   /**
    * @brief Returns the embedded key
    * @return The key
    */
   public String getCryptoKey() {
       return _cryptoKey;
   }
   
   public String getCharSet() {
       return this._charset;
   }
   
   public void setCharSet(String s) {
       this._charset=s;
   }
   
   /**
    * @brief Encripts a String into another string by using a 128bits-RSA Algorithm and the embedded key
    * @param text The text to be encrypted
    * @return The ecnrypted text
    */
   public String enCrypt(String text) {
        return enCryptAES(text);
    }

   /**
    * @brief It tries to decrypt a string with the embedded key
    * @param text The text to decrypt
    * @return The decrypted string
    */
    public String deCrypt(String text) {
        return deCryptAES(text);
    }

    public String enCryptAES(String text) {
       String res= "";
        Key aesKey;
        Cipher cipher;
        try {
            aesKey = new SecretKeySpec(_cryptoKey.getBytes(StandardCharsets.US_ASCII), "AES");
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.US_ASCII));
            res = new String(encrypted, StandardCharsets.US_ASCII);
        } catch (Exception ex) {
            System.err.println("DBA.encrypt "+ex.toString());
        }
        return res;
    }

    public String deCryptAES(String text) {
        String res= "";
        Key aesKey;
        Cipher cipher;
        try {
            aesKey = new SecretKeySpec(_cryptoKey.getBytes(StandardCharsets.US_ASCII), "AES");
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] encrypted = text.getBytes(StandardCharsets.US_ASCII);
            res = new String(cipher.doFinal(encrypted), StandardCharsets.US_ASCII);
        } catch (Exception ex) {
            System.err.println("DBA.decrypt: "+ex.toString());
        }
        return res;
    }
    public String enCrypt64(String text) {
        byte[] encodedBytes = Base64.getEncoder().encode(text.getBytes());
        return new String(encodedBytes);
    }

    public String deCrypt64(String text) {
        byte[] decodedBytes = Base64.getDecoder().decode(text);
        return new String(decodedBytes);
    }
  
}