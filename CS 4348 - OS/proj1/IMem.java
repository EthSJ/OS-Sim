
import java.io.*;
import java.util.*;

public class IMem
{
    //The memory!
    static int[] memory;

    public static void main(String[] args)
    {
	// If no input file, exit
	if(args.length < 1)
	    {
		System.err.println("I don't know what I'm getting input from; exiting. . .");
		System.exit(1);
	    }

	//inputfile is our arg
	String inputFile = args[0];
	//try to read from file
	try
	    {
		memStart(inputFile);
	    }
	catch (FileNotFoundException e)
	    {
		System.err.println("I can't find that file :(. Please try again.");
		System.exit(1);
	    }

	//while reading from input
	Scanner input = new Scanner(System.in);
	while(input.hasNextLine())
	    {
		String l = input.nextLine();
		char cmd = l.charAt(0);
		int addr, data;
		switch(cmd)
		{
		    //Read command
		    case 'r':
		    //goes until the first space
		    addr = Integer.parseInt(l.substring(1));
		    System.out.println(read(addr));

		    break;

		    //write command
		    case 'w':
			//split up the string, write the first address
			//And then the data
			String[] params = l.substring(1).split(",");
			addr = Integer.parseInt(params[0]);
			data = Integer.parseInt(params[1]);
			write(addr, data);

			break;
		    //end the whole thing i.e. quit and finish
		    case 'e':
			System.exit(0);
		}

	    }
	input.close();
    }

    //go read/return the data at an address
    protected static int read(int addr)
    { return memory[addr]; }

    //Go to the address and write the data into it's place
    protected static void write(int addr, int data)
    { memory[addr] = data;  }

    protected static void memStart(String in) throws FileNotFoundException
    {
	//memory is int array of 2000
	memory = new int[2000];
	//reader for the instruction
	Scanner scanIn = new Scanner(new File(in));
	//the current spot in memory
	int mark = 0;
	//While there's stuff coming from the instructions
	while(scanIn.hasNextLine())
	    {
		//go get the next line and trim it so it's easier to work with
		String l = scanIn.nextLine().trim();
		//if the line is empty, move on
		if(l.length() < 1)
		    continue;
		// For lines like .1000, move to that position in mem
		//then move on
		if(l.charAt(0) == '.')
		    {
			mark = Integer.parseInt(l.substring(1).split("\\s+")[0]);
			continue;
		    }
		// First part of line isn't number, move on, nothing to do
		if(l.charAt(0) < '0' || l.charAt(0) > '9')
		    continue;
		// Split at spaces and save that
		String[] s = l.split("\\s+");
		//empty line, moving right along. . .
		if(s.length < 1)
		    continue;
		//finally doing something besides jumping which is reading
		//the int into mem
		else
		    memory[mark++] = Integer.parseInt(s[0]);
	    }
	//all done, close up
	scanIn.close();
    }
}
