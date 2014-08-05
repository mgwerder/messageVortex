package net.gwerder.java.mailvortex;

 
public abstract class MailVortex {

    private MailVortex() {
        super();
    }

    public static int main(String[] args) {
      if(args!=null && args.length>0 && "--help".equals(args[0])) {
        System.out.println("MailVortex V"+Version.getVersion());
      }    
      return 0;
    }
}
