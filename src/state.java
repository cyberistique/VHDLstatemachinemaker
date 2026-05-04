import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class state {
    private static int out;
    private static String name;
    private static String out_name;
    private static List<Transition> transitions;

    public state (String name, int out, String out_name) {
        this.name = name;
        this.out = out;
        this.out_name = out_name;
        this.transitions = new ArrayList<>();
    }

    public void add_transition(Transition new_state){
        this.transitions.add(new_state);
    }

    public String getOut() {
        return "when "+name+" => "+out_name+" <= "+out;
    }

    public String getName(){
        return name+",";
    }

    public List<Transition> getTransitions(){
        return transitions;
    }

    public String case_(){
        StringBuilder T_out = new StringBuilder();
        for (int i = 0;i<transitions.size() ;i++){
            Transition t_curr = transitions.get(i);
            if (i==0) {
                if (t_curr.isNone()) {
                    T_out.append("else sstate <= "+t_curr.getNext().getName()+t_curr.getDelay()+";\n");
                } else {
                    T_out.append("sstate <= " + t_curr.getNext().getName() + ";\n");
                }
            } else {
                if (t_curr.isNone()) {
                    T_out.append("else sstate <= "+t_curr.getNext().getName()+";\n");
                }
                else{
                    T_out.append("els"+t_curr.condition()+"sstate <= "+t_curr.getNext().getName()+";\n");
                }
            }

        }
        return T_out.toString();
    }

    public String iterate(String option) {
        String first;
        if (option.equals("case_")) {
            first = this.case_();
        } else if (option.equals("getName")){
            first = name +",";
        } else if (option.equals("getOut")){
            first = this.getOut();
        }else {
            first = this.toString();
        }
        StringBuilder T_out = new StringBuilder(first);
        for (Transition i : this.transitions) {
            if (option.equals("case_")) {
                T_out.append(i.getNext().case_());
            } else if (option.equals("getName")){
                T_out.append(i.getNext().getName());
            } else if (option.equals("getOut")) {
                T_out.append(i.getNext().getOut());
            } else {
                this.toString();
            }
            T_out.append(i.getNext().iterate(option));
        }

        return T_out.toString();
    }

    @Override
    public String toString(){
        return name+" | "+out_name+" | "+out+" |\n";
    }
}
