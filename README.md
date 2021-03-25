# Categorizer

Éste es un categorizador basado en el método [Asignación Latente de Dirichlet](https://en.wikipedia.org/wiki/Latent_Dirichlet_allocation) (LDA por sus siglas en inglés), donde un texto se categoriza de acuerdo a unas agrupaciones de palabras o topics que proporciona un modelo preentrenado. Éste modelo se obtiene a través de la extracción del texto de las entradas de distintos blogs, de forma que se sacan los topics de todos los blogs que introduzcamos. 

Para crear el categorizador, se han utilizado las siguientes librerías Open Source:
+ [LDA mallet](http://mallet.cs.umass.edu/)
+ [ixa-pipe-pos](https://github.com/ixa-ehu/ixa-pipe-pos)
+ [ixa-pipe-tok](https://github.com/ixa-ehu/ixa-pipe-tok)

El objetivo de éste proyecto es generar modelos para categorizar textos a partir de los textos recogidos en blogs que estén relacionados por alguna temática en concreto: tecnología, seguros, energía, política, etc.

## Entrenamiento del modelo
A continuación se explica cómo se entrena el modelo desde 0:

### Extracción de los textos

Para extraer los textos de los distintos blogs, se utiliza la clase `TextExtractor`. Lo primero de todo y muy a tener en cuenta es que es muy probable que para cada blog se necesite una configuración totalmente distinta, impidiendo realizar un extractor genérico para todos. 

La obtención de los textos es un procedimiento reiterativo, hay que realizar varias pruebas hasta que se leen únicamente los textos que sean de interés, evitando entrar en las URL de páginas no deseadas, como pueden ser redes sociales o cualquier otro tipo de página. Primero se inspecciona las páginas de los blogs y se obtienen todas las URL de las mismas, pasando después a entrar a las URL que sean de interés y de las cuales sólo va a obtener el texto.

A continuación se detallan los pasos a seguir, primero se obtienen las URL que lee el extractor de texto sin sacar los textos, de forma que se van acotando poco a poco las URL de los posts y luego ya se ejecuta para que lo lea completamente:

1. Primero se comenta la parte que guarda los textos que se han extraído.
1. Luego se inspeccionan los elementos de la página, y se comprueba en el HTML los elementos que contienen las URL (generalmente corresponden al elemento <a href= "http://www. ......"> </a>). Si es así, no hay que cambiar nada, si no lo fuese, habría que realizar los cambios pertinentes en el elemento `entradas` y en sus atributos.
1. Obtención de la URL base, que corresponde al elemento `url`. Ésta URL es la dirección que sigue la paginación de los blogs. Para su obtención, se pasa a la segunda página del blog y se introduce en `url` sustituyendo el `2` por `%s`. La razón de esto es que de manera general, la URL de la primera página del blog es diferente al resto, siendo necesario pasar a la página siguiente.
1. Como al principio se está haciendo una prueba, se define el número máximo de páginas del blog de las que va a leer la URL `maxPages`, que no debe ser grande, por lo que leyendo 3 o 4 páginas es suficiente.
1. Ahora se lee el formato de las URL de las entradas, entrando a unas pocas entradas del blog y cogiendo la parte el principio de la URL que sea común a todas, que se le asigna a `my_site`.
1. Una vez hecho todo esto, iniciamos el extractor de texto, visualizando por pantalla las URL de las que se obtendría el texto. Lo más seguro es que la gran mayoría de las URL que muestre no pertenezcan a entradas del blog, por lo que hay que añadir excepciones. Hay diversas formas, dependiendo de la distribución del blog: 
    1. Si son URL únicas que se repiten en todas las páginas y que cumplen todos los requisitos, pero no se quiere entrar en ellas, se introducen en el array `not_sites`.
    1. Hay veces que las páginas contienen directorios que cumplen las condiciones de los posts, pero que no lo son. Éstos directorios tienen una parte de la URL que hace referencia a ellos, por lo que se deben añadir excepciones incluyendo ésta parte de la URL de la siguiente forma `!link.contains("excepción")` en el mismo lugar donde se comprueba el formato de la URL, si se ha entrado ya en la misma o si es una de las URL que componen las `not_sites`.
    1. Hay que realizar estos pasos iterativamente hasta que se consiga finalmente que imprima por pantalla únicamente las URL de los posts.
1. Ya se está en condiciones de mirar a ver si los textos generados tienen el formato deseado:``texto0	X	Texto Texto Texto Texto Texto Texto Texto Texto Texto Texto``. Para ello, se descomenta la parte que guarda el contenido en un archivo.
1. Cabe la posibilidad de que al inspeccionar los textos, se imprima no sólo el texto, si no que además incluya comentarios o cualquier otro elemento del post, por lo que en el caso de que extraiga más texto del deseado, habría que inspeccionar el elemento que contiene a los textos y seleccionar el elemento que contenga únicamente el contenido de la entrada.
1. Una vez conseguido que en el archivo se obtengan únicamente el contenido de las entradas, ya se puede leer todas las páginas del blog, aumentando el valor de `maxPages` al número máximo de páginas que tiene el blog.


### Entrenamiento del modelo

Tras realizar la extracción del texto de varios blogs, se unen todos en un mismo fichero con formato `.mallet`. Éste fichero, contendrá toda la información de la que se quiera extraer los topics, que se realizará a través de la clase TrainModel. Para ello, se introduce el nombre del fichero con formato mallet en la variable `malletFile`, se introducen el número de topics que se desee obtener en `numTopics` y el número de iteraciones en `numIterations` que deba realizar el entrenamiento para obtener el modelo. Éste modelo de entrenamiento está preparado para trabajar con varios idiomas:

+ Alemán (de)
+ Inglés (en)
+ Castellano (es)
+ Euskera (eu)
+ Francés (fr)
+ Gallego (gl)
+ Flamenco (nl)

La clase se encarga automáticamente de preprocesar el contenido del fichero mallet:
1. Se pone todo en mayúsculas
1. Se lematiza se eliminan todas las palabras que no sean o bien nombres o adjetivos
1. Se elimina cualquier caracter que no sea del alfabeto.
1. Se pasa por un filtrado en la que se eliminan las stopwords
1. Finalmente asigna un vector al texto.

Tanto el idioma en el que se van a introducir los textos como los modelos para lematizarles, se encuentran en la carpeta `src/main/resources/Lemma_models`, por lo que hay que asignar los valores que correspondan en la clase `LemmatizerPipe`. Éstos modelos se encargarán de realizar el lematizado de las palabras, así como de definir qué tipo de palabras son. Se han cogido de la librería [ixa-pipe-pos](https://github.com/ixa-ehu/ixa-pipe-pos).

Tras éste preprocesado, el modelo de entrenamiento ya está en condiciones de poder entrenar con el modelo LDA. Tras entrenar el modelo, proporciona:
1. Un archivo "topWords.txt", que contiene los topics y sus palabras más representativas. Hay que visualizarlo para comprobar que no haya topics demasiado parecidos.
1. Dos archivos: "model.data" e "instances.dat", que contienen el modelo ya entrenado.
1. Por pantalla imprime un valor: LogLikelihood, que da cuenta de lo preciso que ha podido ser el categorizador, cuanto más cercano a 0 sea el modelo, mejor.

Éstos modelos se generan en la carpeta `models`, en una carpeta que proporciona el número de topis, el número de iteraciones el Llh y la fecha de cada modelo generado: `LDA--topics-NumTopics--iter-NumIteractions--llh-LogLikelihood--date`.
