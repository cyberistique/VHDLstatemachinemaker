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
    private state idle;
    private HashMap<String,state> State_Dict;

    public MachineStruct(String type, int out_size,int inpSize, int outSize) {
        this.inputs = new String[inpSize];
        this.outputs = new String[outSize];
        this.type = type;
        this.out_size = out_size;
        this.State_Dict = new HashMap<>();
    }


    public void set_idle(state idle){
        this.idle = idle;
    }

    public void setInputs(String[] inputs){
        this.inputs = inputs;
    }

    public void setOutputs(String[] outputs){
        this.outputs = outputs;
    }

    public String getCase(String option){
        StringBuilder T_out = new StringBuilder("case sstate is\n");
        T_out.append(idle.iterate(option));
        T_out.append("end case;\n");
        return T_out.toString();
    }

    @Override
    public String toString(){
        String out = '|'+type+'|'+out_size+'\n';
        return out+idle.toString();
    }
    public static void Main(String[] args){
        int stage = 0;
        Scanner in = new Scanner(args[0]);
        String[] header = in.nextLine().split(" ");
        String[] inputs = in.nextLine().split(" ");
        String[] outputs = in.nextLine().split(" ");
        MachineStruct main = new MachineStruct(header[0],Integer.parseInt(header[1]), inputs.length, outputs.length);
        main.setInputs(inputs);
        main.setOutputs(outputs);
        while (in.hasNext()) {
            String in_line = in.nextLine();
            if (in_line.trim().isEmpty()){
                stage = 1;
            }
            else {
                if (stage == 0) {
                    String[] next = in_line.split(" ");
                    state newState = new state(next[0], Integer.parseInt(next[2]), next[1]);
                    main.State_Dict.put(next[0], newState);
                } else if (stage == 1){
                    String[] next = in_line.split(" ");
                    if ((next.length-1%4)==0) {
                        for (int i = 0; i< (next.length-1)/4;i++){
                            Transition next_transition;
                            if (Objects.equals(next[i + 1], "NA")){
                                next_transition = new Transition(main.State_Dict.get(next[i+4]),Integer.parseInt(next[i+3]));
                            } else {
                                next_transition = new Transition(main.State_Dict.get(next[i+4]),next[i+1],Integer.parseInt(next[i+2]),Integer.parseInt(next[i+3]));
                            }
                            main.State_Dict.get(next[0]).add_transition(next_transition);
                        }
                    }
                }
            }
        }
    }

}
