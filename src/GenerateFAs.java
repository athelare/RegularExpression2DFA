import automaton.DeterministicFiniteAutomaton;
import automaton.NonfiniteAutomaton;
import exception.MyException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GenerateFAs {
    public static void main(String args[]){
        /*
        Set<Character>charSet=new HashSet<>();
        charSet.add('a');
        charSet.add('b');
        charSet.add('c');
        try {
            NonfiniteAutomaton NFA=new NonfiniteAutomaton("a(b|c)*",charSet);
            NFA.MakeGraph();
            DeterministicFiniteAutomaton DFA=new DeterministicFiniteAutomaton(NFA);
            DFA.MakeGraph();
            DeterministicFiniteAutomaton MinDFA=new DeterministicFiniteAutomaton(DFA);
            MinDFA.MakeGraph();
        } catch (MyException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
