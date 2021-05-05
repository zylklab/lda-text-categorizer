package net.zylk.microproductos.categorizador.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Properties;
import java.util.Scanner;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import ixa.kaflib.KAFDocument;

public class LemmatizerPipe extends Pipe implements Serializable {

	private static final long serialVersionUID = 1;

	private String language;
	private String pathOutput;

	public String modelBin;
	public String lemmaBin;

	public LemmatizerPipe(String pathOutput, String language, String modelBin, String lemmaBin) {
		this.language = language;
		this.pathOutput = pathOutput + language;
		this.modelBin = modelBin;
		this.lemmaBin = lemmaBin;

	}

	public Instance pipe(Instance carrier) {

		String frase = "";
		if (carrier.getData() instanceof CharSequence) {
			String line = carrier.getData().toString();
			// Abrimos un documento KAF
			KAFDocument kaf = new KAFDocument(language, "v1.naf");

			// Con el tokenizador se da formato al documento KAF, que contiene todo el
			// párrafo.

			// Con el lematizador, añadimos el lema para cada token

			try {
				doTokenization(pathOutput, language, kaf, line);
				doPartOfSpeechTagging(pathOutput, language, kaf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int i = 0; i < kaf.getTerms().size(); i++) {
				// Filter by Nouns (N) and Adjectives (G)

				if (kaf.getTerms().get(i).getPos().toString().equals("N")
						|| kaf.getTerms().get(i).getPos().toString().equals("G")) {

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

	public void doTokenization(String resourcesDirectory, String language, KAFDocument kaf, String text)
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

	public void doPartOfSpeechTagging(String resourcesDirectory, String language, KAFDocument kaf) throws IOException {
		Properties properties = new Properties();
		properties.setProperty("language", language);
		properties.setProperty("model", resourcesDirectory + modelBin);
		properties.setProperty("lemmatizerModel", resourcesDirectory + lemmaBin);
		properties.setProperty("resourcesDirectory", resourcesDirectory);
		properties.setProperty("multiwords", "false");
		properties.setProperty("dictag", "false");
		properties.setProperty("useModelCache", "true");
		new eus.ixa.ixa.pipe.pos.Annotate(properties).annotatePOSToKAF(kaf);
	}

}
