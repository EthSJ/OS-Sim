
import java.io.*;
import java.util.*;

public class proj3
{
    static jobObject[] ARRAY = new jobObject[26];

    public static void main(String[] args) throws FileNotFoundException
    {
        //check arg length
	if(args.length < 2)
        {System.out.println("Missing an arg. :("); System.exit(-1);}

        try
        {
            Scanner input = new Scanner(new File(args[0]));
            while(input.hasNextLine())
            {
                //get the next line, split up up all at space and parse
                String s = input.nextLine().trim();
                String[] split = s.split("\\s");
                String job = split[0];
                int arrival = Integer.parseInt(split[1]);
                int duration = Integer.parseInt(split[2]);

                //create new jobObjects in the null spots.
                jobObject a = new jobObject(job, arrival, duration);
                for(int i=0; i<ARRAY.length;i++)
                {
                    if(ARRAY[i]==null)
                    {
                        ARRAY[i] = a;
                        break;
                    }
                }
            }
            //upper case it so I don't have to bother with case
            String switchy = args[1].toUpperCase();
            //go find what to do
            switch(switchy)
            {
                case "FCFS": FCFS();
                    break;
                case "RR": RR();
                    break;
                case "SPN": SPN();
                    break;
                case "SRT" : SRT();
                    break;
                case "HRRN": HRRN();
                    break;
                case "FB": FB();
                    break;
                case "ALL": ALL();
                    break;
                default: System.out.println("Dunno what I was told to do. :(");
                    break;

            }
        }
        catch (FileNotFoundException e)
	{
            System.err.println("I can't find that file :(. Please try again.");
            System.exit(1);
	}
    }

    public static class jobObject
    {
        //the job ID, its arrival time, its duration, and a formating push's worth of spaces
        public String job;
        public int arrival;
        public int duration;
        public String push="";

        public jobObject()
        {}

