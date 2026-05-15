import java.util.*;

public class State {
    private int out;
    private String name;
    private String out_name;
    private List<Transition> transitions;

    public State(String name, int out, String out_name) {
        this.name = name;
        this.out = out;
        this.out_name = out_name;
        this.transitions = new ArrayList<>();
    }

    public void add_transition(Transition new_state) {
        this.transitions.add(new_state);
    }

    public String getOut() {
        return "when " + this.name + " => " + this.out_name + " <= " + this.out+";\n";
    }

    public String getName() {
        return this.name;
    }

    public List<Transition> getTransitions() {
        return this.transitions;
    }

    public String case_() {
        StringBuilder T_out = new StringBuilder("when "+this.name+" => ");
        for (int i = 0; i < transitions.size(); i++) {
            Transition t_curr = transitions.get(i);
            if (i == 0) {
                if (t_curr.isNone()) {
                    T_out.append("sstate <= " + t_curr.getNext().getName() + t_curr.getDelay() + ";\n");
                } else {
                    T_out.append("sstate <= " + t_curr.condition() + t_curr.getNext().getName() + ";\n");
                }
            } else if (i == transitions.size()-1) {
                T_out.append("  else sstate <= " + t_curr.getNext().getName() + ";\n"+" end if;\n");
            } else {
                if (t_curr.isNone()) {
                    T_out.append("  elsif sstate <= " + t_curr.getNext().getName() + ";\n");
                }
            }
            }
            return T_out.toString();
    }

        @Override
        public String toString () {
            return this.name + " | " + this.out_name + " | " + this.out + " |\n";
        }
    }
