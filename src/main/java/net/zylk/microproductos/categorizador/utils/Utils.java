package net.zylk.microproductos.categorizador.utils;

public class Utils {

	static String lemmaBin;

	static String modelBin;

	static String stopwords;

	// Method to select the Stop Words List

	public static String stopWordsSelector(String language) {
		stopwords = "./stoplists/" + language + ".txt";

		return stopwords;
	}

	// Method to select model binary
	public static String modelSelector(String language) {
		switch (language) {
		case "es":
			modelBin = Constants.modelBinEs;
			break;
		case "en":
			modelBin = Constants.modelBinEn;
			break;
		case "nl":
			modelBin = Constants.modelBinNl;
			break;
		case "de":
			modelBin = Constants.modelBinDe;
			break;
		case "eu":
			modelBin = Constants.modelBinEu;
			break;
		case "fr":
			modelBin = Constants.modelBinFr;
			break;
		case "gl":
			modelBin = Constants.modelBinGl;
			break;
		case "it":
			modelBin = Constants.modelBinIt;
			break;
		default:
			System.out.println("Language input invalid. Languages supported:");
			System.out.println();
			System.out.println("German: de");
			System.out.println("English: en");
			System.out.println("Spanish: es");
			System.out.println("Basque: eu");
			System.out.println("French: fr");
			System.out.println("Galician: gl");
			System.out.println("Dutch: nl");
			System.out.println("Italian: it");
			System.out.println();
		}
		return modelBin;
	}

	// Method to select lemma binary

	public static String LemmaSelector(String language) {
		switch (language) {
		case "es":
			lemmaBin = Constants.lemmaBinEs;
			break;
		case "en":
			lemmaBin = Constants.lemmaBinEn;
			break;
		case "nl":
			lemmaBin = Constants.lemmaBinNl;
			break;
		case "de":
			lemmaBin = Constants.lemmaBinDe;
			break;
		case "eu":
			lemmaBin = Constants.lemmaBinEu;
			break;
		case "fr":
			lemmaBin = Constants.lemmaBinFr;
			break;
		case "gl":
			lemmaBin = Constants.lemmaBinGl;
			break;
		case "it":
			lemmaBin = Constants.lemmaBinIt;
			break;
		default:
			System.out.println("Language input invalid. Languages supported:");
			System.out.println();
			System.out.println("German: de");
			System.out.println("English: en");
			System.out.println("Spanish: es");
			System.out.println("Basque: eu");
			System.out.println("French: fr");
			System.out.println("Galician: gl");
			System.out.println("Dutch: nl");
			System.out.println("Italian: it");
			System.out.println();
		}
		return lemmaBin;
	}
}
