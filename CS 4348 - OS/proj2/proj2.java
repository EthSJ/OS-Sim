import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class proj2
{
        //control for threads other than customer
        static boolean RUN = true;
        
        //for fun. You'll see. Look for what the patient's issue is
	static Random rnd = new Random();
        
	//How many patients, and how many doctors
	public static int NUM_CUSTOMERS;
        public static int DOCS;
        
        //storing patient ID for receptionist, patient ID for nurse, patient ID for doctor
        static int PATIENT_ID;
        static int PATIENT_ID2[] = new int[3];
        static int PATIENT_ID3[] = new int[3];
	
	//Receptionist semaphores: start talks, receptionist talks, anti-time travel semaphore
	static Semaphore receptionistReady = new Semaphore(0, true);
        static Semaphore receptionistBusy = new Semaphore(0, true);
        static Semaphore receptionistDone = new Semaphore(0, true);
        
        //Nurse semaphores: try and flag a nurse, start and link to nurse, nurse does something, patient finishes something
        static Semaphore nurseStart = new Semaphore(0, true);
        static Semaphore nurseReady[] = {new Semaphore(0, true), new Semaphore(0, true), new Semaphore(0, true)};
	static Semaphore nurseBusy[] = {new Semaphore(0, true), new Semaphore(0, true), new Semaphore(0, true)};
        static Semaphore nurseDone[] = {new Semaphore(0, true), new Semaphore(0, true), new Semaphore(0, true)};
        
        //Doctor semaphores: start and link to doc, doc does something, patient finishes something
        static Semaphore doctorReady[] = {new Semaphore(0, true), new Semaphore(0, true), new Semaphore(0, true)};
        static Semaphore doctorBusy[] = {new Semaphore(0, true), new Semaphore(0, true), new Semaphore(0, true)};
        static Semaphore doctorDone[] = {new Semaphore(0, true), new Semaphore(0, true), new Semaphore(0, true)};
        
	public static void main(String[] args)
	{
                
                //check arg length
		if(args.length < 2)
                {System.out.println("Missing an arg. =("); System.exit(-1);}
                
                //parse docs and people
                DOCS = Integer.parseInt(args[0]);
                NUM_CUSTOMERS = Integer.parseInt(args[1]);
                
                //too many or no doctors
                if(DOCS > 3 || DOCS < 1)
                {System.out.println("Too many or no doctors. =(");System.exit(-1);}
                
                //too many or no patients
                if(NUM_CUSTOMERS < 1 || NUM_CUSTOMERS > 30)
                {System.out.println("Too many or no patients. =(");System.exit(-1);}
                
                
		//Spawn a receptionist
		Thread receptionist = new Thread(new Receptionist());
                
                //spawn a bunch of nurses. Depending on count told
                Thread nurses[] = new Thread[DOCS];
                for(int k=0;k<DOCS;k++)
                    nurses[k] = new Thread(new Nurse(k));
                
                //spawn a bunch of docs. same number as nurses
		Thread docteur[] = new Thread[DOCS];
                for(int k=0;k<DOCS;k++)
                    docteur[k] = new Thread(new Docteur(k));
                
                //spawn a bunch of customers based on what it was called.
		Thread customers[] = new Thread[NUM_CUSTOMERS];
		for(int k=0; k<NUM_CUSTOMERS; k++)
                    customers[k] = new Thread(new Customer(k));
		
		// Start all threads: nurse, doc, patients
		receptionist.start();
		for(int k=0;k<DOCS;k++)
                    nurses[k].start();
                for(int k=0;k<DOCS;k++)
                    docteur[k].start();
        for(int k=0; k<NUM_CUSTOMERS; k++)
        {
            try
            {
                    customers[k].start();
                    Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {}
        }
		
		//Join all the patient threads
		for(int k=0; k<NUM_CUSTOMERS; k++)
		{
                    try
                    {customers[k].join();} 
                    catch (InterruptedException e)
                    {}
		}
		
                //clean up all other remaining threads. Turn off run to aid in blocking threads continuing to run
                RUN=false;
                try
                {
                    //interrupt receptionist and join him/her
                    receptionist.interrupt();
                    receptionist.join();
                    //System.out.println("Receptionist joined");
                  
                    //interrupt each nurse and then join them
                    for(int k=0;k<DOCS;k++)
                    {
                        nurses[k].interrupt();
                        nurses[k].join();
                        //System.out.println("Nurse "+ k + " joined.");
                    }
                    //interrupt all the docs and then join them
                    for(int k=0;k<DOCS;k++)
                    {
                        docteur[k].interrupt();
                        docteur[k].join();
                        //System.out.println("Docteur "+ k + " joined.");
                    }
                  
                }
                catch(InterruptedException e)
                {}
	}	
        
	//patient
	public static class Customer implements Runnable
	{
		// ID of the patient
		int id;
		public Customer(int id)
		{this.id = id;}
		
		@Override
		public void run()
		{
			try
			{
                System.out.println("Patient " + id + " waiting for receptionist.");
                Thread.sleep(1000);
                                
                                //get the receptionist's attention
				receptionistReady.acquire();
                                //tell your ID now
                                PATIENT_ID = id;
                                //You're not busy talking with reception now
                                receptionistBusy.release();
                                
                                //But you do have to move into the waiting room now
                                receptionistDone.acquire();
                                System.out.println("Patient " + id + " enters waiting room.");
                                Thread.sleep(1000);
                                
                               
                                //get a nurse to talk to you. Maybe try and look really sick?
                                //Figure out ID by grabbing first available and break to them. Otherwise wait until we can grab someone
                                int nurseID=0;
                                nurseStart.acquire();
                                for(int k=0; k<DOCS;k++)
                                {
                                    if(nurseReady[k].tryAcquire())
                                    {
                                        nurseID=k;
                                        PATIENT_ID2[k]=id;
                                        //bug fix: because of printing speeds, sleep for half a second after getting a nurse to preserve order
                                        Thread.sleep(500);
                                        break;
                                    }
                                }  
                                
                                
                                //Be shown back
                                System.out.println("Nurse " + nurseID + " shows Patient " + id + " back to doctor " + nurseID +".");
                                Thread.sleep(1000);
                                
                                //Last bit of nurse.
                                nurseBusy[nurseID].release();
                                nurseDone[nurseID].acquire();

                                //docteur
                                //state the nature of your issues
				doctorReady[nurseID].acquire();
                                
                                //Give your ID and say you're talking.
                                PATIENT_ID3[nurseID] = id;

                                //Doc listening what the patient did
                                //I mighta had a might bit too much fun
                                System.out.println(stupid(id, nurseID));
                                Thread.sleep(1000);
                                
                                //You're not busy talking with doc now
                                doctorBusy[nurseID].release();
                                
                                //ok, time to leave
                                doctorDone[nurseID].acquire();              
                                System.out.println("Patient " + id + " leaves.");
                                Thread.sleep(1000);



			} 
                        catch (InterruptedException e)
                        {}
                        
			
		}
	}
	
        public static class Receptionist implements Runnable
        {
            @Override
            public void run()
            {
		try
		{
                    while(RUN)
                    {
                        //show you're ready
                        receptionistReady.release();
                        
                        //deal with the patient
                        receptionistBusy.acquire();
                        
                        System.out.println("Patient " + PATIENT_ID + " is registering with Receptionist.");
                        Thread.sleep(1000);
                        
                        //We're done with each other. But so help me don't time travel
                        receptionistDone.release();
                    }
		} 
                catch (InterruptedException e)
                {}
            }
        }

        //nurse threads
        public static class Nurse implements Runnable
        {
            // ID of the nurse
            int id;
            public Nurse(int id)
            {this.id = id;}
            
            @Override
            public void run()
            {
		try
		{
                    while(RUN)
                    {   
                        
                        //start nurse leaving them as they walk into office
                        nurseBusy[id].acquire();
                        
                        int peep = PATIENT_ID2[id];
                        System.out.println("Patient "+ peep +" entered doctor "+id+ "'s office.");
                        Thread.sleep(1000);
                        
                        //nurse is done messing with things
                        nurseDone[id].release();
                    }
		} 
                catch (InterruptedException e)
                {}
            }
        }
        
        //doctor threads
        public static class Docteur implements Runnable
        {
            //ID of the doc
            int id;
            public Docteur(int id)
            {this.id = id;}
            
            @Override
            public void run()
            {
                try
                {
                    while(RUN)
                    {
                        //free self, nurse, and say there's a nurse ready
                        doctorReady[id].release();
                        nurseReady[id].release();
                        nurseStart.release();
                        
                        //my turn to talk
                        doctorBusy[id].acquire();
                        
                        int peep = PATIENT_ID3[id];
                        
                        System.out.println("Doctor "+ id +" prescribes a treatment to Patient " + peep +".");
                        Thread.sleep(1000);
                        
                        //patient is done
                        doctorDone[id].release();
                        
                    }
                }
                catch (InterruptedException e)
                {}
            }
        }
        //I totally don't work with the public at all from what this suggests
        //Nope. Not at all. Enjoy a good laugh at some of these, at least.
        public static String stupid(int id, int doc)
        {
            String idiotsaywhat;
            switch(rnd.nextInt(11))
            {
                case 0: idiotsaywhat = ("Doctor " + doc + " listens to Patient "+ id + " explains he wants to lose weight.");
                    break;
                case 1: idiotsaywhat = ("Doctor " + doc + " listens to Patient "+ id+ " explains she has bad allergies.");
                    break;
                case 2: idiotsaywhat = ("Doctor " + doc + " listens to Patient "+ id+ " explains he might have the flu.");
                    break;
                case 3: idiotsaywhat = ("Doctor " + doc + " listens to Patient "+ id+ " explains he's vomiting what he ate for lunch after eating lunch.");
                    break;
                case 4: idiotsaywhat = ("Doctor " + doc + " listens to Patient "+ id+ " explain her knees hurt when she trips and falls into the ground.");
                    break;
                case 5: idiotsaywhat = ("Doctor " + doc + " listens to Patient "+ id+ " explain a hilarious problem involving something that does not go in a ceiling fan.");
                    break;
                case 6: idiotsaywhat = ("Doctor " + doc + " listens to Patient "+ id+ " explain he's constantly exhausted and has no energy after saying up until 4am.");
                    break;
                case 7: idiotsaywhat = ("Doctor " + doc + " listens to Patient "+ id+ " explain she wants a vaccine against mosquitoes.");
                    break;
                case 8: idiotsaywhat = ("Doctor " + doc + " listens to Patient "+ id+ " mindlessly yells at him. Poor Doctor "+ doc +".");
                    break;
                case 9: idiotsaywhat = ("Doctor " + doc + " listens to Patient "+ id+ " explains it feels like there's something in his eye every time he rubs it.");
                    break;
                case 10: idiotsaywhat = ("Doctor " + doc + " listens to Patient "+ id+ " explains she walked into a bar, and it wasn't funny at all, but she might have a concussion.");
                    break;
                default: idiotsaywhat =("Doctor " + doc + " listens to Patient "+ id+ "'s issue.");
                    break;
            }
            return idiotsaywhat;
        }
}