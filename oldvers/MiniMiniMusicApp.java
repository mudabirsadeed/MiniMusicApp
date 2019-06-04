import javax.sound.midi.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class MiniMiniMusicApp {
	
	static int currentChannel = 1;
	
	static MiniMiniMusicApp mini;
	static MiniMiniMusicApp.MyStartListener startListener;
	static MiniMiniMusicApp.MyStopListener stopListener;
	static MiniMiniMusicApp.MyUpTempoListener upTempoListener;
	static MiniMiniMusicApp.MyDownTempoListener downTempoListener;
		
	static JFrame frame;
	static JPanel mainPanel;
	
	Sequencer player;
	Sequence seq;
	Track track;
	ArrayList<JCheckBox> checkboxList;
	
	String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
		"Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle","Low Conga","Cowbell", "Vibraslap",
			"Low-mid Tom","High Agogo","Open Hi Conga"};
	
	int[] instrumentsID = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
	
	
	public static void main(String[] args){
		
		mini = new MiniMiniMusicApp();
		startListener = new MiniMiniMusicApp().new MyStartListener();
		stopListener = new MiniMiniMusicApp().new MyStopListener();
		upTempoListener = new MiniMiniMusicApp().new MyUpTempoListener();
		downTempoListener = new MiniMiniMusicApp().new MyDownTempoListener();
		
		mini.createWindow();
		
		
	}
	
	public void createWindow(){
			
		this.setUpMidi();
		frame = new JFrame("Beatbox");
			
		BorderLayout layout = new BorderLayout();
		JPanel background = new JPanel(layout);
		background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));		
		checkboxList = new ArrayList<JCheckBox>();			
		Box buttonBox = new Box(BoxLayout.Y_AXIS);
		
		JButton start = new JButton("Start");
		start.addActionListener(startListener);
		buttonBox.add(start);
		
		JButton stop = new JButton("Stop");
		start.addActionListener(stopListener);
		buttonBox.add(stop);
		
		JButton upTempo = new JButton("Tempo Up");
		start.addActionListener(upTempoListener);
		buttonBox.add(upTempo);
		
		JButton downTempo = new JButton("Tempo Down");
		start.addActionListener(downTempoListener);
		buttonBox.add(downTempo);
		
		Box nameBox = new Box(BoxLayout.Y_AXIS);
		for(int i = 0; i <16;i++){
			nameBox.add(new Label(instrumentNames[i]));
		}
		
		background.add(BorderLayout.EAST, buttonBox);
		background.add(BorderLayout.WEST, nameBox);
		
		frame.getContentPane().add(background);
		
		GridLayout grid = new GridLayout(16,16);
		grid.setVgap(1);
		grid.setHgap(2);
		mainPanel = new JPanel(grid);
		background.add(BorderLayout.CENTER,mainPanel);
		
		for(int i=0;i<256;i++){
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			checkboxList.add(c);
			mainPanel.add(c);
		}
		
		
		frame.setBounds(50,50,300,300);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	


	public class MyStartListener implements ActionListener {
		public void actionPerformed(ActionEvent a){
			buildTrackAndStart();
		}
	}
	
	public class MyStopListener implements ActionListener {
		public void actionPerformed(ActionEvent a){
			player.stop();
		}
	}
	
	public class MyUpTempoListener implements ActionListener {
		public void actionPerformed(ActionEvent a){
			float tempoFactor = player.getTempoFactor();
			player.setTempoFactor((float)(tempoFactor*1.03));
			
		}
	}
	
	public class MyDownTempoListener implements ActionListener {
		public void actionPerformed(ActionEvent a){
			float tempoFactor = player.getTempoFactor();
			player.setTempoFactor((float)(tempoFactor*0.97));
			
		}
	}
	
	public void buildTrackAndStart(){
		int[] trackList;
		
		seq.deleteTrack(track);
		track=seq.createTrack();
		
		changeChannel(9);
		
		for(int i = 0;i<16;i++){
			
			trackList = new int[16];
			
			int instrumentID = instrumentsID[i];
			
			for(int j = 0;j<16;j++){
				
				JCheckBox jc = checkboxList.get(j+16*i);
				
				if(jc.isSelected()){
					trackList[j]=instrumentID;
				}
				else{
					trackList[j]=0;
				}
			}
			
			makeTrack(trackList);
			addNoteToTrack(1,0,15);
		}
		
		try{
			player.setSequence(seq);
			player.setLoopCount(player.LOOP_CONTINUOUSLY);
			player.start();
			player.setTempoInBPM(120);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	public void makeTrack(int[] list){
		//static int first = 0;
		
		for(int i=0;i<16;i++){
			
			int instrumentID = list[i];
			
			if(instrumentID!=0){

				addNoteToTrack(instrumentID, 100, i);
			}
		}
		//first=1;
	}
	
	
	//sets up and open the player, sequence and track to be used in other parts of the program

	public void setUpMidi(){
		try{
			
			player = MidiSystem.getSequencer();
			player.open();
			seq = new Sequence(Sequence.PPQ,4);
			track = seq.createTrack();
			player.setTempoInBPM(120);
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}
		
	//a condensed note adding method, with integrated 'current time' rather than absolute time(eg. array[time+=length] as opposed to array[time])
	public void addNoteToTrack(int note, int velocity, int time){
		
			startNote(note, velocity, time);;
			endNote(note, velocity, time+1);
			
	}

	public void startNote(int note, int velocity, int time){
		doInstruction(144, note, velocity, time);
	}
	
	public void endNote(int note, int velocity, int time){
		doInstruction(128, note, velocity, time);
	}
	
	//adds a controlEvent to the track at certain points so it can trigger other things, in this case controlChange in the animation inner class

	
	
	//a generalised MidiEvent creator, which also adds said event to the current track
	public void doInstruction(int instruction,  int noteOrInstrument,  int velocity, int time){
		
		try{
			ShortMessage a = new ShortMessage();
			a.setMessage(instruction, currentChannel, noteOrInstrument, velocity);
			MidiEvent noteOn = new MidiEvent (a, time);
			track.add(noteOn);	
		}
		catch (Exception ex){
			ex.printStackTrace();
		}	
	}
	
	public void changeChannel(int newChannel){
		currentChannel=newChannel;
	}
}