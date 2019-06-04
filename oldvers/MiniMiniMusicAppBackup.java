import javax.sound.midi.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class MiniMiniMusicAppBackup {
	static int c1time = 0;
	static int c2time = 0;
	static int c3time = 0;
	static int c4time = 0;
	static int c5time = 0;
	static int c6time = 0;
	static int c7time = 0;
	static int c8time = 0;
	static int c9time = 0;
	static int time = 0;	
	static int currentChannel = 1;
	
	static MiniMiniMusicAppBackup mini;
	//static MiniMiniMusicAppBackup.MyAnimator animator;
	static MiniMiniMusicAppBackup.MyWindowCreator windowCreator;
	
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
		
		mini = new MiniMiniMusicAppBackup();
		windowCreator = new MiniMiniMusicAppBackup().new MyWindowCreator();
		//animator = new MiniMiniMusicAppBackup().new MyAnimator();
		
		windowCreator.createWindow();
		
		
	}

	public class MyWindowCreator{
		
		public void createWindow(){
			
			mini.setUpMidi();
			frame = new JFrame("Beatbox");
			
			BorderLayout layout = new BorderLayout();
			JPanel background = new JPanel(layout);
			background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			
			checkboxList = new ArrayList<JCheckBox>();
			Box buttonBox = new Box(BoxLayout.Y_AXIS);
			
			JButton start = new JButton("Start");
			start.addActionListener(new MyStartListener());
			buttonBox.add(start);
			
			JButton stop = new JButton("Stop");
			start.addActionListener(new MyStopListener());
			buttonBox.add(stop);
			
			JButton upTempo = new JButton("Tempo Up");
			start.addActionListener(new MyUpTempoListener());
			buttonBox.add(upTempo);
			
			JButton downTempo = new JButton("Tempo Down");
			start.addActionListener(new MyDownTempoListener());
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
			
			
			
			/*frame.getContentPane().add(BorderLayout.CENTER,animator);*/
			
			frame.setBounds(50,50,300,300);
			frame.pack();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
		
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
					trackList[j]=1;
				}
			}
			
			makeTrack(trackList);
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
			if(instrumentID==1){
				addNoteToTrack(1,1);
			}
			else if(instrumentID!=1){
				addControl();
				addNoteToTrack(instrumentID, 100);
			}
		}
		time=0;
		//first=1;
	}
	
	
	//sets up and open the player, sequence and track to be used in other parts of the program

	public void setUpMidi(){
		try{
			int[] events = {127};
			
			player = MidiSystem.getSequencer();
			player.open();
			player.addControllerEventListener(animator, events);
			seq = new Sequence(Sequence.PPQ,4);
			track = seq.createTrack();
			player.setTempoInBPM(120);
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
	}
		
	//a condensed note adding method, with integrated 'current time' rather than absolute time(eg. array[time+=length] as opposed to array[time])
	public void addNoteToTrack(int note, int length, int velocity){
		
			startNote(note, velocity);
			time=time+length;
			endNote(note, velocity);
			
	}
	
	public void addNoteToTrack(int note, int velocity){
		addNoteToTrack(note, 1, velocity);
	}

	public void startNote(int note, int velocity){
		doInstruction(144, note, velocity);
	}
	
	public void endNote(int note, int velocity){
		doInstruction(128, note, velocity);
	}
	
	public void changeInstrument(int instrument){
		doInstruction(192, instrument, 0);
	}
	//adds a controlEvent to the track at certain points so it can trigger other things, in this case controlChange in the animation inner class
	public void addControl(){
		doInstruction(176, 127, 0);
	}
	
	
	//a generalised MidiEvent creator, which also adds said event to the current track
	public void doInstruction(int instruction,  int noteOrInstrument,  int velocity){
		
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
		
		if (newChannel<=9 && newChannel>=1){
			saveCurrentChannelTime();
			loadNewChannelTime(newChannel);
		}
		
	}
	
	public void saveCurrentChannelTime(){
		
		switch(currentChannel){
			case 1:
				c1time = time;
				break;
			case 2:
				c2time = time;
				break;
			case 3:
				c3time = time;
				break;
			case 4:
				c4time = time;
				break;
			case 5:
				c5time = time;
				break;
			case 6:
				c6time = time;
				break;
			case 7:
				c7time = time;
				break;
			case 8:
				c8time = time;
				break;
			case 9:
				c9time = time;
				break;
		}
			
	}
	
	public void loadNewChannelTime(int newChannel){
		
		switch(newChannel){
			case 1:
				time = c1time;
				break;
			case 2:
				time = c2time;
				break;
			case 3:
				time = c3time;
				break;
			case 4:
				time = c4time;
				break;
			case 5:
				time = c5time;
				break;
			case 6:
				time = c6time;
				break;
			case 7:
				time = c7time;
				break;
			case 8:
				time = c8time;
				break;
			case 9:
				time = c9time;
				break;
		}
			
	}
		/*public class MyAnimator extends JPanel implements ControllerEventListener {
		boolean msg = false;
		
		public void controlChange(ShortMessage event){
			msg = true;
			repaint();
		}
		
		public void paintComponent(Graphics g1){
			if(msg){
				
				Graphics2D g2d = (Graphics2D) g1;
		
				int r = (int) (Math.random()*256);
				int g = (int) (Math.random()*256);
				int b = (int) (Math.random()*256);
		
				Color randomColor = new Color(r,g,b);
		
				r = (int) (Math.random()*256);
				g = (int) (Math.random()*256);
				b = (int) (Math.random()*256);
		
				Color randomColor2 = new Color(r,g,b);
		
				int x = (int) ((Math.random()*100)+50);
				int y = (int) ((Math.random()*100)+50);
				int w = (int) ((Math.random()*50)+100);
				int h = (int) ((Math.random()*50)+100);
		
				//x1,y1 is startpos1, x2,y2 is size, ditto x3,y3 , x4,y4
		
				GradientPaint gradient = new GradientPaint(x,y,randomColor,w,h, randomColor2);

				g2d.setPaint(gradient);
				g2d.fillOval(x,y,w,h);
				
				msg = false;
			}
		}
	}*/
	/*
	public void go(){
		int[] events = {127};
		
		this.play(events);
	}
	public void play(int[] events) {
		
		try{
			
			//makeMusic();
			player.setSequence(seq);
			player.start();
			System.out.println("Playing Music");
		
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
		
	}	
	
	public void makeMusic(){
		
		
		int length = 6;
		int offset = 64;
		
		
		changeInstrument(1);
		
		addNoteToTrack(offset, length, 100);
		addNoteToTrack(offset+9, length, 100);					
		addNoteToTrack(offset+6, length, 100);					
		addNoteToTrack(offset-4, length, 100);
		addNoteToTrack(offset, length, 100);
		addNoteToTrack(offset+9, length, 100);					
		addNoteToTrack(offset+6, length, 100);					
		addNoteToTrack(offset-4, length, 100);
		addNoteToTrack(offset, length, 100);
		addNoteToTrack(offset+9, length, 100);					
		addNoteToTrack(offset+6, length, 100);					
		addNoteToTrack(offset-4, length, 100);
		addNoteToTrack(offset, length, 100);
		addNoteToTrack(offset+9, length, 100);					
		addNoteToTrack(offset+6, length, 100);					
		addNoteToTrack(offset-4, length, 100);
		
		changeChannel(2);
		
		addNoteToTrack(offset, length, 1);
		
		
		addNoteToTrack(offset, length, 100);
		addNoteToTrack(offset+4, length, 100);
		addNoteToTrack(offset+9, length, 100);
		addNoteToTrack(offset-4, length, 100);
		addNoteToTrack(offset, length, 100);
		addNoteToTrack(offset+4, length, 100);
		addNoteToTrack(offset+9, length, 100);
		addNoteToTrack(offset-4, length, 100);
		addNoteToTrack(offset, length, 100);
		addNoteToTrack(offset+4, length, 100);
		addNoteToTrack(offset+9, length, 100);
		addNoteToTrack(offset-4, length, 100);
		addNoteToTrack(offset, length, 100);
		addNoteToTrack(offset+4, length, 100);
		addNoteToTrack(offset+9, length, 100);
		addNoteToTrack(offset-4, length, 100);
		
		changeChannel(3);
		changeInstrument(12);
		
		addNoteToTrack(offset, length, 100);
		addNoteToTrack(offset, length, 1);
		addNoteToTrack(offset+6, length, 80);
		addNoteToTrack(offset, length, 1);
		addNoteToTrack(offset, length, 100);
		addNoteToTrack(offset, length, 1);				
		addNoteToTrack(offset+6, length, 80);			
		addNoteToTrack(offset, length, 1);
		addNoteToTrack(offset, length, 100);
		addNoteToTrack(offset, length, 1);
		addNoteToTrack(offset+6, length, 80);
		addNoteToTrack(offset, length, 1);
		addNoteToTrack(offset, length, 100);
		addNoteToTrack(offset, length, 1);
		addNoteToTrack(offset+6, length, 80);
		addNoteToTrack(offset, length, 1);			

		changeChannel(4);
		changeInstrument(5);
		
		addNoteToTrack(offset, length+2, 40);
		addNoteToTrack(offset+10, length, 20);
		addNoteToTrack(offset, length, 1);
		addNoteToTrack(offset, length, 1);
		addNoteToTrack(offset, length, 40);
		addNoteToTrack(offset+10, length, 20);
		addNoteToTrack(offset, length, 1);
		addNoteToTrack(offset, length, 1);
		addNoteToTrack(offset, length, 40);
		addNoteToTrack(offset+10, length, 20);
		addNoteToTrack(offset, length, 1);
		addNoteToTrack(offset, length, 1);
		addNoteToTrack(offset, length, 40);
		addNoteToTrack(offset+10, length, 20);
		addNoteToTrack(offset, length, 1);
		addNoteToTrack(offset, length, 1);
		
		changeChannel(1);
		
	}*/
	

}