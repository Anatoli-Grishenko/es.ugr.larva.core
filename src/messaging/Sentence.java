/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messaging;

import swing.LARVADash;

/**
 *
 * @author Anatoli Grishenko Anatoli.Grishenko@gmail.com
 */
public class Sentence {
    protected String sentence;
    protected String sentenceTokens[];
    protected int token;
    
    public Sentence() {
        token = -1;
    }

    public String getSentence() {
        return sentence;
    }
    
    public String getPastSentence() {
        String res ="";
        for (int i=0; i<token; i++)
            res += sentenceTokens[i]+" ";
        return res;
    }
    
    public String getFutureSentence() {
        String res ="";
        for (int i=token; i<size(); i++)
            res += sentenceTokens[i]+" ";
        return res;
    }
    
    public String getCursorSentence() {
        return getPastSentence()+" ^^^ "+getFutureSentence();
    }
    
    public Sentence parseSentence(String sentence) {
        if (sentence.contains(LARVADash.MARK))
            sentence = sentence.replace(LARVADash.MARK, "");
        this.sentence = sentence;
        this.sentenceTokens = this.sentence.split(" ");
        token=0;
        return this;
    }
    
    public boolean hasNext() {
        return token < sentenceTokens.length;
    }
    
    public String next() {
        if (hasNext()) {
            return sentenceTokens[token++];
        } else
            return null;
    }

    public String next(int many) {
        while(next() != null && many >1) {
            many--;
        }
        return next();
    }

    public boolean isNext(String tok) {
        if (hasNext()) {
            if (sentenceTokens[token].toUpperCase().equals(tok.toUpperCase())) {
                token++;
                return true;
            } else {
                return false;
            }
        } else {           
            return false;
        }
    }
    
    public boolean isNext(String tok1, String tok2) {
        if (hasNext()) {
            return isNext(tok1.toUpperCase()) && 
                    isNext(tok2.toUpperCase());
        } else
            return false;
    }
    
    public boolean isNext(String tok1, String tok2, String tok3) {
        if (hasNext()) {
            return isNext(tok1.toUpperCase()) && 
                    isNext(tok2.toUpperCase()) &&
                    isNext(tok3.toUpperCase());
        } else
            return false;
    }
    
    public int size() {
        if (sentenceTokens == null)
            return -1;
        else
            return sentenceTokens.length;
    }
    
    
}
