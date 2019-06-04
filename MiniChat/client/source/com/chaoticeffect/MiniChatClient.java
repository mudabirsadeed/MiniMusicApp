package com.chaoticeffect;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class MiniChatClient{
	
	JTextArea incoming;
	JTextField outgoing;
	BufferedReader reader;
	PrintWriter writer;
	Socket sock;
	String username;
	JTextField usernameField;
	JFrame usernameFrame;
	
	
	public static void main(String[] args){
		new MiniChatClient().go();
	}

	public void go(){
		setUpGui();
		setUpNetworking();
		
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
	}
		
	//sets up the GUI
		
	public void setUpGui(){
		
		JFrame frame = new JFrame("Simple Chat Client");
		JPanel messagesPanel = new JPanel();
		JPanel sendPanel = new JPanel();
		
		incoming = new JTextArea(15,50);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		JScrollPane qScroller = new JScrollPane(incoming);
		
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		outgoing = new JTextField (20);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new SendButtonListener());
		
		messagesPanel.add(qScroller);
		
		sendPanel.add(outgoing);
		sendPanel.add(sendButton);

		frame.getContentPane().add(BorderLayout.CENTER,messagesPanel);
		frame.getContentPane().add(BorderLayout.SOUTH, sendPanel);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		getUsername();
	}	
	
	public void getUsername(){
		
		usernameFrame = new JFrame();
		JPanel messagesPanel = new JPanel();
		
		JLabel label = new JLabel("Enter Username");
		JButton confirmButton = new JButton("Confirm");
		confirmButton.addActionListener(new ConfirmButtonListener());
		usernameField = new JTextField(10);
		
		messagesPanel.add(label);
		messagesPanel.add(usernameField);
		messagesPanel.add(confirmButton);
		
		usernameFrame.getContentPane().add(BorderLayout.CENTER, messagesPanel);
		usernameFrame.setSize(400,75);
		usernameFrame.pack();
		usernameFrame.setVisible(true);
		usernameField.requestFocus();
	}
	
	//establishes a connection to the server, `
	
	public void setUpNetworking(){ 
		try{
			sock = new Socket("127.0.0.1", 5000);
			InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(streamReader);
			writer = new PrintWriter(sock.getOutputStream());
			System.out.println("networking established");
			
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	public class SendButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent ev){
			try{
				writer.println(username + ": " + outgoing.getText());
				System.out.println("sent " + username + ": " + outgoing.getText());
				writer.flush();
				
				
			}catch(Exception ex){
				ex.printStackTrace();
			}
			
			outgoing.setText("");
			outgoing.requestFocus();
			
		}
	}
	
	public class ConfirmButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent ev){
			username = usernameField.getText();
			usernameFrame.dispose();
			outgoing.requestFocus();
		}
	}
	
	public class IncomingReader implements Runnable{
		public void run(){
			String message;
			try{
				while((message=reader.readLine())!=null){
					System.out.println("recieved "+message);
					incoming.append(message +"\n");
					
				}
			}catch(Exception ex){
					ex.printStackTrace();
			}
			 
		}
	}
}