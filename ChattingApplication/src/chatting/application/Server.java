package chatting.application;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Server extends JFrame implements ActionListener{
	Server(){
		setLayout(null);
		JPanel t1 = new JPanel();
		t1.setBackground(new Color(7,84,94));
		setSize(450,700);
		t1.setBounds(0,0,450,70);
		ImageIcon i1 = new ImageIcon(
			    getClass().getResource("icons/back.png")
			);

		Image i2 = i1.getImage().getScaledInstance(25,25,Image.SCALE_SMOOTH);
		ImageIcon i3 = new ImageIcon(i2);
		JLabel back = new JLabel(i3);
		back.setBounds(5,2,25,25);
		back.addMouseListener(new MouseAdapter() {
			public void mouseclicked(MouseEvent e) {
				System.exit(0);
			}
		});
		t1.add(back);
		ImageIcon i4 = new ImageIcon(
			    getClass().getResource("icons/profile.png")
			);

		Image i5 = i4.getImage().getScaledInstance(25,25,Image.SCALE_SMOOTH);
		ImageIcon i6 = new ImageIcon(i5);
		JLabel profile = new JLabel(i6);
		profile.setBounds(0, 20, 25, 25);
		t1.add(profile);
		
		ImageIcon i7 = new ImageIcon(
			    getClass().getResource("icons/video.png")
			);

		Image i8 = i7.getImage().getScaledInstance(25,25,Image.SCALE_SMOOTH);
		ImageIcon i9 = new ImageIcon(i5);
		JLabel video = new JLabel(i9);
		video.setBounds(300, 20, 25, 25);
		t1.add(profile);
		
		
		
		profile.setBounds(40,10,25,25);
		setVisible(true);
		setLocation(200,50);
		add(t1);
		getContentPane().setBackground(Color.WHITE);
	}
	public void actionPerformed(ActionEvent e) {
		
	}
	public static void main(String arg[]) {
		new Server();
	}
}
