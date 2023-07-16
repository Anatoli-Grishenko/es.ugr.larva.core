/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface OleSerializer {
   String ToolTip()default ""; 
   boolean FromFile() default false;
   boolean FromFolder() default true;
   String [] SelectFrom() default  {}; 
   String TriggersTo() default "NONE";
   String Label() default "NONE";
   String SelectWith() default "NONE";   // Fill combobox
   boolean Validate() default false;   // Fill combobox
   String ValidateLabelGUI() default "DEFAULT";  // Button's label
    
}
