package automaton;

import automaton.com.NFANode;
import exception.MyException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class NonfiniteAutomaton {
    private List<NFANode> nodes;
    private Set<Character> charSet;

    public NonfiniteAutomaton(String reg,Set<Character>charSet) throws MyException{
        super();
        nodes=new ArrayList<>();
        this.charSet=charSet;
        nodes.add(new NFANode(nodes.size()));
        nodes.add(new NFANode(nodes.size(),true));
        CreateNFA(reg,0,1);
    }
    /**
    * The Construction of NFA.
    * @param reg Input Regex String
    * @param start Start Node index
    * @param next end Node index
    * */
    private void CreateNFA(String reg, int start, int next) throws MyException{
        /*Empty string: add an epsilon edge to the end.*/
        if(reg.length()==0){
            addEpsilonEdge(start,next);
            return;
        }
        /*Single char:directly add one edge.*/
        if(reg.length()==1){
            char ch=reg.charAt(0);
            if(!charSet.contains(ch))
                throw new MyException("Character not in charSet!");
            addEdge(start,next,ch);
            return;
        }

        /*if exists option regex:*/
        int indexOP=0;
        if((indexOP=FindOption(reg))!=-1){
            do{
                String subReg=reg.substring(0,indexOP);
                addOptionEdge(subReg,start,next);
                reg=reg.substring(indexOP+1);
            }while(((indexOP=FindOption(reg))!=-1));
            /*one reg string left.*/
            addOptionEdge(reg,start,next);
            return;
        }

        /*Only left regex string need to be connect.*/
        int curFinal=nodes.size();
        String subReg;
        nodes.add(new NFANode(nodes.size()));

        /*reg in parentheses*/
        if(reg.charAt(0)=='('){
            indexOP=matchRightParenthesis(reg,0);
            subReg=reg.substring(1,indexOP);
        }else{
            /*single character*/
            subReg=reg.substring(0,1);
            indexOP=0;
        }

        /*match unlimited times.(*)*/
        if (indexOP + 1 < reg.length() && reg.charAt(indexOP + 1) == '*') {
            int q1 = nodes.size();
            int q2 = q1 + 1;
            nodes.add(new NFANode(nodes.size()));
            nodes.add(new NFANode(nodes.size()));

            /*Thompson Algorithm*/
            /*start - - - - - - - - - - - - - > curFinal
             *   |                                 |
             *   |- - - > q1---(subReg)--->q2- - ->|
             *             |                |
             *             |<- - - - - - - -|
             * */
            CreateNFA(subReg, q1, q2);
            addEpsilonEdge(start, curFinal);
            addEpsilonEdge(start, q1);
            addEpsilonEdge(q2, curFinal);
            addEpsilonEdge(q2, q1);
            indexOP++;
        } else {
            CreateNFA(subReg, start, curFinal);
        }
        CreateNFA(reg.substring(indexOP + 1), curFinal, next);

    }
    /*start - - - -> curMid --(subReg)-> curFinal - - - -> next*/
    private void addOptionEdge(String subReg, int start, int next) throws MyException{
        int curMid,curFinal;
        curMid=this.nodes.size();
        curFinal=curMid+1;
        nodes.add(new NFANode(nodes.size()));
        nodes.add(new NFANode(nodes.size()));
        CreateNFA(subReg,curMid,curFinal);
        /*Add epsilon edge for option*/
        addEpsilonEdge(start,curMid);
        addEpsilonEdge(curFinal,next);
    }

    private void addEdge(int n1, int n2, char ch){
        nodes.get(n1).getEdges().put(ch,n2);
    }

    private void addEpsilonEdge(int n1, int n2){
        nodes.get(n1).getEpsilonEdges().add(n2);
    }

    void ExpandEpsilon(){
        for(int i=0;i<nodes.size();++i)
            for(int j=0;j<nodes.size();++j){
                Set<Integer>epsilonEdges=new HashSet<>(nodes.get(j).getEpsilonEdges());
                epsilonEdges.remove(j);
                for(int epsilonEdge:epsilonEdges){
                    Set<Integer>childEpsilons=nodes.get(epsilonEdge).getEpsilonEdges();
                    nodes.get(j).getEpsilonEdges().addAll(childEpsilons);
                }
            }
    }

    private int matchRightParenthesis(String str,int left) throws MyException{
        int pCount=1;
        int cur=left+1;
        while(pCount>0 && cur<str.length()){
            ++cur;
            if(str.charAt(cur)=='(')
                ++pCount;
            else if(str.charAt(cur)==')')
                --pCount;
        }
        if(cur == str.length()){
            throw new MyException("Parentheses not match!");
        }
        return cur;
    }

    private int FindOption(String str) throws MyException{
        int cur=0;
        while(cur<str.length()){
            if(str.charAt(cur)=='|')return cur;
            else if(str.charAt(cur)=='(')cur=matchRightParenthesis(str,cur)+1;
            else cur++;
        }
        return -1;
    }

    Set<Character> getCharSet() {
        return charSet;
    }

    public void setCharSet(Set<Character> charSet) {
        this.charSet = charSet;
    }
    public void MakeGraph() throws IOException {
        PrintWriter fout=new PrintWriter(new FileWriter("./graphSrc/nfa.dot"));
        fout.write("digraph epsilon{\n");
        fout.write("    rankdir=LR;\n");
        fout.write("    fontsize= 14;\n");
        fout.write("    node [shape = circle, fontname = \"Microsoft YaHei\", fontsize = 14];\n");
        fout.write("    edge [fontname = \"Microsoft YaHei\", fontsize = 12];\n");
        fout.write("    0 [color=red];\n");
        fout.write("    \"\"[shape = \"none\"];\n");
        fout.write("    \"\" -> 0;\n");
        for(int i=0;i<nodes.size();++i){
            if(nodes.get(i).isFinish())
                fout.write("\t"+i+" [ shape = doublecircle ];\n");
            for(Map.Entry edge:nodes.get(i).getEdges().entrySet()){
                fout.write("\t"+i+" -> "+edge.getValue()+" [ label = \""+edge.getKey()+"\" ];\n");
            }
            for(int epsilon:nodes.get(i).getEpsilonEdges()){
                if(i != epsilon)
                    fout.write("\t"+i+" -> "+epsilon+" [ style= \"dashed\" ];\n");
            }
        }
        fout.write("    \"NFA:\" [ shape = plaintext ];\n}\n");
        fout.close();
        Runtime.getRuntime().exec("dot -Tpng ./graphSrc/nfa.dot -o ./graph/nfa.png");
    }

    public List<NFANode> getNodes() {
        return nodes;
    }
}
