import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class DEVSBasedANN {
	// GUI declarations
	JFrame frame, upFrame;
	JPanel panUp, panLeft, panRight, panDown, leftUp, leftMid, leftDown, rightUp, rightMid, disLPan, rightDown, allPan, inPan1, outPan2, testPan, contPan;
	JButton addPat, dispMod, run, predict, compare, compare2, upload;
	JTextField hidText, neuText, inText, outText, lrText, momText, minEText, epochText, colText, corText, inpTxt, outTxt, testTxt;
	JComboBox actList,actList2, algList, compList2, simList;
	JList compList,actList22;
	JCheckBox chkEpoch;
	JScrollPane scroller, scroller2, viewScroller, leftScroller;
	JFileChooser fileChooser_;
	
	// DEVS ANN parameters declarations
	int no_hid, no_inp, no_out;
	float learnRate, momentum, minError, sim_time;
	String actFunc, learnAlgo;
	int[] hiddenNeurons;
	File fileInput, fileOutput, fileTest;
	ArrayList<String[]> trainingInput, trainingOutput, testingInput;
	float[][] trainInput, trainOutput, testInput;
	String[][] data;
	ArrayList<String> title;
	DefaultTableModel model;
	JTable table;
	JTableHeader header;
	JScrollPane pane;
	ArrayList<Float> mse;
	String calculatedResult;
	ArrayList<String> algoShortName;
	String[] calculatedResults;
	ArrayList<Float>[] mses;
	
	// boolean controls
	boolean isModelSet=false;
	boolean arePatternsSet=false;
	
	public DEVSBasedANN(){
		changeFeel();
		buildGUI();
	}
	
	// ********************************************
	// build GUI for DEVS Based ANN
	// ********************************************
	public void buildGUI(){
		frame = new JFrame("DEVS Based ANN");
		frame.setLayout(new BorderLayout());
		JLabel titLab = new JLabel("DEVS BASED ARTIFICIAL NEURAL NETWORK (ANN) PLATFORM");
		titLab.setFont(new Font("Serif", Font.BOLD, 20));
		titLab.setForeground(Color.BLUE);
		JLabel cLab = new JLabel("(c) David Adelani 2014, MSc Project supervised by Prof Mamadou Traore in African University of Science & Technology, Abuja");
		cLab.setFont(new Font("Serif", Font.BOLD, 10));
		panUp = new JPanel();
		panLeft = new JPanel();
		panLeft.setLayout(new BoxLayout(panLeft, BoxLayout.Y_AXIS));
		panRight = new JPanel();
		panRight.setLayout(new BorderLayout());
		panDown = new JPanel();
		
		JLabel parLab = new JLabel("Parameter Configuration");
		parLab.setFont(new Font("Serif", Font.BOLD, 17));
		parLab.setForeground(Color.gray);
		
		// building of Model Parameter Panel
		leftUp = new JPanel();
		leftUp.setLayout(new BoxLayout(leftUp, BoxLayout.Y_AXIS));
		leftUp.setBorder(new TitledBorder(new LineBorder(Color.BLUE),"Model Parameters"));
		JLabel hidLab = new JLabel("Number of hidden layers");
		JLabel neuLab = new JLabel("Hidden neurons' values");
		JLabel inLab = new JLabel("Number of inputs");
		JLabel outLab = new JLabel("Number of outputs");
		JLabel actLab = new JLabel("Activation Function");
		JLabel algLab = new JLabel("Learning Algorithm");
		
		hidText = new JTextField(11);
		neuText = new JTextField(12);
		inText = new JTextField(15);
		outText = new JTextField(14);
		
		actList = new JComboBox(new String[]{"Binary Sigmoid","Bipolar Sigmoid", "Hyperbolic Tangent", "Gaussian"});
		algList = new JComboBox(new String[]{"Standard BP","BP with Momentum", "Silva & Almeida", "Delta-Bar", "QuickProp", "RPROP"});
		
		addPat = new JButton("View/ Upload Patterns");
		dispMod = new JButton("Set Values & Display Model");
		
		JPanel parPan = new JPanel();
		JPanel hidPan = new JPanel();
		JPanel neuPan = new JPanel();
		JPanel inPan = new JPanel();
		JPanel outPan = new JPanel();
		JPanel actPan = new JPanel();
		JPanel algPan = new JPanel();
		JPanel disPan = new JPanel();
		
		parPan.add(parLab);
		hidPan.add(hidLab);
		hidPan.add(hidText);
		neuPan.add(neuLab);
		neuPan.add(neuText);
		inPan.add(inLab);
		inPan.add(inText);
		outPan.add(outLab);
		outPan.add(outText);
		actPan.add(actLab);
		actPan.add(actList);
		algPan.add(algLab);
		algPan.add(algList);
		disPan.add(addPat);
		disPan.add(dispMod);
		
		leftUp.add(hidPan);
		leftUp.add(neuPan);
		leftUp.add(new JLabel("Space delimiter for layers"));
		leftUp.add(inPan);
		leftUp.add(outPan);
		leftUp.add(actPan);
		leftUp.add(algPan);
		leftUp.add(disPan);
		
		panLeft.add(parPan);
		panLeft.add(leftUp);
		
		// building of Execution Parameter JPanel
		leftMid = new JPanel();
		leftMid.setLayout(new BoxLayout(leftMid, BoxLayout.Y_AXIS));
		leftMid.setBorder(new TitledBorder(new LineBorder(Color.BLUE),"Execution Parameters"));
		
		
		JLabel lrLab = new JLabel("Learning Rate");
		JLabel momLab = new JLabel("Momentum");
		JLabel minELab = new JLabel("Minimum Error");
		JLabel simLab = new JLabel(" DEVS Simulation Time");
		JLabel epochLab = new JLabel("Limit Max Epoch");
		
		lrText = new JTextField(11);
		lrText.setText("0.3");
		momText = new JTextField(13);
		momText.setText("0.7");
		minEText = new JTextField(11);
		minEText.setText("0.001");
		simList = new JComboBox(new String[]{"1000","2000", "5000", "10000", "15000", "20000"});
		epochText = new JTextField(6);
		
		chkEpoch = new JCheckBox();
		
		run = new JButton("Run Simulation");
		predict = new JButton("Predict Input");
		
		JPanel lrPan = new JPanel();
		JPanel momPan = new JPanel();
		JPanel minEPan = new JPanel();
		JPanel simPan = new JPanel();
		JPanel epochPan = new JPanel();
		JPanel runPan = new JPanel();
		
		lrPan.add(lrLab);
		lrPan.add(lrText);
		momPan.add(momLab);
		momPan.add(momText);
		minEPan.add(minELab);
		minEPan.add(minEText);
		simPan.add(simLab);
		simPan.add(simList);
		epochPan.add(chkEpoch);
		epochPan.add(epochLab);
		epochPan.add(epochText);
		runPan.add(run);
		runPan.add(predict);
		
		leftMid.add(lrPan);
		leftMid.add(momPan);
		leftMid.add(minEPan);
		leftMid.add(simPan);
		//leftMid.add(epochPan);
		leftMid.add(runPan);
		
		panLeft.add(leftMid);
		
		// building of Comparison Parameter JPanel
		leftDown = new JPanel();
		leftDown.setBorder(new TitledBorder(new LineBorder(Color.BLUE),"Comparison"));
		/*JLabel colLab = new JLabel("GUI Color");
		JLabel corLab = new JLabel("Coordinates of Graph");
		JLabel compLab = new JLabel("Select: ");
		colText = new JTextField(10);
		corText = new JTextField(11);*/
		compList = new JList(new String[]{"Standard BP","BP with Momentum", "Silva & Almeida", "Delta-Bar", "QuickProp", "RPROP"});
		compList2 = new JComboBox(new String[]{"Standard BP","BP with Momentum", "Silva & Almeida", "Delta-Bar", "QuickProp", "RPROP"});
		actList2 = new JComboBox(new String[]{"Binary Sigmoid","Bipolar Sigmoid", "Hyperbolic Tangent", "Gaussian"});
		actList2.setPreferredSize(new Dimension(50, 20));
		actList22 = new JList(new String[]{"Binary Sigmoid","Bipolar Sigmoid", "Hyperbolic Tangent", "Gaussian"});
		compList2.setPreferredSize(new Dimension(20, 20));
		scroller = new JScrollPane(compList);
		scroller.setPreferredSize(new Dimension(50, 75));
		scroller2 = new JScrollPane(actList22);
		scroller2.setPreferredSize(new Dimension(80, 30));
		
		compare = new JButton("Run & Compare");
		compare2 = new JButton("Run & Compare");
		/*JPanel colPan = new JPanel();
		JPanel corPan = new JPanel();*/
		JPanel compPan = new JPanel();
		compPan.setLayout(new BoxLayout(compPan, BoxLayout.X_AXIS));
		JPanel scrolComPan = new JPanel();
		scrolComPan.setLayout(new BoxLayout(scrolComPan, BoxLayout.Y_AXIS));
		JPanel actComPan = new JPanel();
		actComPan.setLayout(new BoxLayout(actComPan, BoxLayout.Y_AXIS));
		
		JPanel compPan2 = new JPanel();
		compPan2.setLayout(new BoxLayout(compPan2, BoxLayout.X_AXIS));
		JPanel scrolComPan2 = new JPanel();
		scrolComPan2.setLayout(new BoxLayout(scrolComPan2, BoxLayout.Y_AXIS));
		JPanel actComPan2 = new JPanel();
		actComPan2.setLayout(new BoxLayout(actComPan2, BoxLayout.Y_AXIS));
		
		/*colPan.add(colLab);
		colPan.add(colText);
		corPan.add(corLab);
		corPan.add(corText);
		compPan.add(compLab);*/
		ImageIcon plainPics;
		plainPics = createImageIcon("images/plain.jpg", "plain pics");
		JLabel plainLab = new JLabel("", plainPics, JLabel.CENTER);
		plainPics = createImageIcon("images/plain2.jpg", "plain pics");
		JLabel plainLab2 = new JLabel("", plainPics, JLabel.CENTER);
	
		JLabel algL = new JLabel("Learning Algorithm", SwingConstants.LEFT);
		algL.setFont(new Font("Serif", Font.BOLD, 12));
		algL.setForeground(Color.GRAY);
		JLabel actL = new JLabel("Activation");
		actL.setFont(new Font("Serif", Font.BOLD, 12));
		actL.setForeground(Color.GRAY);
		
		scrolComPan.add(algL);
		scrolComPan.add(scroller);
		
		actComPan.add(plainLab2);
		actComPan.add(actL);
		actComPan.add(actList2);
		actComPan.add(plainLab);
		
		compPan.add(scrolComPan);
		compPan.add(actComPan);
		compPan.add(new JLabel("-->"));
		compPan.add(compare);
		
		
		plainPics = createImageIcon("images/plain.jpg", "plain pics");
		plainLab = new JLabel("", plainPics, JLabel.CENTER);
		plainPics = createImageIcon("images/plain2.jpg", "plain pics");
		plainLab2 = new JLabel("", plainPics, JLabel.CENTER);
		algL = new JLabel("Algorithm");
		algL.setFont(new Font("Serif", Font.BOLD, 12));
		algL.setForeground(Color.GRAY);
		actL = new JLabel("Activation");
		actL.setFont(new Font("Serif", Font.BOLD, 12));
		actL.setForeground(Color.GRAY);
		scrolComPan2.add(actL);
		scrolComPan2.add(scroller2);
		
		actComPan2.add(plainLab2);
		actComPan2.add(algL);
		actComPan2.add(compList2);
		actComPan2.add(plainLab);
		compPan2.add(scrolComPan2);
		compPan2.add(actComPan2);
		compPan2.add(new JLabel("-->"));
		compPan2.add(compare2);
		
		//leftDown.add(colPan);
		//leftDown.add(corPan);
		
		leftDown.add(compPan);
		leftDown.add(compPan2);
		leftDown.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT,compPan, compPan2));
		panLeft.add(leftDown);
		leftScroller = new JScrollPane(panLeft);
		leftScroller.setPreferredSize(new Dimension(400, 580));
		
		// Building of DEVS Model View
		rightUp = new JPanel();
		JLabel dispL = new JLabel("Display View");
		dispL.setFont(new Font("Serif", Font.BOLD, 17));
		dispL.setForeground(Color.gray);
		disLPan = new JPanel();
		disLPan.add(dispL);
		panRight.add(disLPan, BorderLayout.NORTH);
		rightMid = new JPanel();
		viewScroller = new JScrollPane(rightMid);
		viewScroller.setBorder(new TitledBorder(new LineBorder(Color.BLUE),"DEVS Model View"));
		viewScroller.setPreferredSize(new Dimension(550, 270));
		panRight.add(viewScroller, BorderLayout.CENTER);
		
		// Building of the Result Graph
		rightDown = new JPanel();
		rightDown.setPreferredSize(new Dimension(550, 270));
		rightDown.setBorder(new TitledBorder(new LineBorder(Color.BLUE),"Result Graph"));
		panRight.add(rightDown, BorderLayout.SOUTH);
	
		// overall fitting to frame
		panUp.add(titLab);
		panDown.add(cLab);
		
		frame.add(panUp, BorderLayout.NORTH);
		frame.add(leftScroller, BorderLayout.WEST);
		frame.add(panRight, BorderLayout.EAST);
		frame.add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroller, panRight), BorderLayout.CENTER);
		frame.add(panDown, BorderLayout.SOUTH);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		
		// action listeners
		addPat.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				upFrame = new JFrame("Upload Data");
				allPan = new JPanel();
				allPan.setLayout(new BoxLayout(allPan, BoxLayout.Y_AXIS));
				inPan1 = new JPanel();
				outPan2 = new JPanel();
				testPan = new JPanel();
				contPan = new JPanel();
				inpTxt = new JTextField(30);
				outTxt = new JTextField(30);
				testTxt = new JTextField(30);
				testTxt.setText("test input for prediction");
				JButton inpBut = new JButton("Choose Input File");
				JButton outBut = new JButton("Choose Output File");
				JButton testBut = new JButton("Choose Test Input File");
				upload = new JButton("Upload");
				JButton exit = new JButton("Exit");
				JButton close = new JButton("Close");
				fileChooser_ = new JFileChooser();
				
				inPan1.add(inpTxt);
				inPan1.add(inpBut);
				outPan2.add(outTxt);
				outPan2.add(outBut);
				testPan.add(testTxt);
				testPan.add(testBut);
				contPan.add(upload);
				contPan.add(exit);
				
				allPan.add(inPan1);
				allPan.add(outPan2);
				allPan.add(testPan);
				allPan.add(contPan);
				
				upFrame.add(allPan);
				upFrame.setVisible(true);
				upFrame.pack();
				upFrame.setLocationRelativeTo(null);
				
				if(arePatternsSet){
					allPan.removeAll();
					allPan.add(inPan1);
					allPan.add(outPan2);
					allPan.add(testPan);
					allPan.add(contPan);
					allPan.add(pane);
					upFrame.add(allPan);
					allPan.revalidate();
					upFrame.pack();
				}
				
				inpBut.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						fileInput = getDocument();		// This gets a document via a file chooser (dialog box)
						if (fileInput != null) {
							inpTxt.setText(fileInput.getPath());							
						}
					}
				});
				
				outBut.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						fileOutput = getDocument();		// This gets a document via a file chooser (dialog box)
						if (fileOutput != null) {
							outTxt.setText(fileOutput.getPath());							
						}
					}
				});
				
				testBut.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						fileTest = getDocument();		// This gets a document via a file chooser (dialog box)
						if (fileTest != null) {
							testTxt.setText(fileTest.getPath());							
						}
					}
				});
				
				exit.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						upFrame.dispose();
					}
				});
				
				upload.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						trainingInput = new ArrayList<String []>();
						trainingOutput = new ArrayList<String []>();
						testingInput = new ArrayList<String []>();
						trainInput = setTrainingData(fileInput, trainingInput);
						trainOutput = setTrainingData(fileOutput, trainingOutput);
						testInput = setTrainingData(fileTest, testingInput);
						arePatternsSet=true;
						
						data = new String[trainInput.length][trainInput[0].length+trainOutput[0].length];
						for(int i=0; i<data.length; i++){
							for(int j=0; j<trainInput[0].length; j++)
								data[i][j] = String.valueOf(trainInput[i][j]);
						}
						int startPos=trainInput[0].length;
						for(int i=0; i<data.length; i++){
							for(int j=startPos; j<data[0].length; j++){
								//System.out.println(j);
								data[i][j] = String.valueOf(trainOutput[i][j-startPos]);
							}
						}
						
						title = new ArrayList<String>();
						for(int i=0; i<trainInput[0].length; i++){
							title.add("Input"+(i+1));
						}
						for(int i=0; i<trainOutput[0].length; i++){
							title.add("Output"+(i+1));
						}
						model = new DefaultTableModel(data, title.toArray());
						table = new JTable(model);
						header = table.getTableHeader();
						header.setForeground(Color.BLUE);
						pane = new JScrollPane(table);
						pane.setPreferredSize(new Dimension(700, 250));
						allPan.removeAll();
						allPan.add(inPan1);
						allPan.add(outPan2);
						allPan.add(testPan);
						allPan.add(contPan);
						allPan.add(pane);
						upFrame.add(allPan);
						allPan.revalidate();
						upFrame.pack();
					}
				});
				
				close.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						upFrame.setVisible(false);
					}
				});
			}
		});
		
		dispMod.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				no_hid = hidText.getText().isEmpty()?0:Integer.parseInt(hidText.getText());
				String[] neuronText = neuText.getText().split("\\s+");
				no_inp = inText.getText().isEmpty()?0:Integer.parseInt(inText.getText());
				no_out = outText.getText().isEmpty()?0:Integer.parseInt(outText.getText());
				actFunc = (String) actList.getSelectedItem();
				learnAlgo = (String) algList.getSelectedItem();
				
				if(no_hid!=0 && no_hid==neuronText.length){
					hiddenNeurons = new int[neuronText.length+2];
					hiddenNeurons[0]=no_inp;
					for(int i=1; i<hiddenNeurons.length-1; i++){
						hiddenNeurons[i]=Integer.parseInt(neuronText[i-1]);
					}
					hiddenNeurons[hiddenNeurons.length-1]=no_out;
					//System.out.println(Arrays.toString(hiddenNeurons));
					isModelSet=true;
					JOptionPane.showMessageDialog(null, "Model Parameters correctly set");
					ModelDiagram mdiagram = new ModelDiagram(hiddenNeurons);
					
					fillInitWeightsFile(hiddenNeurons);
					panRight.removeAll();
					panRight.add(disLPan, BorderLayout.NORTH);
					panRight.add(mdiagram.panScrollPane(), BorderLayout.CENTER);
					panRight.add(rightDown, BorderLayout.SOUTH);
					panRight.revalidate();
					/*hidText.setText("");
					neuText.setText("");
					inText.setText("");
					outText.setText("");*/
				}
				else if(no_hid!=neuronText.length){
					JOptionPane.showMessageDialog(frame, "The number of hidden layer is different from the specified neurons", "Failure", JOptionPane.ERROR_MESSAGE);
				}
				/*else{
					actFunc = (String) actList.getSelectedItem();
					learnAlgo = (String) algList.getSelectedItem();
					hidText.setText("");
					neuText.setText("");
					inText.setText("");
					outText.setText("");
				}*/
			}
		});
		
		run.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(isModelSet && arePatternsSet && no_inp==trainInput[0].length && no_out==trainOutput[0].length){
					learnRate = Float.parseFloat(lrText.getText());
					momentum = Float.parseFloat(momText.getText());
					minError = Float.parseFloat(minEText.getText());
					sim_time = Float.parseFloat((String) simList.getSelectedItem());
					/*lrText.setText("");
					momText.setText("");
					minEText.setText("");*/
					Simulation s = new Simulation("",trainInput, trainOutput, minError, learnAlgo, actFunc, hiddenNeurons,trainInput.length , sim_time, true);
					s.start();
					try {
						s.join();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					java.net.URL fileURL = DEVSBasedANN.class.getResource("files/ERROR.txt");
					File newFile = new File(fileURL.getPath());
					printResult(newFile, "Pattern Calculated Output");
					plotGraph(mse, learnAlgo);
					fileURL = DEVSBasedANN.class.getResource("files/UPDATEDWEIGHTS.txt");
					newFile = new File(fileURL.getPath());
					setCorrectedWeights(newFile, hiddenNeurons.length-1);
				}
				else{
					if(no_inp!=trainInput[0].length || no_out!=trainOutput[0].length){
						JOptionPane.showMessageDialog(frame, "Number of Input/Output parameter is different from that of the uploaded file", "Execution Error", JOptionPane.ERROR_MESSAGE);
					}
					else{
						JOptionPane.showMessageDialog(frame, "Set Model Parameters and/or Upload Training Data", "Execution Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		
		predict.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
					float[][] testOutput = new float[testInput.length][trainOutput[0].length];
					Simulation s = new Simulation("",testInput, testOutput, minError, learnAlgo, actFunc, hiddenNeurons,testInput.length , sim_time, false);
					s.start();
					try {
						s.join();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					java.net.URL fileURL = DEVSBasedANN.class.getResource("files/ERROR.txt");
					File newFile = new File(fileURL.getPath());
					printResult(newFile, "Predicted Output");
			}
		});
		
		compare.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				algoShortName = new ArrayList<String>();
				ArrayList<File> algoFiles = new ArrayList<File>();
				
				if(isModelSet && arePatternsSet && no_inp==trainInput[0].length && no_out==trainOutput[0].length){
					learnRate = Float.parseFloat(lrText.getText());
					momentum = Float.parseFloat(momText.getText());
					minError = Float.parseFloat(minEText.getText());
					sim_time = Float.parseFloat((String) simList.getSelectedItem());
					
					String actf = (String) actList2.getSelectedItem();
					Object[] algos = compList.getSelectedValues();
					Simulation[] sim = new Simulation[algos.length];
					String apName="";
					if(algos.length<1){
						JOptionPane.showMessageDialog(null, "Please Select at least one algorithm");
					}
					else{
						for(int i=0; i<algos.length; i++){
							String algo = algos[i].toString();
							if(algo.equals("Standard BP")) apName="BP";
							else if(algo.equals("BP with Momentum")) apName="MB";
							else if(algo.equals("Silva & Almeida")) apName="SA";
							else if(algo.equals("Delta-Bar")) apName="DB";
							else if(algo.equals("QuickProp")) apName="QP";
							else apName="RP";
							algoShortName.add(apName);
							sim[i] = new Simulation(apName, trainInput, trainOutput, minError, algo, actf, hiddenNeurons,trainInput.length , sim_time, true);
							sim[i].start();
							//java.net.URL fileURL = DEVSBasedANN.class.getResource("files/ERROR"+apName+".txt");
							//algoFiles.add(new File(fileURL.getPath()));
						}
						
						for(int i=0; i<algoShortName.size(); i++){
							try {
								sim[i].join();
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							java.net.URL fileURL = DEVSBasedANN.class.getResource("files/ERROR"+algoShortName.get(i)+".txt");
							algoFiles.add(new File(fileURL.getPath()));
						}
						
						printResult(algoFiles, algoShortName);
						plotGraph(mses, algoShortName);
					}					
					java.net.URL fileURL = DEVSBasedANN.class.getResource("files/UPDATEDWEIGHTS.txt");
					File newFile = new File(fileURL.getPath());
					setCorrectedWeights(newFile, hiddenNeurons.length-1);
					
				}
				else{
					if(no_inp!=trainInput[0].length || no_out!=trainOutput[0].length){
						JOptionPane.showMessageDialog(frame, "Number of Input/Output parameter is different from that of the uploaded file", "Execution Error", JOptionPane.ERROR_MESSAGE);
					}
					else{
						JOptionPane.showMessageDialog(frame, "Set Model Parameters and/or Upload Training Data", "Execution Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				compList.clearSelection();
			}
		});
		
		compare2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				algoShortName = new ArrayList<String>();
				ArrayList<File> algoFiles = new ArrayList<File>();
				
				if(isModelSet && arePatternsSet && no_inp==trainInput[0].length && no_out==trainOutput[0].length){
					learnRate = Float.parseFloat(lrText.getText());
					momentum = Float.parseFloat(momText.getText());
					minError = Float.parseFloat(minEText.getText());
					sim_time = Float.parseFloat((String) simList.getSelectedItem());
					
					String algor = (String) compList2.getSelectedItem();
					Object[] acfs = actList22.getSelectedValues();
					Simulation[] sim = new Simulation[acfs.length];
					String apName="";
					if(acfs.length<1){
						JOptionPane.showMessageDialog(null, "Please Select at least one Activation Function");				
					}
					else{
						for(int i=0; i<acfs.length; i++){
							String act = acfs[i].toString();
							if(act.equals("Binary Sigmoid")) apName="BN";
							else if(act.equals("Bipolar Sigmoid")) apName="BP";
							else if(act.equals("Hyperbolic Tangent")) apName="HT";
							else apName="GS";
							algoShortName.add(apName);
							sim[i] = new Simulation(apName, trainInput, trainOutput, minError, algor, act, hiddenNeurons,trainInput.length , sim_time, true);
							sim[i].start();
						}
						//actList22 = new JList(new String[]{"Binary Sigmoid","Bipolar Sigmoid", "Hyperbolic Tangent", "Gaussian"});

						for(int i=0; i<algoShortName.size(); i++){
							try {
								sim[i].join();
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							java.net.URL fileURL = DEVSBasedANN.class.getResource("files/ERROR"+algoShortName.get(i)+".txt");
							algoFiles.add(new File(fileURL.getPath()));
						}
						printResult(algoFiles, algoShortName);
						plotGraph(mses, algoShortName);
					}					
					java.net.URL fileURL = DEVSBasedANN.class.getResource("files/UPDATEDWEIGHTS.txt");
					File newFile = new File(fileURL.getPath());
					setCorrectedWeights(newFile, hiddenNeurons.length-1);
					
				}
				else{
					if(no_inp!=trainInput[0].length || no_out!=trainOutput[0].length){
						JOptionPane.showMessageDialog(frame, "Number of Input/Output parameter is different from that of the uploaded file", "Execution Error", JOptionPane.ERROR_MESSAGE);
					}
					else{
						JOptionPane.showMessageDialog(frame, "Set Model Parameters and/or Upload Training Data", "Execution Error", JOptionPane.ERROR_MESSAGE);
					}
				}
				actList22.clearSelection();
			}
		});
		
	}
	
	// ********************************************
	// Method to fill initial Weights into File.
	// ********************************************
	public void fillInitWeightsFile(int[] layers) {
		PrintWriter out = null;
		try {
			java.net.URL fileURL = DEVSBasedANN.class.getResource("files/readme.txt");
			File file = new File(fileURL.getPath());
			String fileDir = file.getParent(); 
			String fileName = "INITWEIGHTS.txt";
			File newFile = new File (fileDir,fileName);
			out = new PrintWriter(newFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Random r = new Random();
		float weight;
		String text="";
		for(int i=0; i<layers.length-1; i++){
			text="#"+i+"@ : ";
			for(int j=0; j<layers[i]+1; j++){
				for(int k=0; k<layers[i+1]; k++){
					weight =r.nextFloat()*2-1.0f;
					text += weight+", ";
				}
				text+=" # ";
			}
			out.println(text);
		}
		out.close();
	}

	// ********************************************
	// Gets a Document from a File Chooser
	// ********************************************
	protected File getDocument() {
		File file = null;
		int returnVal = fileChooser_.showOpenDialog(upFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fileChooser_.getSelectedFile();
		}
		return file;
	}
	
	// ********************************************
	// creates and returns ImageIcon
	// ********************************************
	protected ImageIcon createImageIcon(String path, String description) {
		java.net.URL imgURL = DEVSBasedANN.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
	
	// ********************************************
	// Initialize the data list from the file given
	// ********************************************
	public float[][] setTrainingData(File file, ArrayList<String[]> infos_) {
		// Load the file
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;

		float[][] trainVal = null;
		try {
			fis = new FileInputStream(file);
			// Here BufferedInputStream is added for fast reading
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
	         // = new BufferedReader(new InputStreamReader(in));
	          
			// dis.available() returns 0 if the file does not have more lines
			String[] infos = null;
			while (dis.available() != 0) {
				String line = dis.readLine();
				infos = line.split("\\s+");
				infos_.add(infos);
			}
			
			trainVal = new float[infos_.size()][infos.length];
			for(int i=0; i<infos_.size(); i++){
				for(int j=0; j<infos.length; j++)
					trainVal[i][j]=Float.parseFloat(infos_.get(i)[j]);
			}
			// Dispose all the resources after using them
			fis.close();
			bis.close();
			dis.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return trainVal;
	}
	
	// ********************************************
	// Display values gotten from File
	// ********************************************
	public void printResult(File file, String heading) {
		// Load the file
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		mse = new ArrayList<Float>();

		try {
			fis = new FileInputStream(file);
			// Here BufferedInputStream is added for fast reading
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			while (dis.available() != 0) {
				String line = dis.readLine();
				if(line.contains("@")){
					//System.out.println("-> "+line);
					String[] epMSE = line.split("@");
					mse.add(Integer.parseInt(epMSE[0]), Float.parseFloat(epMSE[1]));
					calculatedResult="";
				}
				else{
					//System.out.println("<-> "+line);
					calculatedResult+=line+"\n";
				}
				
			}
			JTextArea textArea = new JTextArea(calculatedResult);
			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setPreferredSize(new Dimension(400, 400));
			JOptionPane.showMessageDialog(null, scrollPane, heading, JOptionPane.INFORMATION_MESSAGE);
			// Dispose all the resources after using them
			fis.close();
			bis.close();
			dis.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	// ********************************************
	// Display values gotten from File
	// ********************************************
	public void printResult(ArrayList<File> files, ArrayList<String> algnames) {
		// Load the file
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		//mse = new ArrayList<Float>();
		calculatedResults = new String[files.size()];
		mses = new ArrayList[files.size()];
		String cOutput="";
		try {
			for(int i=0; i<files.size(); i++){
				fis = new FileInputStream(files.get(i));
				// Here BufferedInputStream is added for fast reading
				bis = new BufferedInputStream(fis);
				dis = new DataInputStream(bis);
				mses[i] = new ArrayList<Float>();
				while (dis.available() != 0) {
					String line = dis.readLine();
					if(line.contains("@")){
						//System.out.println("-> "+line);
						String[] epMSE = line.split("@");
						mses[i].add(Integer.parseInt(epMSE[0]), Float.parseFloat(epMSE[1]));
						calculatedResults[i]="";
					}
					else{
						//System.out.println("<-> "+line);
						calculatedResults[i]+=line+"\n";
					}
					
				}
				cOutput+="For "+algnames.get(i)+"\n"+calculatedResults[i]+"\n";
			}
			JTextArea textArea = new JTextArea(cOutput);
			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setPreferredSize(new Dimension(400, 400));
			JOptionPane.showMessageDialog(null, scrollPane, "Pattern Calculated Output", JOptionPane.INFORMATION_MESSAGE);

			// Dispose all the resources after using them
			fis.close();
			bis.close();
			dis.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
	}
	
	// ***************************************************
	// plot Graph using JFreeChart Package
	// ***************************************************
	public void plotGraph(ArrayList<Float> yValues, String algname){
		final XYSeries s1 = new XYSeries(algname);
		
	    for (int i = 0; i <yValues.size(); i++) {
	        s1.add(i+1, yValues.get(i));
	    }

        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(s1);

        final JFreeChart chart = ChartFactory.createXYLineChart(
            "EPOCH VS MSE GRAPH",          // chart title
            "Category",               // domain axis label
            "Value",                  // range axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,
            false
        );

        final XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("Epoch");
        final NumberAxis rangeAxis = new LogarithmicAxis("Mean Square Error");
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        chart.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(Color.black);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        panRight.remove(rightDown);
        panRight.add(chartPanel, BorderLayout.SOUTH);
        panRight.revalidate();
        //add(chartPanel);
        frame.pack();
	}
	
	// ***************************************************
	// plot Graph using JFreeChart Package
	// ***************************************************
	public void plotGraph(ArrayList<Float>[] yValues, ArrayList<String> shortNames){
		XYSeries[] xyser = new XYSeries[yValues.length];
		for(int i=0; i<xyser.length;i++){
			xyser[i] = new XYSeries(shortNames.get(i));
		}
		
		for(int i=0; i<yValues.length; i++){
			for(int j=0; j<yValues[i].size(); j++){
				//System.out.println(" "+i+" nnn "+j);
				xyser[i].add(j+1, yValues[i].get(j));
			}
		}
	        

        final XYSeriesCollection dataset = new XYSeriesCollection();
        for(int i=0; i<xyser.length;i++){
        	dataset.addSeries(xyser[i]); 
		}
        

        final JFreeChart chart = ChartFactory.createXYLineChart(
            "EPOCH VS MSE GRAPH",          // chart title
            "Category",               // domain axis label
            "Value",                  // range axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,
            false
        );

        final XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("Epoch");
        final NumberAxis rangeAxis = new LogarithmicAxis("Mean Square Error");
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        chart.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(Color.black);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        panRight.remove(rightDown);
        panRight.add(chartPanel, BorderLayout.SOUTH);
        panRight.revalidate();
        //add(chartPanel);
        frame.pack();
	}
	
	// ********************************************
	// set the last uodated weight into new file
	// ********************************************
	public void setCorrectedWeights(File myfile, int num_con)
	{
		String[] weightList = new String[num_con];
		
		// Load the file
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		mse = new ArrayList<Float>();

		try {
			fis = new FileInputStream(myfile);
			// Here BufferedInputStream is added for fast reading
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			int cnt = num_con-1;
			while (dis.available() != 0) {
				String line = dis.readLine();
			       if(line.contains("&")){
			    	   cnt=num_con-1;
			    	   continue;
			       }
			       if(cnt>=0){
			    	   weightList[cnt--]=line.substring(0, line.length()-1);
			       }     
			}
			java.net.URL fileURL = DEVSBasedANN.class.getResource("files/readme.txt");
			File file = new File(fileURL.getPath());
			String fileDir = file.getParent(); 
			String fileName = "TRAINEDWEIGHTS.txt";
			File newFile = new File (fileDir,fileName);
			PrintWriter out = new PrintWriter(newFile);
			
			for(int i=0; i<weightList.length; i++){
				out.println(weightList[i]);
			}
			out.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	// ***************************************************
	// To select the Look and Feel of Windows for the GUI
	// ***************************************************
	public void changeFeel(){
		
    	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {	
			e.printStackTrace();
		} catch (InstantiationException e) {			
			e.printStackTrace();
		} catch (IllegalAccessException e) {		
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {	
			e.printStackTrace();
		}
	}	
	
	public static void main(String args[]){
		DEVSBasedANN devann = new DEVSBasedANN();
	}
}
