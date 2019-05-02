
import java.io.*;
import java.util.*;

public class proj1
{
    public static void main(String[] args)
    {
	//check to make sure we have all the arguments
	if(args.length < 2)
	    {
		System.err.println("Missing arg: file or timeout");
		System.exit(1);
	    }
	//arg0 = read file. arg1 = timeout int
	String read = args[0];
	int timeout = Integer.parseInt(args[1]);

	//Don't judge me. I ran out of name ideas.
	Runtime runtime = Runtime.getRuntime();
	try
	    {
		//start up input and IMem
		Process IMem = runtime.exec("java IMem " + read);
		final InputStream error = IMem.getErrorStream();
		//Bug fix only, not program.
		//Buffer would get stuck, need to make it bigger.
		//Actual program below this
		new Thread(new Runnable()
		  {
			public void run()
			{
			    //buffer just guessed 2^12 seems to work
			    byte[] buffer = new byte[4096];
			    int i = -1;
			        try
				    {
				       //make sure the buffer's ok
				       while((i = error.read(buffer)) > 0)
				       {}
				    }
				catch (IOException e)
				{}
		       	}
		}).start();
		//Set up read in, print out, the CPU, and run!
		Scanner memIn = new Scanner(IMem.getInputStream());
		PrintWriter memOut = new PrintWriter(IMem.getOutputStream());
		//Actual program. No thread here, just in the fix
		CPU cpu = new CPU(memIn, memOut, timeout);
		cpu.run();
	    }
	//I don't except this to run unless I screw up
	catch (IOException e)
	    {
		System.err.println("Process creation err;");
		System.exit(1);
	    }
    }

    protected static class CPU
    {
	//registers
	protected int PC, SP, IR, AC, X, Y, timer, timeout;
	//kernel mode
	protected boolean kernelMode;
	//instruction area
	protected Scanner memIn;
	protected PrintWriter memOut;

	public CPU(Scanner memIn, PrintWriter memOut, int timeout)
	{
	    //set up all bases and turn on user mode
	    this.memIn = memIn;
	    this.memOut = memOut;
	    this.timeout = timeout;
	    kernelMode = false;
	    //set up all registers (and a timer)
	    PC = IR = AC = X = Y = timer = 0;
	    SP = 1000;
	}

	//Kernel mode enabled!
        protected void kernelMode()
        {
            //turn it on, save stack, move stack, push all registers
            kernelMode = true;
            int tempSP = SP;
            SP = 2000;
            push(tempSP);
            push(PC);
            push(IR);
            push(AC);
            push(X);
            push(Y);
        }

        //fetch a command
        protected void fetch()
        { IR = readMem(PC++); }

        //push on stack. Really simple.
        protected void push(int data)
        { writeMem(--SP, data); }

        //pop from stack. I mean, what did you expect?
        protected int pop()
        { return readMem(SP++); }

	//Hands over e to end prog
	protected void endMem()
        {
            memOut.println("e");
	    //flush input or it gets stuck at end
	    memOut.flush();
        }

        protected void writeMem(int addr, int data)
        {
            //write out the data to mem and clean out input
            memOut.printf("w%d,%d\n", addr, data);
            memOut.flush();
        }

	protected int readMem(int addr)
	{
	    int returnVal;
	    //if being bad and trying to get in sys mem
	    if(addr >= 1000 && !kernelMode)
		{
		    //Fun with errors
		    System.err.println("No! You're in User mode. Req: Kernel");
		    System.exit(-1);
		}
	    //print out address, clean it out, and move up and return num
	    //r for read
	    memOut.println("r"+addr);
	    memOut.flush();
	    returnVal = Integer.parseInt(memIn.nextLine());

	    return returnVal;
	}

	//runs the whole thing in a while
	public void run()
	{
	    boolean on = true;
	    while(on)
		{
		    //get, instruction, do it, and move along the timer
		    fetch();
		    //this will be what changes the while off
		    on = execute();
		    timer++;
		    //increased timer way up when .1000 seen
		    if(timer >= timeout)
			{
			    if(!kernelMode)
				{
				    //if not kernel mode, timer to 0 go into
				    //kernel mode, and move to kernel area
				    timer = 0;
				    kernelMode();
				    PC = 1000;
				}

			}
		}
	}

