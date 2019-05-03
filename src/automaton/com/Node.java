package automaton.com;

import java.util.HashMap;
import java.util.Map;

public class Node {
    public Map<Character, Integer> getEdges() {
        return edges;
    }

    public void setEdges(Map<Character, Integer> edges) {
        this.edges = edges;
    }

    Map<Character,Integer> edges;
    boolean finish;
    public Node(){
        edges=new HashMap<>();
        finish=false;
    }


    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }
}
