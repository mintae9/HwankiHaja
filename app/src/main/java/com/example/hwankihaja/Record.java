package com.example.hwankihaja;

public class Record {
    private int auto = 1;
    private int status = 1;

    public int cnt = 0;

    public int getStatus(){
        return status;
    }
    public void setStatus(int s){
        this.status = s;
    }
    public int getAuto(){
        return auto;
    }
    public void setAuto(int a){
        this.auto = a;
    }

    private static Record instance = null;
    public static Record getInstance() {
        if(null == instance){
            instance = new Record();
        }
        return instance;
    }
}