        public jobObject(String job, int arrival, int duration)
        {
            this.job = job;
            this.arrival = arrival;
            this.duration = duration;
            for(int i =0; i<arrival;i++)
                push+=" ";
        }

    }
    //first come, first serve
    public static void FCFS()
    {
        System.out.println("Here comes the First Come First Serve!");
        for(int i=0;i<ARRAY.length;i++)
            if(ARRAY[i] !=null)
                System.out.print(ARRAY[i].job+" ");
        System.out.println();

        //begin the output printing
        //for each one, so long it isn't null
        for(int i=0;i<ARRAY.length;i++)
            if(ARRAY[i] !=null)
            {
                //print the duration of them
                for(int k=0;k<ARRAY[i].duration;k++)
                {
                    System.out.print(ARRAY[i].push);
                    //then print X
                    System.out.println("X");
                }
            }

    }
    //round robin
    public static void RR()
    {
        jobObject[] arr = new jobObject[26];
        jobObject tt;
        for(int i=0;i<ARRAY.length;i++)
            if(ARRAY[i]!=null)
                arr[i] = (tt = new jobObject(ARRAY[i].job, ARRAY[i].arrival, ARRAY[i].duration));

        //print all items: A-Z, and what it is
        System.out.println("Here comes the Round Robin!");
        for(int i=0;i<arr.length;i++)
            if(arr[i] !=null)
                System.out.print(arr[i].job+" ");
        System.out.println();


        //begin the output printing
        //for each one, so long it isn't null
        int k = 1;  //breaks our loop
        int i =0;   //index of item being worked on
        while(k!=0)
        {
            k=0;                        //will break if it stays this
            if(arr[i]==null || i >26)   //loop back to start if I run into null
                i=0;
            for(int j=0;j<arr.length;j++)   //go through and find if there exists one item with a duration left
            {
                if(arr[j]==null)
                    break;
                else if(arr[j].duration > 0)    //then set this duration to k so we don't break
                    k=arr[j].duration;
            }
            // i's turn to run for 1 quantum
            if(arr[i].duration !=0)
            {
                System.out.print(arr[i].push+"X");  //push out to our spot and print an X
                System.out.println();               //println formating
                arr[i].duration--;                  //decrease our duration
            }
            i++;                                    //move to next item
        }

    }
    //shortest process next
    public static void SPN()
    {
        //set up this to work with our array.
        jobObject[] arr = new jobObject[26];
        jobObject tt;
        for(int i=0;i<ARRAY.length;i++)
            if(ARRAY[i]!=null)
                arr[i] = (tt = new jobObject(ARRAY[i].job, ARRAY[i].arrival, ARRAY[i].duration));

        //I'm totally not just sorting them to lowest via bubblesort. . .
        int n = arr.length;
        jobObject temp = new jobObject();
        for(int i=0; i < n; i++)
        {
            for(int j=1; j < (n-i); j++)
            {
                if(arr[j]!= null)
                    if(arr[j-1].duration > arr[j].duration)
                    {
                        //swap elements
                        temp = arr[j-1];
                        arr[j-1] = arr[j];
                        arr[j] = temp;
                    }

            }
        }

        //print all items: A-Z, and what it is
        System.out.println("Here comes the shortest process next!");
        for(int i=0;i<ARRAY.length;i++)
            if(ARRAY[i] !=null)
                System.out.print(ARRAY[i].job+" ");
        System.out.println();

        //FCFS's print but now on a sorted so it's SPN.
        for(int i=0;i<arr.length;i++)
            if(arr[i] !=null)
            {
                //print the duration of them
                for(int k=0;k<arr[i].duration;k++)
                {
                    System.out.print(arr[i].push);
                    //then print X
                    System.out.println("X");
                }
            }

    }
    //shortest time remaining
    public static void SRT()
    {

        //print all items: A-Z, and what it is
        System.out.println("Here comes the shortest remaining time!");
        for (jobObject ARRAY1 : ARRAY)
            if (ARRAY1 != null)
                System.out.print(ARRAY1.job + " ");
        System.out.println();

        //go find how big it is
        int size=0;
        for (jobObject ARRAY1 : ARRAY)
            if (ARRAY1 != null)
                size++;

        //make a copy of arry. Just to be safe
        jobObject[] arr = new jobObject[size];
        jobObject tt;
        for(int i=0;i<size;i++)
            arr[i] = (tt = new jobObject(ARRAY[i].job, ARRAY[i].arrival, ARRAY[i].duration));


        //here we see a wild LL, free of Prof. Smith's influence. Also it's now our queue
        LinkedList<jobObject> listy = new LinkedList<jobObject>();
        for(int i =0;i<size;i++)
            listy.addLast(arr[i]);

        //2 temp objects to work with
        jobObject temp = new jobObject();
        jobObject temp2 = new jobObject();

        //peek at our queue and make sure it's not empty
        boolean brker = true;
        while(brker)
        {
            //if we're 0'ed out and nothing left break
            if(listy.peek() == null && temp.duration <=0 && temp2.duration <=0)
                brker=false;

            //check if we're ready to replace temp
            if(temp.duration <= 0 &&listy.peek() !=null)
                temp = listy.removeFirst();
            //check if we're ready to replace temp2
            if(temp2.duration <=0 && listy.peek() != null)
                temp2=listy.removeFirst();

            //if temp's above 0 and smaller, print it once
            if((temp.duration < temp2.duration) && temp.duration > 0)
            {
                System.out.print(temp.push+"X\n");
                temp.duration--;
            }
            //else we're printing temp2 once
            else
            {
                if(temp2.duration > 0)
                {
                    System.out.print(temp2.push+"X\n");
                    temp2.duration--;
                }
            }
        }
    }
    //highest response ratio next
    public static void HRRN()
    {

        //print all items: A-Z, and what it is
        System.out.println("Here comes the highest response ratio next!");
        for (jobObject ARRAY1 : ARRAY)
            if (ARRAY1 != null)
                System.out.print(ARRAY1.job+" ");
        System.out.println();

        //go find how big it is
        int size=0;
        for (jobObject ARRAY1 : ARRAY)
            if (ARRAY1 != null)
                size++;

        //make a copy of arry. Just to be safe
        jobObject[] arr = new jobObject[size];
        jobObject tt;
        for(int i=0;i<size;i++)
            arr[i] = (tt = new jobObject(ARRAY[i].job, ARRAY[i].arrival, ARRAY[i].duration));


        //here we see a wild LL, free of Prof. Smith's influence. Also it's now our queue
        LinkedList<jobObject> listy = new LinkedList<jobObject>();
        for(int i =0;i<size;i++)
            listy.addLast(arr[i]);

        //find the one to print
        int highest=0, spot=0;
        while(listy.size() >0)
        {
            //highest back to 0
            highest=0;
            //go through and find the highest ratio and the spot it's at
            for(int i=0; i<listy.size();i++)
            {

                if( highest < ((listy.get(i).duration+ listy.get(i).arrival) / listy.get(i).duration))
                {
                    highest = ((listy.get(i).duration+ listy.get(i).arrival) / listy.get(i).duration);
                    spot = i;
                }
            }
            //print that job out
            for(int i=0; i<listy.get(spot).duration;i++)
                System.out.print(listy.get(spot).push+"X\n");

            //remove it from the list and get ready to start anew!
            listy.remove(spot);
        }




    }
    //Feedback 3 queues
    public static void FB()
    {
        //print all items: A-Z, and what it is
        System.out.println("Here comes the feedback!");
        for (jobObject ARRAY1 : ARRAY)
            if (ARRAY1 != null)
                System.out.print(ARRAY1.job + " ");
        System.out.println();

        //go find how big it is
        int size=0;
        for (jobObject ARRAY1 : ARRAY)
            if (ARRAY1 != null)
                size++;

        //make a copy of arry. Just to be safe
        jobObject[] arr = new jobObject[size];
        jobObject tt;
        for(int i=0;i<size;i++)
            arr[i] = (tt = new jobObject(ARRAY[i].job, ARRAY[i].arrival, ARRAY[i].duration));

        //the quantum
        int quantum =1;

        //here we see wild LLs, free of Prof. Smith's influence. Also it's now our queues
        LinkedList<jobObject> Q1 = new LinkedList<jobObject>();
        LinkedList<jobObject> Q2 = new LinkedList<jobObject>();
        LinkedList<jobObject> Q3 = new LinkedList<jobObject>();
        for(int i =0;i<size;i++)
            Q1.addLast(arr[i]);

        jobObject temp = new jobObject();
        while(Q1.size() > 0)
        {
            //extra breaking help
            if(Q1.peek() == null)
                break;

            //so long as it's no 0, run for a quantum and add to Q2
            temp = Q1.removeFirst();
            for(int i=0; i<quantum;i++)
            {
                if(temp.duration > 0)
                {
                    System.out.print(temp.push+"X\n");
                    temp.duration--;
                    Q2.addLast(temp);
                }
            }
        }
        //2nd queue working
        while(Q2.size() > 0)
        {
            //extra help breaking
            if(Q2.peek() == null)
                break;

            //take off the first
            temp = Q2.removeFirst();
            //so long as it's not 0, run it for 2 quantums. Then add it to Q3
            for(int i=0; i<(quantum*2);i++)
            {
                if(temp.duration > 0)
                {
                    System.out.print(temp.push+"X\n");
                    temp.duration--;
                    Q3.addLast(temp);
                }
            }
        }
        //Third queue working
        while(Q3.size() > 0)
        {
            //extra help breaking
            if(Q3.peek() == null)
                break;

            //take off the first
            temp = Q3.removeFirst();
            //so long as it's not 0, run it for 2 quantums. Then add it to Q3
            for(int i=0; i<(quantum*3);i++)
            {
                if(temp.duration > 0)
                {
                    //oh and if it's not done, add it back
                    System.out.print(temp.push+"X\n");
                    temp.duration--;
                    if(temp.duration > 0)
                        Q3.addLast(temp);
                }
            }
        }

    }
    //take a wild guess. Now take a reasonable one, that first 1 was way too wild
    public static void ALL()
    {
        //I mean, what did you really expect me to do?
        //It's just all in order followed by a println
        FCFS();
        System.out.println();
        RR();
        System.out.println();
        SPN();
        System.out.println();
        SRT();
        System.out.println();
        HRRN();
        System.out.println();
        FB();
        System.out.println();
    }
}
