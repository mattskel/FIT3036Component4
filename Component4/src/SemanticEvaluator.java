/*
 * Calculates a score for a semantic relation
 * Evaluates how strongly the spoken relationship matches the physical relationship
 * Considers topological and projective relations
 */

public class SemanticEvaluator {
	
	Concept object;
	String relation;
	Concept landmark;
	Concept speaker;

	public SemanticEvaluator() {}
	
	public void SetSpeaker(Concept speakerIn) { speaker = speakerIn; }	// Set the speaker
	
	// Location_on(Concept, Concept)
	public float Location_on(Concept object, Concept landmark) {
		if (object.z() == landmark.z() + landmark.h()) {
			return A_xy(object,landmark) / (float) Math.min(A_xy(object),A_xy(landmark));
		} else {
			return 0.0f;
		}
	}
	
	public float Location_inthecenterof(Concept object, Concept landmark) {
//		System.out.println(Location_on(object,landmark));
//		System.out.println(Location_near(object.GetCentre(), landmark.GetCentre()));
		return Location_on(object, landmark) * Location_near(object.GetCentre(), landmark.GetCentre());
	}
	
	// Location_near(Concept, Concept)
	// Need to check for "on-ness" and "in-ness"
	// If an object is on a landmark then typically we don't use near
	// We can assume "near-ness" equates to "off-ness"
	public float Location_near(Concept object, Concept landmark, int flag) {
		int a = 1, b = 1, t = 1;
//		System.out.println("object = " + object.GetSurfaceArea());
//		System.out.println("landmark = " + landmark.GetSurfaceArea());
		float surfaceArea;
		float dist;
		switch (flag) {
		case 0:
			surfaceArea = (float) Math.max(object.GetSurfaceArea(),landmark.GetSurfaceArea());
			dist = Distance(object.GetCentre(), landmark.GetCentre());
			break;
		case 1:
			// Divide the surface area by 4 to represent the 4 corners
//			surfaceArea = (float) Math.max(object.GetSurfaceArea(),landmark.GetSurfaceArea()) / 4;
			// Assume the surface area is 1 cm^2
			surfaceArea = 1.0f;
			dist = (float) Math.min(Math.min(Distance(object.GetCentre(),landmark.Cnr_lnt()),
											Distance(object.GetCentre(),landmark.Cnr_lft())),
									Math.min(Distance(object.GetCentre(),landmark.Cnr_rft()),
											Distance(object.GetCentre(),landmark.Cnr_rnt())));
			break;
		default: 
			surfaceArea = (float) Math.max(object.GetSurfaceArea(),landmark.GetSurfaceArea());
			dist = Distance(object.GetCentre(), landmark.GetCentre());
			break;
		}
//		dist = Distance(object.GetCentre(), landmark.GetCentre());
//		System.out.println("dist = " + dist);
//		System.out.println("near " + Math.pow(2.0, -t * Math.pow(dist, a) / Math.pow(surfaceArea, b)));
		return (float) Math.pow(2.0, -t * Math.pow(dist, a) / Math.pow(surfaceArea, b));
	}
	
	// Location_inthecornerof_on
	// Check for "on-ness"
	public float Location_inthecornerof_on(Concept object, Concept landmark) {
//		System.out.println("location_on = " + Location_on(object,landmark));
//		System.out.println("location_near = " + Location_near(object,landmark,1));
		return Location_on(object,landmark) * Location_near(object,landmark,1);
	}
	
	// Location_near(float[], float[])
	public float Location_near(float[] p1, float[] p2) {
		int a = 1, t = 1;
		float dist = Distance(p1, p2);
		return (float) Math.pow(2.0, -t * Math.pow(dist, a));
	}
	
