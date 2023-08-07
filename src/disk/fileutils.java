/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package disk;

import JsonObject.JsonArray;
import JsonObject.JsonObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author lcv
 * To load and save binary content of files into Json Strings. It encapsulated the name and content of a file
 * so that it can be easily transferred thoruh a JSon sequence
 */
public class fileutils {
    /**
     * It takes the binary (byte[]) content of any file, dump it as an integer Json Array. It encapsulates not only the 
     * binary content (key "filedata") but also the original filename (key "filename", just the filename, 
     * without the path to it, if such is given in @p filefrom
     * @param filefrom  Name of the file to load
     * @return a JSon Object that encapsulates the content of the file. If an error occured
     * during the load of fila, an empty JSON object is returned instead
     */
    public static JsonObject FileToJson(String filefrom) {
        JsonObject res= new JsonObject();
        try {
            JsonArray arraydata = new JsonArray();
            Path path = Paths.get(filefrom);
            byte[] data = Files.readAllBytes(path);
            for (int i=0; i<data.length; i++)
                arraydata.add((int) data[i]);
            res=new JsonObject().add("filename", path.getFileName().toString()).add("filedata",arraydata);
        } catch (IOException ex) {
        }
        return res;
    }
    
    /**
     * Given a Json encapsulated binary file obtained with the method @p FileToJson,
     * it saves the image exactly with the concatenation of the name encapsulated in it, 
     * and the path contained in @p outputfolder
     * 
     * @param filejson The JSon encapsulated file
     * @param outputfolder The folder on which to save the encapsulated file
     * @return 
     */
    public static boolean JsonToFile(JsonObject filejson, String outputfolder) {
        boolean res=false;
            String filename = filejson.get("filename").asString();
            JsonArray content = filejson.get("filedata").asArray();
            byte [] data = new byte[content.size()];
            for (int i=0; i<data.length; i++)
                data[i] = (byte) content.get(i).asInt();
            try {
                FileOutputStream fos = new FileOutputStream(outputfolder+"/"+filename);
                fos.write(data);
                fos.close();
                res=true;
            } catch (Exception ex) {
            }            
        return res;
    }
    
    /**
     * Renames the encapsulated file, byt changing the name included in the Json. It does not save
     * the file, just changes its encapsulated name
     * @param filejson The encapsulated file
     * @param newname The new name
     * @return The new Json Object
     */
    public static JsonObject JsonRenameFile(JsonObject filejson, String newname) {
        String oldname=filejson.getString("filename", ""),
                name = FilenameUtils.getBaseName(oldname), 
                extension = FilenameUtils.getExtension(oldname);
        return filejson.set("filename", newname+"."+extension);
    }

    /**
     * List the set of files in a given foled that meets a given extension
     * @param folder The folder to search files for.
     * @param extension The expected extension of files in that folder
     * @return An array of file names in that folder which have this extension
     */
    public static String[] listFiles(String folder, String extension) {
        String [] filenames;
        File dir = new File(folder);
        File[] folderinputs = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(extension.toLowerCase());
            }
        });      
        filenames = new String [folderinputs.length];
        for (int i=0; i<folderinputs.length; i++) 
            filenames[i] = folderinputs[i].getName();
        return filenames;
    } 
    
}
