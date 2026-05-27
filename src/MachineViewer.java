import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.util.Objects;

public class MachineViewer extends Application implements Observer<MachineModel, String> {
    private MachineModel model;
    private Label statusLabel;

    @Override
    public void init() {
        String outBaseName = getParameters().getRaw().isEmpty() ? "fsm" : getParameters().getRaw().getFirst();
        this.model = new MachineModel(outBaseName);
        this.model.addObserver(this);
    }

    @Override
    public void start(Stage stage) throws Exception {
        TextField machineName = new TextField("my_fsm");
        ChoiceBox<String> machineType = new ChoiceBox<>();
        machineType.getItems().addAll("moore", "meely");
        machineType.setValue("moore");

        TextField defaultOutWidth = new TextField("1");
        defaultOutWidth.setPrefColumnCount(4);

        TextArea inputs = new TextArea("inp1 inp2");
        inputs.setPromptText("Space-separated input names (excluding clk/rst)");
        inputs.setPrefRowCount(2);

        TextArea outputs = new TextArea("out1:1\nout2:8");
        outputs.setPromptText("Outputs: one per line as name:width (width optional, uses default)");
        outputs.setPrefRowCount(2);

        TextArea states = new TextArea("S0 out1=0 out2=0\nS1 out1=1 out2=3");
        states.setPromptText("One per line: <stateName> out1=0 out2=3 ... (first line is idle)");
        states.setPrefRowCount(6);

        TableView<TransitionRow> transitionTable = new TableView<>();
        transitionTable.setEditable(true);
        ObservableList<TransitionRow> transitionRows = FXCollections.observableArrayList();
        transitionTable.setItems(transitionRows);

        TableColumn<TransitionRow, String> fromCol = new TableColumn<>("From");
        fromCol.setCellValueFactory(c -> c.getValue().from);
        fromCol.setOnEditCommit(e -> e.getRowValue().from.set(e.getNewValue()));

        TableColumn<TransitionRow, String> inputNameCol = new TableColumn<>("Input");
        inputNameCol.setCellValueFactory(c -> c.getValue().inputName);
        inputNameCol.setOnEditCommit(e -> e.getRowValue().inputName.set(e.getNewValue()));

        TableColumn<TransitionRow, Integer> inputValueCol = new TableColumn<>("Value");
        inputValueCol.setCellValueFactory(c -> c.getValue().inputValue.asObject());
        inputValueCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        inputValueCol.setOnEditCommit(e -> e.getRowValue().inputValue.set(e.getNewValue()));

        TableColumn<TransitionRow, Integer> delayCol = new TableColumn<>("Delay(ns)");
        delayCol.setCellValueFactory(c -> c.getValue().delay.asObject());
        delayCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        delayCol.setOnEditCommit(e -> e.getRowValue().delay.set(e.getNewValue()));

        TableColumn<TransitionRow, String> toCol = new TableColumn<>("To");
        toCol.setCellValueFactory(c -> c.getValue().to);
        toCol.setOnEditCommit(e -> e.getRowValue().to.set(e.getNewValue()));

        transitionTable.getColumns().addAll(fromCol, inputNameCol, inputValueCol, delayCol, toCol);
        transitionTable.setPrefHeight(220);

        Button refreshChoices = new Button("Refresh lists");
        refreshChoices.setOnAction(e -> {
            try {
                String[] inputList = splitWords(inputs.getText());
                String[] stateList = parseStateNames(states.getText());

                fromCol.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(stateList)));
                toCol.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(stateList)));

