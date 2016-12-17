import model.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class DEVSANN extends CoupledModel {
	int no_of_neurons;

	public DEVSANN(String name, String desc,String apName, float[][] trainInput, float[][] trainOutput, int[] layerNeurons, int num_patterns, String algorithm, String actfunction, float mse, boolean doTraining) {
		super(name, desc);
		// Definition of {Md}
		java.net.URL  fileURL;
		PrintStream trajectory = null;
		File newFile=null;
		try {
			fileURL = DEVSBasedANN.class.getResource("files/readme.txt");
			File file = new File(fileURL.getPath());
			String fileDir = file.getParent(); 
			String fileName = "UPDATEDWEIGHTS.txt";
			newFile = new File (fileDir,fileName);
			trajectory = new PrintStream(newFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		CalculationLayer[] Mcal = new CalculationLayer[layerNeurons.length];
		DeltaWeight[] Mdw = new DeltaWeight[layerNeurons.length];
		InputGenerator Mpgen = new InputGenerator("INPGEN","Generator of patterns",trainInput);
		InputLayer Minp = new InputLayer("INPUT","Input layer model", layerNeurons[0]);
		boolean isFirstDW=false, isLastDW=false;
		for(int i=0; i<layerNeurons.length-1; i++){
			isFirstDW = (i==0?true:false);
			isLastDW = (i==layerNeurons.length-2?true:false);
			Mcal[i] = new CalculationLayer("CALMODEL_"+i,"hidden or output layer", layerNeurons[i], layerNeurons[i+1], num_patterns, actfunction, i, trajectory, layerNeurons.length-2, doTraining);
			Mdw[i] = new DeltaWeight("DWEIGHT"+i,"error calculation", layerNeurons[i], layerNeurons[i+1], num_patterns, actfunction, algorithm, isFirstDW, isLastDW);
		}
		ErrorGenerator Merr = new ErrorGenerator("ERROR"+apName,"Repository of results", layerNeurons[layerNeurons.length-1], num_patterns, actfunction, mse, doTraining);
		TargetGenerator Mtar = new TargetGenerator("TARGEN","Repository of results",trainOutput);
		
		// Definition of D
		addSubModel(Mpgen);
		addSubModel(Minp);
		for(int i=0; i<layerNeurons.length-1; i++){
			addSubModel(Mcal[i]);
		}
		addSubModel(Merr);
		addSubModel(Mtar);
		for(int i=0; i<layerNeurons.length-1; i++){
			addSubModel(Mdw[i]);
		}
		
		// There is no EIC, since the global model is input-free

		// Definition of IC:
		// SOURCE(OUT)-->(COMMAND)TARGET(COLOR)-->(STORE)SINK
		addIC(Mpgen.getOutputPortStructure("INPGEN.POUT"), Minp.getInputPortStructure("INPUT.IIN")) ;
		addIC(Mpgen.getOutputPortStructure("INPGEN.TIME"), Minp.getInputPortStructure("INPUT.TIMEIN")) ;
		
		for(int i=0; i<layerNeurons[0];i++){
			addIC(Minp.getOutputPortStructure("INPUT.IOUT"+i), Mcal[0].getInputPortStructure("CALMODEL_0.HIN"+i)) ;
		}
		
		if(layerNeurons.length>1){
			for(int i=1; i<layerNeurons.length-1;i++){
				for(int j=0; j<layerNeurons[i]; j++){
					addIC(Mcal[i-1].getOutputPortStructure("CALMODEL_"+(i-1)+".HOUT"+j), Mcal[i].getInputPortStructure("CALMODEL_"+i+".HIN"+j));
				}
			}
			
			//addIC(Mhid.getOutputPortStructure("HIDDEN.HWOUT"), Mout.getInputPortStructure("OUTPUT.HWIN")) ;
			int lastIndex=layerNeurons.length-1;
			for(int i=0; i<layerNeurons[lastIndex];i++){
				addIC(Mcal[lastIndex-1].getOutputPortStructure("CALMODEL_"+(lastIndex-1)+".HOUT"+i), Merr.getInputPortStructure("ERROR"+apName+".EIN"+i));
			}
		}
		else{
			for(int i=0; i<layerNeurons[0];i++){
				addIC(Mcal[0].getOutputPortStructure("CALMODEL_0.HOUT"+i), Merr.getInputPortStructure("ERROR"+apName+".EIN"+i));
			}
		}
		
		addIC(Mtar.getOutputPortStructure("TARGEN.TOUT"), Merr.getInputPortStructure("ERROR"+apName+".ETIN")) ;
		
		for(int i=0; i<layerNeurons.length-1;i++){
			addIC(Mcal[i].getOutputPortStructure("CALMODEL_"+i+".HWOUT"), Mdw[i].getInputPortStructure("DWEIGHT"+i+".HWIN"));
		}
		
		int lastIdx=layerNeurons.length-2;
		addIC(Merr.getOutputPortStructure("ERROR"+apName+".EOUT"), Mdw[lastIdx].getInputPortStructure("DWEIGHT"+(lastIdx)+".DWIN")) ;
		
		if(layerNeurons.length>1){
			for(int i=lastIdx; i>0; i--){
				addIC(Mdw[i].getOutputPortStructure("DWEIGHT"+i+".DEOUT"), Mdw[i-1].getInputPortStructure("DWEIGHT"+(i-1)+".DEIN"));
			}
		}
		
		for(int i=layerNeurons.length-2; i>=0;i--){
			addIC(Mdw[i].getOutputPortStructure("DWEIGHT"+i+".UPOUT"), Mcal[i].getInputPortStructure("CALMODEL_"+i+".UPIN"));
		}
				
		// There is no EOC, since the global model is output-free
	}
	
	public Model select(ArrayList<Model> possibleModels) {
		// The traffic light has the higher priority
		// Otherwise a kind of random choice is done
		for ( Model m : possibleModels ) {
			if ( m instanceof InputLayer ) return m ;
		}
		return possibleModels.get(0);
	}
}
