import exception.DEVS_Exception;
import model.*;
import types.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class CalculationLayer extends AtomicModel {
	// Definition of S :
	// there is only one single possible state which lasts for ever until a trigger is received 
	// a state variable is used for the current repository (a file)
	PrintStream trajectory, out;
	int num_input, num_output, no_of_patterns;
	ArrayList<float[]> input;
	float[][] output, weightedSum;
	float[][] weights;
	int cnt=0, pat=0, p=0, counter=0, maxId;
	String actType, recv="", recv1="",modelID, dir;
	boolean inputReceived=false, updateReceived=false;
	
	int state;
	// 0 for passive
	// 1 for active
	
	public CalculationLayer(String name, String desc, int no_input, int no_output, int no_pat, String actT, int lyno, PrintStream ot, int maxID, boolean doTraining) {
		super(name, desc);
		out = ot;
		num_input = no_input;
		num_output = no_output;
		no_of_patterns = no_pat;
		actType = actT;
		
		input = new ArrayList<float[]>();
		output = new float[no_of_patterns][num_output];
		weightedSum = new float[no_of_patterns][num_output];
		weights = new float[num_input+1][num_output];
				
		state = 0;
				
		for(int i=0; i<num_output; i++){
			addOutputPortStructure(new DEVS_String(""), this.getName()+".HOUT"+i, "calculated values for output");
		}
		
		addOutputPortStructure(new DEVS_String(""), this.getName()+".HWOUT", "sends out weight_list");
		
		// Definition of the input port (for X):
		// the name of the port is xxx.store if the name of the model is xxx
		for(int i=0; i<no_input; i++){
			addInputPortStructure(new DEVS_String(""), this.getName()+".HIN"+i, "Data to process");
		}
		
		addInputPortStructure(new DEVS_String(""), this.getName()+".UPIN", "Update Weights");
		// State initialization: the name of the file is xxx.txt if the name of the model is xxx
		String fileDir="";
		java.net.URL  fileURL;
		try {
			fileURL = DEVSBasedANN.class.getResource("files/readme.txt");
			File file = new File(fileURL.getPath());
			fileDir = file.getParent(); 
			String fileName = name+".txt";
			File newFile = new File (fileDir,fileName);
			trajectory = new PrintStream(newFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String[] nm = name.split("_");
		modelID = nm[1];
		
		dir = fileDir;
		maxId = maxID;
		
		if(doTraining==true){
			fileURL = DEVSBasedANN.class.getResource("files/INITWEIGHTS.txt");
		}
		else{
			fileURL = DEVSBasedANN.class.getResource("files/TRAINEDWEIGHTS.txt");
		}
		initializeWeights(new File(fileURL.getPath()));
	}

	public double ta() {
		// ta(active) = 0
		// ta(passive) = infinity
		if (state == 1) return 0;
		else return DEVS_Real.POSITIVE_INFINITY;
	}
	
	public void deltaInt() {
		// active -> passive
		if (state == 1) state = 0;
	}

	public void lambda() throws DEVS_Exception {
		if(pat<no_of_patterns){
			if(counter==0 || (counter>0 && updateReceived==true)){
				transferFunction(pat, input.get(pat));
				for(int i=0; i<num_output; i++){
					setOutputPortData(this.getName()+".HOUT"+i, output[pat][i]);
					setOutputPortData(this.getName()+".HWOUT", patternWeights(pat)+":"+patternInput(pat)+":"+patternOutput(pat));
				}
				pat++;
				updateReceived=false;
			}
		}
		if(pat==no_of_patterns){
			pat=0;
			counter++;
		}
	}

	public void deltaExt(double e) throws DEVS_Exception {
		// Let's get the value received and the simulation time it has been received
		//System.out.print("\nce "+this.getName());
		
		String rec = getInputPortData(this.getName()+".HIN0").toString();	
		String received2 = getInputPortData(this.getName()+".UPIN").toString();
		
		if(input.size()==no_of_patterns){
			input.clear();
		}
		
		if(!received2.isEmpty() && !recv.equals(received2)){
			double when = this.getSimulator().getTL();
			changetoArrays(received2.substring(0, received2.length()-1));
			if(maxId == Integer.parseInt(modelID)){
				out.println("&");
			}
			out.println("#"+modelID+"@"+" : "+received2);
			updateReceived=true;
			recv=received2;
		}
		else if(!rec.isEmpty()){
			String[] received = new String[num_input];
			for(int i=0; i<num_input; i++){
				received[i]= getInputPortData(this.getName()+".HIN"+i).toString();		
			}
			cnt++;
			
			double when = this.getSimulator().getTL();
			
			if(cnt==num_input){
				float[] patternInput = new float[num_input+1];
				for(int i=0; i<num_input; i++){
					patternInput[i] = Float.parseFloat(received[i]);
					//trajectory.println(when + " : " + received[i]);
				}
				cnt=0;
				input.add(patternInput);
				state=1;
			}	
			
		}

	}
	
	public void changetoArrays(String longVal){
		String[] patWeight = longVal.split("#");
		for(int i=0; i<weights.length; i++){
			String[] weig = patWeight[i].split(",");
			for(int j=0; j< weights[0].length; j++){
				weights[i][j]=Float.parseFloat(weig[j]);
			}
		}
	}
	
	// weight initialisation
	/*public void initializeWeights(){
		Random r = new Random();
		for(int i=0; i<weights.length; i++){
			for(int j=0; j<weights[0].length; j++){
				weights[i][j] =r.nextFloat()*2-1.0f;
			}
		}
	}*/
	
	public float[][] initializeWeights(File file) {
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
				infos = line.split(":");
				if(infos[0].contains(modelID)){
					changetoArrays(infos[1]);
				}
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
	
	// Feed-Forward
	public float[] transferFunction(int pat, float[] inp){
		System.arraycopy(inp, 0, input.get(pat), 0, inp.length);
		input.get(pat)[input.get(0).length-1]=1;
		for(int j=0; j<output[pat].length;j++){
			weightedSum[pat][j]=0;
			for(int i=0;i<input.get(pat).length;i++){
				weightedSum[pat][j]+= weights[i][j] * input.get(pat)[i];
			}
			// select activation function
			output[pat][j] = activation(weightedSum[pat][j]);
		}
		return output[pat];
	}
	
	//activation function
	// 0 for binary sigmoid, 1 for bipolar sigmoid and 2 for hyperbolic tangent
	public float activation(float val){
		if(actType.equals("Binary Sigmoid")) return (float) (1 / (1 + Math.exp(-val)));
		else if(actType.equals("Bipolar Sigmoid")) return (float) ( (2/(1 + Math.exp(-val) ) )-1 );
		else if(actType.equals("Hyperbolic Tangent")) return (float) Math.tanh(val);	
		else return (float) Math.exp(-val*val);
	}
	
	
	public String patternWeights(int pat){
		String text="";
		for(int i=0; i<weights.length; i++){
			for(int j=0; j<weights[0].length; j++){
				text+=weights[i][j]+",";
			}
			text+="#";
		}
		return text;
	}
	
	public String patternOutput(int pat){
		String text="";
		for(int i=0; i<output[pat].length; i++){
			text+=weightedSum[pat][i]+"~"+output[pat][i]+",";
		}
		return text;
	}
	
	public String patternInput(int pat){
		String text="";
		for(int i=0; i<input.get(0).length; i++){
			text+=input.get(pat)[i]+",";
		}
		return text;
	}
	
	
	
}