	public float Location_infrontof(Concept object, Concept landmark, int flag) {
		float perspectiveAlignment;
		// Check if the landmark has a face or not
		if (landmark.GetFace()) {
//			System.out.println("!HERE");
			perspectiveAlignment = LandmarkPerspective(object,landmark, flag, 0);
		} else {
			perspectiveAlignment = SpeakerPerspective(object, landmark, flag, 0);
		}
		if (flag == 0) {
			// off
			return perspectiveAlignment * Location_near(object, landmark, 0) * (1 - Location_on(object, landmark));
		} else {
			// on
			return perspectiveAlignment * Location_near(object, landmark, 0) * (Location_on(object, landmark));
		}
	}
	
	public float Location_inbackof(Concept object, Concept landmark, int flag) {
		float perspectiveAlignment;
		// Check if the landmark has a face or not
		if (landmark.GetFace()) {
			perspectiveAlignment = LandmarkPerspective(object,landmark, flag, 2);
		} else {
			perspectiveAlignment = SpeakerPerspective(object, landmark, flag, 2);
//			System.out.println(perspectiveAlignment);
		}
		if (flag == 0) {
			// off
			return perspectiveAlignment * Location_near(object, landmark, 0) * (1 - Location_on(object, landmark));
		} else {
			// on
			return perspectiveAlignment * Location_near(object, landmark, 0) * (Location_on(object, landmark));
		}
	}
	
	public float Location_totherightof(Concept object, Concept landmark, int flag) {
		float perspectiveAlignment;
		// Check if the landmark has a face or not
		if (landmark.GetFace()) {
			perspectiveAlignment = LandmarkPerspective(object,landmark, flag, 1);
		} else {
			perspectiveAlignment = SpeakerPerspective(object, landmark, flag, 1);
		}
		if (flag == 0) {
			// off
			return perspectiveAlignment * Location_near(object, landmark, 0) * (1 - Location_on(object, landmark));
		} else {
			// on
			return perspectiveAlignment * Location_near(object, landmark, 0) * (Location_on(object, landmark));
		}
	}
	
	public float Location_totheleftof(Concept object, Concept landmark, int flag) {
		float perspectiveAlignment;
		// Check if the landmark has a face or not
		if (landmark.GetFace()) {
			perspectiveAlignment = LandmarkPerspective(object,landmark, flag, 3);
		} else {
			perspectiveAlignment = SpeakerPerspective(object, landmark, flag, 3);
		}
		if (flag == 0) {
			// off
			return perspectiveAlignment * Location_near(object, landmark, 0) * (1 - Location_on(object, landmark));
		} else {
			// on
			return perspectiveAlignment * Location_near(object, landmark, 0) * (Location_on(object, landmark));
		}
	}
	