	//takes care of instructions. returns boolean while running or fin
	public boolean execute()
	{
	    switch(IR)
		{
		case 1:
		    // Load value into AC
		    fetch();
		    AC = IR;

		    break;
		case 2:
		    // Load addr value into AC
		    fetch();
		    AC = readMem(IR);

		    break;
		case 3:
		    // LoadInd addr at given address into AC
		    fetch();
		    AC = readMem(readMem(IR));

		    break;
		case 4:
		    // LoadInxX addr (given address + X) into AC
		    fetch();
		    AC = readMem(IR + X);

		    break;
		case 5:
		    // LoadInxY addr (given address + Y) into AC
		    fetch();
		    AC = readMem(IR + Y);

		    break;
		case 6:
		    // LoadSpX  from (SP+X) into AC
		    AC = readMem(SP+X);

		    break;
		case 7:
		    // Store addr ( AC ) to address
		    fetch();
		    writeMem(IR, AC);

		    break;
		case 8:
		    //Get random int 1-100 into AC
		    AC = (int) (Math.random()*100+1);

		    break;
		case 9:
		    //If IR=1, write AC to screen as int
		    //if IR=2, write AC to screen as char
		    fetch();
		    if(IR == 1)
			{ System.out.print(AC); }
		    else if(IR == 2)
			{ System.out.print((char)AC); }

		    break;
		case 10:
		    // AddX (X) to AC
		    AC += X;

		    break;
		case 11:
		    // AddY (Y) to AC
		    AC += Y;

		    break;
		case 12:
		    // SubX (X) to AC
		    AC -= X;

		    break;
		case 13:
		    // SubY (Y) to AC
		    AC -= Y;

		    break;
		case 14:
		    // CopyToX in AC to X
		    X = AC;

		    break;
		case 15:
		    // CopyFromX in X to AC
		    AC = X;

		    break;
		case 16:
		    // CopyToY in AC to Y
		    Y = AC;

		    break;
		case 17:
		    // CopyFromY in Y to AC
		    AC = Y;

		    break;
		case 18:
		    // CopyToSp in AC to SP
		    SP = AC;

		    break;
		case 19:
		    // CopyFromSp in SP to AC
		    AC = SP;

		    break;
		case 20:
		    // JUMP! To the address
		    fetch();
		    PC = IR;

		    break;
		case 21:
		    //JUMP! Only if AC is zero
		    fetch();
		    if(AC == 0)
			{ PC = IR; }


		    break;
		case 22:
		    //JUMP! If AC is not zero
		    fetch();
		    if(AC != 0)
			{ PC = IR; }

		    break;
		case 23:
		    //Push return addr to stack, JUMP!
		    fetch();
		    push(PC);
		    PC = IR;

		    break;
		case 24:
		    //Pop return addr, JUMP! back
		    PC = pop();

		    break;
		case 25:
		    //Increase X
		    X++;

		    break;
		case 26:
		    //Decrease X
		    X--;

		    break;
		case 27:
		    //Push AC onto stack
		    push(AC);


		    break;
		case 28:
		    //Pop from stack onto AC
		    AC = pop();


		    break;
		case 29:
		    //Set sys mode, switch stack, push and set PC/SP
		    //No interrupts while interrupt processing
		    if(!kernelMode)
			{
			    kernelMode();
			    PC = 1500;
			}

		    break;
		case 30:
		    //Restore registers and restore user mode
		    Y = pop();
		    X = pop();
		    AC = pop();
		    IR = pop();
		    PC = pop();
		    SP = pop();
		    kernelMode = false;

		    break;
		case 50:
		    //All done! Close it all up
		    endMem();
		    return false;

		default:
		    //So. . . You gave me something not here.
		    System.err.println("So. . . You gave me a bad instruction; try another instruction");
		    endMem();
		    return false;
		}
	    return true;
	}

    }
}
