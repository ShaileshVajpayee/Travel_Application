import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * 
 * @author shaileshvajpayee
 *
 */
public class AirTravel_App extends Application{

	private static Scanner s = new Scanner(System.in);
	public static StringBuilder sb = new StringBuilder();
	public static Stage window;
	public static String source_city;
	public static String source_country;
	public static String destination_city;
	public static String destination_country;
	public static String date;
	

	/**
	 * This function is used to read the stream and return a string of JSON
	 * file.
	 * 
	 * @param api_url
	 *            The URL of the api
	 * @return The JSON file as String
	 * @throws Exception
	 */
	public static String API_consumer(String api_url) throws Exception {
		URL url = new URL(api_url);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.addRequestProperty("Accept", "application/json");
		connection.setRequestMethod("GET");
		connection.connect();
		BufferedReader bufferReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String str;
		StringBuffer stringBuffer = new StringBuffer();
		while ((str = bufferReader.readLine()) != null) {
			stringBuffer.append(str);
			stringBuffer.append("\n");
		}
		str = stringBuffer.toString();
		// sb.append(str);
		return str;
	}

	/**
	 * This function is used to parse the JSON string
	 * 
	 * @param str
	 *            The JSON string
	 * @param key
	 *            The key used to acquire appropriate object in JSON file
	 * @return the iterator for the message
	 * @throws Exception
	 */
	public static Iterator<?> JSON_parser(String str, String key) throws Exception {
		JSONParser parser = new JSONParser();

		JSONObject obj = (JSONObject) parser.parse(str);
		JSONArray msg = (JSONArray) obj.get(key);

		Iterator<?> iterator = msg.iterator();

		return iterator;
	}

	/**
	 * This function is used to iterate the JSON object
	 * 
	 * @param json_file
	 *            The JSON file
	 * @return Travel places iterator
	 * @throws Exception
	 */
	public static Iterator<?> get_airport(String json_file, String key) throws Exception {
		Iterator<?> iterator = JSON_parser(json_file, key);
		JSONObject travel_places = null;
		while (iterator.hasNext()) {
			travel_places = (JSONObject) iterator.next();
			sb.append("\n" + travel_places.get("name"));
		}

		return JSON_parser(json_file, "results");
	}

	/**
	 * This function is used to get the flight information
	 * 
	 * @param json_file
	 *            The JSON file
	 * @throws Exception
	 */
	public static void get_flights(String json_file) throws Exception {
		Iterator<?> iterator = JSON_parser(json_file, "Quotes");
		JSONObject flights = null;
		ArrayList<String> carriers = new ArrayList<>();
		ArrayList<String> prices = new ArrayList<>();

		while (iterator.hasNext()) {
			flights = (JSONObject) iterator.next();
			// sb.append("MinPrice: " + flights.get("MinPrice"));
			prices.add("" + flights.get("MinPrice"));
		}
		double min_price = Double.MAX_VALUE;
		double max_price = 0.0;
		if (prices.size() == 0) {
			sb.append("\n\nPrice range is currently not available!");
		} else {
			for (String i : prices) {
				double p = Double.parseDouble(i);
				if (min_price > p) {
					min_price = p;
				}
				if (p > max_price) {
					max_price = p;
				}
			}
			if (max_price == min_price) {
				sb.append("\nPrice range is " + (min_price) + " to " + (max_price + 200) + "$");
			} else {
				sb.append("\nPrice range is " + min_price + " to " + max_price + "$");
			}
		}

		iterator = JSON_parser(json_file, "Carriers");
		while (iterator.hasNext()) {
			flights = (JSONObject) iterator.next();
			// sb.append("Carriers: " + flights.get("Name"));
			carriers.add("" + flights.get("Name"));
		}
		sb.append("\n\nThere are a total of " + carriers.size() + " available airlines during travel dates: -");
		for (String i : carriers) {
			sb.append(i +"\n");
		}
//		sb.append(carriers.toString());
	}

	/**
	 * This function used the google places api for travel places info
	 * 
	 * @param city
	 * @param country
	 * @throws Exception
	 */
	public static String google_places(String city, String country, String type) throws Exception {
		String Google_key = “YOUR_API_KEY_GOES_HERE”;
		String api_url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=";
		api_url += city + "," + country + "&radius=500&type=";
		api_url += type + "&key=" + Google_key;
		String json_file = API_consumer(api_url);
		// sb.append(json_file);
//		Iterator<?> google = get_airport(json_file, "results");
		return json_file;
	}

