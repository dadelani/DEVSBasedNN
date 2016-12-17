import exception.DEVS_Exception;
import model.*;
import types.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class DeltaWeight extends AtomicModel {
	// Definition of S :
	// there is only one single possible state which lasts for ever until a trigger is received 
	// a state variable is used for the current repository (a file)
	PrintStream trajectory;
	int no_input, no_output, no_pattern;
	int cnt, nt;
	int p; // total number of patterns
	ArrayList<String> target;
	float[][] weights;
	float[][] input, newError;
	float[][] output, WS;
	ArrayList<float[]> error;
	String actType;
	
	float[][] delta;
	float[][] prevDelta;
	
	float[][] gradient;
	float[][] prevGradient;
	float[][] prevChange;
	float[][] learnRateList;
	float[][] barDeltaList;
	boolean isCurrent=false;
	float decreaseFactor=0.5f;
	float increaseFactor=1.2f;
	float maxDelta=50;
	float minDelta=1e-6f;
	float barDeltaBaseConstant=0.7f;
	final float ZERO_TOLERANCE = 1e-27f;
	float weightDecay = -0.0001f;
	float learningRate = 0.3f;
	float momentum = 0.7f;
	float u,d;
	int state;
	boolean isRec=false, isFirstDW=false, isLastDW=false;
	String rec="", recv="", learningAlgorithm;
	
	public DeltaWeight(String name, String desc, int no_inp, int no_out, int no_pat, String actT, String algorithm, boolean isFirst, boolean isLast) {
		super(name, desc);
		nt=0;
		cnt = 0;
		p=0;
		no_input = no_inp;
		no_output = no_out;
		no_pattern = no_pat;
		actType = actT;
		learningAlgorithm=algorithm;
		isFirstDW = isFirst;
		isLastDW = isLast;
		
		// Definition of the input port (for X):
		// the name of the port is xxx.store if the name of the model is xxx
		addInputPortStructure(new DEVS_String(""), this.getName()+".HWIN", "output weights");
		
		if(isLast)
			addInputPortStructure(new DEVS_String(""), this.getName()+".DWIN", "received error List");
		else
			addInputPortStructure(new DEVS_String(""), this.getName()+".DEIN", "received error from another DeltaWeight");
		addOutputPortStructure(new DEVS_String(""), this.getName()+".DEOUT", "sends out error List to another DeltaWeight");
		addOutputPortStructure(new DEVS_String(""), this.getName()+".UPOUT", "sends out new weights to layers");
		addOutputPortStructure(new DEVS_String(""), this.getName()+".FOUT", "sends out new weights to layers");
		
		// State initialization: the name of the file is xxx.txt if the name of the model is xxx
		target = new ArrayList<String>();
		weights = new float[no_input+1][no_output];
		input = new float[no_pattern][no_input+1];
		output = new float[no_pattern][no_output]; // to remove bias values
		WS = new float[no_pattern][no_output];
		newError = new float[no_pattern][no_input];
		error = new ArrayList<float[]>();
		
		
		delta = new float[no_input+1][no_output];
		prevDelta = new float[no_input+1][no_output];
		prevChange = new float[no_input+1][no_output];
		gradient = new float[no_input+1][no_output];
		prevGradient = new float[no_input+1][no_output];
		learnRateList = new float[no_input+1][no_output];
		barDeltaList = new float[no_input+1][no_output];
		fillChangeArray();
		
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
		state=0;
	}
	
	public double ta() {
		if(state==1){
			return 0;
		}
		else{
			return DEVS_Real.POSITIVE_INFINITY;
		}
	}
	
	public void deltaInt() {
		if(state==1){
			state=0;
		}
	}

	public void lambda() throws DEVS_Exception {
		if(p>0){
			if(p==1) resetGradient();
			if(!isFirstDW){
				setOutputPortData(this.getName()+".DEOUT", patternError(updateWeights(p-1))+nt);
				setOutputPortData(this.getName()+".UPOUT", patternWeights(p-1)+nt);
				nt++;
				if(nt==9) nt=0;
			}
			else{
				updateWeights(p-1);
				setOutputPortData(this.getName()+".UPOUT", patternWeights(p-1)+nt);
				nt++;
				if(nt==9) nt=0;
			}
		}
		
	}

	public void deltaExt(double e) throws DEVS_Exception {
		// Let's get the value received and the simulation time it has been received
		//System.out.print("\nde "+this.getName());
		if(p==0 || p==no_pattern){
			error.clear();
			p=0;
		}
		
		double when = this.getSimulator().getTL();
				
		if(isLastDW){
			String received = getInputPortData(this.getName()+".DWIN").toString();		
			//System.out.print(" "+received+"\n");
			if (!received.isEmpty() && !rec.equals(received)){
				trajectory.println(when + " : " + received);
				error.add(errorValues(received.substring(0, received.length()-1)));
				rec=received;
				p++;
				state=1;
			}
		}
		else if(!isLastDW){
			String received1 = getInputPortData(this.getName()+".DEIN").toString();
			//System.out.print(" "+received1+"\n");
			if (!received1.isEmpty() && !rec.equals(received1)){
				//trajectory.println(when + " : " + received1.substring(0, received1.length()-1));
				error.add(errorValues(received1.substring(0, received1.length()-1)));
				rec=received1;
				p++;
				state=1;
			}
		}
		
		String received2 = getInputPortData(this.getName()+".HWIN").toString();
		if(!received2.isEmpty() && !recv.equals(received2)){
			//System.out.print(" "+received2+"\n");
			changetoArrays(p, received2);
			//trajectory.println(when + " : " + received2);
			recv=received2;
		}	
				
	}
	
	
	public void fillChangeArray(){
		for(int i=0; i<prevChange.length; i++){
			for(int j=0; j<prevChange[0].length; j++){
				prevChange[i][j] = 0.1f;
				learnRateList[i][j]=0.05f;
				barDeltaList[i][j]=0;
				prevGradient[i][j] = 0;
				prevDelta[i][j]=0;
			}	
		}		
	}
	
	public void resetGradient(){
		for(int i=0; i<gradient.length; i++){
			for(int j=0; j<gradient[0].length; j++){
				gradient[i][j]=0;
			}	
		}
	}
	
	public void changetoArrays(int pat, String longVal){
		String[] weightVsOutput = longVal.split(":");
		String[] patWeight = weightVsOutput[0].split("#");
		String[] patInput = weightVsOutput[1].split(",");
		String[] patOutput = weightVsOutput[2].split(",");
		for(int i=0; i<weights.length; i++){
			String[] weig = patWeight[i].split(",");
			//System.out.println(this.getName()+ "  "+pat+" "+Arrays.toString(weig));
			for(int j=0; j< weights[i].length; j++){
				weights[i][j]=Float.parseFloat(weig[j]);
			}
		}
		for(int i=0; i<patInput.length; i++){
			input[pat][i]=Float.parseFloat(patInput[i]);
		}
		
		for(int i=0; i<patOutput.length; i++){
			String[] prev = patOutput[i].split("~");
			//System.out.println(no_output+" "+prev.length +" "+i);
			WS[pat][i] = Float.parseFloat(prev[0]);
			output[pat][i] =Float.parseFloat(prev[1]);
		}	
	}
	
	public float[] errorValues(String recValue){
		String[] errV = recValue.split(",");
		float[] errVal = new float[errV.length];
		
		for(int i=0; i<errV.length;i++){
			errVal[i] = Float.parseFloat(errV[i]);
		}
		return errVal;
	}
	
	public String patternError(float[] errVals){
		String text="";
		for(int i=0; i<errVals.length; i++){
			text+=errVals[i]+",";
		}
		return text;
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
	
	
	public float[] updateWeights(int pat){		
		float grad = 0;
		for(int i=0; i<output[pat].length; i++){
			for(int j=0; j<input[pat].length; j++){
				grad = error.get(pat)[i]* derivativeActivation(WS[pat][i], output[pat][i])*input[pat][j];
				gradient[j][i] +=grad;
			}
		}
		float nerror;
		if(!isFirstDW){
			for(int i=0; i<no_input;i++){
				nerror = 0;
				for(int j=0; j<output[pat].length; j++){
					nerror += error.get(pat)[j]* derivativeActivation(WS[pat][j], output[pat][j]) * weights[i][j];
				}
				newError[pat][i] = nerror;
			}
		}
		
		float newDelta;
		if(pat==no_pattern-1){
			for(int i=0; i<input[pat].length; i++){
				for(int j=0; j<output[pat].length; j++){
					if(learningAlgorithm.equals("Standard BP")){
						weights[i][j] += learningRate*gradient[i][j];
					}
					else if(learningAlgorithm.equals("BP with Momentum")){
						weights[i][j] += learningRate*gradient[i][j] + momentum * prevDelta[i][j];
						prevDelta[i][j] = gradient[i][j];
					}
					else if(learningAlgorithm.equals("Silva & Almeida")){
						u=1.1f;
						d=0.7f;
						if(actType.equals("Hyperbolic Tangent") || actType.equals("Gaussian")) momentum=0f;
						newDelta = sign(silvaAlmeida(i, j))* gradient[i][j] + momentum * prevDelta[i][j];
						weights[i][j] +=  newDelta;
						prevDelta[i][j] = newDelta;
					}
					else if(learningAlgorithm.equals("Delta-Bar")){
						decreaseFactor=0.01f;
						increaseFactor=0.01f;
						weights[i][j] +=  deltaBar(i,j);
					}
					else if(learningAlgorithm.equals("QuickProp")){
						learningRate = 0.0055f;
						weights[i][j] +=  quickprop(i,j);
					}
					else if(learningAlgorithm.equals("RPROP")){
						weights[i][j]+= resilientPropagation(i,j);
					}
				}
			}
		}
		
		return newError[pat];
		
		
		
		/*float[] derror = new float[error.get(0).length];
		for(int j=0; j<derror.length; j++){
			derror[j] += derivativeActivation(WS[pat][j], output[pat][j])* error.get(pat)[j];
		}
		
		for(int j=0; j<error.get(pat).length; j++){
			for(int i=0; i<input[pat].length; i++){
				float newDelta=0;
				if(learningAlgorithm.equals("Standard BP")){
					delta[i][j] = learningRate*derror[j]*input[pat][i];
					weights[i][j] = weights[i][j] + delta[i][j];
				}
				else if(learningAlgorithm.equals("BP with Momentum")){
					delta[i][j] = learningRate * derror[j]*input[pat][i]+ momentum * prevDelta[i][j];
					weights[i][j] = weights[i][j] + delta[i][j];
					prevDelta[i][j]=delta[i][j];
				}
				else if(learningAlgorithm.equals("RPROP")){
					gradient[i][j] = derror[j]*input[pat][i];
					weights[i][j]= weights[i][j]+resilientPropagation(i,j);
				}
				else if(learningAlgorithm.equals("Silva & Almeida")){
					increaseFactor=1.05f;
					decreaseFactor=0.5f;
					if(actType.equals("Hyperbolic Tangent")) 	momentum=0.0f;
					gradient[i][j] = derror[j]*input[pat][i];
					newDelta = sign(silvaAlmeida(i, j))* gradient[i][j]+ momentum * prevDelta[i][j];
					weights[i][j]= weights[i][j]+newDelta;
					prevDelta[i][j]=newDelta;
				}
				else if(learningAlgorithm.equals("Delta-Bar")){
					decreaseFactor=0.01f;
					increaseFactor=0.01f;
					gradient[i][j] = derror[j]*input[pat][i];
					weights[i][j]= weights[i][j]+deltaBar(i,j);
				}
				else if(learningAlgorithm.equals("QuickProp")){
					gradient[i][j] = derror[j]*input[pat][i];
					weights[i][j] = weights[i][j] +quickprop(i, j);
				}
			}	
			
		}
		
		if(!isFirstDW){
			for(int i=0; i<no_input;i++){
				for(int j=0; j<derror.length; j++){
					newError[pat][i] += weights[i][j]*derror[j];
				}
			}
		}		
		
		return newError[pat];*/
	}
	
	
	private int sign(final float value) {
        if (Math.abs(value) < ZERO_TOLERANCE) {
            return 0;
        } else if (value > 0) {
            return 1;
        } else {
            return -1;
        }
	}
	
	private float silvaAlmeida(int i, int j){
		float gradientSignChange = prevGradient[i][j]*gradient[i][j];
		float change;
		if(gradientSignChange >= 0 && prevChange[i][j]<=5){
			change = prevChange[i][j]*u;
		}
		else{
			change = prevChange[i][j]*d;
		}
		prevGradient[i][j] = gradient[i][j];
		prevChange[i][j] = change;
		return change;		
	}
	
	// Extracted from Joe Coyle Implementation of ANN http://liquidself.com/neural/
	private float deltaBar(int i, int j){

        // initialize values
        float previousLearningRate = learnRateList[i][j];
        float barDelta = barDeltaList[i][j];

        // set learning rate
        float deltaLearningRate;
        float product = barDelta * gradient[i][j];
        if (product > 0){
            deltaLearningRate = increaseFactor;
        }
        else if (product < 0){
            deltaLearningRate = -1.0f * decreaseFactor *
              previousLearningRate;
        }
        else{
            deltaLearningRate = 0.0f;
        }
        float newLearningRate = previousLearningRate + deltaLearningRate;
        
        learnRateList[i][j] = newLearningRate;

        // update bar delta
        barDeltaList[i][j] = ((1.0f - barDeltaBaseConstant) * gradient[i][j]) +
        (barDeltaBaseConstant * barDelta);

        // update weight
        // NOTE: newLearningRate * error needs to be added,
        //   not subtracted like the paper says
        float newDelta = newLearningRate * gradient[i][j]+momentum*prevDelta[i][j];
        prevDelta[i][j]= newDelta;
        
        return newDelta;
      
	}
	
	// Extracted from Joe Coyle Implementation of ANN http://liquidself.com/neural/
	private float quickprop(int i, int j){
		float maximumGrowthFactor_=1.75f;
		float modeSwitchThreshold_=0.0f;
		float weightChange = 0;
		float previousGradient = prevGradient[i][j];
		float previousWeightChange = prevDelta[i][j];
		float shrinkFactor = maximumGrowthFactor_ / (1 + maximumGrowthFactor_);

        if (previousWeightChange > modeSwitchThreshold_){
            if (gradient[i][j] > 0){
                weightChange += (learningRate * gradient[i][j]);
            }
 
            if (gradient[i][j] > (shrinkFactor * previousGradient)){
                weightChange += (maximumGrowthFactor_ * previousWeightChange);
            }
            else{
                // use the estimate of the minimum of the error curve,
                // which is assumed to be a parabola
                weightChange += ((gradient[i][j] / (previousGradient - gradient[i][j])) *
                  previousWeightChange);
            }
        }
        else if (previousWeightChange < (-1.0 * modeSwitchThreshold_)){
            if (gradient[i][j] < 0){
                weightChange += (learningRate * gradient[i][j]);
            }

            if (gradient[i][j] < (shrinkFactor * previousGradient)){
                weightChange += (maximumGrowthFactor_ * previousWeightChange);
            }
            else{
                // use the estimate of the minimum of the error curve,
                // which is assumed to be a parabola
                weightChange += ((gradient[i][j] / (previousGradient - gradient[i][j])) *
                  previousWeightChange);
            }
        }
        else{
            // use gradient descent with momentum
            weightChange += (learningRate * gradient[i][j]) + (momentum * previousWeightChange);
        }


		prevGradient[i][j] = gradient[i][j];
		prevDelta[i][j] = weightChange;
		return weightChange;
	}
	
	private float resilientPropagation(int i, int j){
		float gradientSignChange = sign(prevGradient[i][j]*gradient[i][j]);
		float delta = 0;
		if(gradientSignChange > 0){
			float change = Math.min((prevChange[i][j]*increaseFactor), maxDelta);
			delta = sign(gradient[i][j])*change;
			prevChange[i][j] = change;
			prevGradient[i][j] = gradient[i][j];
		}
		else if(gradientSignChange < 0){
			float change = Math.max((prevChange[i][j]*decreaseFactor), minDelta);
			prevChange[i][j] = change;
			delta = -prevDelta[i][j];
			prevGradient[i][j] = 0;
		}
		else if(gradientSignChange == 0){
			float change = prevChange[i][j];
			delta = sign(gradient[i][j])*change;
			prevGradient[i][j] = gradient[i][j];
		}
		prevDelta[i][j] = delta;
		return delta;	
	}
	
	public float derivativeActivation(float sum, float fsum){
		if(actType.equals("Binary Sigmoid")) return (float) (1-fsum)*fsum;
		else if(actType.equals("Bipolar Sigmoid")) return (float) (0.5*(1+fsum)*(1-fsum));
		else if(actType.equals("Hyperbolic Tangent")) return (float) (1.0 - Math.pow(fsum, 2));
		else return (float) (-2*sum*fsum);
	}
	
}