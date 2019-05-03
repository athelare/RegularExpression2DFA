package ui;
import automaton.DeterministicFiniteAutomaton;
import automaton.NonfiniteAutomaton;
import exception.MyException;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.String;
import java.util.*;
import java.util.List;

public class MainPage extends JFrame implements ActionListener {
	/**
	 * 需要把graphviz的dot加入到环境变量path里面
	 */
	private static final long serialVersionUID = -5146281710032081675L;

	private NonfiniteAutomaton nfa;
	private DeterministicFiniteAutomaton dfa,minDfa;
	private Set<Character>charSet;

	private int RowsofNFA,RowsofDFA,RowsofMinDFA;
	private List<Character> int2Char;
	private int nCol;
	private ErrorShow err;//出错信息的显示
	private JLabel Regex;//显示输入的正规表达式
	private JTextField InputReg;//输入正规式的文本框
	private DefaultTableModel model;//表格模型
	private String[][] contents;
	private JButton start;
	private String[] titles;
	private String[] Epsilon;//存储NFA中epsilon闭包
	private String[] DFANodes;//存储DFA中原来拥有的NFA节点
	private String[] MDFANodes;//存储MinDFA中原来的DFA节点
	private MyCanvas Pic;//图片的容器
	private boolean BuildFlag;//标记,代表是否已经建立自动机
	MainPage(String charset)
	{
		charSet=new HashSet<>();
		int2Char=new ArrayList<>();
		setTitle("Regular Expression -> Minimized Deterministic Finite Automaton");
		for(int i=0;i<charset.length();++i){
			this.charSet.add(charset.charAt(i));
		}
		for (Character character : charSet) {
			int2Char.add(character);
		}

		nCol=charSet.size();
		titles=new String[nCol+2];
		titles[0]="序号";
		for(int i=0;i<nCol;++i) {
			titles[i+1]=int2Char.get(i)+"";
		}
		BuildFlag=false;
		titles[charSet.size()+1]="ε-Closure";
		model=new DefaultTableModel(contents,titles);
		//展示状态转移矩阵
		JTable statusTable = new JTable(model);

		setSize(1200,750);
		setLocation(20,40);

		//prignt:右边栏的显示，pu代表上半部分，pn是下半部分
		JPanel pright = new JPanel();
		JPanel pu = new JPanel();
		JPanel pn = new JPanel();

		//提示输入正规表达式,并且展示字符集
		String txt="输入正规表达式:(∑:";
		for(char c:charSet){
			txt+=c;
		}
		txt+=" )";
		JLabel intendRg = new JLabel(txt);
		InputReg=new JTextField(80);
		start= new JButton("Generate!");
		JButton drawNFA = new JButton("绘制ε-NFA");
		JButton drawDFA = new JButton("确定化DFA");
		JButton minDFA = new JButton("最小化DFA");
		//各个按钮
		JButton show = new JButton("高清无码。");
		Regex=new JLabel();

		pright.setPreferredSize(new Dimension(330,740));

		pu.setLayout(new BorderLayout());
		pu.add(Regex,BorderLayout.NORTH);
		pu.add(new JScrollPane(statusTable),BorderLayout.CENTER);
		pu.setPreferredSize(new Dimension(320,350));
		pn.setPreferredSize(new Dimension(320,340));
		statusTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		Pic=new MyCanvas();
		Pic.setFocusable(true);

		this.setLocationRelativeTo(null);
		try{
			File file=new File("./graph/BackGround.jpg");
			BufferedImage bi=ImageIO.read(file);
			Pic.setImage(bi);
		}catch(Exception e){
			e.printStackTrace();
		}
		Pic.repaint();

		pn.setLayout(new GridLayout(7,1,5,5));
		pn.add(intendRg);
		pn.add(InputReg);
		pn.add(start);
		pn.add(drawNFA);
		pn.add(drawDFA);
		pn.add(minDFA);
		pn.add(show);

		pright.add(pu,BorderLayout.NORTH);
		pright.add(pn,BorderLayout.CENTER);


		add(new JScrollPane(Pic),BorderLayout.CENTER);
		add(pright,BorderLayout.EAST);

		setVisible(true);


		validate();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		start.addActionListener(this);
		InputReg.addActionListener(this);
		drawNFA.addActionListener(e -> {
			if(!BuildFlag) {
				err=new ErrorShow("还没有建立自动机。");
				return;
			}
			titles[charSet.size()+1]="ε-闭包";
			model.setDataVector(contents, titles);

			String line[]=new String[nCol+2];
			for(int i=0;i<RowsofNFA;++i) {
				line[0]=i+"";
				for(int j=0;j<nCol;++j) {
					line[j+1]=nfa.getNodes().get(i).getEdges().getOrDefault(int2Char.get(j),-1)+"";
				}
				line[nCol+1]=Epsilon[i];
				model.addRow(line);
			}
			try{
				File file=new File("./graph/nfa.png");
				BufferedImage bi=ImageIO.read(file);
				Pic.setImage(bi);
			}catch(Exception e1){
				e1.printStackTrace();
			}
			Pic.repaint();
			Pic.requestFocus();
		});
		drawDFA.addActionListener(e -> {
			if(BuildFlag==false) {
				err=new ErrorShow("还没有建立自动机。");
				return;
			}

			titles[charSet.size()+1]="N-Nodes";
			model.setDataVector(contents, titles);
			String line[]=new String[nCol+2];
			for(int i=0;i<RowsofDFA;++i) {
				line[0]=i+"";
				for(int j=0;j<nCol;++j) {
					line[j+1]=dfa.getNodes().get(i).getEdges().getOrDefault(int2Char.get(j),-1)+"";

				}
				line[nCol+1]=DFANodes[i];
				model.addRow(line);
			}
			try{
				File file=new File("./graph/dfa.png");
				BufferedImage bi=ImageIO.read(file);
				Pic.setImage(bi);
			}catch(Exception e1){
				e1.printStackTrace();
			}
			Pic.repaint();
			Pic.requestFocus();
		});
		minDFA.addActionListener(e -> {

			if(BuildFlag==false) {
				err=new ErrorShow("还没有建立自动机。");
				return;
			}

			titles[charSet.size()+1]="D-Nodes";
			model.setDataVector(contents, titles);

			String line[]=new String[nCol+2];
			for(int i=0;i<RowsofMinDFA;++i) {
				line[0]=i+"";
				for(int j=0;j<nCol;++j) {
					line[j+1]=minDfa.getNodes().get(i).getEdges().getOrDefault(int2Char.get(j),-1)+"";

				}
				line[nCol+1]=MDFANodes[i];
				model.addRow(line);
			}

			try{
				File file=new File("./graph/mdfa.png");
				BufferedImage bi=ImageIO.read(file);
				Pic.setImage(bi);
			}catch(Exception e1){
				e1.printStackTrace();
			}
			Pic.repaint();
			Pic.requestFocus();
		});
		show.addActionListener(arg0 -> {
			if(!BuildFlag) {
				err=new ErrorShow("还没有建立自动机。");
			}
		});
		InputReg.requestFocus();
	}
	public void actionPerformed(ActionEvent e) {
		titles[charSet.size()+1]="ε-闭包";
		model.setDataVector(contents, titles);

		try {

			nfa=new NonfiniteAutomaton(InputReg.getText(),charSet);
			nfa.MakeGraph();
			dfa=new DeterministicFiniteAutomaton(nfa);
			dfa.MakeGraph();
			minDfa=new DeterministicFiniteAutomaton(dfa);
			minDfa.MakeGraph();
		} catch (MyException ex){
			System.err.println(ex.getMessage());
			err=new ErrorShow(ex.getMessage());
			BuildFlag=false;
			ex.printStackTrace();
			return;
		} catch (IOException ex) {
			err=new ErrorShow("文件读写出现错误");
			ex.printStackTrace();
			BuildFlag=false;
			return;
		}

		RowsofNFA=nfa.getNodes().size();
		RowsofDFA=dfa.getNodes().size();
		RowsofMinDFA=minDfa.getNodes().size();
		Epsilon=new String[RowsofNFA];
		DFANodes=new String[RowsofDFA];
		MDFANodes=new String[RowsofMinDFA];
		for(int i=0;i<RowsofNFA;++i) {
			boolean flag=false;
			Epsilon[i]="";
			for(int nodeIndex:nfa.getNodes().get(i).getEpsilonEdges()){
				if(flag)Epsilon[i]+=',';
				Epsilon[i]+=nodeIndex;
				flag=true;
			}
		}
		for(int i=0;i<RowsofDFA;++i) {
			boolean flag=false;
			DFANodes[i]="";
			for(int nodeIndex:dfa.getNodes().get(i).getPreviousNodes()){
				if(flag)DFANodes[i]+=',';
				DFANodes[i]+=nodeIndex;
				flag=true;
			}
		}
		for(int i=0;i<RowsofMinDFA;++i) {
			boolean flag=false;
			MDFANodes[i]="";
			for(int nodeIndex:minDfa.getNodes().get(i).getPreviousNodes()){
				if(flag)MDFANodes[i]+=',';
				MDFANodes[i]+=nodeIndex;
				flag=true;
			}
		}


		System.out.println("Generate Successfully!");
		String line[]=new String[nCol+2];
		for(int i=0;i<RowsofNFA;++i) {
			line[0]=i+"";
			for(int j=0;j<nCol;++j) {
				line[j+1]=nfa.getNodes().get(i).getEdges().getOrDefault(int2Char.get(j),-1)+"";
			}
			line[nCol+1]=Epsilon[i];
			model.addRow(line);
		}
		Regex.setText(InputReg.getText()+" 的转移矩阵");

		try{
			File file=new File("./graph/nfa.png");
			BufferedImage bi=ImageIO.read(file);
			Pic.setImage(bi);
		}catch(Exception e1){
			e1.printStackTrace();
		}
		Pic.repaint();
		Pic.requestFocus();
		BuildFlag=true;
	}
}
class MyCanvas extends JPanel{
	/**
	 * 显示图片的容器
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage bi;

	void setImage(BufferedImage bi){
		this.bi=bi;
		this.setSize(bi.getWidth(),bi.getHeight());
		this.setPreferredSize(new Dimension(bi.getWidth(),bi.getHeight()));
	}

	public void paint(Graphics g){
		g.clearRect(0, 0, this.getWidth(), this.getHeight());
		g.drawImage(bi,0,0,bi.getWidth(),bi.getHeight(),
				0,0,bi.getWidth(),bi.getHeight(),this);

		// 根据比例绘制图片
	}//后面的意思是按照比例缩放图片
}
class ErrorShow extends JFrame{
	/**
	 * 展示出错信息的窗口
	 */
	private static final long serialVersionUID = 1L;

	JLabel Message;
	ErrorShow(String s){
		Message = new JLabel(s);
		Message.setForeground(Color.RED);
		Message.setSize(this.getWidth(),this.getHeight());
		Message.setFont(new Font("微软雅黑",Font.ITALIC,16));
		Message.setHorizontalTextPosition(SwingConstants.CENTER);
		Message.setFocusable(true);
		this.setFocusable(true);
		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent e){
				//System.out.println(e);
				if(e.getKeyCode()==KeyEvent.VK_ENTER)
					dispose();
			}
		});
		setTitle("出错啦！");
		setBounds(500, 500, 300,100);
		setLayout(new BorderLayout());
		add(Message,BorderLayout.CENTER);
		setVisible(true);
		setAlwaysOnTop(true);
		validate();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.requestFocus();
	}
}