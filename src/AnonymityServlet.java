import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

/**
 * Servlet implementation class, AnonymityServlet.  The main purpose of this class is to 
 * implement a server interface for the Anonymity Server.  The clients perform HTTP POST requests
 * and send their latitude, longitude, timestamp and anonymization policy (no policy, basic anonymity,
 * strong anonymity)
 * NOTE: Although not explicitly invoked, the http requests are handled by the library by many threads.
 */
@WebServlet("/AnonymityServlet")
public class AnonymityServlet extends HttpServlet  {
	private static final long serialVersionUID = 1L;
	 
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AnonymityServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
 
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/** No need for a GET request, we prefer POST */
	}

	/**
	 * We get from a potential client the latitude, longitude, timestamp and anonymity policy
	 * lat and lon will be used for spatial cloaking, timestamp for temporal cloaking and 
	 * policy will help us define the k-level of anonymity.
	 * Then the parameters are combined into an AnonymityMessage and then loaded to the Incoming Queue
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	    /** Parameters of request */
	    String latitude = request.getParameter("latitude").toString();
	    String longitude = request.getParameter("longitude").toString();
	    String timeStamp = request.getParameter("timestamp").toString();
	    int id = Integer.parseInt(request.getParameter("id").toString());
	    String anonymityPolicy = request.getParameter("anonymity_policy").toString();
	    String mech = request.getParameter("mech").toString();

	    
	    /** Create message from parameters */
//0 msg, 1 msg, 2 msg	    
//0 msg, 0 msg, 0 msg  

	    /** timeStampInt is temporary id, we can use Profile ID in the future **/
	    AnonymityMessage message = new AnonymityMessage(id,latitude,longitude,timeStamp,anonymityPolicy,mech,response);	  
	   
	    /** Put message into queue */
		AnonymityQueues.incomingQueue.offer(message);
	  

		System.out.println("POST recieved LAT "+ latitude);
		System.out.println("AnonymityQueues || "+ AnonymityQueues.incomingQueue.toString());
		System.out.println("---------------------------------");
	    Lock lockObject = new Lock(message.getID());
	    AnonymityQueues.lockTable.put(message.getID(), lockObject);
	    
	    synchronized (lockObject){
	    	try {
				lockObject.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    //Critical response section
	  
	 if(AnonymityQueues.outgoingTable.get(lockObject.getID()) != null) { // K -> DP
		 	System.out.println("Message is sent successfully!!!!!!!!!!!!!!!!!!!!");
		 	String pol = AnonymityQueues.outgoingTable.get(lockObject.getID()).getPolicy();
		 	String jsonString = "{\"id\" : " +AnonymityQueues.outgoingTable.get(lockObject.getID()).getID()+",\"policy\" : \"" + ( pol != null ? pol : "K") +"\",\"latitude\" : " + AnonymityQueues.outgoingTable.get(lockObject.getID()).getLatitude() + ", \"longitude\" : "+AnonymityQueues.outgoingTable.get(lockObject.getID()).getLongitude() +"}" ;
		 	response.addHeader("Access-Control-Allow-Origin", "*");
	        PrintWriter out = response.getWriter();
	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");
	        out.print(jsonString);
	        out.flush();  
		   //response.addHeader("latitude", AnonymityQueues.outgoingTable.get(lockObject.getID()).getLatitude()+"");
		   //response.addHeader("longitude", AnonymityQueues.outgoingTable.get(lockObject.getID()).getLongitude()+""); 
	 } else { // Minimized
		 response.addHeader("Access-Control-Allow-Origin", "*");
		 	System.out.println("Message is expired!!!!!!!!!!!!!!!!!!");
		 	response.addHeader("Expired", lockObject.getID()+""); // Expiration message
	 } 

	}

	
	

}
