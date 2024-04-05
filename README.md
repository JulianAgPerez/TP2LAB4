# Tecnicatura Universitaria en Programación - UTN-FRM

---

## **Laboratorio de Computación IV**
**IMPORTANTE**
Para el correcto funcionamiento se debe crear un archivo config.properties con el siguiente cuerpo

\# Configuración de conexión a la base de datos  
db.url=  
db.username=  
db.password=  

---

## **Parte B**  

---

### Métodos en MongoDB: 

Describir el efecto del método drop() en una colección y base de datos: 

drop() elimina la colección completa en la que se aplica, borrando todos los documentos y el índice asociado. Si se aplica a una base de datos, elimina todas las colecciones de esa base de datos. 
Describir el efecto del método skip() en una colección: 

skip() se utiliza para omitir un número específico de documentos en una consulta. Por ejemplo, si aplicamos skip(5) en una colección, se saltará los primeros 5 documentos y comenzará a devolver los documentos a partir del sexto. 
Describir y ejemplificar el uso de expresiones regulares en MongoDB: 

Las expresiones regulares en MongoDB se utilizan para buscar cadenas de texto que coincidan con un patrón específico. Por ejemplo, para buscar todos los documentos donde el nombre del país comience con "A", podemos usar la expresión regular /^A/: 

db.paises.find({nombrePais: /^A/}) 
Crear un nuevo índice para la colección paises utilizando el campo código: 

El siguiente comando crea un índice ascendente en el campo codigoPais de la colección paises: 

db.paises.createIndex({codigoPais: 1}) 

Describir cómo realizar un backup de la base de datos paises_db: 

Para realizar un backup de la base de datos paises_db, podemos utilizar la herramienta mongodump. Por ejemplo: 

mongodump --db paises_db --out /ruta/de/backup 

Esto generará un archivo de backup de la base de datos paises_db en el directorio especificado.
