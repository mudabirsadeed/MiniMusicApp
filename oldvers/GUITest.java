import javax.sound.midi.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.net.*;


public class GuiTest{

	JFrame theFrame;
	JPanel mainPanel;
	Sequencer sequencer;
	Sequence sequence;
	Track track;
	ArrayList<JCheckBox> checkboxList;
	JPanel background;
	JLabel status;
	
	int[] presets = {8,17,19,21,23,25,34,42,66,86,88,100,174,203,205,220,223,238,256};
	
	String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
		"Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle","Low Conga","Cowbell", "Vibraslap",
			"Low-mid Tom","High Agogo","Open Hi Conga"};
	
	int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
	
	public static void main(String[] args){
		
		new GuiTest().buildGUI();
	}
	
	public void buildGUI(){
			
		theFrame = new JFrame("Beatbox");
			
		BorderLayout layout = new BorderLayout();
		background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));	
			
		checkboxList = new ArrayList<JCheckBox>();			
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		
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
		
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		for(int i = 0; i <16;i++){
			nameBox.add(new Label(instrumentNames[i]));
		}
		
	

		
		status = new JLabel("MiniMiniMusicApp", SwingConstants.CENTER);
		
		background.add(BorderLayout.NORTH, status);
		background.add(BorderLayout.EAST, buttonBox);
		background.add(BorderLayout.WEST, nameBox);
		
		theFrame.getContentPane().add(background);
		
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
		
		setUpMidi();
		
		theFrame.setBounds(50,50,300,300);
		theFrame.pack();
		theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		theFrame.setVisible(true);
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
			System.out.println("tempoFactor is :" + sequencer.getTempoFactor());
			
		}
	}
	
	public class MyDownTempoListener implements ActionListener {
		public void actionPerformed(ActionEvent a){
			float tempoFactor = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float)(tempoFactor*0.97));
			System.out.println("tempoFactor is :" + sequencer.getTempoFactor());
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