/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package telegram2;

import java.util.HashMap;

/**
 *
 * @author Anatoli Grishenko <Anatoli.Grishenko@gmail.com>
 */
public class KeyboardSystem {

    protected HashMap<Long, String> ContextKeyboard;
    protected HashMap<String, Keyboard> Keyboards;
    protected Keyboard Default;

    public KeyboardSystem() {
        ContextKeyboard = new HashMap();
        Keyboards = new HashMap();
        Default = null;
    }

    public KeyboardSystem addKeyBoard(Keyboard k) {
        Keyboards.put(k.getName(), k);
        if (Default == null) {
            setDefaultKeyboard(k.getName());
        }
        return this;
    }

    public KeyboardSystem linkKeyboard(Long cid, String kname) {
        if (Keyboards.keySet().contains(kname)) {
            ContextKeyboard.put(cid, kname);
        }
        return this;
    }

    public KeyboardSystem setDefaultKeyboard(String kname) {
        if (Keyboards.keySet().contains(kname)) {
            Default = Keyboards.get(kname);
        }
        return this;
    }
    
    public Keyboard getLinkedKeyboard(Long cid) {
        return Keyboards.get(ContextKeyboard.get(cid));
    }
    public Keyboard getKeyboard(String kid) {
        return Keyboards.get(kid);
    }
}
