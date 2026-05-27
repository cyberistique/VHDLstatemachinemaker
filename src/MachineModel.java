import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

public class MachineModel {
    private MachineStruct machine;
    private PrintWriter writer;
    private final List<Observer<MachineModel, String>> observers = new LinkedList<>();
    private String status;
    private final String fileBaseName;

    public MachineModel(String fileBaseName) {
        this.fileBaseName = fileBaseName;
        this.status = "Ready";
    }

    public void setMachine(MachineStruct machine) {
        this.machine = machine;
        setStatus("Machine configured: " + machine.getName());
    }

    public String init_Lines() {
        // uses machine.getCase("IO") and machine.getCase("name")
        // rest of needed syntax can be added by you, we will use String builders
        if (machine == null) throw new IllegalStateException("Machine not configured");

        String out = "library ieee;\n" +
                "use ieee.std_logic_1164.all;\n" +
                "use ieee.numeric_std.all;\n\n" +
                "entity " + machine.getName() + " is\n" +
                machine.getCase("IO") +
                "\n" +
                machine.getCase("name");
        return out;

    }

    public String conditional_Lines() {
        // uses machine.clock_()); and machine.getCase("case")) to get state switching lines.
        // rest of needed syntax can be added by you, we will use String builders
        if (machine == null) throw new IllegalStateException("Machine not configured");
        return machine.clock_();
    }

    public String output_Lines() {
        //uses machine.getCase("out")
        // rest of needed syntax can be added by you, we will use String builders
        if (machine == null) throw new IllegalStateException("Machine not configured");
        String out = "\n-- output decode\n" +
                "process(sstate) is\n" +
                "begin\n" +
                machine.getCase("out") +
                "end process;\n" +
                "end architecture;\n";
        return out;
    }

    public void FSMmake() {
        //using the above 3 functions and printwriter make the vhdl file of the required name;
        if (machine == null) throw new IllegalStateException("Machine not configured");
        setStatus("Generating VHDL...");
        try {
            this.writer = new PrintWriter(new File(fileBaseName + ".vhdl"));
            writer.print(init_Lines());
            writer.print(conditional_Lines());
            writer.print(output_Lines());
            setStatus("Wrote " + fileBaseName + ".vhdl");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) writer.close();
        }
    }

    public void TBmake() {
        if (machine == null) throw new IllegalStateException("Machine not configured");
        setStatus("Generating testbench...");
        try {
            this.writer = new PrintWriter(new File(fileBaseName + "_tb.vhdl"));
            writer.print(testbench_Lines());
            setStatus("Wrote " + fileBaseName + "_tb.vhdl");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) writer.close();
        }
    }

    private String testbench_Lines() {
        String dut = machine.getName();
        String tb = dut + "_tb";
        int periodNs = 10;

        StringBuilder out = new StringBuilder();
        out.append("library ieee;\n");
        out.append("use ieee.std_logic_1164.all;\n");
        out.append("use ieee.numeric_std.all;\n\n");

        out.append("entity ").append(tb).append(" is\nend entity;\n\n");
        out.append("architecture sim of ").append(tb).append(" is\n");

        out.append("    constant c_clk_period : time := ").append(periodNs).append(" ns;\n");
        out.append("    signal clk : std_logic := '0';\n");
        out.append("    signal rst : std_logic := '0';\n");

        for (String in : machine.getInputs()) {
            out.append("    signal ").append(in).append(" : std_logic := '0';\n");
        }
        String[] outs = machine.getOutputs();
        int[] widths = machine.getOutputWidths();
        for (int i = 0; i < outs.length; i++) {
            int w = (widths == null || widths.length != outs.length) ? 1 : widths[i];
            if (w <= 1) {
                out.append("    signal ").append(outs[i]).append(" : std_logic;\n");
            } else {
                out.append("    signal ").append(outs[i]).append(" : std_logic_vector(").append(w - 1).append(" downto 0);\n");
            }
        }

        out.append("begin\n\n");
        out.append("    clk <= not clk after c_clk_period/2;\n\n");

        out.append("    uut: entity work.").append(dut).append("\n");
        out.append("    port map(\n");
        out.append("        clk => clk,\n");
        out.append("        rst => rst");
        for (String in : machine.getInputs()) {
            out.append(",\n        ").append(in).append(" => ").append(in);
        }
        for (String o : outs) {
            out.append(",\n        ").append(o).append(" => ").append(o);
        }
        out.append("\n    );\n\n");

        out.append("    stim: process is\n");
        out.append("        variable v : integer := 0;\n");
        out.append("    begin\n");
        out.append("        rst <= '0';\n");
        out.append("        wait for 3*c_clk_period;\n");
        out.append("        rst <= '1';\n");
        out.append("        wait for c_clk_period;\n\n");

        int nInputs = machine.getInputs().length;
        int maxVectors = 1 << Math.min(nInputs, 8);
        out.append("        -- drives up to ").append(maxVectors).append(" input vectors\n");
        out.append("        for v in 0 to ").append(maxVectors - 1).append(" loop\n");
        for (int i = 0; i < nInputs; i++) {
            String in = machine.getInputs()[i];
            out.append("            if ((v / ").append(1 << i).append(") mod 2) = 1 then\n");
            out.append("                ").append(in).append(" <= '1';\n");
            out.append("            else\n");
            out.append("                ").append(in).append(" <= '0';\n");
            out.append("            end if;\n");
        }
        out.append("            wait for 2*c_clk_period;\n");
        out.append("        end loop;\n\n");
        out.append("        wait;\n");
        out.append("    end process;\n");
        out.append("end architecture;\n");

        return out.toString();
    }

    public String getStatus() {
        return this.status;
    }

    private void setStatus(String status) {
        this.status = status;
        notifyObservers(status);
    }

    private void notifyObservers(String message) {
        for (Observer<MachineModel, String> obs : this.observers) {
            obs.update(this, message);
        }
    }

    public void addObserver(Observer<MachineModel, String> observer) {
        this.observers.add(observer);
    }

    private void alertObservers(String data) {
        for (var observer : observers) {
            observer.update(this, data);
        }
    }

    @Override
    public String toString() {
        return status + "\n";
    }
}
