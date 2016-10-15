import java.util.List;
import java.io.IOException;

public class ConceptTester {
	
	Concept greenBookcase;
	Concept brownBookcase;
	
	public ConceptTester() throws IOException {
		
		ConceptReader cr = new ConceptReader();
		List<Concept> concepts =  cr.GetFromImage("Assets/Image/image4.kb");
		
		for (Concept concept : concepts) {
			if (concept.GetID().equalsIgnoreCase("green_bookcase4")) {
				greenBookcase = concept;
			} else if (concept.GetID().equalsIgnoreCase("brown_bookcase7")) {
				brownBookcase = concept;
			}
		}
	}
	
	// To test that the corner function is working correctly
	public void CornerTesting() {
		System.out.println("Testing Cnr_... Method for Concept");
//		System.out.println(greenBookcase.Cnr_lfb());
		System.out.println(brownBookcase.Cnr_lnb()[1]);
		System.out.println(brownBookcase.Cnr_rnb()[0]);
		System.out.println(brownBookcase.Cnr_rnb()[1]);
		/*
		size_wdh = 0.819:0.441:1.499
		location_xyz = 5.121:5.013:0
		angle = 1.571
		*/
		
		
		
		
	}

}
