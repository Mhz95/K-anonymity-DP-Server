import javax.servlet.http.HttpServletResponse;

/** This class defines an Anonymity message, i.e. a message that has lat and lon which are NOT anonymous
 * and does NOT fullfill time requirements. Anonymity messages always come from  the incoming Queue.
 * @author airwizard
 *
 */
public class AnonymityMessage {

int id;
String mech;
double lati;
double longi;
int time;
int expiration;
String anonymitypolicy;
HttpServletResponse response;

	public AnonymityMessage(int identity,String latitude, String longitude,
			String timeStamp, String anonymityPolicy, String mecha, HttpServletResponse resp) {
		id=identity;
	    lati = Double.parseDouble(latitude);
		longi=Double.parseDouble(longitude);;
		time=Integer.parseInt(timeStamp);
		anonymitypolicy=anonymityPolicy;
		mech=mecha;
		response=resp;
	}
	
	public HttpServletResponse getResponse(){
		return response;
	}
	
	public int getID(){
		return id;
	}
	
	public String getMech(){
		return mech;
	}
	
	public double getLatitude(){
		return lati;
	}
	public double getLongitude(){
		return longi;
	}
	public int getTimeStamp(){
		return time;
	}
	public String getAnonymityPolicy(){
		return anonymitypolicy;
	}
	public String toString() {
		return id+" "+lati+" "+longi+" "+time+" "+anonymitypolicy;
	}

	public static int compare(AnonymityMessage o1, AnonymityMessage o2) {
		if(o1.expiration < o2.expiration) {
			return 1;
		}
		else if(o1.expiration > o2.expiration) {
			return -1;
		}
		else {
			return 0;
		}
	}

}
