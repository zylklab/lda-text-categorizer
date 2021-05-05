package net.zylk.microproductos.categorizador;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import net.zylk.microproductos.categorizador.model.LemmatizerPipe;
import net.zylk.microproductos.categorizador.utils.Constants;
import net.zylk.microproductos.categorizador.utils.Utils;

public class TrainModel {

	private static final Logger _log = LoggerFactory.getLogger(TrainModel.class);

	private static ParallelTopicModel model;
	private static InstanceList modelInstances;

	public static void train(String inputFile, Integer numberOfTopics, Integer iterations, String pathOutput,
			String language) throws IOException {
		// Begin by importing documents from text to feature sequences

		// Files which contains the model binaries
		String lemmaBin = Utils.LemmaSelector(language);
		String modelBin = Utils.modelSelector(language);

		String pathOutputFinal = pathOutput;
		String pathJarModelsFinal = Constants.pathToLemmaModels;
		String stopwords = Utils.stopWordsSelector(language);

		createModels(pathJarModelsFinal + language, modelBin, pathOutputFinal + language, lemmaBin);

		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		_log.debug("Preparing pipes");
		// Pipes: LemmatizerPipe, tokenize, remove stopwords, map to features

		// Put everything in lowercase, lemmatize the words and catch the nouns and
		// adjetives
		pipeList.add(new LemmatizerPipe(pathOutputFinal, language, modelBin, lemmaBin));

		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		pipeList.add(
				new TokenSequenceRemoveStopwords(new File(stopwords), "UTF-8", false, false, false)); /** STOPWORDS */
		pipeList.add(new TokenSequence2FeatureSequence());

		InstanceList instances = new InstanceList(new SerialPipes(pipeList));

		_log.debug("Reading input texts");
		Reader fileReader = new InputStreamReader(new FileInputStream(new File(inputFile)), "UTF-8");
		instances
				.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1)); // data,
																															// label,

		// name
		// fields

		_log.debug("Creating model");
		// Create a model with default alpha and beta, they will be optimized
		model = new ParallelTopicModel(numberOfTopics);

		_log.debug("Adding instances");
		model.addInstances(instances);

		// Use two parallel samplers, which each look at one half the corpus and combine
		// statistics after every iteration.
		_log.debug("Parallel samplers");
		model.setNumThreads(2);

		// Run the model for 50 iterations and stop (this is for testing only, for real
		// applications, use 1000 to 2000 iterations)
		_log.debug("Number of iterations...");
		model.setNumIterations(iterations);
		model.estimate();

		modelInstances = instances;

		List<Double> llh = new ArrayList<Double>();
		llh.add(model.modelLogLikelihood());
		System.out.println("LogLikelihood    = " + llh);
	}

	// Method to create (or not) the binaries that needs the LemmatizerPipe
	public static void createModels(String pathJarModelsFinal, String modelBin, String pathOutput, String lemmaBin) {
		File fileModel = new File(pathOutput + modelBin);
		File fileLemmas = new File(pathOutput + lemmaBin);

		if (!fileModel.exists()) {
			try {
				InputStream inputModelStream = TrainModel.class.getResourceAsStream(pathJarModelsFinal + modelBin);
				FileUtils.copyInputStreamToFile(inputModelStream, fileModel);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (!fileLemmas.exists()) {
			try {
				InputStream inputLemmassStream = TrainModel.class.getResourceAsStream(pathJarModelsFinal + lemmaBin);
				FileUtils.copyInputStreamToFile(inputLemmassStream, fileLemmas);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void saveModel(Integer numTopics, Integer numIterations) throws Exception {

		isModelInitialized();

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH:mm");
		File directorio = new File(String.format("./models/LDA--topics-%s--iter-%s--llh-%s--%s", numTopics,
				numIterations, model.modelLogLikelihood(), dtf.format(now)));
		try {
			File models = new File("./models");
			if (!models.exists()) {
				models.mkdir();
			}
			directorio.mkdir();
			// printTopWords crea
			// printTopWords crea un .txt con las palabras más relevantes por topic
			Integer wordsPerTopicToPrint = Constants.RETURN_WORDS_PER_TOPIC;
			model.printTopWords(new File(directorio + "/topWords.txt"), wordsPerTopicToPrint, false);

			// Estos dos pasos son los necesarios para guardar correctamente el
			// entrenamiento y genera dos ficheros que se tendrán que cargar cuando se
			// quiera utilizar este model ya entrenado.
			model.write(new File(directorio + "/model.dat"));
			modelInstances.save(new File(directorio + "/instances.dat"));

		} catch (IOException e) {
			_log.error(String.format("Error saving model state on %s", directorio));
			throw e;
		}
	}

	public static void getBestModel(String modelsDirectory) throws Exception {
		// List all models
		File directoryPath = new File(modelsDirectory);
		FilenameFilter modelFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith("model.dat")) {
					return true;
				} else {
					return false;
				}
			}
		};
		String models[] = directoryPath.list(modelFilter);
		System.out.println(directoryPath);
		System.out.println(directoryPath.list());
		// iterate over all models
		List<Double> llh = new ArrayList<Double>();
		List<Integer> nt = new ArrayList<Integer>();
		for (String modelFile : models) {
			model = ParallelTopicModel.read(new File(modelFile));
			llh.add(model.modelLogLikelihood());
			nt.add(model.getNumTopics());
		}

		System.out.println(llh);
		System.out.println(nt);
	}

	private static void isModelInitialized() throws Exception {
		if ((model == null) || (modelInstances == null)) {
			_log.warn("Model is not trained yet");

			throw new Exception("Model is not initialized");

		} else {
			_log.info("Model is already trained");
		}
	}


}
