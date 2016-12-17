import model.AtomicModel;
import exception.DEVS_Exception;
import types.*;
import java.util.*;

public class TargetGenerator extends AtomicModel {
	// Definition of S : no state variable since there is only one single possible state
	
	// Additional attribute necessary for random number generation
	int cnt;
	float[][] target;
	
	public TargetGenerator(String name, String desc, float[][] trainOutput) {
		super(name, desc);
		
		// Definition of the output port (for Y):
		// the name of the port is xxx.out if the name of the model is xxx
		// the value sent out is a pattern: Array of attribute values of a Pattern
		addOutputPortStructure(new DEVS_String(""), this.getName()+".TOUT", "My pattern output port");

		// No input port : X = {}
		
		// No state initialization: the state will always be the same implicit one
		
		// Initialization of the additional attribute
		cnt=0;
		target = trainOutput;
		//target = new float[][]{new float[]{0}, new float[]{1}, new float[]{1}, new float[]{0}};
		//target = new float[][]{new float[]{0,0}, new float[]{1,0.5f}, new float[]{1,0.5f}, new float[]{0,1}};
		//target = new float[][]{new float[]{1,0,0}, new float[]{0,1,0}, new float[]{0,0,1}};
	}

	public void deltaInt() {
		// Nothing to say: we always return to the same implicit state
	}

	public double ta() {
		return 0.5;
	}

	public void lambda() throws DEVS_Exception {
		// The value sent is a pattern in form of Array exported to text
		if(cnt<target.length){
			setOutputPortData(this.getName()+".TOUT", patternAttributes(target[cnt]));
			cnt++;
		}
		/*if(cnt==target.length){
			setOutputPortData(this.getName()+".TOUT", "SENT");
			cnt++;
		}*/
	}

	public void deltaExt(double e) throws DEVS_Exception {
		// Not defined, since there is no input
	}
	
	public String patternAttributes(float[] input){
		String text="";
		for(int i=0; i<input.length; i++){
			text+=String.valueOf(input[i])+",";
		}
		return text;
	}
}