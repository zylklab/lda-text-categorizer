package net.zylk.microproductos.categorizador.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Properties;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import ixa.kaflib.KAFDocument;

public class LemmatizerPipe extends Pipe implements Serializable {

	private static final String LANGUAGE = "es";

	private static final String PATH_TO_LEMMA_MODELS = "./src/main/resources/Lemma_models/" + LANGUAGE;
	private static final String MODEL_BIN = "es-pos-perceptron-autodict01-ancora-2.0.bin";
	private static final String LEMMATIZER_BIN = "es-lemma-perceptron-ancora-2.0.bin";

	public Instance pipe(Instance carrier) {

		String frase = "";
		if (carrier.getData() instanceof CharSequence) {
			String line = carrier.getData().toString();

			// Open the KAF Document
			KAFDocument kaf = new KAFDocument(LANGUAGE, "v1.naf");

			// The tokenizer formats the KAF document
			// The lemmatizer add the lemma to each token
			try {
				doTokenization(PATH_TO_LEMMA_MODELS, LANGUAGE, kaf, line);
				doPartOfSpeechTagging(PATH_TO_LEMMA_MODELS, LANGUAGE, kaf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int i = 0; i < kaf.getTerms().size(); i++) {
				// Fiilter by Nouns (N) and Adjetives (G)
				if (kaf.getTerms().get(i).getPos().toString() == "N" || kaf.getTerms().get(i).getPos() == "G") {

					frase += kaf.getTerms().get(i).getLemma().toString() + " ";
				}

			}
			carrier.setData(frase);
		} else {
			throw new IllegalArgumentException(
					"CharSequenceLowercase expects a CharSequence, found a " + carrier.getData().getClass());
		}
		return carrier;

	}

	// Serialization

	public static void doTokenization(String resourcesDirectory, String language, KAFDocument kaf, String text)
			throws IOException {
		Properties properties = new Properties();
		properties.setProperty("language", language);
		properties.setProperty("resourcesDirectory", resourcesDirectory);
		properties.setProperty("normalize", "default");
		properties.setProperty("untokenizable", "no");
		properties.setProperty("hardParagraph", "no");

		BufferedReader input = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(text.getBytes("UTF-8"))));
		new eus.ixa.ixa.pipe.tok.Annotate(input, properties).tokenizeToKAF(kaf);
	}

	public static void doPartOfSpeechTagging(String resourcesDirectory, String language, KAFDocument kaf)
			throws IOException {
		Properties properties = new Properties();
		properties.setProperty("language", language);
		properties.setProperty("model", new File(resourcesDirectory, MODEL_BIN).getAbsolutePath());
		properties.setProperty("lemmatizerModel", new File(resourcesDirectory, LEMMATIZER_BIN).getAbsolutePath());
		properties.setProperty("resourcesDirectory", resourcesDirectory);
		properties.setProperty("multiwords", "false");
		properties.setProperty("dictag", "false");
		properties.setProperty("useModelCache", "true");

		new eus.ixa.ixa.pipe.pos.Annotate(properties).annotatePOSToKAF(kaf);
	}

}
