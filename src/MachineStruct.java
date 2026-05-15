import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;
import java.util.TreeMap;

public class MachineStruct {
    private final static String MOORE = "moore";
    private final static String MEELY = "meely";
    private String[] inputs;
    private String[] outputs;
    private String type;
    private int out_size;
    private State idle;
    private String name;
    private HashMap<String,State> State_Dict;

    public MachineStruct(String name,String type, int out_size,int inpSize, int outSize) {
        this.inputs = new String[inpSize];
        this.outputs = new String[outSize];
        this.type = type;
        this.name = name;
        this.out_size = out_size;
        this.State_Dict = new HashMap<>();
    }


    public void set_idle(State idle){
        this.idle = idle;
    }

    public void setInputs(String[] inputs){
        this.inputs = inputs;
    }

    public void setOutputs(String[] outputs){
        this.outputs = outputs;
    }

    public String getCase(String option) {
        StringBuilder T_out;
        if (option.equals("IO")) {
            T_out = new StringBuilder("port(\n" +
                    "    clk         : in std_logic;\n"
                    );
            for (String i: inputs){
                T_out.append("   "+i+"   : in std_logic;\n");
            }
            for (int i = 0; i< outputs.length ; i++) {
                if (i == outputs.length - 1) {
                    T_out.append("   " + outputs[i] + "   : out std_logic);\n");
                } else {
                    T_out.append("   " + outputs[i] + "   : out std_logic;\n");
                }
            }
            T_out.append("end entity;\n");

        } else if (option.equals("name")){
            T_out = new StringBuilder("architecture beh of "+name+" is\n"+"type t_State is (");
            T_out.append(iterate(option, State_Dict));
            T_out.append(");\nsignal sstate : t_State;\n");
        } else{
            T_out = new StringBuilder("case sstate is\n");
            T_out.append(iterate(option, State_Dict));
            T_out.append("end case;\n");

        }
        return T_out.toString();
    }

    public String iterate(String option, HashMap<String,State> states){
        StringBuilder T_out = new StringBuilder();
        for (String key : states.keySet()) {
            if (option.equals("case")){
                T_out.append(states.get(key).case_());
            } else if (option.equals("out")) {
                T_out.append(states.get(key).getOut());
            } else if (option.equals("name")) {
                T_out.append(states.get(key).getName()+',');
            } else {
                T_out.append(states.get(key));
            }
        }
        return T_out.toString();
    }

    public String getName(){
        return name;
    }

    public String clock_(){
        return "begin\n" +
                "process(clk) is\n" +
                "begin\n" +
                "if rising_edge(clk) then\n" +
                "   if rst = '0' then\n" +
                "       sstate <= "+idle.getName()+";\n"+
                "   else\n"+getCase("case")+
                "end if;\n" +
                "end process;\n";
    }

    @Override
    public String toString(){
        return iterate("case",State_Dict);
    }

    public static void main(String[] args) throws FileNotFoundException {
        int stage = 0;
        Scanner in = new Scanner(new File(args[0]));
        String[] header = in.nextLine().split("\\s+");
        String[] inputs = in.nextLine().split("\\s+");
        String[] outputs = in.nextLine().split("\\s+");
        MachineStruct main = new MachineStruct(header[0],header[1],Integer.parseInt(header[2]), inputs.length, outputs.length);
        main.setInputs(inputs);
        main.setOutputs(outputs);

        String[] next = in.nextLine().split("\\s+");
        State newState_  = new State(next[0], Integer.parseInt(next[2]), next[1]);
        main.State_Dict.put(next[0], newState_);
        main.idle = newState_;

        while (in.hasNext()) {
            String in_line = in.nextLine();
            if (in_line.trim().isEmpty()){
                stage = 1;
            }
            else {
                if (stage == 0) {
                    next = in_line.split("\\s+");
                    State newState = new State(next[0], Integer.parseInt(next[2]), next[1]);
                    main.State_Dict.put(next[0], newState);
                    System.out.println("LINE: " + in_line);
                } else if (stage == 1){
                    next = in_line.split("\\s+");
                    System.out.println("LINE: " + in_line);
                    if ((next.length - 1) % 4 == 0) {
                        for (int i = 0; i< (next.length-1)/4;i++){
                            Transition next_transition;
                            if (Objects.equals(next[4*i + 1], "NA")){
                                next_transition = new Transition(main.State_Dict.get(next[4*i+4]),Integer.parseInt(next[4*i+3]));
                            } else {
                                next_transition = new Transition(main.State_Dict.get(next[4*i+4]),next[4*i+1],Integer.parseInt(next[4*i+2]),Integer.parseInt(next[4*i+3]));
                            }
                            main.State_Dict.get(next[0]).add_transition(next_transition);
                        }
                    }
                }
            }
        }
        System.out.println(main.getCase("IO"));
        System.out.println(main.getCase("name"));
        System.out.println(main.clock_());
        System.out.println(main.getCase("case"));
        System.out.println(main.getCase("out"));
    }

}
