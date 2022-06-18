/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class ExitRequestedException extends UncheckedIOException{
    
    public ExitRequestedException(String message, IOException cause) {
        super(message, cause);
    }
    
}
