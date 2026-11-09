import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.util.Optional;

public class Main extends Application {

    private double income = 0;
    private double expenses = 0;
    private double monthlyBudget = 0;

    private ObservableList<Transaction> transactions =
            FXCollections.observableArrayList();

    private Label incomeLabel;
    private Label expenseLabel;
    private Label balanceLabel;
    private Label budgetLabel;

    private ListView<String> transactionList;

    @Override
    public void start(Stage primaryStage) {

        loadData();

        Label title = new Label("Smart Personal Expense Planner");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        incomeLabel = new Label();
        expenseLabel = new Label();
        balanceLabel = new Label();
        budgetLabel = new Label();

        Button addIncomeButton = new Button("Add Income");
        Button addExpenseButton = new Button("Add Expense");
        Button setBudgetButton = new Button("Set Monthly Budget");
        Button reportButton = new Button("Expense Report");

        addIncomeButton.setOnAction(e -> addTransaction("INCOME"));
        addExpenseButton.setOnAction(e -> addTransaction("EXPENSE"));
        setBudgetButton.setOnAction(e -> setBudget());
        reportButton.setOnAction(e -> showExpenseReport());

        transactionList = new ListView<>();

        VBox dashboard = new VBox(10);
        dashboard.setPadding(new Insets(15));

        dashboard.getChildren().addAll(
                title,
                incomeLabel,
                expenseLabel,
                balanceLabel,
                budgetLabel,
                addIncomeButton,
                addExpenseButton,
                setBudgetButton,
                reportButton,
                new Label("Transaction History"),
                transactionList
        );

        updateDashboard();

        Scene scene = new Scene(dashboard, 500, 600);

        primaryStage.setTitle("Smart Personal Expense Planner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addTransaction(String type) {

        ChoiceDialog<String> categoryDialog;

        if (type.equals("INCOME")) {
            categoryDialog = new ChoiceDialog<>(
                    "Salary",
                    "Salary",
                    "Business",
                    "Freelance",
                    "Other"
            );
        } else {
            categoryDialog = new ChoiceDialog<>(
                    "Food",
                    "Food",
                    "Travel",
                    "Shopping",
                    "Bills",
                    "Education",
                    "Entertainment",
                    "Health",
                    "Other"
            );
        }

        categoryDialog.setTitle("Transaction Category");
        categoryDialog.setHeaderText("Select " + type + " Category");
        categoryDialog.setContentText("Category:");

        Optional<String> categoryResult = categoryDialog.showAndWait();

        if (!categoryResult.isPresent()) {
            return;
        }

        TextInputDialog amountDialog = new TextInputDialog();

        amountDialog.setTitle("Transaction Amount");
        amountDialog.setHeaderText("Enter " + type + " Amount");
        amountDialog.setContentText("Amount:");

        Optional<String> amountResult = amountDialog.showAndWait();

        if (!amountResult.isPresent()) {
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountResult.get());

            if (!Double.isFinite(amount) || amount <= 0) {
                showWarning("Invalid Amount",
                        "Please enter a valid amount greater than zero.");
                return;
            }

        } catch (NumberFormatException ex) {
            showWarning("Invalid Amount",
                    "Please enter a valid numeric amount.");
            return;
        }

        String category = categoryResult.get();

        // Check available income before adding an expense
        if (type.equals("EXPENSE")) {

            double availableBalance = income - expenses;

            if (amount > availableBalance) {
                showWarning(
                        "Insufficient Balance",
                        "Expense cannot be added!\n\n"
                                + "Available Balance: " + availableBalance
                                + "\nRequested Expense: " + amount
                );
                return;
            }

            // Check monthly budget before adding an expense
            if (monthlyBudget > 0 && expenses + amount > monthlyBudget) {

                showWarning(
                        "Budget Exceeded",
                        "Expense cannot be added!\n\n"
                                + "Monthly Budget: " + monthlyBudget
                                + "\nCurrent Expenses: " + expenses
                                + "\nRequested Expense: " + amount
                                + "\n\nYour expense exceeds the set budget."
                );

                return;
            }
        }

        // Add transaction only after all validations pass
        Transaction transaction =
                new Transaction(type, category, amount);

        transactions.add(transaction);

        if (type.equals("INCOME")) {
            income += amount;
        } else {
            expenses += amount;
        }

        saveData();
        updateDashboard();

        showMessage(
                "Success",
                type + " added successfully!"
        );
    }

