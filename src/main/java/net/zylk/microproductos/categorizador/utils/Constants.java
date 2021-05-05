package net.zylk.microproductos.categorizador.utils;

public class Constants {

	public static final String MODEL_SAVESTATE_FILENAME = "model-saved-state";
	public static final String MODEL_SAVESTATE_FILEPATH = "./";

	public static final String MODEL_TRAINING_INPUTFILE = "./mallet-text-file.txt";
	public static final Integer MODEL_NUMBEROFTOPICS_DEFAULT = 20;
	public static final Double MODEL_ALPHASUM_DEFAULT = 1.0;
	public static final Double MODEL_BETA_DEFAULT = 0.01;

	public static final Integer RETURN_WORDS_PER_TOPIC = 5;

	// Binaries used by the LemmatizerPipe:

	// Spanish
	public static String modelBinEs = "/es-pos-perceptron-autodict01-ancora-2.0.bin";
	public static String lemmaBinEs = "/es-lemma-perceptron-ancora-2.0.bin";
	// English
	public static String modelBinEn = "/en-xpos-perceptron-autodict01-ud.bin";
	public static String lemmaBinEn = "/en-xlemma-perceptron-ud.bin";
	// Dutch
	public static String modelBinNl = "/nl-pos-perceptron-autodict01-alpino.bin";
	public static String lemmaBinNl = "/nl-lemma-perceptron-alpino.bin";
	// German
	public static String modelBinDe = "/de-pos-perceptron-autodict01-conll09.bin";
	public static String lemmaBinDe = "/de-lemma-perceptron-conll09.bin";
	// Basque
	public static String modelBinEu = "/eu-pos-perceptron-ud.bin";
	public static String lemmaBinEu = "/eu-lemma-perceptron-ud.bin";
	// French
	public static String modelBinFr = "/fr-pos-perceptron-autodict01-sequoia.bin";
	public static String lemmaBinFr = "/fr-lemma-perceptron-sequoia.bin";
	// Galician
	public static String modelBinGl = "/gl-pos-perceptron-autdict05-ctag.bin";
	public static String lemmaBinGl = "/gl-lemma-perceptron-autodict05-ctag.bin";
	// Italian
	public static String modelBinIt = "/it-pos-perceptron-autodict01-ud.bin";
	public static String lemmaBinIt = "/it-lemma-perceptron-ud.bin";

	// Path where the Lemma models are saved

	public static String pathToLemmaModels = "/Lemma_models/";


}
