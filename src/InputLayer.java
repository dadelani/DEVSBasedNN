import java.util.ArrayList;
import java.util.Arrays;

import exception.DEVS_Exception;
import model.*;
import types.*;

public class InputLayer extends AtomicModel {
	// Definition of S: a state variable is used for the current stae

	int state;
	// 0 for passive
	// 1 for active
	int N; // total Number of patterns
	ArrayList<String> patterns;
	float dt;
	int p, num_input, cnt;
	
	public InputLayer(String name, String desc, int no_input) {
		super(name, desc);
		
		// Definition of the output port (for Y):
		// the name of the port is xxx.color if the name of the model is xxx
		for(int i=0; i<no_input; i++){
			addOutputPortStructure(new DEVS_String(""), this.getName()+".IOUT"+i, "pattern sent out");
		}
		
		// Definition of the input port (for X):
		// the name of the port is xxx.pin if the name of the model is xxx
		addInputPortStructure(new DEVS_String(""), this.getName()+".IIN", "Pattern received");
		addInputPortStructure(new DEVS_Integer(state), this.getName()+".TIMEIN", "Pattern received");

		
		// State initialization: the light starts Green
		state = 0;
		p=0;
		N=0;
		num_input = no_input;
		cnt=0;
		patterns = new ArrayList<String>();
	}

	public void deltaInt() {
		// The behavior is the following:
		// passive -> active
		if (state == 1) state = 1;
	}

	public double ta() {
		// ta(active) = 1/N
		if (state == 1) return dt;
		else return DEVS_Real.POSITIVE_INFINITY;
	}

	public void lambda() throws DEVS_Exception {
		// L(Active) = pattern attribute
		if (state==1 && p<N) {
			String[] sentPattern = patterns.get(p).split(",");
			for(int i=0; i<num_input; i++){
				setOutputPortData(this.getName()+".IOUT"+i, sentPattern[i]);
			}
			p++;
			if(p==N) p=0;
		}
	}

	public void deltaExt(double e) throws DEVS_Exception {
		// Let's get the value received
		int rec = (Integer) getInputPortData(this.getName()+".TIMEIN");
		String received = getInputPortData(this.getName()+".IIN").toString();
		cnt++;
		
		if(cnt==2 && rec==0){
			patterns.add(N,received);
			N++;
			cnt=0;
		}
		else if (cnt==1 && rec==1){
			dt=1/(float)N;
			state=1;
			cnt=0;
		}
		else{
			System.out.println(rec+" >> "+received+" >>"+this.getSimulator().getTL());
		}
	}
}