	/**
	 * This function used the skyscanner api for flight info
	 * 
	 * @param source_city
	 * @param source_country
	 * @param dest_city
	 * @param dest_country
	 * @throws Exception
	 */
	public static void skyscanner(String source_city, String source_country, String dest_city, String dest_country,
			String IATA, String dates) throws Exception {
		String skyscanner_key = "YOUR_API_KEY_GOES_HERE";
		String api_url = "http://partners.api.skyscanner.net/apiservices/browsequotes/v1.0/US/USD/en-US/" + IATA + "/"
				+ dates + "?apiKey=" + skyscanner_key;
		String json_file = API_consumer(api_url);
//		sb.append(json_file);
		get_flights(json_file);
	}

	/**
	 * Used to get IATA airport code based on latitude and longitude
	 * 
	 * @param airport_name
	 * @throws Exception
	 */
	public static String get_IATA_airport_code(String lat_long) throws Exception {
		String api_url = "http://iatageo.com/getCode/" + lat_long;
		String json_file = API_consumer(api_url);
		// sb.append(json_file);
		JSONParser parser = new JSONParser();

		JSONObject obj = (JSONObject) parser.parse(json_file);
		return obj.get("IATA") + "";
	}

	/**
	 * Used to get the latitude & longitude from JSON response
	 * @param iterator
	 * @return lat_long string
	 * @throws Exception
	 */
	public static String get_lat_long(Iterator<?> iterator) throws Exception {
		String lat_long = "";
		JSONObject location = null;
		JSONObject nest_obj1 = null;
		JSONObject nest_obj2 = null;
//		while (iterator.hasNext()) {
			location = (JSONObject) iterator.next();
			nest_obj1 = (JSONObject) location.get("geometry");
			nest_obj2 = (JSONObject) nest_obj1.get("location");
			lat_long += nest_obj2.get("lat") + "/" + nest_obj2.get("lng");
			sb.append("\n" + nest_obj2.get("lat") + " " + nest_obj2.get("lng"));
//		}
		return lat_long;
	}

	/**
	 * Used to iterate the JSON response
	 * @param json_file
	 * @param key
	 * @throws Exception
	 */
	public static void weather_helper(String json_file, String key) throws Exception {
		Iterator<?> iterator = JSON_parser(json_file, key);
		JSONObject weather = null;
		while (iterator.hasNext()) {
			weather = (JSONObject) iterator.next();
			sb.append("\n " + weather.get("description"));
		}
		
		JSONParser parser = new JSONParser();

		JSONObject obj = (JSONObject) parser.parse(json_file);
		weather = (JSONObject) obj.get("main");
		sb.append("\n " + weather.get("temp") + "C");

	}

	/**
	 * Used to get weather information from openweathermap API
	 * @param city
	 * @throws Exception
	 */
	public static void get_weather(String city) throws Exception {
		String weather_key = "YOUR_API_KEY_GOES_HERE";
		String api_url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&APPID=" + weather_key;
		String json_file = API_consumer(api_url);
		// sb.append(json_file);
		sb.append("\nWeather in " + city + ": ");
		weather_helper(json_file, "weather");
	}
	
	/**
	 * This function iterates the json response
	 * @param json_file
	 * @param key
	 * @throws Exception
	 */
	public static void get_places(String json_file, String key) throws Exception{
		Iterator<?> iterator = JSON_parser(json_file, key);
		JSONObject travel_places = null;
		while (iterator.hasNext()) {
			travel_places = (JSONObject) iterator.next();
			sb.append("\n" + travel_places.get("name"));
		}

	}
	
	/**
	 * This function is used to get travel places in a city using Google API
	 * @param city
	 * @param country
	 * @throws Exception
	 */
	public static void get_travel_places(String city, String country) throws Exception{
		String json_file = google_places(city, country, "zoo");
//		sb.append(json_file);
		get_places(json_file, "results");
		json_file = google_places(city, country, "amusement_park");
		get_places(json_file, "results");
		json_file = google_places(city, country, "art_gallery");
		get_places(json_file, "results");
		json_file = google_places(city, country, "museum");
		get_places(json_file, "results");
		json_file = google_places(city, country, "stadium");
		get_places(json_file, "results");
	}
	
