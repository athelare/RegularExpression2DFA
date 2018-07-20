package re2dfa;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.lang.String;

public class MainPage extends JFrame implements ActionListener {
	/**
	 * Main Frame of User Interface
	 * 依赖的库文件需要自己生成，放在和jar同目录的文件夹下面，只能在windows下使用
	 * 需要把graphviz的dot加入到环境变量path里面
	 */
	private static final long serialVersionUID = -5146281710032081675L;
	static {
		System.loadLibrary("Re2DFA");
	}
	public native int getRowofNFA();//获得NFA行数
	public native int getRowofDFA();
	public native int getRowofMDFA();
	public native int[]getEpsilon(int n);//获得NFA中的epsilon闭包
	public native int[]getGroupofDFA(int n);//DFA节点所包含的NFA中的节点
	public native int[]getGroupofMDFA(int n);//MinDFA节点所包含的DFA中的节点
	public native int[][]getNFA();//存储NFA的状态转移矩阵
	public native int[][]getDFA();
	public native int[][]getMinDFA();
	public native int addCharSet(String charset);//添加进字符集中,并返回重复状态
	public native String GetCharSet();//返回按字符集中字符顺序排列的字符串,用于表格上信息的显示
	public native int buildFA(String RE);//建立FAs,这个函数将所有信息都返回
	public native void ShowPicture();

