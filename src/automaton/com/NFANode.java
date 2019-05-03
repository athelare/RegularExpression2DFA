package automaton.com;

import java.util.HashSet;
import java.util.Set;

public class NFANode extends Node {
    Set<Integer> epsilonEdges;
    public NFANode(int i){
        super();
        epsilonEdges=new HashSet<>();
        epsilonEdges.add(i);
    }
    public NFANode(int i,boolean flag){
        super();
        epsilonEdges=new HashSet<>();
        epsilonEdges.add(i);
        this.finish=flag;
    }

    public Set<Integer> getEpsilonEdges() {
        return epsilonEdges;
    }
}
