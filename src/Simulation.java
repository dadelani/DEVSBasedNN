import simulator.RootCoordinator;
import exception.DEVS_Exception;


public class Simulation extends Thread{
	DEVSANN study;
	float simulationTime;
	public Simulation(String apName, float[][] trainInput, float[][] trainOutput, float minError, String trainAlg, String actFunc, int[] layersNeurons, int no_patterns, float simTime, boolean doTraining){
		DEVSANN study = new DEVSANN("DANN","DEVS/ANN mapping", apName, trainInput, trainOutput, layersNeurons,no_patterns,trainAlg, actFunc, minError, doTraining);
		this.study=study;
		simulationTime = simTime;
		/*// Creation of the simulation tree
		RootCoordinator root = new RootCoordinator(study.getSimulator());
		// Experimentation:
		// initial time is 0.0
		// final time is 1000.0
		try {
			root.init(0.0);
			root.run(simTime);
		} catch (DEVS_Exception e) {
			e.printStackTrace();
		}	*/
	}
	
	
	public void run(){
		// Creation of the simulation tree
		RootCoordinator root = new RootCoordinator(study.getSimulator());
		// Experimentation:
		// initial time is 0.0
		// final time is 1000.0
		try {
			root.init(0.0);
			root.run(simulationTime);
		} catch (DEVS_Exception e) {
			e.printStackTrace();
		}	
	}
	/*public static void main(String[] args) {
		// Creation of the study
		float minimumError = 0.00001f;
		String trainingAlgorithm = "DELTABAR";
		// 0 binary sigmoid
		// 1 bipolar sigmoid
		// 2 hyperbolic tangent
		// 3 gaussian function
		int activationFunction = 3;
		int no_inputs=5;
		int no_output = 3;
		int no_patterns = 3;
		
		int no_hid_layers=2;
		int[] layersNeurons = new int[no_hid_layers+2];
		layersNeurons[0]=no_inputs;
		layersNeurons[1]=4;
		layersNeurons[2]=3;
		layersNeurons[3]=no_output;
		DEVSANN study = new DEVSANN("DANN","DEVS/ANN mapping",layersNeurons,no_patterns,trainingAlgorithm, activationFunction, minimumError);
		
		// Creation of the simulation tree
		RootCoordinator root = new RootCoordinator(study.getSimulator());
		
		// Experimentation:
		// initial time is 0.0
		// final time is 1000.0
		try {
			root.init(0.0);
			root.run(2000.0);
		} catch (DEVS_Exception e) {
			e.printStackTrace();
		}	
	}*/
}
