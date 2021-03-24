#PROCESO PARA GENERAR LOS MODELOS#

1 Descargar todos los textos y dejarlos en la misma carpeta

2 Pasar todos los textos a formato **MALLET** mediante este *script* de *Python*:

```python

import glob

outfilename = '/home/idoia/Escritorio/WIKIPEDIA/wikipedia.mallet'
read_files = glob.glob('/home/idoia/Escritorio/WIKI/*')
        
with open(outfilename, "w") as outfile:
    for n, f in enumerate(read_files):
       with open(f, "r") as infile:
           txt = "texto{}\tX\t{}\n".format(n, infile.read())
           outfile.write(txt)
```

*Ejemplo formato MALLET:*

```
texto0	X	Upgrade del portal de zylk a liferay portal 6.0.6 CE
texto1	X	hadoop ecosystem hortonworks home nifi Procesando fi
texto2	X	corporate Aglunos consejos sobre la aplicación de la
texto3	X	industry 4.0 innovación / i+d pentaho cloudera home 
texto4	X	liferay Optimizaciones SEO para liferay portal III S
texto5	X	tech linux linux commands to check network performan
texto6	X	alfresco Tips de Libreoffice para transformaciones e
texto7	X	Eventos industry 4.0 home ¡Basque Industry fue todo 
texto8	X	alfresco Creación de sitios para un subconjunto de u
texto9	X	home sinadura El futuro de sinadura (sinadura 5) El 
texto10	X	hortonworks hadoop ecosystem Visión general de una a
```

3 Generar diferentes modelos junto a su valor *loglikelihood*. Para ello hemos utilizado la clase **train**.

Para ello cambiamos el número de iteraciones y el número de *topics* a generar. El número de *topics* lo variamos entre 5 y 100; en cambio el número de iteraciones podemos ir aumentándolo según nos acercamos a un *topic* óptimo, ya que, generalmente, cuantas más iteraciones, mejora el modelo. El rango que utilizamos está entre 1000 y 2000 iteraciones.

El valor del *likelihood* determina los valores de los parámetros de un modelos. Como este valor es "difícil" de diferenciar, se simplifica tomando el logaritmo de la expresión. Por lo que imprimiremos el valor que representa "cual de bueno es nuestro modelo", teniendo en cuenta que el "mejor valor" será el más cercano al cero.

```java

		List<Double> llh = new ArrayList<Double>();
				llh.add(model.modelLogLikelihood());
				System.out.println("LogLikelihood    = " + llh);
```

4 Añadir palabra a la lista de *stop words* *(/home/idoia/eclipse-workspace/categorizador/stoplist/es_wiki.txt)* si fuera necesario.

Si vemos una palabra que se repite y carece de significado en los *topics*, la añadimos a mano en la lista de *stop words*.

5 Elegir el modelo con mayor valor *loglikelihood*, siempre y cuando tenga sentido, es decir, que las palabras de los *topics* tengan relación entre ellas. Los *topic models* han de evaluarse también desde la perspectiva del entendimiento humano, ya que deben poder expresar información entendible a nivel humano. Esto conlleva un análisis cualitativo y cuantitativo del modelo.
