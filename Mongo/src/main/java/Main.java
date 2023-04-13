import com.mongodb.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {

        //Creamos la conexion a la base de datos
        MongoClient mongoClient = crearConexion();

        //Si la conexion fue exitosa entramos al if
        if (mongoClient != null){
            try{
                //Elegimos la base de datos y la colección
                DB database = mongoClient.getDB("paises_db");
                DBCollection paises = database.getCollection("paises");

                //Hacemos un for para realizar llamdas seguidas a la URL
                for (int i = 1 ; i <= 300 ; i++){
                    //Creamos la URL agregandole el indice del bucle para hacer la llamada correspondiente
                    URL url = new URL("https://restcountries.com/v2/callingcode/"+i);

                    //Hacemos la conexion con laURL
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    //Obtenemos el codigo de respuesta de la conexion
                    int responseCode = connection.getResponseCode();

                    //Si el código de respuesta da 200 (lo que indica que fue exitosa) entramos al if
                    if(responseCode == 200){

                        //Creamos un StringBuilde y un Scanner para leer la información de la URL
                        StringBuilder infoString = new StringBuilder();
                        Scanner scanner = new Scanner(url.openStream());

                        //Leemos los datos y los almacenamos en la variable infoString
                        while (scanner.hasNext()){
                            infoString.append(scanner.nextLine());
                        }
                        scanner.close();

                        //Obtenemos todos los objetos que hay en la llamda en un arreglo
                        JSONArray jsonArray = new JSONArray(infoString.toString());

                        //variable bandera que nos va a indicar si ya se insertó un archivo en la base de datos
                        boolean updated = false;

                        //Bucle para recorrer todos los objetos que hay dentro del objeto anterior
                        for (int j = 0 ; j < jsonArray.length(); j++){

                            //Variable que contiene el objeto a guardar en la base de datos
                            JSONObject jsonObject = jsonArray.getJSONObject(j);

                            //Documento en el que se van a guardar los diferentes datos
                            BasicDBObject pais = new BasicDBObject();

                            //Si ya hay un objeto en la base de datos entra al if
                            if(updated){

                                //Se crea otro documento y se ponen los datos nuevos
                                BasicDBObject paisUpdate = new BasicDBObject()
                                        .append("nombre",jsonObject.get("name"))
                                        .append("region",jsonObject.get("region"))
                                        .append("poblacion",jsonObject.get("population"));

                                if(jsonObject.isNull("capital")){
                                    paisUpdate.append("capital","NO TIENE");
                                }else {
                                    paisUpdate.append("capital",jsonObject.get("capital"));
                                }

                                if(jsonObject.isNull("latlng")){
                                    paisUpdate.append("latitud",0)
                                            .append("longitud",0);
                                }else {
                                    paisUpdate.append("latitud",jsonObject.getJSONArray("latlng").get(0))
                                            .append("longitud",jsonObject.getJSONArray("latlng").get(1));
                                }

                                //Se muestra por consoloa el nuevo objeto que se va a actualizar
                                System.out.println("ACTUALIZANDO: "+paisUpdate);

                                //Se actualiza el documento anterior por el creado recientemente
                                paises.update(pais,paisUpdate);



                            }else {
                                //Si no existe ningún documento en la base de datos se ejecuta lo siguiente

                                //Se agregan los diferenetes datos al documento creado anteriormente
                                pais.append("codigoPais",i)
                                        .append("nombre",jsonObject.get("name"))
                                        .append("region",jsonObject.get("region"))
                                        .append("poblacion",jsonObject.get("population"));

                                if(jsonObject.isNull("capital")){
                                    pais.append("capital","NO TIENE");
                                }else {
                                    pais.append("capital",jsonObject.get("capital"));
                                }

                                if(jsonObject.isNull("latlng")){
                                    pais.append("latitud",0)
                                            .append("longitud",0);
                                }else {
                                    pais.append("latitud",jsonObject.getJSONArray("latlng").get(0))
                                            .append("longitud",jsonObject.getJSONArray("latlng").get(1));
                                }

                                //Se muestra por consola los datos del objeto
                                System.out.println("CREANDO: "+pais);

                                //Se lo inserta en la base de datos
                                paises.insert(pais);
                                updated = true;
                            }


                        }
                    }else {
                        //Si el codigo de respuesta es diferente a 200 (lo que indica que no tuvo éxito la conexión) se mostrará lo siguiente
                        System.out.println("NO SE ENCONTRÓ NINGUN VALOR EN LA LLAMADA "+i);
                    }
                }



            }catch (Exception e){
                e.printStackTrace();
            }

        }
        mongoClient.close();
    }

    //Método donde se hace la conexion a la base de datos
    private static MongoClient crearConexion(){
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient("localhost",27017);

        }catch (Exception e){
            e.printStackTrace();
        }
        return mongoClient;
    }
}
