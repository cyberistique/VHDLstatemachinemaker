import java.util.*;

public class State {
    private int out;
    private String name;
    private String out_name;
    private Map<String, Integer> outputs;
    private List<Transition> transitions;

    public State(String name, int out, String out_name) {
        this.name = name;
        this.out = out;
        this.out_name = out_name;
        this.outputs = null;
        this.transitions = new ArrayList<>();
    }

    public State(String name, Map<String, Integer> outputs) {
        this.name = name;
        this.out = 0;
        this.out_name = null;
        this.outputs = new HashMap<>(outputs);
        this.transitions = new ArrayList<>();
    }

    public void add_transition(Transition new_state) {
        this.transitions.add(new_state);
    }

    public String getOut(MachineStruct machine) {
        StringBuilder out = new StringBuilder("when " + this.name + " =>\n");
        if (outputs == null) {
            if (out_name == null) {
                out.append("    null;\n");
            } else {
                out.append("    ").append(machine.formatOutputAssign(out_name, this.out)).append("\n");
            }
            return out.toString();
        }

        for (String outputName : machine.getOutputs()) {
            int value = outputs.getOrDefault(outputName, 0);
            out.append("    ").append(machine.formatOutputAssign(outputName, value)).append("\n");
        }
        return out.toString();
    }

    public String getName() {
        return this.name;
    }

    public void setOutput(String outputName, int value) {
        if (outputs == null) outputs = new HashMap<>();
        outputs.put(outputName, value);
    }

    public List<Transition> getTransitions() {
        return this.transitions;
    }

    public String case_() {
        StringBuilder out = new StringBuilder("when " + this.name + " =>\n");
        if (transitions.isEmpty()) {
            out.append("    null;\n");
            return out.toString();
        }

        Transition first = transitions.getFirst();
        if (first.isNone()) {
            out.append("    sstate <= ").append(first.getNext().getName()).append(first.getDelay()).append(";\n");
            return out.toString();
        }

        boolean closed = false;
        out.append("    if ").append(first.getInputName()).append(" = '").append(first.getInput()).append("' then\n");
        out.append("        sstate <= ").append(first.getNext().getName()).append(first.getDelay()).append(";\n");

        for (int i = 1; i < transitions.size(); i++) {
            Transition t = transitions.get(i);
            boolean last = i == transitions.size() - 1;

            if (last) {
                out.append("    else\n");
                out.append("        sstate <= ").append(t.getNext().getName()).append(t.getDelay()).append(";\n");
                out.append("    end if;\n");
                closed = true;
            } else if (t.isNone()) {
                out.append("    else\n");
                out.append("        sstate <= ").append(t.getNext().getName()).append(t.getDelay()).append(";\n");
                out.append("    end if;\n");
                closed = true;
                return out.toString();
            } else {
                out.append("    elsif ").append(t.getInputName()).append(" = '").append(t.getInput()).append("' then\n");
                out.append("        sstate <= ").append(t.getNext().getName()).append(t.getDelay()).append(";\n");
            }
        }

        if (!closed) {
            out.append("    else\n");
            out.append("        sstate <= ").append(this.name).append(";\n");
            out.append("    end if;\n");
        }

        return out.toString();
    }

        @Override
        public String toString () {
            return this.name + " | " + this.out_name + " | " + this.out + " |\n";
        }
    }
