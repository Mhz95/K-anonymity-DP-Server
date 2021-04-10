import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.GeodeticCalculator;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.UndirectedSubgraph;

import org.opengis.referencing.operation.TransformException;
import org.opengis.geometry.coordinate.Position;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

/** We denote with ** comments the explanations */
//  We denote with line comments the parts of the algorithm (in pseudo-code) that we have implemented
//  Full algorithm (in pseudo-code): Protecting location privacy with personalized k-anonymity:
//    Architecture and Algorithms, B.Gedik, page: 8

public class MessagePerturbationEngineThread extends Thread {

	LinkedList<AnonymityMessage> processingQueue = new LinkedList<AnonymityMessage>();
	// serious consideration: processingQueue should be implemented more
	// efficiently with R* trees
	// another consideration: UNIQUE identifier for each message
	String content = "";
	UndirectedGraph<AnonymityMessage, DefaultEdge> graph = new SimpleGraph<AnonymityMessage, DefaultEdge>(
			DefaultEdge.class);
	PriorityQueue<AnonymityMessage> maxHeap = new PriorityQueue<AnonymityMessage>(new Comparator<AnonymityMessage>() {
		@Override
		public int compare(AnonymityMessage o1, AnonymityMessage o2) {
			return AnonymityMessage.compare(o1, o2);
		}
	});

	SecureRandom ranGen = new SecureRandom();
	byte[] aesKey = new byte[16]; // 16 bytes = 128 bits

