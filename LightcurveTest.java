import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LightcurveTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
//		File testFile = new File("testCurve.dat");
//		Lightcurve l = new Lightcurve(testFile);
		
//		System.out.println(l.getMaxFlux());
		
//		Lightcurve normalTest = l.normalizeFlux(0, 1);
//		System.out.println(normalTest.toString());
//		
//		Lightcurve subTest = l.subsection(1.0480054E8, 1.0489717E8);
//		Lightcurve normalSubTest = subTest.normalizeFlux();
//		System.out.println(subTest.toString() + "\n\n");
//		System.out.println(subTest.normalizeFlux(0, 1).toString() + "\n\n");
//		System.out.println(subTest.toString());
//		
//		double max1 = l.getMaxFlux(1.0488028E8, 1.0488699E8);
//		double max2 = normalSubTest.getMaxFlux(1.0488028E8, 1.0488699E8);
//		double max3 = l.getMaxFlux();
//		double max4 = normalSubTest.getMaxFlux();
//		System.out.println(max1 + " " + max2 + " " + max3 + " " + max4);
		
//		Lightcurve smooth1 = l.boxcarSmooth(120);
//		Lightcurve smooth5 = normalTest.boxcarSmooth(120);
//		Lightcurve smooth2 = l.boxcarSmooth(20);
//		Lightcurve smooth3 = subTest.boxcarSmooth(15);
//		Lightcurve smooth4 = normalSubTest.boxcarSmooth(15);
		
//		smooth1.toFile("smooth1.dat");
//		l.toFile("l.dat");
//		smooth5.toFile("smooth5.dat");
//		normalTest.toFile("normalTest.dat");
		
//		String workingDir = System.getProperty("user.dir");
//		System.out.println("Current working directory : " + workingDir);
		
		Lightcurve keplerDAV = new Lightcurve(new File("../20150617/Q11-Q17lc_nooutliers.dat"));
		System.out.println("Lightcurve Read");
		File eventTime = new File("../20150617/Q11-Q17eventproperties.dat");
		System.out.println("Event Times Read");
		
		// REMEMBER TO CHANGE OUTPUT FILE NAMES!
		AstroTools.meanEventProfile(eventTime, keplerDAV, false);
		
		System.out.println("Done.");
		
		
	}
}
