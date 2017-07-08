package analyzer;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlPullParserException;

import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
import soot.options.Options;

public class SootAnalyzer {

	public static String homeUsr = System.getProperty("user.home");
	public static String androidJarLocation = homeUsr + "/Android/Sdk/platforms";
	public static String apkFileLocation = "apks/toy-example.apk";

	public static String rootDir = System.getProperty("user.dir");
	public static String entryPointTarget = "AndroidCallbacks.txt";
	public static String entryPointTargetLocation = rootDir + entryPointTarget;

	public static String entryPointSource = "SourcesAndSinks.txt";
	public static String entryPointSourceLocation = "entrypoints/" + entryPointSource;

	public static void main(String[] args) {
		SetupApplication app = new SetupApplication(androidJarLocation, apkFileLocation);
		String path = ClassLoader.getSystemClassLoader().getResource(entryPointSourceLocation).getPath();
		
		try {
			FileUtils.copyFile(new File(path), new File(entryPointTargetLocation));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			app.calculateSourcesSinksEntrypoints(entryPointTarget);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		soot.G.reset();

		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_process_dir(Collections.singletonList(apkFileLocation));
		Options.v().set_android_jars(androidJarLocation);
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().setPhaseOption("cg.spark", "on");

		Scene.v().loadNecessaryClasses();

		SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();
		Options.v().set_main_class(entryPoint.getSignature());
		Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
		//System.out.println(entryPoint.getActiveBody());

		PackManager.v().runPacks();
		PackManager.v().writeOutput();

		//CallGraph appCallGraph = Scene.v().getCallGraph();
		//System.out.println(Scene.v().getCallGraph().size());
	}
}