	public void run() {

		System.out.println(Thread.currentThread().getName() + " has started");
		printQ();

		System.out.println("MessagePerturbationEngineThread:\n   -Engine is ON");

		

			/**
			 * While the servlet is up the boolean engineRunning is true. When it stops the
			 * servlet context makes it false and the engine stops by getting to the end of
			 * the run() thread function.
			 */
			while (AnonymityQueues.engineRunning == true) { // while engine_running=true
				AnonymityMessage message;
				try {
					System.out.println("||||||| WAIT |||||||...");
					Thread.sleep(3000L);
					RemoveExpieredMessages();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				/**
				 * We are constantly polling the asynchronous incoming queue to check for new
				 * http requests
				 */
				if ((message = AnonymityQueues.incomingQueue.poll()) != null) {// if Qm <> {} then msc <- Pop the first
																				// item
																				// in Qm
					System.out.println("CURRENT: " + message);
					processingQueue.add(message);
					System.out.println("processingQueue: " + processingQueue.toString());
					graph.addVertex(message); // Add the message msc into Gm as a node

					message.expiration = message.time + AnonymityQueues.timeLimit;// NOT IMPLEMENTED: Add msc to Im with
																					// L(msc), i.e. use geo-spatial
																					// index
					maxHeap.add(message); // NOT IMPLEMENTED: Add msc to Hm with (msc*t+msc*dt), i.e. temporal
											// cloaking

					Set<AnonymityMessage> subset = null;
					try {
						subset = searchForNeighbors(message); // N <- Range search of the processing queue using simple
																// Search
																// NOT IMPLEMENTED: N <- Range search in Im using
																// Bcn(msc),
																// i.e. using the spatial index and geo-Bounds
					} catch (TransformException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					/**
					 * At this point we have found the candidates for anomymization, i.e. we have
					 * formed the subset
					 */
					System.out.println("-VertexSet:" + graph.vertexSet().toString());
					System.out.println("-SubsetOfNeighbors:" + subset.toString());

					UndirectedSubgraph<AnonymityMessage, DefaultEdge> subGraph = new UndirectedSubgraph<AnonymityMessage, DefaultEdge>(
							graph, subset, null); // Gm' is created here based on subset

					System.out.println("-SubGraph:" + subGraph);

					CliqueFinder cliqueFinder = new CliqueFinder(graph, message); // M <- LOCAL-k_SEARCH(ms*k,msc,Gm')
					Collection resultCliques = cliqueFinder.getCliques();
					if (resultCliques != null) {// if M <> {}
						System.out.println("-Cliques:" + resultCliques.toString());
					} else {
						System.out.println("-Not Suitable Clique found");
					}

					printQ();

					// fork 1: take a copy + visualize
					// fork 2: implement time constraint functionality (Release as it is if expired)
					// fork 2.1: our updates

					// loc, loc modified -> file
					// listen to file

					if (resultCliques != null) {
						// NOT IMPLEMENTED YET: Randomize the order of messages in M
						// System.out.println(ranGen.nextInt(resultCliques.size()));
						// ranGen.nextBytes(aesKey);
						Iterator it = resultCliques.iterator();
						LinkedList<AnonymityMessage> cloakingQueue = new LinkedList<AnonymityMessage>();
						while (it.hasNext()) {// foreach ms in M
							message = (AnonymityMessage) it.next();
							cloakingQueue.add(message);
							graph.removeVertex(message);// Remove ms from Gm
							processingQueue.removeFirstOccurrence(message);// Remove ms Processing Queue
							// NOT IMPLEMENTED: remove ms from Hm
							maxHeap.remove(message);
							// NOT IMPLEMENTED: remove ms from Im

						}
						// NOT IMPLEMENTED: temporal cloaking
						RemoveExpieredMessages();

						// NOT IMPLEMENTED: ms <- Topmost item in Hm
						// NOT IMPLEMENTED: if ms*t+ms*dt<now, remove ms from Gm,Im; Pop the topmost
						// element in Hm else break
						LinkedList<AnonymizedMessage> out = obfuscateMessages(cloakingQueue);// Perturbed messages, mt
						for (int i = 0; i < cloakingQueue.size(); i++) {
							AnonymityQueues.outgoingQueue.offer(out.get(i));

							System.out.println("-Message id Out = " + out.get(i).getID());

							System.out.println("Successfully wrote to the file.");
						}
						System.out.println("-Messages outputted to Queue, #messages = " + cloakingQueue.size());

					}

					System.out.println("-VertexSet:" + graph.vertexSet().toString());
					System.out.println("-processingQueue:" + processingQueue.toString());

				}
			}
			System.out.println("MessagePerturbationEngineThread\n   -Engine is OFF");

			System.out.println(Thread.currentThread().getName() + " is about to exit");


	}

	/**
	 * This function check and remove the messages that are not processed yet and
	 * are expiered.
	 * 
	 * @param
	 */
	private void RemoveExpieredMessages() {

		AnonymityMessage msg = maxHeap.peek();

		if (msg != null) {
			long now = System.currentTimeMillis() / 1000L;
			if ((msg.expiration) < now) {
				
					Lock lockObject = AnonymityQueues.lockTable.get(msg.getID());
					synchronized (lockObject) {
						AnonymityQueues.lockTable.remove(msg.getID()).notify();
						graph.removeVertex(msg);// Remove ms from Gm
						processingQueue.removeFirstOccurrence(msg);// Remove ms Processing Queue
						maxHeap.remove(msg);
						
						double sensitivity = 0.3;
						double disruptedLatitude = 0.0;
						double disruptedLongitude = 0.0;
						String policy = "";

						if(msg.getMech() != null && !msg.getMech().isEmpty() && msg.getMech().equals("lap")) {
							/** 
							 * Laplacian Mechanism
							 * **/
							System.out.println("\n********************** LAPLACIAN *******************\n");
							policy = "L";
							LaplacianNoiseGenerator lpGenerator = new LaplacianNoiseGenerator();
							
							
							double mu = 0.0;
							double eps = 0.7;
							double b = sensitivity / eps;
							double noiseOne = lpGenerator.nextLaplacian(mu, b);
							disruptedLatitude = msg.getLatitude() + noiseOne;
							
							System.out.println("Non perturbed value = (" + msg.getLatitude() + "," + msg.getLongitude()
							+ ")" + "\nFor epsilon = " + eps + "\nperturbation generated = " + noiseOne
							+ " perturbed Latitude value = " + disruptedLatitude + "\n");
							
							eps = 0.7;
							b = sensitivity / eps;
							double noiseTwo = lpGenerator.nextLaplacian(mu, b);
							disruptedLongitude = msg.getLongitude() + noiseTwo;

							System.out.println("For epsilon = " + eps + "\nperturbation generated = " + noiseTwo
							+ " perturbed Longitude value = " + disruptedLongitude + "\n");
						} else if(msg.getMech() != null && !msg.getMech().isEmpty() && msg.getMech().equals("gau")) {
							/** 
							 * Gaussian Mechanism
							 * **/
							policy = "G";

							System.out.println("\n********************** GAUSSIAN *******************\n");
							
							GaussianNoiseGenerator GauGenerator = new GaussianNoiseGenerator();
							double Gmu = 0.0;
							
							double G_eps = 0.5; // (0 , 1)
							double delta = 0.0001;  // (0 , 1)
							double var = GauGenerator.calculateVariance(sensitivity, G_eps, delta);
							double noiseGOne = GauGenerator.nextGaussian(Gmu, var);
							disruptedLatitude = msg.getLatitude() + noiseGOne;
							
							System.out.println("Non perturbed value = (" + msg.getLatitude() + "," + msg.getLongitude()
							+ ")" + "\nFor epsilon = " + G_eps + "\nperturbation generated = " + noiseGOne
							+ " perturbed Latitude value = " + disruptedLatitude + "\n");
	
							G_eps = 0.6; // (0 , 1)
							delta = 0.0001;  // (0 , 1)
							var = GauGenerator.calculateVariance(sensitivity, G_eps, delta);
							double noiseGTwo = GauGenerator.nextGaussian(Gmu, var);
							disruptedLongitude = msg.getLongitude() + noiseGTwo;
	
							System.out.println("For epsilon = " + G_eps + "\nperturbation generated = " + noiseGTwo
							+ " perturbed Longitude value = " + disruptedLongitude + "\n");
						}

						/** 
						 * Release the anonymized message
						 * **/
						
						AnonymizedMessage m = new AnonymizedMessage(msg.getID(), disruptedLatitude, disruptedLongitude,
								msg.getResponse());
						m.setPolicy(policy);
						
						AnonymityQueues.outgoingTable.put(m.getID(), m);

						AnonymityQueues.outgoingQueue.offer(m);

						System.out.println("-Message id Out = " + m.getID());


					}

			}
		}
	}
	/**
	 * This function calculates the central positioning of all the group, so as to
	 * create the anonymous group location. We find the centroid of the geometrical
	 * point collection The suggested way is calculation of MBR but I do not have a
	 * clear opinion about optimality so far
	 * 
	 * @param queue
	 */
	private LinkedList<AnonymizedMessage> obfuscateMessages(LinkedList<AnonymityMessage> queue) {
		System.out.println("INSIDE obfuscateMessages");
		LinkedList<AnonymizedMessage> result = new LinkedList<AnonymizedMessage>();

		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

		Coordinate[] coords;

		coords = new Coordinate[queue.size()];
		for (int i = 0; i < queue.size(); i++) {
			double lat = queue.get(i).getLatitude();
			double lon = queue.get(i).getLongitude();
			coords[i] = new Coordinate(lat, lon);

		}

		/** Geometry Collection */
		MultiPoint points = geometryFactory.createMultiPoint(coords);

		/** Get the anonymous location */
		Point center = points.getCentroid();

		System.out.println("-Perturbed location: " + center.getX() + "/" + center.getY() + "\n");
		for (int i = 0; i < queue.size(); i++) {

			System.out.println("ID = " + queue.get(i).getID());

			AnonymizedMessage m = new AnonymizedMessage(queue.get(i).getID(), center.getX(), center.getY(),
					queue.get(i).getResponse());
			result.add(new AnonymizedMessage(queue.get(i).getID(), center.getX(), center.getY(),
					queue.get(i).getResponse()));

			Lock lockObject = AnonymityQueues.lockTable.get(queue.get(i).getID());
			m.setPolicy("K");
			AnonymityQueues.outgoingTable.put(m.getID(), m);

			synchronized (lockObject) {
				// lockObject.notify();
				AnonymityQueues.lockTable.remove(queue.get(i).getID()).notify();
			}
		}

		return result;

	}

	/**
	 * This function will search all current clients in order to determine which can
	 * be grouped together for anonymity purposes
	 * 
	 * @param message
	 * @return
	 * @throws TransformException
	 */
	private Set<AnonymityMessage> searchForNeighbors(AnonymityMessage message) throws TransformException {

		Set<AnonymityMessage> subset = new HashSet<AnonymityMessage>();
		subset.add(message);
		// Foreach ms in N(processingQueue), ms<>msc
		for (int i = 0; i < processingQueue.size(); i++) {
			System.out.println("-Message Processing started\n   -lat/lon= " + processingQueue.get(i).getLatitude() + "/"
					+ processingQueue.get(i).getLongitude());
			if (distanceIsCloseEnough(message, processingQueue.get(i))// if L(ms) in inside B(msc)
					&& (notSameID(message, processingQueue.get(i)))) {
				System.out.println("The message is: " + message.toString());
				System.out.println("The messageCand is: " + processingQueue.get(i).toString());
				graph.addEdge(message, processingQueue.get(i)); // add the edge (msc,ms) into Gm
				subset.add(processingQueue.get(i));
			}
		}
		return subset; // Gm' <- subgraph of Gm consisting of messages in N

	}

	/** Just checking that we don't use the same node as a neighbour of itself */
	private boolean notSameID(AnonymityMessage message, AnonymityMessage messageCand) {
		boolean result = true;
		if (message == messageCand) {
			result = false;
		}

		return result;
	}

	/**
	 * This function will check if two clients are close enough s.t. they can be
	 * grouped together for anonymity
	 * 
	 * @param message
	 * @param messageCand
	 * @return
	 * @throws TransformException
	 */
	private boolean distanceIsCloseEnough(AnonymityMessage message, AnonymityMessage messageCand)
			throws TransformException {
		System.out.println("INSIDE distanceIsCloseEnough");
		boolean closeEnough;

		DirectPosition2D pos = new DirectPosition2D(message.getLongitude(), message.getLatitude());

		DirectPosition2D posCand = new DirectPosition2D(messageCand.getLongitude(), messageCand.getLatitude());

		GeodeticCalculator calc = new GeodeticCalculator();
		calc.setStartingPosition((Position) pos);
		calc.setDestinationPosition((Position) posCand);

		System.out.println("-Distance is: " + calc.getOrthodromicDistance());

		if (calc.getOrthodromicDistance() <= AnonymityQueues.safetyDistance) {
			closeEnough = true;
			System.out.println("\n\nClose Enough\n\n");
		} else {
			closeEnough = false;
			System.out.println("\n\nNOT Close Enough\n\n");
		}

		return closeEnough;
	}

	public void printQ() {
		System.out.println("\n\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println("incomingQueue || " + AnonymityQueues.incomingQueue.toString());
		System.out.println("outgoingQueue || " + AnonymityQueues.outgoingQueue.toString());
		System.out.println("lockTable 	  || " + AnonymityQueues.lockTable.toString());
		System.out.println("outgoingTable || " + AnonymityQueues.outgoingTable.toString());
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n\n");
	}

}
