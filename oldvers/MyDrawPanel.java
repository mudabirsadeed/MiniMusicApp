import java.awt.*;
import javax.swing.*;
import java.awt.event.*;


public class MyDrawPanel extends JPanel {
	
	static JFrame frame;
	static MyDrawPanel drawPanel;
	static MyDrawPanel.MyInner myInner;
	
	public static void main(String[] args){
		
		drawPanel= new MyDrawPanel();
		myInner= new MyDrawPanel().new MyInner();
		
		
		drawPanel.myInner.go();
	}
	
	public void paintComponent(Graphics g){
		
		Graphics2D g2d = (Graphics2D) g;
		
		int r1 = (int) (Math.random()*256);
		int gr1 = (int) (Math.random()*256);
		int b1 = (int) (Math.random()*256);
		
		Color randomColor = new Color(r1,gr1,b1);
		
		int r2 = (int) (Math.random()*256);
		int gr2 = (int) (Math.random()*256);
		int b2 = (int) (Math.random()*256);
		
		Color randomColor2 = new Color(r2,gr2,b2);
		
		int x1 = (int) ((Math.random()*100)+50);
		int y1 = (int) ((Math.random()*100)+50);
		int x2 = (int) ((Math.random()*100)+50);
		int y2 = (int) ((Math.random()*100)+50);
		
		
		
		//x1,y1 is startpos1, x2,y2 is size, ditto x3,y3 , x4,y4
		
		
		
			GradientPaint gradient = new GradientPaint(x1,y1,randomColor,x2,y2, randomColor2);

			g2d.setPaint(gradient);
			g2d.fillOval(x1,y1,x2,y2);
		}
	
	
	
	public class MyInner implements ActionListener{
		
		public void go(){
			JButton button = new JButton();
		
			frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			button.addActionListener(this);
			
			frame.getContentPane().add(BorderLayout.SOUTH, button);
			frame.getContentPane().add(BorderLayout.CENTER, drawPanel);
			
			frame.setSize(300,400);
			frame.setVisible(true);
			
		}
		
		public void actionPerformed(ActionEvent event){
			frame.repaint();
		}
		
		
	}
	
}