	/**
	 * This function is responsible for making the API calls
	 * @throws Exception
	 */
	public static void run_flow() throws Exception{

		String source_IATA = "";
		String dest_IATA = "";

		get_weather(source_city);
		get_weather(destination_city);
		// 2017-03-15/2017-04-15

		sb.append("\n" + source_city + " Airport(s): ");
		String json_file= google_places(source_city, source_country,"airport");
		Iterator<?> google = get_airport(json_file, "results");
		sb.append("\nLatitude and Longitude of " + source_city + " : ");
//		System.out.println(json_file);
		String source_lat_long = get_lat_long(google);
		source_IATA = get_IATA_airport_code(source_lat_long);
		sb.append("\n"+source_city + " IATA code: " + source_IATA);
		sb.append("\n"+destination_city + " Airport(s): ");
		json_file = google_places(destination_city, destination_country,"airport");
		google = get_airport(json_file, "results");
		sb.append("\nLatitude and Longitude of " + destination_city + " : ");
		String dest_lat_long = get_lat_long(google);
		dest_IATA = get_IATA_airport_code(dest_lat_long);
		sb.append("\n"+destination_city + " IATA code: " + dest_IATA);
		skyscanner(source_city, source_country, destination_city, destination_country, source_IATA + "/" + dest_IATA,
				date);
		sb.append("\n\nVisiting Destinations: -");
		get_travel_places(destination_city, destination_country);
//		System.out.println(sb.toString());
	}

	/**
	 * The main function of the class
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		launch(args);
		
	}
	
	/**
	 * The start function for Java FX
	 */
	public void start(Stage primaryStage) throws Exception {
		
		window = primaryStage;
		window.setTitle("WhereNext!");

		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10, 10, 10, 10));
		grid.setVgap(8);
		grid.setHgap(10);

		Label sourcecity = new Label("Source City (eg. New+York)");
		GridPane.setConstraints(sourcecity, 0, 0);

		TextField src_city = new TextField("Rome");
		GridPane.setConstraints(src_city, 1, 0);

		Label sourcecountry = new Label("Source Country (eg. United+States)");
		GridPane.setConstraints(sourcecountry, 0, 1);

		TextField src_country = new TextField("Italy");
		GridPane.setConstraints(src_country, 1, 1);

		Label destinationcity = new Label("Destination City (eg. Mumbai)");
		GridPane.setConstraints(destinationcity, 0, 2);

		TextField dst_city = new TextField("Paris");
		GridPane.setConstraints(dst_city, 1, 2);

		Label destinationcountry = new Label("Destination Country (eg. India)");
		GridPane.setConstraints(destinationcountry, 0, 3);

		TextField dst_country = new TextField("France");
		GridPane.setConstraints(dst_country, 1, 3);

		Label dep_date = new Label("Departure date (YYYY-MM-DD)");
		GridPane.setConstraints(dep_date, 0, 4);

		TextField dp_date = new TextField("2017-03-10");
		GridPane.setConstraints(dp_date, 1, 4);

		Label arr_date = new Label("Arrival date (YYYY-MM-DD)");
		GridPane.setConstraints(arr_date, 0, 5);

		TextField ar_date = new TextField("2017-03-20");
		GridPane.setConstraints(ar_date, 1, 5);
		
		TextArea output = new TextArea();
		output.setPrefColumnCount(50);
		output.setPrefRowCount(50);
		output.setEditable(false);
		GridPane.setConstraints(output, 0, 10);
		
		Label note = new Label("Please wait while APIs are loaded...");
		GridPane.setConstraints(note, 0, 6);
		
		Button enter = new Button("Enter");
		GridPane.setConstraints(enter, 1, 6);
		enter.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent arg0){
				output.clear();
				source_city = src_city.getText();
				source_country = src_country.getText();
				destination_city = dst_city.getText();
				destination_country = dst_country.getText();
				date = dp_date.getText() + "/" + ar_date.getText();
				
				try {
					run_flow();
				} catch (Exception e) {
					e.printStackTrace();
				}
				output.setText(sb.toString());
				sb = new StringBuilder();
			}
		});
		grid.getChildren().addAll(sourcecity, src_city, sourcecountry, src_country, destinationcity, dst_city,
				destinationcountry, dst_country, dep_date, dp_date, arr_date, ar_date, note, enter, output);

		Scene scene = new Scene(grid, 1024, 768);
		window.setScene(scene);

		window.show();
		
	}

}
