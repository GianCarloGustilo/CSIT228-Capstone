package com.example.capstone.controller;

import com.example.capstone.database.CategoryDAO;
import com.example.capstone.database.ProductDAO;
import com.example.capstone.database.SupplierDAO;
import com.example.capstone.model.Category;
import com.example.capstone.model.Product;
import com.example.capstone.model.Supplier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainController {

    /* ── Stat card labels ── */
    @FXML private Label statSkuLabel;
    @FXML private Label statValueLabel;
    @FXML private Label statLowLabel;

    /* ── Search field ── */
    @FXML private TextField searchField;

    /* ── Products table ── */
    @FXML private TableView<Product>           productTable;
    @FXML private TableColumn<Product,Integer> idColumn;
    @FXML private TableColumn<Product,String>  nameColumn;
    @FXML private TableColumn<Product,String>  categoryColumn;
    @FXML private TableColumn<Product,String>  supplierColumn;
    @FXML private TableColumn<Product,Double>  priceColumn;
    @FXML private TableColumn<Product,Integer> qtyColumn;

    /* ── Categories table ── */
    @FXML private TableView<Category>           categoryTable;
    @FXML private TableColumn<Category,Integer> catIdColumn;
    @FXML private TableColumn<Category,String>  catNameColumn;
    @FXML private TableColumn<Category,String>  catDescColumn;

    /* ── Suppliers table ── */
    @FXML private TableView<Supplier>           supplierTable;
    @FXML private TableColumn<Supplier,Integer> supIdColumn;
    @FXML private TableColumn<Supplier,String>  supNameColumn;
    @FXML private TableColumn<Supplier,String>  supContactColumn;
    @FXML private TableColumn<Supplier,String>  supPhoneColumn;
    @FXML private TableColumn<Supplier,String>  supEmailColumn;

    /* ── Content panes (only one visible at a time) ── */
    @FXML private javafx.scene.layout.VBox productsPane;
    @FXML private javafx.scene.layout.VBox categoriesPane;
    @FXML private javafx.scene.layout.VBox suppliersPane;

    /* ── Sidebar nav buttons ── */
    @FXML private Button navProducts;
    @FXML private Button navCategories;
    @FXML private Button navSuppliers;

    /* ── DAOs ── */
    private final ProductDAO  productDAO  = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    /* ── Observable lists ── */
    private final ObservableList<Product>  productList  = FXCollections.observableArrayList();
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList();


    /* ══════════════════════════════════════════════════════════
       INITIALIZE
       ══════════════════════════════════════════════════════════ */
    @FXML
    public void initialize() {
        setupProductTable();
        setupCategoryTable();
        setupSupplierTable();
        setupSearch();
        loadAll();
        showProducts();   // default view
    }


    /* ── Table column bindings ── */

    private void setupProductTable() {
        idColumn      .setCellValueFactory(new PropertyValueFactory<>("productId"));
        nameColumn    .setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        priceColumn   .setCellValueFactory(new PropertyValueFactory<>("price"));
        qtyColumn     .setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // colour low-stock rows amber
        productTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                if (p == null || empty) {
                    setStyle("");
                } else if (p.getQuantity() < p.getLowStockThreshold()) {
                    setStyle("-fx-background-color: rgba(239,159,39,0.08);");
                } else {
                    setStyle("");
                }
            }
        });

        productTable.setItems(productList);
    }

    private void setupCategoryTable() {
        catIdColumn  .setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        catNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        catDescColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryTable.setItems(categoryList);
    }

    private void setupSupplierTable() {
        supIdColumn     .setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        supNameColumn   .setCellValueFactory(new PropertyValueFactory<>("name"));
        supContactColumn.setCellValueFactory(new PropertyValueFactory<>("contactName"));
        supPhoneColumn  .setCellValueFactory(new PropertyValueFactory<>("phone"));
        supEmailColumn  .setCellValueFactory(new PropertyValueFactory<>("email"));
        supplierTable.setItems(supplierList);
    }

    /* ── Live search ── */
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            productList.setAll(
                    newVal.isBlank()
                            ? productDAO.getAllProducts()
                            : productDAO.searchProducts(newVal.trim())
            );
            refreshStatCards();
        });
    }


    /* ══════════════════════════════════════════════════════════
       LOAD DATA
       ══════════════════════════════════════════════════════════ */
    private void loadAll() {
        productList .setAll(productDAO .getAllProducts());
        categoryList.setAll(categoryDAO.getAll());
        supplierList.setAll(supplierDAO.getAll());
        refreshStatCards();
    }

    private void refreshStatCards() {
        int[]  summary = productDAO.getSummary();
        double value   = productDAO.getTotalValue();
        statSkuLabel .setText(String.valueOf(summary[0]));
        statValueLabel.setText(String.format("₱%,.2f", value));
        statLowLabel .setText(String.valueOf(summary[1]));
    }


    /* ══════════════════════════════════════════════════════════
       SIDEBAR NAVIGATION
       ══════════════════════════════════════════════════════════ */
    private static final String ACTIVE_STYLE =
            "-fx-background-color: rgba(110,231,183,0.08); " +
                    "-fx-background-radius: 8; -fx-text-fill: #6EE7B7; " +
                    "-fx-font-family: 'Outfit'; -fx-font-size: 13; " +
                    "-fx-alignment: CENTER_LEFT; -fx-padding: 9 10; -fx-border-width: 0;";

    private static final String INACTIVE_STYLE =
            "-fx-background-color: transparent; " +
                    "-fx-background-radius: 8; -fx-text-fill: #6B7280; " +
                    "-fx-font-family: 'Outfit'; -fx-font-size: 13; " +
                    "-fx-alignment: CENTER_LEFT; -fx-padding: 9 10; -fx-border-width: 0;";

    @FXML
    public void showProducts() {
        productsPane  .setVisible(true);  productsPane  .setManaged(true);
        categoriesPane.setVisible(false); categoriesPane.setManaged(false);
        suppliersPane .setVisible(false); suppliersPane .setManaged(false);
        navProducts  .setStyle(ACTIVE_STYLE);
        navCategories.setStyle(INACTIVE_STYLE);
        navSuppliers .setStyle(INACTIVE_STYLE);
        productList.setAll(productDAO.getAllProducts());
        refreshStatCards();
    }

    @FXML
    public void showCategories() {
        productsPane  .setVisible(false); productsPane  .setManaged(false);
        categoriesPane.setVisible(true);  categoriesPane.setManaged(true);
        suppliersPane .setVisible(false); suppliersPane .setManaged(false);
        navProducts  .setStyle(INACTIVE_STYLE);
        navCategories.setStyle(ACTIVE_STYLE);
        navSuppliers .setStyle(INACTIVE_STYLE);
        categoryList.setAll(categoryDAO.getAll());
    }

    @FXML
    public void showSuppliers() {
        productsPane  .setVisible(false); productsPane  .setManaged(false);
        categoriesPane.setVisible(false); categoriesPane.setManaged(false);
        suppliersPane .setVisible(true);  suppliersPane .setManaged(true);
        navProducts  .setStyle(INACTIVE_STYLE);
        navCategories.setStyle(INACTIVE_STYLE);
        navSuppliers .setStyle(ACTIVE_STYLE);
        supplierList.setAll(supplierDAO.getAll());
    }


    /* ══════════════════════════════════════════════════════════
       PRODUCT ACTIONS
       ══════════════════════════════════════════════════════════ */
    @FXML
    public void onAddProduct() {
        openProductDialog(null);
    }

    @FXML
    public void onEditProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a product to edit.");
            return;
        }
        openProductDialog(selected);
    }

    @FXML
    public void onDeleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a product to delete.");
            return;
        }
        Optional<ButtonType> result = showConfirm(
                "Delete \"" + selected.getName() + "\"? This cannot be undone.");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            productDAO.deleteProduct(selected.getProductId());
            loadAll();
        }
    }

    private void openProductDialog(Product existing) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/capstone/AddProductView.fxml"));
            Parent root = loader.load();

            AddProductController ctrl = loader.getController();
            ctrl.setCategories(categoryDAO.getAll());
            ctrl.setSuppliers(supplierDAO.getAll());
            if (existing != null) ctrl.prefill(existing);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(existing == null ? "Add Product" : "Edit Product");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            if (ctrl.isSaved()) {
                Product p = ctrl.getProduct();
                if (existing == null) {
                    productDAO.insertProduct(p);
                } else {
                    p.setProductId(existing.getProductId());
                    productDAO.updateProduct(p);
                }
                loadAll();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /* ══════════════════════════════════════════════════════════
       CATEGORY ACTIONS
       ══════════════════════════════════════════════════════════ */
    @FXML
    public void onAddCategory() {
        openCategoryDialog(null);
    }

    @FXML
    public void onEditCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select a category to edit."); return; }
        openCategoryDialog(selected);
    }

    @FXML
    public void onDeleteCategory() {
        Category selected = categoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select a category to delete."); return; }
        Optional<ButtonType> result = showConfirm(
                "Delete category \"" + selected.getName() + "\"?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            categoryDAO.delete(selected.getCategoryId());
            categoryList.setAll(categoryDAO.getAll());
        }
    }

    private void openCategoryDialog(Category existing) {
        // Inline dialog using a simple TextInputDialog-based flow
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Category" : "Edit Category");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #111214;");

        TextField nameField = styledField(existing != null ? existing.getName() : "");
        TextField descField = styledField(existing != null ? existing.getDescription() : "");

        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(10,
                styledLabel("Category Name"), nameField,
                styledLabel("Description"),   descField);
        box.setStyle("-fx-padding: 20; -fx-background-color: #111214;");
        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !nameField.getText().isBlank()) {
                Category c = existing != null ? existing : new Category();
                c.setName(nameField.getText().trim());
                c.setDescription(descField.getText().trim());
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(c -> {
            if (existing == null) categoryDAO.insert(c);
            else                  categoryDAO.update(c);
            categoryList.setAll(categoryDAO.getAll());
        });
    }


    /* ══════════════════════════════════════════════════════════
       SUPPLIER ACTIONS
       ══════════════════════════════════════════════════════════ */
    @FXML
    public void onAddSupplier() {
        openSupplierDialog(null);
    }

    @FXML
    public void onEditSupplier() {
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select a supplier to edit."); return; }
        openSupplierDialog(selected);
    }

    @FXML
    public void onDeleteSupplier() {
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a supplier to delete.");
            return;
        }

        Optional<ButtonType> result = showConfirm(
                "Delete supplier \"" + selected.getName() + "\"?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            supplierDAO.delete(selected.getSupplierId());
            supplierList.setAll(supplierDAO.getAll());
        }
    }

    private void openSupplierDialog(Supplier existing) {
        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Supplier" : "Edit Supplier");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: #111214;");

        TextField nameField    = styledField(existing != null ? existing.getName()        : "");
        TextField contactField = styledField(existing != null ? existing.getContactName() : "");
        TextField phoneField   = styledField(existing != null ? existing.getPhone()       : "");
        TextField emailField   = styledField(existing != null ? existing.getEmail()       : "");
        TextField addressField = styledField(existing != null ? existing.getAddress()     : "");

        javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(10,
                styledLabel("Company Name"),   nameField,
                styledLabel("Contact Person"), contactField,
                styledLabel("Phone"),          phoneField,
                styledLabel("Email"),          emailField,
                styledLabel("Address"),        addressField);
        box.setStyle("-fx-padding: 20; -fx-background-color: #111214;");
        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !nameField.getText().isBlank()) {
                Supplier s = existing != null ? existing : new Supplier();
                s.setName(nameField.getText().trim());
                s.setContactName(contactField.getText().trim());
                s.setPhone(phoneField.getText().trim());
                s.setEmail(emailField.getText().trim());
                s.setAddress(addressField.getText().trim());
                return s;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(s -> {
            if (existing == null) supplierDAO.insert(s);
            else                  supplierDAO.update(s);
            supplierList.setAll(supplierDAO.getAll());
        });
    }


    /* ══════════════════════════════════════════════════════════
       HELPERS
       ══════════════════════════════════════════════════════════ */
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirm(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        return alert.showAndWait();
    }

    private TextField styledField(String value) {
        TextField tf = new TextField(value);
        tf.setStyle("-fx-background-color: #1A1C1F; -fx-text-fill: #F5F4F0; " +
                "-fx-border-color: rgba(255,255,255,0.10); -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-padding: 8 12;");
        return tf;
    }

    private Label styledLabel(String text) {
        Label l = new Label(text.toUpperCase());
        l.setStyle("-fx-font-family: 'DM Mono'; -fx-font-size: 10; -fx-text-fill: #6B7280;");
        return l;
    }
}