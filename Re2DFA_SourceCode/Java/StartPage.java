package re2dfa;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class StartPage {

	public static void main(String[] args) {
		new StartWindow();
	}

}
class StartWindow extends JFrame implements ActionListener
{
	/**
	 * 启动程序时弹出的窗口
	 */
	private static final long serialVersionUID = 3599439133776160496L;
	JTextField inputCharSet;
	JButton begin;
	JLabel Intender;

	StartWindow()
	{
		setTitle("ADD TO ∑");
		setLocation(400,400);
		setSize(300,150);
		setVisible(true);
		setLayout(new GridLayout(3,1,5,5));
		
		Intender=new JLabel("请输入字符集：");
		inputCharSet=new JTextField(20);
		begin = new JButton("开始");
		begin.addActionListener(this);
		inputCharSet.addActionListener(this);
		add(Intender);
		add(inputCharSet);
		add(begin);
		validate();
		setVisible(true);
		inputCharSet.requestFocus();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		new MainPage(inputCharSet.getText());
	}
}
