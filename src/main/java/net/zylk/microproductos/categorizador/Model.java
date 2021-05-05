package net.zylk.microproductos.categorizador;

import net.zylk.microproductos.categorizador.TrainModel;

public class Model {
	public static void main(String[] args) throws Exception {
		// Set the name of the mallet file
		String malletTrainingFile = null;

		// Set number of topics and the number of iterations to make the model
		Integer numTopics = null;
		Integer numIterations = null;

		// Path output of the models.
		String pathOutput = null;

		// Language:
		// English: "en"
		// Spanish: "es"
		// German: "de"
		// Basque: "eu"
		// French: "fr"
		// Galician: "gl"
		// Dutch: "nl"
		// Italian: "it"
		String language = null;

		TrainModel.train(malletTrainingFile, numTopics, numIterations, pathOutput, language);
		TrainModel.saveModel(numTopics, numIterations);
	}
}
