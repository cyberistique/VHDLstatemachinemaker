public class Transition {
    private final static String NONE = "NA";
    private state next;
    private String input_name;
    private int input;
    private int delay;

     public Transition(state next,String input_name, int input,int delay){
         this.next = next;
         this.input_name = input_name;
         this.input = input;
         this.delay = delay;
     }
     public Transition(state next,int delay){
         this.next = next;
         this.input_name = NONE;
         this.input = -1;
         this.delay = delay;
     }


     public String condition(){
         return "if "+input_name+" = '"+input+"' then ";
     }

     public String getDelay(){
         if (delay == 0){
             return "";
         }
         return " after "+delay+" ns";
     }
     public boolean isNone() {
         if (input_name.equals(NONE) & (input == -1)) {
             return true;
         }
         return false;
     }

     public state getNext(){
         return next;
     }
}
