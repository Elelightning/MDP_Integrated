import java.io.*;  
import java.net.*; 
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

class OutputThread extends Thread
{
	private Socket soc;
	public OutputThread(Socket soc){
		this.soc=soc;
	}
	public void run() 
    { 
		System.out.println("output here");
		try{
			Scanner sc = new Scanner(System.in);
			PrintStream dout=new PrintStream(soc.getOutputStream());
	
			String string123;
			byte[] data123;
			while(true){
				System.out.println("Write something:");
				string123=sc.nextLine();
				if (string123.equals("-1"))
					break;
		
			data123=string123.getBytes();
			dout.write(data123);
			dout.flush();
	}
	dout.close();
	}
	catch(Exception e){
        e.printStackTrace();}
	}
	
}

class InputThread extends Thread
{ 
	private Socket soc;
    public InputThread(Socket soc) 
    { 
		this.soc=soc;
    }
	public void run() 
    { 
		System.out.println("input here");
		try
        { 
			InputStream din=soc.getInputStream();
            // Displaying the thread that is running 
            while(true){
				if(din.available()!=0){
					byte[] data321 = new byte[512];
					din.read(data321);
					String string321=new String(data321,StandardCharsets.UTF_8);
					System.out.println(string321);
				}
			}
		}
        catch (Exception e) 
        { 
            // Throwing an exception
            System.out.println ("Exception is caught"); 
        }
	}	
} 

public class MockPC {
    public static void main(String[] args) {  
	int i=1;
    try{
		Socket soc=new Socket("192.168.15.15",8080);
		Runnable outputStream = new OutputThread(soc);
		Runnable inputStream = new InputThread(soc);
		Thread ost = new Thread(outputStream);
		Thread ist = new Thread(inputStream);
		ost.start();
		ist.start();
		while(ist.isAlive()||ost.isAlive());
	soc.close();
	}
	catch(Exception e){
        e.printStackTrace();}
	}
}