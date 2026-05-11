package com.example.capstone.model;

public class Supplier {

    private int    supplierId;
    private String name;
    private String contactName;
    private String phone;
    private String email;
    private String address;


    public Supplier() {}

    public Supplier(int supplierId, String name,
                    String contactName, String phone,
                    String email,      String address) {
        this.supplierId  = supplierId;
        this.name        = name;
        this.contactName = contactName;
        this.phone       = phone;
        this.email       = email;
        this.address     = address;
    }


    public int    getSupplierId()                  { return supplierId; }
    public void   setSupplierId(int id)            { this.supplierId = id; }

    public String getName()                        { return name; }
    public void   setName(String name)             { this.name = name; }

    public String getContactName()                 { return contactName; }
    public void   setContactName(String c)         { this.contactName = c; }

    public String getPhone()                       { return phone; }
    public void   setPhone(String phone)           { this.phone = phone; }

    public String getEmail()                       { return email; }
    public void   setEmail(String email)           { this.email = email; }

    public String getAddress()                     { return address; }
    public void   setAddress(String address)       { this.address = address; }

    /* ComboBox display label */
    @Override
    public String toString() { return name; }
}