    private void setBudget() {

        TextInputDialog dialog =
                new TextInputDialog(String.valueOf(monthlyBudget));

        dialog.setTitle("Monthly Budget");
        dialog.setHeaderText("Set Your Monthly Budget");
        dialog.setContentText("Enter Budget Amount:");

        Optional<String> result = dialog.showAndWait();

        if (!result.isPresent()) {
            return;
        }

        try {
            double budget = Double.parseDouble(result.get());

            if (!Double.isFinite(budget) || budget <= 0) {
                showWarning(
                        "Invalid Budget",
                        "Please enter a valid budget greater than zero."
                );
                return;
            }

            monthlyBudget = budget;

            saveData();
            updateDashboard();

            if (expenses > monthlyBudget) {
                showWarning(
                        "Budget Exceeded",
                        "Your current expenses are already greater than "
                                + "the newly set budget.\n"
                                + "New expenses will not be allowed until "
                                + "they are within the budget."
                );
            } else {
                showMessage(
                        "Success",
                        "Monthly Budget set successfully!"
                );
            }

        } catch (NumberFormatException ex) {
            showWarning(
                    "Invalid Budget",
                    "Please enter a valid numeric budget."
            );
        }
    }

    private void updateDashboard() {

        incomeLabel.setText("Total Income: " + income);

        expenseLabel.setText("Total Expenses: " + expenses);

        balanceLabel.setText(
                "Available Balance: " + (income - expenses)
        );

        budgetLabel.setText(
                "Monthly Budget: "
                        + (monthlyBudget > 0
                        ? monthlyBudget
                        : "Not Set")
        );

        if (transactionList != null) {

            transactionList.getItems().clear();

            for (Transaction t : transactions) {

                String item = t.type + " | "
                        + t.category + " | "
                        + t.amount;

                transactionList.getItems().add(item);
            }
        }
    }

    private void showExpenseReport() {

        String report = "Expense Report\n\n"
                + "Total Income: " + income + "\n"
                + "Total Expenses: " + expenses + "\n"
                + "Available Balance: " + (income - expenses) + "\n"
                + "Monthly Budget: "
                + (monthlyBudget > 0 ? monthlyBudget : "Not Set");

        showMessage("Expense Report", report);
    }

    private void saveData() {

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter("planner_data.txt"))) {

            writer.println(income);
            writer.println(expenses);
            writer.println(monthlyBudget);

            writer.println(transactions.size());

            for (Transaction t : transactions) {

                writer.println(t.type);
                writer.println(t.category);
                writer.println(t.amount);
            }

        } catch (IOException e) {
            showWarning(
                    "Save Error",
                    "Unable to save data: " + e.getMessage()
            );
        }
    }

    private void loadData() {

        File file = new File("planner_data.txt");

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            income = Double.parseDouble(reader.readLine());
            expenses = Double.parseDouble(reader.readLine());
            monthlyBudget = Double.parseDouble(reader.readLine());

            int count = Integer.parseInt(reader.readLine());

            for (int i = 0; i < count; i++) {

                String type = reader.readLine();
                String category = reader.readLine();
                double amount = Double.parseDouble(reader.readLine());

                transactions.add(
                        new Transaction(type, category, amount)
                );
            }

        } catch (Exception e) {

            income = 0;
            expenses = 0;
            monthlyBudget = 0;
            transactions.clear();

            System.out.println(
                    "Unable to load saved data: " + e.getMessage()
            );
        }
    }

    private void showMessage(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private void showWarning(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.WARNING);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    static class Transaction {

        String type;
        String category;
        double amount;

        Transaction(String type, String category, double amount) {

            this.type = type;
            this.category = category;
            this.amount = amount;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}