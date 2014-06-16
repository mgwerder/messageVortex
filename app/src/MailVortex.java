package net.gwerder.java.mailvortex;

 
public class MailVortex {

	public static int main(String args[]) {
	  if(args!=null && args.length>0 && args[0].equals("--help")) {
	    System.out.println("MailVortex V"+Version.VERSION);
	  }	
	  return 0;
	}
}
