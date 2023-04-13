import com.mongodb.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {

        MongoClient mongoClient = crearConexion();

        if (mongoClient != null){
            try{
                DB database = mongoClient.getDB("paises_db");
                DBCollection paises = database.getCollection("paises");

                for (int i = 1 ; i <= 300 ; i++){
                    URL url = new URL("https://restcountries.com/v2/callingcode/"+i);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    int responseCode = connection.getResponseCode();

                    if(responseCode == 200){

                        StringBuilder infoString = new StringBuilder();
                        Scanner scanner = new Scanner(url.openStream());

                        while (scanner.hasNext()){
                            infoString.append(scanner.nextLine());
                        }
                        scanner.close();

                        JSONArray jsonArray = new JSONArray(infoString.toString());

                        boolean updated = false;

                        for (int j = 0 ; j < jsonArray.length(); j++){

                            JSONObject jsonObject = jsonArray.getJSONObject(j);

                            BasicDBObject pais = new BasicDBObject();


                            if(updated){
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
                                System.out.println("ACTUALIZANDO: "+paisUpdate);


                                paises.update(pais,paisUpdate);

                                if (j == jsonArray.length() - 1){
                                    updated = false;
                                }

                            }else {
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
                                System.out.println("CREANDO: "+pais);
                                paises.insert(pais);
                                updated = true;
                            }


                        }
                    }else {
                        System.out.println("NO SE ENCONTRÓ NINGUN VALOR EN LA LLAMADA "+i);
                    }
                }



            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }

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
