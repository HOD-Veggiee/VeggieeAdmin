package com.veggiee.veggieeadmin.Model;

public class Staff {

    private String Name, Password, Phone, Roll;

    public Staff() {
    }

    public Staff(String name, String password, String phone, String roll) {
        Name = name;
        Password = password;
        Phone = phone;
        Roll = roll;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getRoll() {
        return Roll;
    }

    public void setRoll(String roll) {
        Roll = roll;
    }
}
