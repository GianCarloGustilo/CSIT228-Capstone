package com.example.capstone.model;

public class Product {

    private int    productId;
    private int    categoryId;
    private int    supplierId;
    private String name;
    private String description;
    private int    quantity;
    private double price;
    private int    lowStockThreshold;

    /* Joined display fields (not columns in products table) */
    private String categoryName;
    private String supplierName;


    public Product() {}

    /* Constructor used when loading from DB with JOIN */
    public Product(int productId, int categoryId, int supplierId,
                   String name, String description,
                   int quantity, double price, int lowStockThreshold,
                   String categoryName, String supplierName) {
        this.productId         = productId;
        this.categoryId        = categoryId;
        this.supplierId        = supplierId;
        this.name              = name;
        this.description       = description;
        this.quantity          = quantity;
        this.price             = price;
        this.lowStockThreshold = lowStockThreshold;
        this.categoryName      = categoryName;
        this.supplierName      = supplierName;
    }

    /* Legacy constructor kept so existing code compiles */
    public Product(int productId, String name, int quantity, double price) {
        this.productId = productId;
        this.name      = name;
        this.quantity  = quantity;
        this.price     = price;
    }


    public int    getProductId()                       { return productId; }
    public void   setProductId(int id)                 { this.productId = id; }

    public int    getCategoryId()                      { return categoryId; }
    public void   setCategoryId(int id)                { this.categoryId = id; }

    public int    getSupplierId()                      { return supplierId; }
    public void   setSupplierId(int id)                { this.supplierId = id; }

    public String getName()                            { return name; }
    public void   setName(String name)                 { this.name = name; }

    public String getDescription()                     { return description; }
    public void   setDescription(String d)             { this.description = d; }

    public int    getQuantity()                        { return quantity; }
    public void   setQuantity(int quantity)            { this.quantity = quantity; }

    public double getPrice()                           { return price; }
    public void   setPrice(double price)               { this.price = price; }

    public int    getLowStockThreshold()               { return lowStockThreshold; }
    public void   setLowStockThreshold(int t)          { this.lowStockThreshold = t; }

    public String getCategoryName()                    { return categoryName; }
    public void   setCategoryName(String n)            { this.categoryName = n; }

    public String getSupplierName()                    { return supplierName; }
    public void   setSupplierName(String n)            { this.supplierName = n; }
}