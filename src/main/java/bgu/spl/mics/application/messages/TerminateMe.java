package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class TerminateMe implements Broadcast{    
    private String senderName;


    public TerminateMe(String senderName) {
        this.senderName = senderName;
    }
    public String getSenderId() {
        return senderName;
    }
}

