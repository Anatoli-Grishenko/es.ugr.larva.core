/**
 * @file OleFile.java
 * @author Anatoli.Grishenko@gmail.com
 *
 */
package data;

import JsonObject.JsonArray;
import crypto.Cryptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.commons.io.FilenameUtils;

/**
 * @brief Subclass of  Ole for reading, writing and transferring files through
 * ASCII messages. It reads the files byte to byte so it can be used for both text and
 * binary files. It is based on two fields one for the name of the file and
 * other for the content. The name of the file contains just the name with the
 * extension and any path indication is removed. The content is encoded as an
 * array of bytes.<p>
 * 
 * Since methods for loading and saving have been override, there is only two possible
 * ways for saving and reading a file, either as binary content or as encrypted contemt,
 * therefore, the Json serialization is nor explicitly dealt with.
 *
 */
public class OleFile extends Ole {

    /**
     * @brief Basic constructor. It defines the specific fields of this object
     */
    public OleFile() {
        super();
        addField("filename");
        addField("filedata");
        setType(oletype.OLEFILE.name());
    }

    /**
     * @brief Copy constructor
     * @param o The Ole object to be cloned
     */
    public OleFile(Ole o) {
        super(o);
        addField("filename");
        addField("filedata");
        setType(oletype.OLEFILE.name());
    }

    /**
     * @brief
     * It takes the binary (byte[]) content of any file, dump it as an
     * integer Json Array. It encapsulates not only the binary content (key
     * "filedata") but also the original filename (key "filename", just the
     * filename, without the path to it
     *
     * @param fullfilename Name of the file to load, including any possible acces path.
     * However, the path will be ignored and only the name and extension will be memorized
     * @return An ole object that encapsulates the content of the file. If an
     * error occured during the load of fila, an empty Ole object is returned
     * instead
     */
    @Override
    public OleFile loadFile(String fullfilename) {
        try {
            ArrayList <Integer> arraydata = new ArrayList();
            Path path = Paths.get(fullfilename);
            // Read all bytes
            byte[] bytedata = Files.readAllBytes(path);
            // If encrypted, first transform the byte sequence into a String,
            // decrypt it, and back to a sequence of bytes
            if (isEncrypted()) {
                Cryptor enigma = new Cryptor(meta().getString("crypto", ""));
                String scontent = new String(bytedata, enigma.getCharSet()),
                        crypcontent = enigma.deCrypt(scontent);
                bytedata = crypcontent.getBytes();
            }
            // Stores the byte sequence as a JsonArray
            for (int i = 0; i < bytedata.length; i++) {
                arraydata.add((int) bytedata[i]);
            }
            setField("filename", path.getFileName().toString());
            setField("filedata", new ArrayList(arraydata));
//            System.out.println(this.toString());
        } catch (IOException ex) {
            System.err.println(ex.toString());
        }
        return this;
    }

    /**
     * @brief Given an encapsulated binary file obtained with the method {@link data.OleFile#loadFile(java.lang.String) }
     * it saves the image exactly with the concatenation of the name
     * encapsulated in it, and the path specified in @param outputfolder. The file is sabed  
     * in binary mode, so it mught be considered as a replica of the former file. 
     *
     * @param outputfolder The folder on which to save the encapsulated file
     * @return
     */
    public boolean saveFile(String outputfolder) {
        boolean res = false;
//        if (getType().equals(oletype.OLEFILE.name())) {
            String filename = get("filename").asString();
            JsonArray content = get("filedata").asArray();
            byte[] bytedata = new byte[content.size()];
            for (int i = 0; i < bytedata.length; i++) {
                bytedata[i] = (byte) content.get(i).asInt();
            }
            try {
//                Cryptor enigma = new Cryptor(meta().getString("crypto", ""));
                FileOutputStream fos = new FileOutputStream(outputfolder + "/" + filename);
                if (!this.isEncrypted()) {
                    fos.write(bytedata);
                } else {
                    String scontent = new String(bytedata, myCryptor.getCharSet()),
                            crypcontent = myCryptor.enCrypt(scontent);
                    fos.write(crypcontent.getBytes());
                }
                fos.close();
                res = true;
            } catch (Exception ex) {
            }
//        }
        return res;
    }

    /**
     * @brief Saves the file wrapped within the Ole object but with a new name
     *
     * @param outputfolder Where to write the file
     * @param newname The new name
     * @return A boolean that says if the operation has been succesfull or not.
     */
    @Override
    public boolean saveAsFile(String outputfolder, String newname, boolean plainjson) {
        String oldname = getString("filename", ""),
                name = FilenameUtils.getBaseName(oldname),
                extension = FilenameUtils.getExtension(oldname);
        set("filename", newname + "." + extension);
        return saveFile(outputfolder);
    }

    public String getStringContent() {
        String res = "";
        JsonArray content = get("filedata").asArray();
        byte[] bytedata = new byte[content.size()];
        for (int i = 0; i < bytedata.length; i++) {
            bytedata[i] = (byte) content.get(i).asInt();
        }
        try {
            res = new String(bytedata, "ISO-8859-1");
        } catch (Exception ex) {
        }
        return res;
    }
    
    /**
     * @brief Retunrs the filename embedded into a OleFile object
     * @return 
     */
    public String getFileName() {
        return getField("filename");
    }

}
