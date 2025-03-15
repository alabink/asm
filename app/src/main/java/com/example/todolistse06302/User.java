package com.example.todolistse06302;

public class User {
    private int id;
    private String email;
    private String password;
    public User(int id , String email , String password){
        this.id=id;
        this.email=email;
        this.password=password;
    }

    //getter and setter method
    public int getId(){
        return this.id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getEmail(){
        return this.email;
    }
    public void setEmail(String email){
        this.email=email;
    }
    public String getPassword(){
        return this.password;
    }
    public void setPassword(String password){
        this.password=password;
    }
}
