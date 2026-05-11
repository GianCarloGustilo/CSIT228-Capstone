package com.example.capstone;

import com.example.capstone.database.ProductDAO;
import com.example.capstone.model.Product;

public class TestDatabase {

    public static void main(String[] args) {

        ProductDAO dao =
                new ProductDAO();


        dao.insertProduct(

                new Product(
                        0,
                        "Keyboard",
                        20,
                        1500
                )
        );


        dao.getAllProducts()

                .forEach(

                        product ->

                                System.out.println(

                                        product.getName()
                                )
                );
    }
}