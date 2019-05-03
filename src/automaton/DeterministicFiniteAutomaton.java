package automaton;

import automaton.com.DFANode;
import automaton.com.NFANode;
import exception.MyException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class DeterministicFiniteAutomaton {
    private Set<Character> charSet;
    private List<DFANode> nodes;
    private boolean Minimized;

    public DeterministicFiniteAutomaton(NonfiniteAutomaton NFA){
        NFA.ExpandEpsilon();
        this.nodes=new ArrayList<>();
        this.charSet = NFA.getCharSet();
        this.Minimized=false;
        DFANode node0=new DFANode();


        node0.getPreviousNodes().addAll(NFA.getNodes().get(0).getEpsilonEdges());
        this.nodes.add(node0);
        /*当前检查的节点编号*/
        int indexCurrentNode=0;
        while(indexCurrentNode<nodes.size()){
            /*当前节点*/
            DFANode currentNode=nodes.get(indexCurrentNode);
            /*对每个字符拓展出来的边都进行检查*/
            for(char oneChar: charSet){
                /*临时节点*/
                DFANode tempNode=new DFANode();
                for(int indexPreviousNode:currentNode.getPreviousNodes()){
                    NFANode previousNode=NFA.getNodes().get(indexPreviousNode);
                    if(previousNode.getEdges().keySet().contains(oneChar)){
                        Set<Integer>epsilonEdges=NFA.getNodes().get(previousNode.getEdges().get(oneChar)).getEpsilonEdges();
                        tempNode.getPreviousNodes().addAll(epsilonEdges);
                    }
                }
                if(tempNode.getPreviousNodes().size()==0)continue;

                /*tempNode是一个节点集合通过某个字母边拓展出来的另一个节点集合
                * 如果存在这个新的集合不用管（重写equals函数）
                * 如果不存在，就创建新的集合
                * 最后添加一条边*/
                if(!this.nodes.contains(tempNode))
                    this.nodes.add(tempNode);
                this.nodes.get(indexCurrentNode).getEdges().put(
                        oneChar,
                        this.nodes.indexOf(tempNode)
                );
            }
            indexCurrentNode++;
        }
        for(DFANode node:this.nodes){
            if(node.getPreviousNodes().contains(1))
                node.setFinish(true);
        }
    }

    public DeterministicFiniteAutomaton(DeterministicFiniteAutomaton DFA) throws MyException{
        this.nodes=new ArrayList<>();
        this.charSet=DFA.charSet;
        this.Minimized=true;
        DFANode q1=new DFANode(),q2=new DFANode();
        for(int i=0;i<DFA.getNodes().size();++i){
            DFANode node=DFA.getNodes().get(i);
            if(node.isFinish())
                q2.getPreviousNodes().add(i);
            else
                q1.getPreviousNodes().add(i);
        }
        if(q1.getPreviousNodes().size()>0)
            this.nodes.add(q1);
        if(q2.getPreviousNodes().size()>0)
            this.nodes.add(q2);

        /*使用Hopcroft思想进行化简*/
        /*当前检查的节点的编号，如果进行一轮检查结束发现发生过改变，那么indexCurrent置零，重新开始*/
        int indexCurrent=0;
        boolean hasChanged=false;
        while(indexCurrent<this.nodes.size()){
            DFANode currentNode=this.nodes.get(indexCurrent);
            for(char oneChar:charSet){
                Set<Integer>destinations=new HashSet<>();
                for(int nodeIndex:currentNode.getPreviousNodes()){
                    DFANode previousNode=DFA.getNodes().get(nodeIndex);
                    destinations.add(previousNode.getEdges().getOrDefault(oneChar, -1));
                }

                if(shouldFurtherDivide(destinations)){
                    DFANode newNode = new DFANode();
                    hasChanged=true;
                    /*将这一个集合里的第一个元素作为基准，不能通过当前字符被划分到一个集合里的元素都添加到另外的一个新集合*/
                    int firstNode=currentNode.getPreviousNodes().iterator().next();
                    int indexFirstDesNode=DFA.getNodes().get(firstNode).getEdges().getOrDefault(oneChar,-1);
                    /*第一个元素经由此字母通向非空集合*/
                    if(-1 != indexFirstDesNode){
                        Set<Integer> DestinationSetOfFirstNode=null;
                        for(DFANode node:this.nodes)
                            if(node.getPreviousNodes().contains(indexFirstDesNode)){
                                DestinationSetOfFirstNode=node.getPreviousNodes();
                                break;
                            }
                        if(null==DestinationSetOfFirstNode){
                            System.err.println("DestinationSetOfFirstNode出现了错误！此错误经过考虑本不会发生");
                            throw new MyException("!unexpected DestinationSetOfFirstNode not found");
                        }

                        for (int index : currentNode.getPreviousNodes()) {
                            /*node: currentNode 包含的DFA节点*/
                            DFANode node = DFA.getNodes().get(index);
                            int desByCurrentChar=node.getEdges().getOrDefault(oneChar, -1);
                            /*如果currentNode包含的DFA节点经由当前字母到达的集合(DestinationSetOfFirstNode.getPreviousNodes())
                            不是第一个元素的终点集合，那么就添加到新增的集合*/
                            if (!DestinationSetOfFirstNode.contains(desByCurrentChar)) {
                                newNode.getPreviousNodes().add(index);
                            }
                        }
                    }else{
                        for (int index : currentNode.getPreviousNodes()) {
                            /*node: currentNode 包含的DFA节点*/
                            DFANode node = DFA.getNodes().get(index);
                            if(node.getEdges().containsKey(oneChar)){
                                newNode.getPreviousNodes().add(index);
                            }
                        }
                    }
                    for(int addedIndex:newNode.getPreviousNodes())
                        currentNode.getPreviousNodes().remove(addedIndex);
                    this.nodes.add(newNode);
                }

            }
            if(hasChanged){
                indexCurrent=0;
                hasChanged=false;
            }else indexCurrent++;
        }
        /*添加终结状态标记*/
        for(DFANode node:this.nodes){
            for(int dfaIndex:node.getPreviousNodes()){
                if(DFA.getNodes().get(dfaIndex).isFinish()){
                    node.setFinish(true);
                    break;
                }
            }
        }
        /*将这些群组所拥有的节点集的边的信息添加到群组本身*/
        for(int i=0;i<this.nodes.size();++i){
            DFANode node=this.nodes.get(i);
            for(char oneChar:this.charSet){
                /*检查群里面的每一个子自动机的边能到哪里，这个群就能到哪里*/
                for(int dfaIndex:node.getPreviousNodes()){
                    DFANode dfaNode=DFA.getNodes().get(dfaIndex);
                    if(dfaNode.getEdges().containsKey(oneChar)){
                        int destination=dfaNode.getEdges().get(oneChar);
                        for(int j=0;j<this.nodes.size();++j){
                            if(this.nodes.get(j).getPreviousNodes().contains(destination)){
                                node.getEdges().put(oneChar,j);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean shouldFurtherDivide(Set<Integer> des){
        /*有单元素或者空集，一定属于已知集合,无需分开*/
        if(des.size()<=1)return false;
        /*同时有不可达的边和可达的边，需要把可达的节点和不可达的节点分开*/
        if(des.contains(-1))return true;
        /*如果到达的节点集是已有的某个节点集的子集，也无需再分*/
        for(DFANode node:this.nodes){
            if(node.getPreviousNodes().containsAll(des))
                return false;
        }
        /*到达的节点集不属于任何一个节点集的子集，需要进行划分（参考编译原理教材）*/
        return true;
    }

    public void MakeGraph() throws IOException {
        String fileName=(Minimized?"m":"")+"dfa";
        PrintWriter fout=new PrintWriter(new FileWriter("./graphSrc/"+fileName));
        fout.write("digraph dfa{\n");
        fout.write("    rankdir = LR ;\n");
        fout.write("    fontsize = 14;\n");
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
        }
        fout.write("    \""+(Minimized?"Min ":"")+"DFA:\" [ shape = plaintext ];\n}\n");
        fout.close();
        Process p=Runtime.getRuntime().exec("dot -Tpng ./graphSrc/"+fileName+" -o ./graph/"+fileName+".png");
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public List<DFANode> getNodes() {
        return nodes;
    }
}
