package me.shiwen.lianjia;

import java.util.Date;

class Instruction implements Comparable<Instruction> {
    int amount;
    int maxTerm;
    Date deadline;

    @Override
    public int compareTo(Instruction i) {
        int c = this.deadline.compareTo(i.deadline);
        return c != 0 ? c : i.amount - this.amount;
    }
}
