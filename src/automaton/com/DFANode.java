package automaton.com;

import java.util.HashSet;
import java.util.Set;

public class DFANode extends Node{
    Set<Integer>previousNodes;
    public DFANode(){
        super();
        previousNodes=new HashSet<>();
    }

    public Set<Integer> getPreviousNodes() {
        return previousNodes;
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof DFANode))return false;
        DFANode b = (DFANode)obj;
        if(this.finish != b.finish)return false;
        if(this.getPreviousNodes().size()!=b.getPreviousNodes().size())return false;
        return this.getPreviousNodes().containsAll(b.getPreviousNodes());
    }
}

