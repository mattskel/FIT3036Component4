/*
 * A class object for storing information about Concepts from an image KB
 * Each Concept has a unique identifiers as well as physical attributes
 * Concept has methods to return additional physical attributes such as corners
 * Additional features hollow and face are added to better interpret Concepts
 */
public class Concept {
	
	
	private String id;			// A unique identification string for the concept
    private String name;		// The common name give to the concept object
    private String colour;
    private float[] colour_coord;	// RGB values for the colour of the object
    private float[] size_wdh;
    private float[] location_xyz;	// Location of the corner closest to the origin
    private float angle;
    float x,y,z;
    float w,d,h;
    boolean hollow;	// The concept is considered to be hollow
    boolean face;	// The concept is considered to have a face
    

    public Concept(){
		id = "";
		name = "";
		colour = "";
		colour_coord = new float[] {0,0,0};
		size_wdh = new float[] {0,0,0};
		location_xyz = new float[] {0,0,0};
		angle = 0;
		hollow = false;
		face = false;
	}

    /* Mutators */
    
	public void setID(String newId) {id = newId;}
	public void setName(String newName) {name = newName;}
	public void setColour(String newColour) {colour = newColour;}
	public void setColourCoord(float[] coords) {colour_coord = coords;}
	public void setSize(float[] wdh) {
		size_wdh = wdh;
		w = size_wdh[0];
    	d = size_wdh[1];
    	h = size_wdh[2];
	}
	public void setLocation(float[] xyz) {
		location_xyz = xyz;
		x = location_xyz[0];
		y = location_xyz[1];
    	z = location_xyz[2];
	}
	public void setAngle(float newAngle) {angle = newAngle;}
	public void setHollow(boolean hollowIn) {hollow = hollowIn;}
	public void setFace(boolean faceIn) {face = faceIn;}
	
	/* Accessors */
    
    public String GetName() {return name;}
    public String GetID() {return id;}
    
    public float x() {return location_xyz[0];}
    public float y() {return location_xyz[1];}
    public float z() {return location_xyz[2];}
    public float w() {return size_wdh[0];}
    public float d() {return size_wdh[1];}
    public float h() {return size_wdh[2];}
    
    public float GetAngle() {return angle;}
    
    public float GetSurfaceArea() {return (2 * w * d) + (2 * w * h) + (2 * d * h);}
    public float GetA_xy() {return (this.w() * this.d());}
    public float GetV_xyz() {return (this.w() * this.d() * this.h());}
    
    public boolean GetHollow() { return hollow; }
    public boolean GetFace() { return face; }
    
    /* GetCentre
     * Returns the centre coordinates of a Concept
     * The centre has to adjust for the rotation of the object
     * Assumes all rotations are right angles to the image xy axis
     */
    public float[] GetCentre() {
    	
    	float[] centreCoords = new float[3];
    	
    	double rotationAngle = angle * 180.0/(Math.PI);
    	int adjustedRotation = (int) (rotationAngle) % 180 + 45;
    	float modX = (adjustedRotation < 90 && adjustedRotation > -90) ? w : d;
    	float modY = (adjustedRotation < 90 && adjustedRotation > -90) ? d : w;
    	
    	centreCoords[0] = location_xyz[0] + modX/2.0f;
    	centreCoords[1] = location_xyz[1] + modY/2.0f;
    	centreCoords[2] = location_xyz[2] + h/2.0f;
    	
    	return centreCoords;
    }
    
    
    
    /* Cnr_
     * Returns the coordinates for all corners of the concept bounding box
     * Assumes all rotations are right angles to the image xy axis
     * t = top, b = bottom, r = right, l = left, n = near, f = far
     */
    
    public float[] Cnr_lnt() { 
    	
    	double rotationAngle = angle * 180.0/(Math.PI);
    	int adjustedRotation = (int) (rotationAngle) % 180 + 45;
    	float mod = (adjustedRotation < 90 && adjustedRotation > -90) ? d : w;
    	return new float[] {x, y + mod, z + h}; 
    }
    
    public float[] Cnr_lft() { return new float[] {x, y, z + h}; }
    
    public float[] Cnr_rft() { 
    	
    	double rotationAngle = angle * 180.0/(Math.PI);
    	int adjustedRotation = (int) (rotationAngle) % 180 + 45;
    	float mod = (adjustedRotation < 90 && adjustedRotation > -90) ? w : d;
    	return new float[] {x + mod , y, z + h}; 
    }
    public float[] Cnr_rnt() { 
    	
    	double rotationAngle = angle * 180.0/(Math.PI);
    	int adjustedRotation = (int) (rotationAngle) % 180 + 45;
    	float modX = (adjustedRotation < 90 && adjustedRotation > -90) ? w : d;
    	float modY = (adjustedRotation < 90 && adjustedRotation > -90) ? d : w;
    	return new float[] {x + modX, y + modY, z + h}; 
    }
    
    public float[] Cnr_lnb() { 
    	
    	double rotationAngle = angle * 180.0/(Math.PI);
    	int adjustedRotation = (int) (rotationAngle) % 180 + 45;
    	float mod = (adjustedRotation < 90 && adjustedRotation > -90) ? d : w;
    	return new float[] {x, y + mod, z}; 
    }
    
    public float[] Cnr_lfb() { return new float[] {x, y, z}; }
    
    public float[] Cnr_rfb() { 
    	
    	double rotationAngle = angle * 180.0/(Math.PI);
    	int adjustedRotation = (int) (rotationAngle) % 180 + 45;
    	float mod = (adjustedRotation < 90 && adjustedRotation > -90) ? w : d;
    	return new float[] {x + mod , y, z}; 
    }
    
    public float[] Cnr_rnb() { 
    	
    	double rotationAngle = angle * 180.0/(Math.PI);
    	int adjustedRotation = (int) (rotationAngle) % 180 + 45;
    	float modX = (adjustedRotation < 90 && adjustedRotation > -90) ? w : d;
    	float modY = (adjustedRotation < 90 && adjustedRotation > -90) ? d : w;
    	return new float[] {x + modX, y + modY, z};
    }
    
}