                ObservableList<String> inputChoices = FXCollections.observableArrayList();
                inputChoices.add("NA");
                inputChoices.addAll(inputList);
                inputNameCol.setCellFactory(ComboBoxTableCell.forTableColumn(inputChoices));
            } catch (Exception ex) {
                update(model, "Error: " + ex.getMessage());
            }
        });

        Button addTransition = new Button("Add transition");
        addTransition.setOnAction(e -> transitionRows.add(new TransitionRow()));

        Button removeTransition = new Button("Remove selected");
        removeTransition.setOnAction(e -> {
            TransitionRow selected = transitionTable.getSelectionModel().getSelectedItem();
            if (selected != null) transitionRows.remove(selected);
        });

        HBox transitionButtons = new HBox(8, refreshChoices, addTransition, removeTransition);

        Button generate = new Button("Generate VHDL");
        CheckBox genTb = new CheckBox("Also generate testbench");
        genTb.setSelected(true);
        generate.setOnAction(e -> {
            try {
                MachineStruct built = buildFromForm(
                        machineName.getText().trim(),
                        machineType.getValue(),
                        Integer.parseInt(defaultOutWidth.getText().trim()),
                        inputs.getText(),
                        outputs.getText(),
                        states.getText(),
                        transitionRows
                );
                model.setMachine(built);
                model.FSMmake();
                if (genTb.isSelected()) model.TBmake();
            } catch (Exception ex) {
                update(model, "Error: " + ex.getMessage());
            }
        });

        statusLabel = new Label(model.getStatus());

        VBox root = new VBox(10,
                row("Machine name", machineName),
                row("Type", machineType),
                row("Default out width", defaultOutWidth),
                block("Inputs", inputs),
                block("Outputs", outputs),
                block("States", states),
                new Label("Transitions:"),
                transitionButtons,
                transitionTable,
                genTb,
                generate,
                statusLabel
        );
        root.setStyle("-fx-padding: 12;");
        VBox.setVgrow(states, Priority.ALWAYS);

        stage.setTitle("VHDL FSM Generator");
        stage.setScene(new Scene(root, 720, 720));
        refreshChoices.fire();
        transitionRows.add(new TransitionRow());
        stage.show();

    }

    @Override
    public void update(MachineModel Model, String message) {
        if (statusLabel != null) statusLabel.setText(message);
    }

    private static HBox row(String label, javafx.scene.Node field) {
        Label l = new Label(label + ":");
        l.setMinWidth(110);
        HBox box = new HBox(8, l, field);
        HBox.setHgrow(field, Priority.ALWAYS);
        return box;
    }

    private static VBox block(String label, javafx.scene.Node field) {
        return new VBox(4, new Label(label + ":"), field);
    }

    private static MachineStruct buildFromForm(
            String machineName,
            String type,
            int defaultOutWidth,
            String inputsText,
            String outputsText,
            String statesText,
            ObservableList<TransitionRow> transitions
    ) {
        String[] inputs = splitWords(inputsText);
        if (machineName.isBlank()) throw new IllegalArgumentException("Machine name is required");
        if (inputs.length == 0) throw new IllegalArgumentException("At least one input is required");

        ParsedOutputs parsedOutputs = parseOutputs(outputsText, defaultOutWidth);
        if (parsedOutputs.names.length == 0) throw new IllegalArgumentException("At least one output is required");

        String[] stateLines = statesText.lines().map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        if (stateLines.length == 0) throw new IllegalArgumentException("At least one state is required");

        MachineStruct machine = new MachineStruct(machineName, type, defaultOutWidth, inputs.length, parsedOutputs.names.length);
        machine.setInputs(inputs);
        machine.setOutputs(parsedOutputs.names, parsedOutputs.widths);

        for (int i = 0; i < stateLines.length; i++) {
            String[] parts = stateLines[i].split("\\s+");
            if (parts.length < 1) throw new IllegalArgumentException("Bad state line: " + stateLines[i]);

            String stateName = parts[0];
            java.util.Map<String, Integer> outMap = new java.util.HashMap<>();
            for (int p = 1; p < parts.length; p++) {
                String token = parts[p];
                String[] kv = token.split("=", 2);
                if (kv.length != 2) throw new IllegalArgumentException("Bad output token in state line: " + token);
                outMap.put(kv[0], Integer.parseInt(kv[1]));
            }
            State s = new State(stateName, outMap);
            machine.addState(s);
            if (i == 0) machine.set_idle(s);
        }

        for (TransitionRow row : transitions) {
            String from = row.from.get();
            String to = row.to.get();
            String inputName = row.inputName.get();
            int inputValue = row.inputValue.get();
            int delay = row.delay.get();

            if (from == null || from.isBlank()) throw new IllegalArgumentException("Transition row missing From");
            if (to == null || to.isBlank()) throw new IllegalArgumentException("Transition row missing To");
            if (inputName == null || inputName.isBlank()) inputName = "NA";

            State fromState = machine.getState(from);
            State toState = machine.getState(to);
            if (fromState == null) throw new IllegalArgumentException("Unknown from-state: " + from);
            if (toState == null) throw new IllegalArgumentException("Unknown to-state: " + to);

            Transition t;
            if (Objects.equals(inputName, "NA")) {
                t = new Transition(toState, delay);
            } else {
                t = new Transition(toState, inputName, inputValue, delay);
            }
            fromState.add_transition(t);
        }

        return machine;
    }

    private static String[] parseStateNames(String statesText) {
        return statesText.lines()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(line -> line.split("\\s+")[0])
                .toArray(String[]::new);
    }

    private record ParsedOutputs(String[] names, int[] widths) {
    }

    private static ParsedOutputs parseOutputs(String outputsText, int defaultWidth) {
        java.util.List<String> names = new java.util.ArrayList<>();
        java.util.List<Integer> widths = new java.util.ArrayList<>();

        outputsText.lines()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(line -> {
                    String[] parts = line.split("\\s+");
                    for (String token : parts) {
                        if (token.isEmpty()) continue;
                        String name = token;
                        int width = defaultWidth;
                        if (token.contains(":")) {
                            String[] nv = token.split(":", 2);
                            name = nv[0];
                            width = Integer.parseInt(nv[1]);
                        }
                        names.add(name);
                        widths.add(width);
                    }
                });

        String[] n = names.toArray(String[]::new);
        int[] w = new int[widths.size()];
        for (int i = 0; i < widths.size(); i++) w[i] = widths.get(i);
        return new ParsedOutputs(n, w);
    }

    private static String[] splitWords(String text) {
        return text.lines()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .flatMap(s -> java.util.Arrays.stream(s.split("\\s+")))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    static class TransitionRow {
        final SimpleStringProperty from = new SimpleStringProperty("");
        final SimpleStringProperty inputName = new SimpleStringProperty("NA");
        final SimpleIntegerProperty inputValue = new SimpleIntegerProperty(0);
        final SimpleIntegerProperty delay = new SimpleIntegerProperty(0);
        final SimpleStringProperty to = new SimpleStringProperty("");
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
