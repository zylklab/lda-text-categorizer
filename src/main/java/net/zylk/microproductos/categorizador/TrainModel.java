package net.zylk.microproductos.categorizador;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

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

public class TrainModel {

	private static final Logger _log = LoggerFactory.getLogger(TrainModel.class);

	private static ParallelTopicModel model;
	private static InstanceList modelInstances;

	// Set number of topics and the number of iterations to make the model
	private static Integer numTopics;
	private static Integer numIterations;

	// Set the name of the mallet file
	private static String malletFile;

	public static void generateModel(String trainFile, boolean saveEachModel) throws Exception {

		List<Double> llh = new ArrayList<Double>();
		Integer iterations = 1000;
		List<Integer> topics = new ArrayList<Integer>(
				Arrays.asList(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100));

		for (Integer n : topics) {
			train(trainFile, n, iterations);
			llh.add(model.modelLogLikelihood());
			if (saveEachModel) {
				saveModel(n, iterations);
			}
		}
		List<Integer> maxTopics = new ArrayList<Integer>();
		Integer maxT = topics.get(llh.indexOf(Collections.max(llh)));
		maxTopics.add(maxT);

		System.out.println();
		System.out.println();
		System.out.println("Number of Topics = " + topics);
		System.out.println("LogLikelihood    = " + llh);
		System.out.println("Max llh correspond with topic number " + maxT);
	}

	public static void train(String inputFile, Integer numberOfTopics, Integer iterations) throws IOException {

		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		_log.debug("Preparing pipes");
		// Pipes: lemmatizer, tokenize, remove stopwords, map to features

		// The lemmatizer takes the lemma of the words, and only passes the nouns and
		// the adjetives
		pipeList.add(new LemmatizerPipe());

		pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
		pipeList.add(new TokenSequenceRemoveStopwords(new File("./stoplists/es.txt"), "UTF-8", false, false,
				false)); /** STOPWORDS */
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

	public static void saveModel(Integer numTopics, Integer numIterations) throws Exception {

		isModelInitialized();

		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd--HH:mm");
		String file = String.format("LDA--topics-%s--iter-%s--%s", numTopics, numIterations, dtf.format(now));
		
		File directorio = new File(String.format("./models/LDA--topics-%s--iter-%s--llh-%s--%s", numTopics, numIterations,
				model.modelLogLikelihood(), dtf.format(now)));
		try {
			File models = new File("./models");
			if (!models.exists()) {
				models.mkdir();
			}
			directorio.mkdir();
			// printTopWords creates an .txt with the most relevant words per topic
			Integer wordsPerTopicToPrint = 5;
			model.printTopWords(new File(directorio + "/topWords.txt"), wordsPerTopicToPrint, false);
			// This two steps are needed to save correctly the train model and generate two
			// files which will be loaded
			// when you want to use this model
			model.write(new File(directorio + "/model.dat"));
			modelInstances.save(new File(directorio + "/instances.dat"));

		} catch (IOException e) {
			_log.error(String.format("Error saving model state on %s", file));
			throw e;
		}
	}

	public static void getBestModel(String modelsDirectory) throws Exception {
		// List all models
		File directoryPath = new File(modelsDirectory);
		FilenameFilter modelFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.startsWith("LDA--") && name.endsWith("--model.dat")) {
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

	public static void main(String[] args) throws Exception {
		TrainModel LDA = new TrainModel();
		String malletTrainingFile = malletFile;

		train(malletTrainingFile, numTopics, numIterations);
		saveModel(numTopics, numIterations);

	}

}
