package com.example.capstone.model;

public class Category {

    private int categoryId;
    private String name;
    private String description;


    public Category() {}

    public Category(int categoryId, String name, String description) {
        this.categoryId  = categoryId;
        this.name        = name;
        this.description = description;
    }


    public int    getCategoryId()              { return categoryId; }
    public void   setCategoryId(int id)        { this.categoryId = id; }

    public String getName()                    { return name; }
    public void   setName(String name)         { this.name = name; }

    public String getDescription()             { return description; }
    public void   setDescription(String desc)  { this.description = desc; }

    /* ComboBox uses toString() as the display label */
    @Override
    public String toString() { return name; }
}