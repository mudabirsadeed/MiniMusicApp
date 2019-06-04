
import javax.swing.event.*;
import javax.sound.midi.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.*;


public class MiniMusicApp{

	JFrame theFrame;
	JPanel mainPanel;
	JList incomingList;
	JTextField userMessage;
	ArrayList<JCheckBox> checkboxList;
	int nextNum;
	Vector<String> listVector = new Vector<String>();
	HashMap<String, boolean[]> otherSeqsMap = new HashMap<String, boolean[]>();
	
	JPanel background;
	JLabel status;
	
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	
	Socket sock;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	JTextArea incoming;
	JTextField outgoing;
	BufferedReader reader;
	PrintWriter writer;
	
	String username;
	JTextField usernameField;
	JFrame usernameFrame;
	
	
	int[] presets = {8,17,19,21,23,25,34,42,66,86,88,100,174,203,205,220,223,238,256};
	
	String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
		"Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle","Low Conga","Cowbell", "Vibraslap",
			"Low-mid Tom","High Agogo","Open Hi Conga"};
	
	int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
	
	public static void main(String[] args){
		
		new MiniMusicApp().startUp();
	}
	
	public void startUp(){
		buildGUI();
		setUpNetworking();
		
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
	}
	
	public void buildGUI(){
			
		theFrame = new JFrame("Beatbox");

		background = new JPanel(new BorderLayout());
		background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));	
			
		checkboxList = new ArrayList<JCheckBox>();			
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		addButtons(buttonBox);
		
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		for(int i = 0; i <16;i++){
			nameBox.add(new Label(instrumentNames[i]));
		}
		
		JPanel buttonAndMessagesPanel = new JPanel();
		buttonAndMessagesPanel.setLayout(new BoxLayout(buttonAndMessagesPanel, BoxLayout.Y_AXIS));
		buttonAndMessagesPanel.add(buttonBox);
		addMessagesPanel(buttonAndMessagesPanel);
		
		status = new JLabel("MiniMiniMusicApp", SwingConstants.CENTER);
		
		background.add(BorderLayout.NORTH, status);
		background.add(BorderLayout.EAST, buttonAndMessagesPanel);
		background.add(BorderLayout.WEST, nameBox);
		
		theFrame.getContentPane().add(background);
		
		addCheckBoxes();
		
		setUpMidi();
		
		theFrame.setBounds(50,50,300,300);
		theFrame.pack();
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theFrame.setVisible(true);
		getUsername();
	}
	
	public void addCheckBoxes(){
		GridLayout grid = new GridLayout(16,16);
		grid.setVgap(1);
		grid.setHgap(2);
		mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER,mainPanel);
		
		for(int i=0;i<256;i++){
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			for(int j=0;j<presets.length;j++){
				if((i+1)==presets[j]){
					c.setSelected(true);
				}
			}
			checkboxList.add(c);
			mainPanel.add(c);
		}
	}
	
	public void addMessagesPanel(JPanel buttonAndMessagesPanel){
		
		JPanel sendPanel = new JPanel();
		
		incoming = new JTextArea(15,25);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		JScrollPane qScroller = new JScrollPane(incoming);
		
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		outgoing = new JTextField (20);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new SendButtonListener());
		
		
		sendPanel.add(outgoing);
		sendPanel.add(sendButton);
		
		buttonAndMessagesPanel.add(qScroller);
		buttonAndMessagesPanel.add(sendPanel);
		
		
	}
	
	public void addButtons(Box buttonBox){
		
		JButton start = new JButton("Start");
		start.addActionListener(new MyStartListener());
		buttonBox.add(start);
		
		JButton stop = new JButton("Stop");
		stop.addActionListener(new MyStopListener());
		buttonBox.add(stop);
		
		JButton upTempo = new JButton("Tempo Up");
		upTempo.addActionListener(new MyUpTempoListener());
		buttonBox.add(upTempo);
		
		JButton downTempo = new JButton("Tempo Down");
		downTempo.addActionListener(new MyDownTempoListener());
		buttonBox.add(downTempo);
		
		JButton save = new JButton("Save Clip");
		save.addActionListener(new MySaveListener());
		buttonBox.add(save);
		
		JButton load = new JButton("Load Clip");
		load.addActionListener(new MyLoadListener());
		buttonBox.add(load);
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
			out= new ObjectstreamReader = new InputStreamReader(sock.getInputStream());
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
				
				System.out.println("sent " + username + ": " + outgoing.getText());
				writer.println(username + ": " + outgoing.getText());
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
		

	public class MyStartListener implements ActionListener {
		public void actionPerformed(ActionEvent a){
			buildTrackAndStart();
			status.setText("Playing...");
		}
	}
	
	public class MyStopListener implements ActionListener {
		public void actionPerformed(ActionEvent a){
			sequencer.stop();
			status.setText("Stopped");
		}
	}
	
	public class MyUpTempoListener implements ActionListener {
		public void actionPerformed(ActionEvent a){
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor*1.03));
			
		}
	}
	
	public class MyDownTempoListener implements ActionListener {
		public void actionPerformed(ActionEvent a){
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor*0.97));
		}
	}
	
	public class MySaveListener implements ActionListener {
		public void actionPerformed(ActionEvent a){
			save();
		}
	}
	
	public class MyLoadListener implements ActionListener {
		public void actionPerformed(ActionEvent a){
			load();
		}
	}
	
	
	public void save(){
		
		try{
				
			FileOutputStream fileStream = new FileOutputStream("GuiTestSaves.ser");
			ObjectOutputStream os = new ObjectOutputStream(fileStream);
			
			os.writeObject(checkboxList);
		
			os.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void load(){
		try{
				
			FileInputStream fileStream = new FileInputStream("GuiTestSaves.ser");
			ObjectInputStream os = new ObjectInputStream(fileStream);
			
			Object o = os.readObject();
			
			checkboxList.clear();
			
			checkboxList = (ArrayList<JCheckBox>) o;
			
			
			background.remove(mainPanel);
			
			GridLayout grid = new GridLayout(16,16);
			grid.setVgap(1);
			grid.setHgap(2);
			mainPanel = new JPanel(grid);
			background.add(BorderLayout.CENTER,mainPanel);
			
			for(int i=0;i<256;i++){
				JCheckBox c = new JCheckBox();
				c=checkboxList.get(i);
				mainPanel.add(c);
			}

			theFrame.invalidate();
			theFrame.validate();
			
			os.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void buildTrackAndStart(){
		int[] trackList;
		
		sequence.deleteTrack(track);
		track=sequence.createTrack();

		
		for(int i = 0;i<16;i++){
			
			trackList = new int[16];
			
			int key = instruments[i];
			
			for(int j = 0;j<16;j++){
				
				JCheckBox jc = checkboxList.get(j+16*i);
				
				if(jc.isSelected()){
					trackList[j]=key;
				}
				else{
					trackList[j]=0;
				}
			}
			
			makeTracks(trackList);
			track.add(makeEvent(176,1,127,0,16));
		}
		
		track.add(makeEvent(192,9,0,1,15));
		try{
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void makeTracks(int[] list){

		
		for(int i=0;i<16;i++){
			
			int key = list[i];
			
			if(key!=0){
				track.add(makeEvent(144,9,key,100,i));
				track.add(makeEvent(128,9,key,100,i+1));

			}
			
		}
	
	}
	
	public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick){
		MidiEvent event = null;
		try{
			ShortMessage a = new ShortMessage();
			a.setMessage(comd, chan, one, two);
			event = new MidiEvent(a,tick);
		}catch(Exception e){e.printStackTrace();}
		return event;
	}
	
	public void setUpMidi(){
		try{
			
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequence = new Sequence(Sequence.PPQ,4);
			track = sequence.createTrack();
			sequencer.setTempoInBPM(120);
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}
}