	int NFATable[][];
	int DFATable[][];
	int MDFATable[][];
	int RowsofNFA,RowsofDFA,RowsofMinDFA;
	int nCol;
	JPanel pu,pn,pright;//prignt:右边栏的显示，pu代表上半部分，pn是下半部分
	ErrorShow err;//出错信息的显示
	JLabel Regex;//显示输入的正规表达式
	JButton Start,DrawNFA,DrawDFA,MinDFA,Show;//各个按钮
	JTextField InputReg;//输入正规式的文本框
	JTable StatusTable;//展示状态转移矩阵
	JLabel IntendRg;//提示输入正规表达式,并且展示字符集
	DefaultTableModel model;//表格模型
	String[][] contents;
    String[] titles;
    String Epsilon[];//存储NFA中epsilon闭包
    String DFANodes[];//存储DFA中原来拥有的NFA节点
    String MDFANodes[];//存储MinDFA中原来的DFA节点
    String RealCharSet;//存储实际程序中的字符集的顺序
	MyCanvas Pic;//图片的容器
	boolean BuildFlag;//标记,代表是否已经建立自动机
	public MainPage(String charset)
	{
		setTitle("Regular Expression -> Minimized Deterministic Finite Automaton");
		int Status = addCharSet(charset);
		if(Status ==1) {
			err = new ErrorShow("出现了重复字符，已去除");
		}
		
		RealCharSet=GetCharSet();
		nCol=RealCharSet.length();
		titles=new String[RealCharSet.length()+2];
		titles[0]="序号";
		for(int i=0;i<RealCharSet.length();++i) {
			titles[i+1]=RealCharSet.charAt(i)+"";
		}
		BuildFlag=false;
		titles[RealCharSet.length()+1]="ε-Closure";
		model=new DefaultTableModel(contents,titles);
		StatusTable=new JTable(model);
		
		setSize(1200,750);
		setLocation(20,40);
		
		pright=new JPanel();
		pu=new JPanel();
		pn=new JPanel();
		
		IntendRg= new JLabel("输入正规表达式:(∑:"+RealCharSet+")");
		InputReg=new JTextField(100);
		Start=new JButton("Generate！");
		DrawNFA=new JButton("绘制ε-NFA");
		DrawDFA=new JButton("确定化DFA");
		MinDFA=new JButton("最小化DFA");
		Show=new JButton("高清无码。");
		Regex=new JLabel();

		pright.setPreferredSize(new Dimension(330,740));
		
		pu.setLayout(new BorderLayout());
		pu.add(Regex,BorderLayout.NORTH);
		pu.add(new JScrollPane(StatusTable),BorderLayout.CENTER);
		pu.setPreferredSize(new Dimension(320,350));
		pn.setPreferredSize(new Dimension(320,340));
		StatusTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		Pic=new MyCanvas();
		Pic.setFocusable(true);
		
		this.setLocationRelativeTo(null);
		try{
			File file=new File("BackGround.jpg");
			BufferedImage bi=ImageIO.read(file);
			Pic.setImage(bi);
		 }catch(Exception e){
			 e.printStackTrace();
	    }
		Pic.repaint();
		
		pn.setLayout(new GridLayout(7,1,5,5));
		pn.add(IntendRg);
		pn.add(InputReg);
		pn.add(Start);
		pn.add(DrawNFA);
		pn.add(DrawDFA);
		pn.add(MinDFA);
		pn.add(Show);
		
		pright.add(pu,BorderLayout.NORTH);
		pright.add(pn,BorderLayout.CENTER);
		
		
		add(new JScrollPane(Pic),BorderLayout.CENTER);
		add(pright,BorderLayout.EAST);
		
		setVisible(true);
		
		
		validate();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		Start.addActionListener(this);
		InputReg.addActionListener(this);
		DrawNFA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(BuildFlag==false) {
					err=new ErrorShow("还没有建立自动机。");
					return;
				}
				titles[RealCharSet.length()+1]="ε-闭包";
				model.setDataVector(contents, titles);
				
				String line[]=new String[nCol+2];
				for(int i=0;i<RowsofNFA;++i) {
					line[0]=i+"";
					for(int j=0;j<nCol;++j) {
						if(NFATable[i][j]==-1)
							line[j+1]="∅";
						else line[j+1]=NFATable[i][j]+"";
						}
					line[nCol+1]=Epsilon[i];
					model.addRow(line);
				}
				try{
					File file=new File("./graph/NFA.png");
					BufferedImage bi=ImageIO.read(file);
					Pic.setImage(bi);
				 }catch(Exception e1){
					 e1.printStackTrace();
			    }
				Pic.repaint();
				Pic.requestFocus();
			}
			
		});
		DrawDFA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if(BuildFlag==false) {
					err=new ErrorShow("还没有建立自动机。");
					return;
				}
				
				titles[RealCharSet.length()+1]="N-Nodes";
				model.setDataVector(contents, titles);
				String line[]=new String[nCol+2];
				for(int i=0;i<RowsofDFA;++i) {
					line[0]=i+"";
					for(int j=0;j<nCol;++j) {
						if(DFATable[i][j]==-1)
							line[j+1]="∅";
						else line[j+1]=DFATable[i][j]+"";
						}
					line[nCol+1]=DFANodes[i];
					model.addRow(line);
				}
				try{
					File file=new File("./graph/DFA.png");
					BufferedImage bi=ImageIO.read(file);
					Pic.setImage(bi);
				 }catch(Exception e1){
					 e1.printStackTrace();
			    }
				Pic.repaint();
				Pic.requestFocus();
			}
			
		});
		MinDFA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if(BuildFlag==false) {
					err=new ErrorShow("还没有建立自动机。");
					return;
				}
				
				titles[RealCharSet.length()+1]="D-Nodes";
				model.setDataVector(contents, titles);
				
				String line[]=new String[nCol+2];
				for(int i=0;i<RowsofMinDFA;++i) {
					line[0]=i+"";
					for(int j=0;j<nCol;++j) {
						if(MDFATable[i][j]==-1)
							line[j+1]="∅";
						else line[j+1]=MDFATable[i][j]+"";
						}
					line[nCol+1]=MDFANodes[i];
					model.addRow(line);
				}
				
				try{
					File file=new File("./graph/M_DFA.png");
					BufferedImage bi=ImageIO.read(file);
					Pic.setImage(bi);
				 }catch(Exception e1){
					 e1.printStackTrace();
			    }
				Pic.repaint();
				Pic.requestFocus();
			}
			
		});
		Show.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(BuildFlag==false) {
					err=new ErrorShow("还没有建立自动机。");
					return;
				}
				ShowPicture();
			}
			
		});
		InputReg.requestFocus();
	}
	public void actionPerformed(ActionEvent e) {
			titles[RealCharSet.length()+1]="ε-闭包";
			model.setDataVector(contents, titles);
			
			int Status=buildFA(InputReg.getText());
			if(Status==1) {
				err=new ErrorShow("出现了没有在字符集出现的字符。");
				BuildFlag=false;
				return;
			}
			if(Status==2) {
				err=new ErrorShow("输入的正则表达式括号不匹配呀！");
				BuildFlag=false;
				return;
			}
			try {
			NFATable=getNFA();
			DFATable=getDFA();
			MDFATable=getMinDFA();
			}catch(Exception e1) {
				e1.printStackTrace();
			}
			
			RowsofNFA=getRowofNFA();
			RowsofDFA=getRowofDFA();
			RowsofMinDFA=getRowofMDFA();
			
			Epsilon=new String[RowsofNFA];
			DFANodes=new String[RowsofDFA];
			MDFANodes=new String[RowsofMinDFA];
			int nodeGroup[];
			for(int i=0;i<RowsofNFA;++i) {
				nodeGroup=getEpsilon(i);
				Epsilon[i]=nodeGroup[0]+"";
				for(int j=1;j<nodeGroup.length;++j)
				Epsilon[i]+=","+nodeGroup[j];
			}
			for(int i=0;i<RowsofDFA;++i) {
				nodeGroup=getGroupofDFA(i);
				DFANodes[i]=nodeGroup[0]+"";
				for(int j=1;j<nodeGroup.length;++j)
				DFANodes[i]+=","+nodeGroup[j];
			}
			for(int i=0;i<RowsofMinDFA;++i) {
				nodeGroup=getGroupofMDFA(i);
				MDFANodes[i]=nodeGroup[0]+"";
				for(int j=1;j<nodeGroup.length;++j)
				MDFANodes[i]+=","+nodeGroup[j];
			}
			
			
			String line[]=new String[nCol+2];
			for(int i=0;i<RowsofNFA;++i) {
				line[0]=i+"";
				for(int j=0;j<nCol;++j) {
					if(NFATable[i][j]==-1)
						line[j+1]="∅";
					else line[j+1]=NFATable[i][j]+"";
					}
				line[nCol+1]=Epsilon[i];
				model.addRow(line);
			}
			Regex.setText(InputReg.getText()+" 的转移矩阵");

			try{
				File file=new File("./graph/NFA.png");
				BufferedImage bi=ImageIO.read(file);
				Pic.setImage(bi);
			 }catch(Exception e1){
				 e1.printStackTrace();
		    }
			Pic.repaint();
			BuildFlag=true;
			Pic.requestFocus();
		}

}
class MyCanvas extends JPanel{
     /**
	 * 显示图片的容器
	 */
	private static final long serialVersionUID = 1L;
	private BufferedImage bi;
    
     public void setImage(BufferedImage bi){
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