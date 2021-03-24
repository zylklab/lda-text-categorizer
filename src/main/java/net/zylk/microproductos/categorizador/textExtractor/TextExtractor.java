package net.zylk.microproductos.categorizador.textExtractor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TextExtractor {

	// Introduce the URL where the posts are found
	public static final String url = null;

	// Max page of URL to read
	public static final Integer maxPages = 0;
	public static String my_site;

	// Create the URL list with the URL already entered
	static ArrayList<String> urlList = new ArrayList<String>();

	// Create the URL list with the URL which we do not want enter
	static ArrayList<String> not_sites = new ArrayList<String>();

	// Create the name of the file with the extracted texts
	static String archivo_textos = null;

	public static void main(String args[]) {

		// URL's structure of the posts
		my_site = null;

		// List of the URL's to not enter
		not_sites.add(null);

		int j = 0;

		try {
			// Open an .txt file to save the texts
			File archivo = new File(archivo_textos);
			FileWriter escribir = null;
			escribir = new FileWriter(archivo, true);

			for (int i = 1; i <= maxPages; i++) {

				// Format of the first URL
				String urlPage = String.format(url, i);

				System.out.println("Checking post from page  " + i);

				// Check if the Status code is 200
				if (getStatusConnectionCode(urlPage) == 200) {

					// Obtain the HTML web format in a Document object
					Document document = getHtmlDocument(urlPage);

					// Search the references to other URL
					Elements entradas = document.select("a[href]");

					// Obtain each URL
					for (Element elem : entradas) {
						String link = elem.attr("abs:href");

						// Check if:
						// + The URL starts with the format that we want
						// + The new URL are not in urlList
						// + The new URL is not a copy of hisself
						if (link.startsWith(my_site) && !urlList.contains(link) && !not_sites.contains(link)) {

							// Check if we can enter in this URL
							if (getStatusConnectionCode(link) == 200) {

								System.out.println(link);
								
								// Comment on this part when narrowing down the URLs
								
								// Read the URL and start the file's line
								Document textos = getHtmlDocument(link);
								Elements parrafos = textos.select("p");
								escribir.write("texto" + j + "\t X \t");

								// Read the URL and write the paragraphs
								for (Element text : parrafos) {
									String parrafo = text.text();
									escribir.write(parrafo + " ");
								}
								
								// End of the commented part when narrowing down the URLs

								urlList.add(link);

								escribir.write("\n");
								j += 1;
							}
						}
					}
				}
			}
			escribir.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Con esta método compruebo el Status code de la respuesta que recibo al hacer
	 * la petición EJM: 200 OK 300 Multiple Choices 301 Moved Permanently 305 Use
	 * Proxy 400 Bad Request 403 Forbidden 404 Not Found 500 Internal Server Error
	 * 502 Bad Gateway 503 Service Unavailable
	 */
	// With this method, we check the status code, which is the response to enter
	// the petition:
	// 200: ok
	// 300: Multiple Choices
	public static int getStatusConnectionCode(String url) {

		Response response = null;

		try {
			response = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(100000).ignoreHttpErrors(true).execute();
		} catch (IOException ex) {
			System.out.println("Excepción al obtener el Status Code: " + ex.getMessage());
		}
		return response.statusCode();
	}

	/**
	 * Con este método devuelvo un objeto de la clase Document con el contenido del
	 * HTML de la web que me permitirá parsearlo con los métodos de la librelia
	 * JSoup
	 * 
	 */
	public static Document getHtmlDocument(String url) {

		Document doc = null;

		try {
			doc = Jsoup.connect(url).userAgent("Mozilla").timeout(100000).get();
		} catch (IOException ex) {
			System.out.println("Excepción al obtener el HTML de la página" + ex.getMessage());
		}
		return doc;

	}
}