	/*
	 * LandmarkPerspective
	 * The landmark is considered to have a face and projection is from the landmarks point of view
	 * Evaluates the dot product of the landmark vector and the landmark to object vector
	 * The landmark vector is the direction the speaker uses to distinguish the object location
	 * "the left of" implies a vector rotated 90 degrees clockwise of the initial forward facing vector
	 * The score will depend on the alignment of these vectors
	 */
	public float LandmarkPerspective(Concept object, Concept landmark, int flag, int rot) {
		
		// First get the rotation vector of the landmark on the xy plane
		// Rotate the vector {0, 1} clockwise using the flag
		// rot: 0 = 0 degrees, 1 = 90 degrees, 2 = 180 degrees, 3 = 270 degrees
		// x' = xcos(a) - ysin(a), y' = ycos(a) + xsin(a)
		double theta = (double)landmark.GetAngle() + rot * Math.PI / 2;
		float x = /*(float)Math.cos(theta)*/ - (float)Math.sin(theta);
		float y = (float)Math.cos(theta) /*+ (float)Math.sin(theta)*/;
		float[] perspectiveVector = {x, y};
//		System.out.println("perspectiveVector = {" + perspectiveVector[0] + ", " + perspectiveVector[1] + "}");
				
		// Need to determine what part of the landmark we are measuring from
		// If we are talking about "on" use the centre of the landmark
		// If we are talking about "off" use the centre edge of the landmark
		float[] absoluteCentre = landmark.GetCentre();
		float[] edgeCentre = new float[2];
		if (rot % 2 == 0) {
			edgeCentre[0] = landmark.GetCentre()[0] + landmark.d()/2 * perspectiveVector[0];
			edgeCentre[1] = landmark.GetCentre()[1] + landmark.d()/2 * perspectiveVector[1];
		} else {
			edgeCentre[0] = landmark.GetCentre()[0] + landmark.w()/2 * perspectiveVector[0];
			edgeCentre[1] = landmark.GetCentre()[1] + landmark.w()/2 * perspectiveVector[1];
		}
		
		// Create a unit vector from the landmark to the object
		float[] objectCentre = object.GetCentre();
		float[] edgeToObject = {objectCentre[0] - edgeCentre[0],
								objectCentre[1] - edgeCentre[1]};
		float[] centreToObject = {objectCentre[0] - absoluteCentre[0],
									objectCentre[1] - absoluteCentre[1]};
		// Normalise the vector
		float edgeVectorLength = (float)Math.sqrt(Math.pow((double) edgeToObject[0], 2)
											+ Math.pow((double) edgeToObject[1],2));
		float[] normalEdgeToObject = {edgeToObject[0]/edgeVectorLength,
										edgeToObject[1]/edgeVectorLength};
		
		float centreVectorLength = (float)Math.sqrt(Math.pow((double) centreToObject[0], 2)
													+ Math.pow((double) centreToObject[1],2));
		float[] normalCentreToObject = {centreToObject[0]/centreVectorLength,
										centreToObject[1]/centreVectorLength};
		
		// Calculate the dot product
		float dotEdge = perspectiveVector[0] * normalEdgeToObject[0]
						+ perspectiveVector[1] * normalEdgeToObject[1];
		float dotCentre = perspectiveVector[0] * normalCentreToObject[0]
						+ perspectiveVector[1] * normalCentreToObject[1];
		
		
		// We only want to deal with positives
		// Negative values become 0
		/*if (flag == 0 && dotEdge <=0) {
			return 0;
		} else if (flag == 1 && dotCentre <= 0) {
			return 0;
		} else {
			return dotCentre;
		}*/
		
		return (flag == 0 && dotEdge <= 0 || flag == 1 && dotCentre <= 0) ? 0 : dotCentre;
	}
	
