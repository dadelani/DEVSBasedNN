import exception.DEVS_Exception;
import model.*;
import types.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ErrorGenerator extends AtomicModel {
	// Definition of S :
	// there is only one single possible state which lasts for ever until a trigger is received 
	// a state variable is used for the current repository (a file)
	PrintStream trajectory;
	int no_output, no_pattern;
	int cnt, pat,nt,p,epoch;
	String actType;
	int N; // total number of patterns
	ArrayList<String> target;
	float[][] errorList;
	float[][] desiredOutput;
	float[][] calOutput;
	int state;
	float minimumError;
	boolean targetGotten=false, backpropagate=true;
	
	public ErrorGenerator(String name, String desc, int no_out, int no_pat, String actT, float minErr, boolean doTraining) {
		super(name, desc);
		nt=0;
		cnt = 0;
		N=0;
		pat=0;
		epoch=0;
		no_output = no_out;
		no_pattern = no_pat;
		actType = actT;
		minimumError = minErr;
		
		backpropagate = doTraining;
		
		// Definition of the input port (for X):
		// the name of the port is xxx.store if the name of the model is xxx
		for(int i=0; i<no_output; i++){
			addInputPortStructure(new DEVS_String(""), this.getName()+".EIN"+i, "training output");
		}
		
		addInputPortStructure(new DEVS_String(""), this.getName()+".ETIN", "received target output");
		
		addOutputPortStructure(new DEVS_String(""), this.getName()+".EOUT", "error list pattern sent out");
		
		// State initialization: the name of the file is xxx.txt if the name of the model is xxx
		target = new ArrayList<String>();
		errorList = new float[no_pattern][no_output];
		calOutput = new float[no_pattern][no_output];
		desiredOutput = new float[no_pattern][no_output];
		
		state = 0;
		
		try {
			java.net.URL fileURL = DEVSBasedANN.class.getResource("files/readme.txt");
			File file = new File(fileURL.getPath());
			String fileDir = file.getParent(); 
			String fileName = name+".txt";
			File newFile = new File (fileDir,fileName);
			trajectory = new PrintStream(newFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public double ta() {
		if (state == 1) return 0;
		else return DEVS_Real.POSITIVE_INFINITY;
	}
	
	public void deltaInt() {
		if (state == 1){
			state = 0;
		}
	}

	public void lambda() throws DEVS_Exception {
		if(backpropagate==true){
			if(state==1){
				setOutputPortData(this.getName()+".EOUT", getErrorList(pat-1)+nt);
				nt++;
				if(nt==9) nt=0;
			}
			if(pat==no_pattern) {
				pat=0;
				epoch++;
				if(meanSquareError()<minimumError){
					backpropagate=false;
				}
			}
		}
		if(pat==no_pattern) {
			pat=0;
		}
	}

	public void deltaExt(double e) throws DEVS_Exception {
		// Let's get the value received and the simulation time it has been received
		String[] received = new String[no_output];
		
		if(N==no_pattern){
			targetGotten=true;
			N=0;
		}
		
		double when = this.getSimulator().getTL();
		
		String received2 = getInputPortData(this.getName()+".ETIN").toString();
	
		if (!received2.isEmpty() && !targetGotten){
			target.add(N,received2);
			changetoFloatArray(N);
			N++;
			//trajectory.println(when + " : " + received2);
		}
		
		String rec = getInputPortData(this.getName()+".EIN0").toString();
		if(!rec.isEmpty()){
			for(int i=0; i<no_output; i++){
				received[i] = getInputPortData(this.getName()+".EIN"+i).toString();
			}
			cnt++;
			
			if(cnt==no_output){
				for(int i=0; i<no_output; i++){
					calOutput[pat][i] = Float.parseFloat(received[i]);
					//trajectory.println(when + " : " +pat+"------> "+ sp[1]);
				}
				if(pat==no_pattern-1){
					trajectory.println(epoch+"@"+meanSquareError());
					printOutputToFile();
					//trajectory.println("\n");
				}
				cnt=0;
				pat++;
				state = 1;
			}
		}
				
	}
	
	//use printwriter to write output to file
	public void printOutputToFile(){
		String txt;
		for(int i=0; i<calOutput.length; i++){
			txt="Pattern "+(i+1)+" : ";
			for(int j=0; j<calOutput[0].length; j++){
				txt +=calOutput[i][j]+" , ";
			}
			trajectory.println(txt);
		}
	}
	
	// converts concatenated String into float array of desired output
	public void changetoFloatArray(int patNo){
		String[] desOut = target.get(patNo).split(",");
		for(int i=0; i<desOut.length; i++){
			desiredOutput[patNo][i] = Float.parseFloat(desOut[i]);
		}
	}
	
	public String getErrorList(int patNo){
		String concError ="";
		for(int i=0; i<no_output; i++){
			errorList[patNo][i] = desiredOutput[patNo][i]-calOutput[patNo][i];
			concError+=errorList[patNo][i]+",";
		}
		return concError;
	}
	
	public float meanSquareError(){
		float mse =0.0f;
		for(int pat=0; pat<calOutput.length; pat++){
			for (int i = 0; i < desiredOutput[pat].length; i++) {
				mse += Math.pow(desiredOutput[pat][i] - calOutput[pat][i], 2);
			}
		}
        return mse / 2;
	}
}