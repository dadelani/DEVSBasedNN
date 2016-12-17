import java.awt.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;


public class ModelDiagram extends JPanel{
	
	int[] layer_neurons;
	public ModelDiagram(int[] neu){
		super();
		layer_neurons=neu;
		this.setPreferredSize(new Dimension(1000,250));
		panScrollPane();
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		this.setBackground(Color.WHITE);
		// Target Generator
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect (10, 15, 80, 30);
		g.setColor(Color.BLACK);
		g.drawRect (10, 15, 80, 30);
		g.drawString("TargetGEN", 13, 33);
		
		// Input Generator
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect (10, 80, 80, 30);
		g.setColor(Color.BLACK);
		g.drawRect (10, 80, 80, 30);
		g.drawString("InputGEN", 13, 100);
		g.setColor(Color.BLUE);
		g.drawLine(90, 87, 120, 87);
		g.fillOval(88, 85, 4, 4);
		g.fillOval(88, 101, 4, 4);
		g.drawLine(90, 103, 120, 103);
		
		// Input Layer
		g.setColor(Color.PINK);
		g.fillRect (120, 60, 60, 80);
		g.setColor(Color.BLACK);
		g.drawRect (120, 60, 60, 80);
		g.drawString("Input", 135, 100);
		g.fillOval(118, 85, 4, 4);
		g.fillOval(118, 101, 4, 4);
		// inp_dist is to distance between each output neuron
		int inp_dist = (int)70/layer_neurons[0];
		int dist;
		// draw lines to depict neurons
		for(int i=1; i<=layer_neurons[0]; i++){
			g.setColor(Color.BLUE);
			dist = 60+(i*inp_dist);
			g.drawLine(180, dist, 210, dist);
			g.fillOval(178, dist-2, 4, 4);
			g.fillOval(206, dist-2, 4, 4);
		}
		
		// hidden layers drawing starts from x=210, y =60
		int x=210;
		int incrX = 90; // distance from one end of a layer to the other end of the next layer.
		int tLine = 0;  // tLine is the last position of x. x=x+incrX for each iteration
		
		
		// Hidden & Output Layer
		for(int i=1; i<layer_neurons.length; i++){
			// the code below draws an hidden layer(rectangle) in pink color and the output neurons (lines)
			g.setColor(Color.PINK);
			g.fillRect (x, 60, 60, 80);
			g.setColor(Color.BLACK);
			g.drawRect(x, 60, 60, 80);
			// the delta weight is also a rectangle in CYAN color with vertical distance of 35 from the hidden layer.
			g.setColor(Color.CYAN);
			g.fillRect (x, 165, 60, 40);
			g.setColor(Color.BLACK);
			g.drawRect(x, 165, 60, 40);
			// draw lines to depict neurons
			inp_dist = (int)70/layer_neurons[i];
			for(int j=1; j<=layer_neurons[i]; j++){
				g.setColor(Color.BLUE);
				dist = 60+(j*inp_dist);
				g.drawLine(x+60, dist, x+90, dist);
				g.fillOval(x+58, dist-2, 4, 4);
				g.fillOval(x+86, dist-2, 4, 4);
			}
			
			g.setColor(Color.BLACK);
			g.drawString("DWeight"+i, x+3, 190);
			// draw lines to represent port sending updated weights from delta weight to hidden layer.
			g.drawLine(x-8, 135, x-8, 175);
			g.drawLine(x-8, 135, x, 135);
			g.drawLine(x-8, 175, x, 175);
			g.fillOval(x-2, 133, 4, 4);
			g.fillOval(x-2, 173, 4, 4);
			
			// if it is the last layer (output layer), then draw the Error Generator and its ports
			if(i==layer_neurons.length-1){
				// draw lines to represent port sending weights from output layer to its delta weight
				g.setColor(Color.BLUE);
				g.drawLine(x+68, 135, x+68, 175);
				g.drawLine(x+60, 135, x+68, 135);
				g.drawLine(x+60, 175, x+68, 175);
				g.fillOval(x+58, 133, 4, 4);
				g.fillOval(x+58, 173, 4, 4);
				// draw lines to represent port sending errors from Error Generator to the delta weight of the output layer
				g.setColor(Color.BLACK);
				g.drawLine(x+60, 195, x+180, 195);
				g.drawLine(x+180, 75, x+180, 195);
				g.fillOval(x+58, 193, 4, 4);
				
				g.drawString("Output", x+12, 100);
				
				x+=incrX;
				tLine = x;
				// draws the rectangle for Error Generator
				g.setColor(Color.CYAN);
				g.fillRect (x, 15, 70, 125);
				g.setColor(Color.BLACK);
				g.drawRect (x, 15, 70, 125);
				g.drawString("ErrorGEN", x+10, 80);
				
				g.setColor(Color.BLACK);
				g.drawLine(x+70, 75, x+90, 75);
				g.fillOval(x+68, 73, 4, 4);
			}
			else{
				// draw lines to represent port sending errors from one delta weight to the other
				g.drawString("Hidden"+i, x+8, 100);
				g.setColor(Color.BLACK);
				g.drawLine(x+60, 195, x+90, 195);
				g.fillOval(x-2+60, 193, 4, 4);
				g.fillOval(x+85, 193, 4, 4);
				
				// draw lines to represent port sending weights from hidden layer to its delta weight
				g.setColor(Color.BLUE);
				g.drawLine(x+68, 135, x+68, 175);
				g.drawLine(x+60, 135, x+68, 135);
				g.drawLine(x+60, 175, x+68, 175);
				g.fillOval(x+58, 133, 4, 4);
				g.fillOval(x+58, 173, 4, 4);
			}
			
			x+=incrX;
		}
		// draw lines from target Generator to Error Generator
		g.setColor(Color.BLUE);
		g.drawLine(90, 25, tLine, 25);
		g.fillOval(88, 23, 4, 4);
		g.fillOval(tLine-2, 23, 4, 4);
	}
	
	public JScrollPane panScrollPane(){
		JScrollPane viewScroller = new JScrollPane(this);
		viewScroller.setPreferredSize(new Dimension(550, 270));
		viewScroller.setBorder(new TitledBorder(new LineBorder(Color.BLUE),"DEVS Model View"));
		return viewScroller;
	}
	
	/*public static void main(String args[]){
		int[] neurons = {5,2,4,3};
		ModelDiagram  m = new ModelDiagram(neurons);
		JFrame f = new JFrame("Model Diagram");
		//f.setSize(1000, 250);
		f.add(m.panScrollPane());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		f.pack();
		
	}*/
}
