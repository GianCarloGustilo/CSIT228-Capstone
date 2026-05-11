package com.example.capstone.controller;

import com.example.capstone.model.Category;
import com.example.capstone.model.Product;
import com.example.capstone.model.Supplier;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class AddProductController {

    @FXML private TextField  nameField;
    @FXML private TextField  descField;
    @FXML private TextField  priceField;
    @FXML private TextField  quantityField;
    @FXML private TextField  thresholdField;
    @FXML private ComboBox<Category> categoryCombo;
    @FXML private ComboBox<Supplier> supplierCombo;

    private boolean saved   = false;
    private Product product = null;


    /* Called by MainController before the dialog opens */
    public void setCategories(List<Category> categories) {
        categoryCombo.setItems(FXCollections.observableArrayList(categories));
    }

    public void setSuppliers(List<Supplier> suppliers) {
        supplierCombo.setItems(FXCollections.observableArrayList(suppliers));
    }

    /* Pre-fill fields when editing an existing product */
    public void prefill(Product p) {
        nameField     .setText(String.valueOf(p.getName()));
        descField     .setText(p.getDescription() != null ? p.getDescription() : "");
        priceField    .setText(String.valueOf(p.getPrice()));
        quantityField .setText(String.valueOf(p.getQuantity()));
        thresholdField.setText(String.valueOf(p.getLowStockThreshold()));

        categoryCombo.getItems().stream()
                .filter(c -> c.getCategoryId() == p.getCategoryId())
                .findFirst()
                .ifPresent(categoryCombo::setValue);

        supplierCombo.getItems().stream()
                .filter(s -> s.getSupplierId() == p.getSupplierId())
                .findFirst()
                .ifPresent(supplierCombo::setValue);
    }


    @FXML
    public void onSave() {

        String name = nameField.getText().trim();
        String priceText    = priceField.getText().trim();
        String quantityText = quantityField.getText().trim();

        if (name.isEmpty() || priceText.isEmpty() || quantityText.isEmpty()) {
            javafx.scene.control.Alert alert =
                    new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.WARNING,
                            "Name, price, and quantity are required.",
                            javafx.scene.control.ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
            return;
        }

        try {
            double price    = Double.parseDouble(priceText);
            int    quantity = Integer.parseInt(quantityText);
            int    threshold = thresholdField.getText().isBlank()
                    ? 10
                    : Integer.parseInt(thresholdField.getText().trim());

            Category selectedCategory = categoryCombo.getValue();
            Supplier selectedSupplier = supplierCombo.getValue();

            product = new Product();
            product.setName(name);
            product.setDescription(descField.getText().trim());
            product.setPrice(price);
            product.setQuantity(quantity);
            product.setLowStockThreshold(threshold);
            product.setCategoryId(selectedCategory != null ? selectedCategory.getCategoryId() : 0);
            product.setSupplierId(selectedSupplier != null ? selectedSupplier.getSupplierId() : 0);

            saved = true;
            close();

        } catch (NumberFormatException e) {
            javafx.scene.control.Alert alert =
                    new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.WARNING,
                            "Price and quantity must be valid numbers.",
                            javafx.scene.control.ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    @FXML
    public void onCancel() {
        close();
    }

    private void close() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    public boolean isSaved()   { return saved; }
    public Product getProduct() { return product; }
}