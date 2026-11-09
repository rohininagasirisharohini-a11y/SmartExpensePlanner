
package com.expenseplanner;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class ExpenseDashboard extends Application {

    private final ObservableList<Transaction> transactions =
            FXCollections.observableArrayList();

    private static final String FILE_NAME = "planner_data.txt";

    private double budget = 8000;

    private Label incomeLabel, expenseLabel;
    private Label balanceLabel, budgetLabel;
    private Label monthlyIncomeLabel, monthlyExpenseLabel;
    private Label savingsLabel;

    private TableView<Transaction> table;
    private PieChart pieChart;
    private BarChart<String, Number> monthlyChart;

    private ComboBox<String> monthBox;

    // Transaction class
    public static class Transaction {

        private String type;
        private String category;
        private double amount;
        private LocalDate date;

        public Transaction(String type, String category,
                           double amount, LocalDate date) {
            this.type = type;
            this.category = category;
            this.amount = amount;
            this.date = date;
        }

        public String getType() {
            return type;
        }

        public String getCategory() {
            return category;
        }

        public double getAmount() {
            return amount;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    @Override
    public void start(Stage stage) {

        loadData();

        Label title = new Label("SMART EXPENSE PLANNER");

        title.setStyle(
                "-fx-font-size: 25px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        HBox header = new HBox(title);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #263238;");

        // Dashboard Summary

        incomeLabel = new Label();
        expenseLabel = new Label();
        balanceLabel = new Label();
        budgetLabel = new Label();

        VBox summary = new VBox(
                12,
                incomeLabel,
                expenseLabel,
                balanceLabel,
                budgetLabel
        );

        summary.setPadding(new Insets(20));

        summary.setStyle(
                "-fx-background-color: #E3F2FD;" +
                        "-fx-background-radius: 12;"
        );

        // Buttons

        Button addIncome = new Button("Add Income");
        Button addExpense = new Button("Add Expense");
        Button setBudget = new Button("Set Budget");
        Button monthlyReport = new Button("Monthly Report");
        Button exportCSV = new Button("Export CSV");

        addIncome.setOnAction(e ->
                showTransactionDialog("INCOME", null));

        addExpense.setOnAction(e ->
                showTransactionDialog("EXPENSE", null));

        setBudget.setOnAction(e -> setBudget());

        monthlyReport.setOnAction(e -> {
            refreshMonths();
            updateMonthlyReport();
        });

        exportCSV.setOnAction(e -> exportCSV(stage));

        addIncome.setStyle(
                "-fx-background-color: #2E7D32;" +
                        "-fx-text-fill: white;"
        );

        addExpense.setStyle(
                "-fx-background-color: #C62828;" +
                        "-fx-text-fill: white;"
        );

        setBudget.setStyle(
                "-fx-background-color: #1565C0;" +
                        "-fx-text-fill: white;"
        );

        monthlyReport.setStyle(
                "-fx-background-color: #7B1FA2;" +
                        "-fx-text-fill: white;"
        );

        exportCSV.setStyle(
                "-fx-background-color: #00897B;" +
                        "-fx-text-fill: white;"
        );

        HBox buttons = new HBox(
                10,
                addIncome,
                addExpense,
                setBudget,
                monthlyReport,
                exportCSV
        );

        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(15));

        // Transaction History

        Label historyTitle =
                new Label("TRANSACTION HISTORY");

        historyTitle.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;"
        );

        table = new TableView<>();
        table.setItems(transactions);

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        TableColumn<Transaction, String> typeCol =
                new TableColumn<>("Type");

        typeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getType()
                )
        );

        TableColumn<Transaction, String> categoryCol =
                new TableColumn<>("Category");

        categoryCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getCategory()
                )
        );

        TableColumn<Transaction, String> amountCol =
                new TableColumn<>("Amount");

        amountCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format(
                                "Rs. %.2f",
                                data.getValue().getAmount()
                        )
                )
        );

        TableColumn<Transaction, String> dateCol =
                new TableColumn<>("Date");

        dateCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getDate().toString()
                )
        );

        // Edit and Delete Buttons

        TableColumn<Transaction, Void> actionCol =
                new TableColumn<>("Actions");

        actionCol.setCellFactory(column ->
                new TableCell<>() {

                    private final Button edit = new Button("Edit");
                    private final Button delete = new Button("Delete");

                    private final HBox box =
                            new HBox(8, edit, delete);

                    {
                        edit.setOnAction(e -> {

                            Transaction t =
                                    getTableView().getItems()
                                            .get(getIndex());

                            showTransactionDialog(t.getType(), t);
                        });

                        delete.setOnAction(e -> {

                            Transaction t =
                                    getTableView().getItems()
                                            .get(getIndex());

                            Alert alert = new Alert(
                                    Alert.AlertType.CONFIRMATION
                            );

                            alert.setTitle("Delete Transaction");
                            alert.setHeaderText("Confirm Delete");

                            alert.setContentText(
                                    "Delete " + t.getCategory() + "?"
                            );

                            alert.showAndWait().ifPresent(response -> {

                                if (response == ButtonType.OK) {

                                    transactions.remove(t);

                                    saveData();
                                    updateDashboard();
                                    refreshMonths();
                                    updateMonthlyReport();
                                }
                            });
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {

                        super.updateItem(item, empty);

                        setGraphic(empty ? null : box);
                    }
                }
        );

        table.getColumns().addAll(
                typeCol,
                categoryCol,
                amountCol,
                dateCol,
                actionCol
        );

        table.setPrefHeight(300);

        // Monthly Report Section

        Label reportTitle =
                new Label("MONTHLY FINANCIAL REPORT");

        reportTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
        );

        monthBox = new ComboBox<>();
        monthBox.setPromptText("Select Month");

        monthlyIncomeLabel = new Label();
        monthlyExpenseLabel = new Label();
        savingsLabel = new Label();

        VBox monthlySummary = new VBox(
                10,
                monthlyIncomeLabel,
                monthlyExpenseLabel,
                savingsLabel
        );

        monthlySummary.setPadding(new Insets(15));

        monthlySummary.setStyle(
                "-fx-background-color: #F3E5F5;" +
                        "-fx-background-radius: 10;"
        );

        monthBox.setOnAction(e -> updateMonthlyReport());

        // Pie Chart

        pieChart = new PieChart();
        pieChart.setTitle("Category-wise Expenses");
        pieChart.setPrefHeight(300);

        // Monthly Bar Chart

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Month");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount (Rs.)");

        monthlyChart = new BarChart<>(xAxis, yAxis);

        monthlyChart.setTitle("Monthly Income vs Expenses");
        monthlyChart.setPrefHeight(350);

        Button refreshReport =
                new Button("Refresh Monthly Report");

        refreshReport.setOnAction(e -> {
            refreshMonths();
            updateMonthlyReport();
        });

        HBox monthControls = new HBox(
                12,
                new Label("Choose Month:"),
                monthBox,
                refreshReport
        );

        monthControls.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(
                18,
                summary,
                buttons,
                historyTitle,
                table,
                reportTitle,
                monthControls,
                monthlySummary,
                pieChart,
                monthlyChart
        );

        content.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);

        BorderPane root = new BorderPane();

        root.setTop(header);
        root.setCenter(scroll);

        updateDashboard();
        refreshMonths();
        updateMonthlyReport();

        Scene scene = new Scene(root, 1100, 850);

        stage.setTitle("Smart Expense Planner");
        stage.setScene(scene);
        stage.show();
    }

    // ==========================================
    // MONTHLY EXPENSE CALCULATION
    // ==========================================

    private double getMonthlyExpenses(
            YearMonth month,
            Transaction excludeTransaction) {

        double total = 0;

        for (Transaction t : transactions) {

            if (t.getType().equals("EXPENSE")
                    && YearMonth.from(t.getDate()).equals(month)
                    && t != excludeTransaction) {

                total += t.getAmount();
            }
        }

        return total;
    }

    // ==========================================
    // BUDGET LIMIT VALIDATION
    // ==========================================

    private boolean isBudgetExceeded(
            double newExpense,
            LocalDate expenseDate,
            Transaction existing) {

        YearMonth month = YearMonth.from(expenseDate);

        double currentExpenses =
                getMonthlyExpenses(month, existing);

        double totalAfterAdding =
                currentExpenses + newExpense;

        if (totalAfterAdding > budget + 0.000001) {

            double remaining = budget - currentExpenses;

            String message =
                    "BUDGET LIMIT EXCEEDED!\n\n" +
                            "Monthly Budget: Rs. " +
                            String.format("%.2f", budget) + "\n" +

                            "Already Spent: Rs. " +
                            String.format("%.2f", currentExpenses) + "\n" +

                            "New Expense: Rs. " +
                            String.format("%.2f", newExpense) + "\n\n" +

                            "Remaining Budget: Rs. " +
                            String.format("%.2f", Math.max(0, remaining)) +
                            "\n\n" +

                            "Expense cannot be added.\n" +
                            "Please enter an amount within your budget.";

            showAlert(message);

            return false;
        }

        return true;
    }

    // ==========================================
    // ADD AND EDIT TRANSACTION
    // ==========================================

    private void showTransactionDialog(
            String defaultType,
            Transaction existing) {

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle(
                existing == null
                        ? "Add Transaction"
                        : "Edit Transaction"
        );

        ButtonType save = new ButtonType(
                "Save",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                save,
                ButtonType.CANCEL
        );

        ComboBox<String> typeBox = new ComboBox<>();

        typeBox.getItems().addAll("INCOME", "EXPENSE");
        typeBox.setValue(defaultType);

        ComboBox<String> categoryBox = new ComboBox<>();

        categoryBox.setPromptText("Select Category");

        TextField amountField = new TextField();

        amountField.setPromptText("Enter Amount");

        DatePicker datePicker =
                new DatePicker(LocalDate.now());

        Runnable categories = () -> {

            if ("INCOME".equals(typeBox.getValue())) {

                categoryBox.setItems(
                        FXCollections.observableArrayList(
                                "Salary",
                                "Business",
                                "Freelance",
                                "Investment",
                                "Other"
                        )
                );

            } else {

                categoryBox.setItems(
                        FXCollections.observableArrayList(
                                "Food",
                                "Travel",
                                "Education",
                                "Shopping",
                                "Health",
                                "Entertainment",
                                "Bills",
                                "Other"
                        )
                );
            }
        };

        typeBox.setOnAction(e -> {

            categories.run();

            categoryBox.setValue(null);
        });

        categories.run();

        if (existing != null) {

            typeBox.setValue(existing.getType());

            categories.run();

            categoryBox.setValue(existing.getCategory());

            amountField.setText(
                    String.valueOf(existing.getAmount())
            );

            datePicker.setValue(existing.getDate());
        }

        typeBox.setDisable(existing == null);

        GridPane grid = new GridPane();

        grid.setHgap(12);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeBox, 1, 0);

        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryBox, 1, 1);

        grid.add(new Label("Amount:"), 0, 2);
        grid.add(amountField, 1, 2);

        grid.add(new Label("Date:"), 0, 3);
        grid.add(datePicker, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> button);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == save) {

            try {

                String category = categoryBox.getValue();

                double amount = Double.parseDouble(
                        amountField.getText().trim()
                );

                LocalDate date = datePicker.getValue();

                String type = typeBox.getValue();

                if (category == null || category.isBlank()
                        || date == null
                        || type == null
                        || !Double.isFinite(amount)
                        || amount <= 0) {

                    throw new IllegalArgumentException();
                }

                // IMPORTANT BUDGET CHECK
                // Check before adding or updating transaction.

                if ("EXPENSE".equals(type)) {

                    boolean allowed = isBudgetExceeded(
                            amount,
                            date,
                            existing
                    );

                    if (!allowed) {

                        return;
                    }
                }

                // Add New Transaction

                if (existing == null) {

                    transactions.add(
                            new Transaction(
                                    type,
                                    category,
                                    amount,
                                    date
                            )
                    );

                } else {

                    // Update Existing Transaction

                    existing.setType(type);
                    existing.setCategory(category);
                    existing.setAmount(amount);
                    existing.setDate(date);

                    table.refresh();
                }

                saveData();

                updateDashboard();

                refreshMonths();

                updateMonthlyReport();

            } catch (Exception ex) {

                showAlert(
                        "Enter valid transaction details."
                );
            }
        }
    }

    // ==========================================
    // SET BUDGET
    // ==========================================

    private void setBudget() {

        TextInputDialog dialog =
                new TextInputDialog(String.valueOf(budget));

        dialog.setTitle("Set Budget");

        dialog.setHeaderText("Monthly Budget");

        dialog.setContentText("Enter Budget:");

        dialog.showAndWait().ifPresent(value -> {

            try {

                double amount =
                        Double.parseDouble(value.trim());

                if (!Double.isFinite(amount) || amount < 0) {

                    throw new NumberFormatException();
                }

                budget = amount;

                saveData();

                updateDashboard();

                updateMonthlyReport();

                showMessage(
                        "Monthly Budget Updated Successfully!\n" +
                                "Budget: Rs. " +
                                String.format("%.2f", budget)
                );

            } catch (NumberFormatException ex) {

                showAlert("Enter a valid budget.");
            }
        });
    }

    // ==========================================
    // DASHBOARD CALCULATION
    // ==========================================

    private void updateDashboard() {

        double income = 0;

        double expenses = 0;

        for (Transaction t : transactions) {

            if (t.getType().equals("INCOME")) {

                income += t.getAmount();

            } else {

                expenses += t.getAmount();
            }
        }

        incomeLabel.setText(
                String.format(
                        "Income: Rs. %.2f",
                        income
                )
        );

        expenseLabel.setText(
                String.format(
                        "Expenses: Rs. %.2f",
                        expenses
                )
        );

        balanceLabel.setText(
                String.format(
                        "Balance: Rs. %.2f",
                        income - expenses
                )
        );

        double currentMonthExpenses =
                getMonthlyExpenses(
                        YearMonth.now(),
                        null
                );

        double remaining =
                budget - currentMonthExpenses;

        budgetLabel.setText(
                String.format(
                        "Monthly Budget: Rs. %.2f | " +
                                "Spent: Rs. %.2f | " +
                                "Remaining: Rs. %.2f",
                        budget,
                        currentMonthExpenses,
                        remaining
                )
        );

        updatePieChart();
    }

    // ==========================================
    // PIE CHART
    // ==========================================

    private void updatePieChart() {

        Map<String, Double> totals =
                new LinkedHashMap<>();

        for (Transaction t : transactions) {

            if (t.getType().equals("EXPENSE")) {

                totals.merge(
                        t.getCategory(),
                        t.getAmount(),
                        Double::sum
                );
            }
        }

        pieChart.getData().clear();

        totals.forEach((category, amount) ->

                pieChart.getData().add(
                        new PieChart.Data(category, amount)
                )
        );
    }

    // ==========================================
    // POPULATE MONTH DROPDOWN
    // ==========================================

    private void refreshMonths() {

        String previous = monthBox.getValue();

        Set<YearMonth> months =
                new TreeSet<>(Comparator.reverseOrder());

        months.add(YearMonth.now());

        for (Transaction t : transactions) {

            months.add(
                    YearMonth.from(t.getDate())
            );
        }

        monthBox.setItems(
                FXCollections.observableArrayList(
                        months.stream()
                                .map(YearMonth::toString)
                                .toList()
                )
        );

        if (previous != null &&
                monthBox.getItems().contains(previous)) {

            monthBox.setValue(previous);

        } else {

            monthBox.setValue(
                    YearMonth.now().toString()
            );
        }
    }

    // ==========================================
    // MONTHLY REPORT
    // ==========================================

    private void updateMonthlyReport() {

        if (monthBox == null ||
                monthlyIncomeLabel == null) {

            return;
        }

        String selected = monthBox.getValue();

        if (selected == null) {
            return;
        }

        YearMonth month = YearMonth.parse(selected);

        double income = 0;

        double expenses = 0;

        for (Transaction t : transactions) {

            if (YearMonth.from(t.getDate()).equals(month)) {

                if (t.getType().equals("INCOME")) {

                    income += t.getAmount();

                } else {

                    expenses += t.getAmount();
                }
            }
        }

        monthlyIncomeLabel.setText(
                String.format(
                        "Monthly Income: Rs. %.2f",
                        income
                )
        );

        monthlyExpenseLabel.setText(
                String.format(
                        "Monthly Expenses: Rs. %.2f",
                        expenses
                )
        );

        savingsLabel.setText(
                String.format(
                        "Monthly Savings: Rs. %.2f",
                        income - expenses
                )
        );

        updateMonthlyChart();
    }

    // ==========================================
    // BAR CHART
    // ==========================================

    private void updateMonthlyChart() {

        monthlyChart.getData().clear();

        XYChart.Series<String, Number> incomeSeries =
                new XYChart.Series<>();

        incomeSeries.setName("Income");

        XYChart.Series<String, Number> expenseSeries =
                new XYChart.Series<>();

        expenseSeries.setName("Expenses");

        Map<YearMonth, double[]> totals =
                new TreeMap<>();

        for (Transaction t : transactions) {

            YearMonth month =
                    YearMonth.from(t.getDate());

            double[] values = totals.computeIfAbsent(
                    month,
                    k -> new double[2]
            );

            if (t.getType().equals("INCOME")) {

                values[0] += t.getAmount();

            } else {

                values[1] += t.getAmount();
            }
        }

        for (Map.Entry<YearMonth, double[]> entry :
                totals.entrySet()) {

            String month = entry.getKey().toString();

            incomeSeries.getData().add(
                    new XYChart.Data<>(
                            month,
                            entry.getValue()[0]
                    )
            );

            expenseSeries.getData().add(
                    new XYChart.Data<>(
                            month,
                            entry.getValue()[1]
                    )
            );
        }

        monthlyChart.getData().addAll(
                incomeSeries,
                expenseSeries
        );
    }

    // ==========================================
    // EXPORT CSV
    // ==========================================

    private void exportCSV(Stage stage) {

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Export Transactions");

        chooser.setInitialFileName("Expense_Report.csv");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "CSV Files",
                        "*.csv"
                )
        );

        File file = chooser.showSaveDialog(stage);

        if (file == null) {
            return;
        }

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(file))) {

            writer.println("Type,Category,Amount,Date");

            for (Transaction t : transactions) {

                writer.printf(
                        Locale.US,
                        "%s,%s,%.2f,%s%n",
                        csvEscape(t.getType()),
                        csvEscape(t.getCategory()),
                        t.getAmount(),
                        t.getDate()
                );
            }

            showMessage("CSV exported successfully!");

        } catch (IOException ex) {

            showAlert("Unable to export CSV file.");
        }
    }

    // CSV Escape

    private String csvEscape(String value) {

        return "\"" +
                value.replace("\"", "\"\"") +
                "\"";
    }

    // ==========================================
    // SAVE DATA
    // ==========================================

    private void saveData() {

        try (PrintWriter writer =
                     new PrintWriter(
                             new FileWriter(FILE_NAME)
                     )) {

            writer.println("BUDGET|" + budget);

            for (Transaction t : transactions) {

                writer.println(
                        t.getType() + "|" +
                                t.getCategory() + "|" +
                                t.getAmount() + "|" +
                                t.getDate()
                );
            }

        } catch (IOException ex) {

            showAlert("Unable to save data.");
        }
    }

    // ==========================================
    // LOAD DATA
    // ==========================================

    private void loadData() {

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(file)
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("\\|", -1);

                if (parts.length == 2 &&
                        parts[0].equals("BUDGET")) {

                    budget = Double.parseDouble(parts[1]);

                } else if (parts.length == 4 &&
                        (parts[0].equals("INCOME") ||
                                parts[0].equals("EXPENSE"))) {

                    transactions.add(
                            new Transaction(
                                    parts[0],
                                    parts[1],
                                    Double.parseDouble(parts[2]),
                                    LocalDate.parse(parts[3])
                            )
                    );
                }
            }

        } catch (Exception ex) {

            transactions.clear();

            showAlert("Unable to load saved data.");
        }
    }

    // ==========================================
    // WARNING ALERT
    // ==========================================

    private void showAlert(String message) {

        Alert alert =
                new Alert(Alert.AlertType.WARNING);

        alert.setTitle("Smart Expense Planner");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    // ==========================================
    // SUCCESS MESSAGE
    // ==========================================

    private void showMessage(String message) {

        Alert alert =
                new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Success");

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