	/* SpeakerPerspective
	 * The landmark is faceless and the alignment is from the viewers point of view
	 * Evaluates the dot product of the landmark to viewer and landmark to object
	 * The score will depend on the alignment of these vectors
	 * We will assume the viewer is the speaker
	 */
	public float SpeakerPerspective(Concept object, Concept landmark, int flag, int rot) {
		// First need a vector from the landmark to the speaker
		// This vector will be rotated depending on the preposition used by the speaker
		float[] landmarkCentre = landmark.GetCentre();
//		System.out.println("landmarkCentre = {" + landmarkCentre[0] + ", " + landmarkCentre[1] + "}");
		float[] speakerCentre = speaker.GetCentre();
//		System.out.println("speakerCentre = {" + speakerCentre[0] + ", " + speakerCentre[1] + "}");
		float[] landmarkToSpeaker = {speakerCentre[0] - landmarkCentre[0],
									speakerCentre[1] - landmarkCentre[1]};
//		System.out.println("landmarkToSpeaker = {" + landmarkToSpeaker[0] + ", " + landmarkToSpeaker[1] + "}");
		/*float[] speakerToLandmark = {landmarkCentre[0] - speakerCentre[0],
									landmarkCentre[1] - speakerCentre[1]};*/
		
		// Normalise the vector
		/*float speakerVectorLength = (float)Math.sqrt(Math.pow((double) speakerToLandmark[0],2)
											+ Math.pow((double) speakerToLandmark[1],2));
		float[] normalSpeakerToLandmark = {speakerToLandmark[0]/speakerVectorLength,
											speakerToLandmark[1]/speakerVectorLength};*/
		float landmarkVectorLength = (float)Math.sqrt(Math.pow((double) landmarkToSpeaker[0],2)
													+ Math.pow((double) landmarkToSpeaker[1],2));
		float[] normalLandmarkToSpeaker = {landmarkToSpeaker[0]/landmarkVectorLength,
											landmarkToSpeaker[1]/landmarkVectorLength};
		
//		System.out.println("normalLandmarkToSpeaker = {" + normalLandmarkToSpeaker[0] + ", " + normalLandmarkToSpeaker[1] + "}");
		
		// Get the forward facing vector of the landmark
		// Although we have previously determined this landmark does not have a face it can have a rotation
		// This will affect the width and depth from the speakers perspective
		// From this we can determine the edge positions
		double theta = (double)landmark.GetAngle() /*+ Math.PI / 2*/;
		float x = /*(float)Math.cos(theta)*/ - (float)Math.sin(theta);
		float y = (float)Math.cos(theta) /*+ (float)Math.sin(theta)*/;
		float[] landmarkHeadingVector = {x, y};
//		System.out.println("landmarkHeadingVector = {" + x + ", " + y + "}");
		
		// Get the speakers rotation perspective
		// Points from the landmark to the object in the direction specified by the speakers perspective
		// rot 0 = front, 1 = right, 2 = back, 3 = left
		// We make the angle negative so it is consistent with the flags in the LandmarkPerspective
		theta = (double)(-rot * Math.PI / 2);
		x = normalLandmarkToSpeaker[0]; y = normalLandmarkToSpeaker[1];
		float xRot = x * (float)Math.cos(theta) - y * (float)Math.sin(theta);
		float yRot = y * (float)Math.cos(theta) + x * (float)Math.sin(theta);
		float[] speakerPerspectiveVector = {xRot, yRot};
//		System.out.println("speakerPerspectiveVector = {" + xRot + ", " + yRot + "}");
		
		// The rotation of the landmark will alter the direction of width and depth
		// The correct width and depth are required to find the edge of the landmark
		// If the absolute dot product of the landmarkToSpeaker and heading are above 0.5 then width and depth are true
		// Otherwise width and depth need to be flipped
		float depth;
		float width;
		/*float dotHeadingPerspective = landmarkHeadingVector[0] * speakerPerspectiveVector[0]
										+ landmarkHeadingVector[1] * speakerPerspectiveVector[1];*/
		float dotHeadingPerspective = landmarkHeadingVector[0] * normalLandmarkToSpeaker[0]
									+ landmarkHeadingVector[1] * normalLandmarkToSpeaker[1];
//		System.out.println("dotHeadingPerspective = " + dotHeadingPerspective);
		if (Math.abs((double)dotHeadingPerspective) >= 0.5 && Math.abs((double)dotHeadingPerspective) <= 1.0) {
			depth = landmark.d;
			width = landmark.w;
		} else {
			depth = landmark.w;
			width = landmark.d;
		}
		
		// Calculate the location of the edge of the landmark
		float[] absoluteCentre = landmark.GetCentre();
		float[] edgeCentre = new float[2];
		if (rot % 2 == 0) {
			edgeCentre[0] = landmark.GetCentre()[0] + depth/2 * speakerPerspectiveVector[0];
			edgeCentre[1] = landmark.GetCentre()[1] + depth/2 * speakerPerspectiveVector[1];
		} else {
			edgeCentre[0] = landmark.GetCentre()[0] + width/2 * speakerPerspectiveVector[0];
			edgeCentre[1] = landmark.GetCentre()[1] + width/2 * speakerPerspectiveVector[1];
		}
		
//		System.out.println("absoluteCentre = {" + absoluteCentre[0] + ", " + absoluteCentre[1] + "}");
//		System.out.println("edgeCentre = {" + edgeCentre[0] + ", " + edgeCentre[1] + "}");
		
		// The position coordinate is on the correct edge
		// Calculate the vector form the two coordinates to the object
		float[] objectCentre = object.GetCentre();
		float[] edgeToObject = {objectCentre[0] - edgeCentre[0],
								objectCentre[1] - edgeCentre[1]};
		float[] centreToObject = {objectCentre[0] - absoluteCentre[0],
									objectCentre[1] - absoluteCentre[1]};
		
		
		// Normalise the vectors
		float edgeVectorLength = (float)Math.sqrt(Math.pow((double) edgeToObject[0], 2)
												+ Math.pow((double) edgeToObject[1],2));
		float[] normalEdgeToObject = {edgeToObject[0]/edgeVectorLength,
										edgeToObject[1]/edgeVectorLength};

		float centreVectorLength = (float)Math.sqrt(Math.pow((double) centreToObject[0], 2)
													+ Math.pow((double) centreToObject[1],2));
		float[] normalCentreToObject = {centreToObject[0]/centreVectorLength,
										centreToObject[1]/centreVectorLength};
		
		// Calculate the dot product
		float dotEdge = speakerPerspectiveVector[0] * normalEdgeToObject[0]
						+ speakerPerspectiveVector[1] * normalEdgeToObject[1];
		float dotCentre = speakerPerspectiveVector[0] * normalCentreToObject[0]
						+ speakerPerspectiveVector[1] * normalCentreToObject[1];
		
//		System.out.println("dotEdge = " + dotEdge);
//		System.out.println("dotCentre = " + dotCentre);
		
		// As before we are only interested if the dot product is greater than 1
		// If the dot product is greater than one we always use the dot product from the centre
		// The dot product to the centre is more forgiving for objects close to the landmark
		return (flag == 0 && dotEdge <= 0 || flag == 1 && dotCentre <= 0) ? 0 : dotCentre;
	}
	
