/*
 * The structure for storing information about a concept from the KB
 * Each image will have a list of Concepts
 * Each concept will hold information about concepts in the image
 */
public class Concept {
	
	
	private String id;			// A unique identification string for the concept
    private String name;		// The common name give to the concept object
    private String colour;
    private float[] colour_coord;
    private float[] size_wdh;
    private float[] location_xyz;
    private float angle;
    

    public Concept() {
        
        colour_coord = new float[3];
        colour_coord = new float[] {0.0f,0.0f,0.0f};
    }
    
    public String GetName() {
    	return name;
    }

}
