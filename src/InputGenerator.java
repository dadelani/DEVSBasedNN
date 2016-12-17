import model.AtomicModel;
import exception.DEVS_Exception;
import types.*;
import java.util.*;

public class InputGenerator extends AtomicModel {
	// Definition of S : no state variable since there is only one single possible state
	
	// Additional attribute necessary for random number generation
	int cnt,t;
	float[][] input;
	
	public InputGenerator(String name, String desc, float[][] trainInput) {
		super(name, desc);
		
		// Definition of the output port (for Y):
		// the name of the port is xxx.out if the name of the model is xxx
		// the value sent out is a pattern: Array of attribute values of a Pattern
		addOutputPortStructure(new DEVS_String(""), this.getName()+".POUT", "My pattern output port");
		addOutputPortStructure(new DEVS_Integer(t), this.getName()+".TIME", "My pattern output port");

		// No input port : X = {}
		
		// No state initialization: the state will always be the same implicit one
		
		// Initialization of the additional attribute
		cnt=0;
		t=0;
		input = trainInput;
		//input = new float[][]{new float[]{0, 0}, new float[]{0, 1}, new float[]{1, 0}, new float[]{1, 1}};
		//input = new float[][]{new float[]{0.4301266f, 0.4888733f, 0.308413f, 0.80125f, 0.485115f}, new float[]{0.0802533f, 0.09396f, 0.012233f, 0.701267f, 0.098404f}, new float[]{0.04794f, 0.0460733f,0.002493f,0.306189f,0.040798f }};
	}

	public void deltaInt() {
		// Nothing to say: we always return to the same implicit state
	}

	public double ta() {
		return 0.5;
	}

	public void lambda() throws DEVS_Exception {
		// The value sent is a pattern in form of Array exported to text
		if(cnt<input.length){
			setOutputPortData(this.getName()+".TIME", t);
			setOutputPortData(this.getName()+".POUT", patternAttributes(input[cnt]));
			cnt++;
		}
		if(cnt==input.length){
			t=1;
			setOutputPortData(this.getName()+".TIME", t);
			//setOutputPortData(this.getName()+".POUT", "SENT");
			cnt++;
		}
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