	/* Distance
	 * Calculates the distance between two points in xyz space
	 */
	public float Distance(float[] p1, float[] p2) {
		float dist = (float) Math.sqrt(Math.pow(p2[0] - p1[0], 2)
									+ Math.pow(p2[1] - p1[1], 2)
									+ Math.pow(p2[2] - p1[2], 2));
		return dist;
	}
	
	/* DistanceXY
	 * Calculates the distance between two points in xy plane
	 */
	public float DistanceXY(float[] p1, float[] p2) {
		float dist = (float) Math.sqrt(Math.pow(p2[0] - p1[0], 2)
				+ Math.pow(p2[1] - p1[1], 2));
		return dist;
	}
	
	
	public float A_xy(Concept concept) {return (concept.w() * concept.d());}	// Area of a concept
	
	/* A_xy
	 * Overlapping area between object and landmark in the xy plane
	 */
	public float A_xy(Concept object, Concept landmark) {
		
		double rotationObject = object.GetAngle() * 180.0/(Math.PI);
		double rotationLandmark = landmark.GetAngle() * 180.0/(Math.PI);
    	int adjustedRotationObj = (int) (rotationObject) % 180 + 45;
    	int adjustedRotationLndmrk = (int) (rotationLandmark) % 180 + 45;
    	
    	float xObject = (adjustedRotationObj < 90 && adjustedRotationObj > -90) ? object.w() : object.d();
    	float yObject = (adjustedRotationObj < 90 && adjustedRotationObj > -90) ? object.d() : object.w();
    	float xLandmark = (adjustedRotationLndmrk < 90 && adjustedRotationLndmrk > -90) ? landmark.w() : landmark.d();
    	float yLandmark = (adjustedRotationLndmrk < 90 && adjustedRotationLndmrk > -90) ? landmark.d() : landmark.w();
		
		float xDist = (float) Math.min(object.x() + xObject, landmark.x() + xLandmark)
							- Math.max(object.x(), landmark.x());
		float yDist = (float) Math.min(object.y() + yObject, landmark.y() + yLandmark)
							- Math.max(object.y(), landmark.y());
		return xDist * yDist;
	}
	
	
}
