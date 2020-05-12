package com.example.hwankihaja;

public class Record {
    private String status = "1";
    public String getStatus(){
        return status;
    }
    public void setStatus(String s){
        this.status = s;
    }

    private static Record instance = null;
    public static Record getInstance() {
        if(null == instance){
            instance = new Record();
        }
        return instance;
    }
}
