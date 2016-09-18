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
    

    public Concept(){
		id = "";
		name = "";
		colour = "";
		colour_coord = new float[] {0,0,0};
		size_wdh = new float[] {0,0,0};
		location_xyz = new float[] {0,0,0};
		angle = 0;
	}

	public void setID(String newId) {id = newId;}
	public void setName(String newName) {name = newName;}
	public void setColour(String newColour) {colour = newColour;}
	public void setColourCoord(float[] coords) {colour_coord = coords;}
	public void setSize(float[] wdh) {size_wdh = wdh;}
	public void setLocation(float[] xyz) {location_xyz = xyz;}
	public void setAngle(float newAngle) {angle = newAngle;}
    
    public String GetName() {
    	return name;
    }
    
    public String GetID() {
    	return id;
    }

}
