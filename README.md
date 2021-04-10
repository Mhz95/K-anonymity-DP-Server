
## K Anonymity & Differential privacy (Gaussian/Laplacian) Server


#### K Anonymity (forked from [kostaspap88/jakas](https://github.com/kostaspap88/jakas))  

This projects creates a server that receives several http requests that contain geo-location.   
If they are close enough, then they are grouped together and their location is obfuscated.   

See this:   


                *  John
        
      * Mary


                          * Jim


Now see this:   



                *  John
        
      * Mary
                  * CENTRAL POSITION

                          * Jim


Everybody will be grouped and his position will become the CENTRAL POSITION, offering him k-anonymity   
(and since there are 3 people, we achive 3-1=2 anonymity for everyone)   

The implementation idea is based on the Georgia Tech paper by B.Gedik and L.Liu.   
We developed a Java servlet that works as a simple K-Anonymity server based on the    
message perturbation engine presented.   

The project has educational purposes only, it does not claim compliance to the original paper,   
it is currently in development and comes with no warranties.   



##### REFERENCES:   
Bugra Gedik, Ling Liu. Protecting Location Privacy with Personalized k-Anonymity:   
Architecture and Algorithms. IEEE Transactions on Mobile Computing, Vol. 7, January 2008.   

#### Differential privacy (Gaussian/Laplacian)   

There are timestamps associated with the locations' messages. In case the message is expired  
i.e. no perturbed location is returned as a response, we send a perturbed location generated  
by adding Gaussian/Laplacian noise to the original location.    

This project is linked to another project which serves as an interface for the server (Laravel Web App)   
the interface receives the messages and visualize them on map and record them on table for both incoming/outgoing messages.

The other project can be found in [Privacy Server Map](https://github.com/Mhz95/privacy-server-map)

> Developed as part of a Computer Science MSc course   
> Supervisor: Dr. Saad Alahmady  
> Course: CSC529: Selected topics in computer systems   
> King Saud university   
> April 2021
