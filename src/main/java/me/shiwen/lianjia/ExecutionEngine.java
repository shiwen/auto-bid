package me.shiwen.lianjia;

import java.util.PriorityQueue;
import java.util.Queue;

public class ExecutionEngine {
    private static final ExecutionEngine INSTANCE = new ExecutionEngine();

    private final Queue<Instruction> instructionQueue = new PriorityQueue<>();
    private Thread bidPollThread = null;

    private ExecutionEngine() {}

    public ExecutionEngine getInstance() {
        return INSTANCE;
    }

    public void submit(Instruction instruction) {
        instructionQueue.add(instruction);
        if (bidPollThread == null || !bidPollThread.isAlive()) {
            bidPoll();
        }
    }

    private void bidPoll() {

    }

//    public static void main(String... args) {
//        Instruction i1 = new Instruction();
//        i1.amount = 500;
//        i1.maxTerm = 30;
//        i1.deadline = new Date();
//        Instruction i2 = new Instruction();
//        i2.amount = 200;
//        i2.maxTerm = 60;
//        Date yesterday = yesterday();
//        i2.deadline = yesterday;
//        instructionQueue.add(i1);
//        instructionQueue.add(i2);
//        Instruction i3 = new Instruction();
//        i3.amount = 300;
//        i3.maxTerm = 60;
//        i3.deadline = yesterday;
//        instructionQueue.add(i3);
//        System.out.println(instructionQueue.poll());
//        System.out.println(instructionQueue.poll());
//        System.out.println(instructionQueue.poll());
//    }
//
//    static private Date yesterday() {
//        final Calendar cal = Calendar.getInstance();
//        cal.add(Calendar.DATE, -1);
//        return cal.getTime();
//    }
}
