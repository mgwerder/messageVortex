package net.messagevortex.commandline;

import static net.messagevortex.commandline.CommandLineHandlerCipherList.CipherType.ASYM;
import static net.messagevortex.commandline.CommandLineHandlerCipherList.CipherType.MODE;
import static net.messagevortex.commandline.CommandLineHandlerCipherList.CipherType.PAD;
import static net.messagevortex.commandline.CommandLineHandlerCipherList.CipherType.SYM;

import java.io.IOException;
import java.util.concurrent.Callable;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.AlgorithmType;
import net.messagevortex.asn1.encryption.Mode;
import net.messagevortex.asn1.encryption.Padding;
import picocli.CommandLine;

@CommandLine.Command(
        description = "list available ciphers",
        name = "list",
        aliases = { "lst" },
        mixinStandardHelpOptions = true
)
public class CommandLineHandlerCipherList implements Callable<Integer> {

  enum CipherType {
    ASYM,
    SYM,
    MODE,
    PAD;
  }

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--type", "-t"}, required = false,
          description = "type of information (ASYM, SYM, MODE, PAD)")
  CipherType[] types = {ASYM, SYM, MODE, PAD};

  @Override
  public Integer call() throws Exception {
    for(CipherType c:types) {
      switch (c) {
        case ASYM:
          System.out.println("Asymmetric cpiher types:");
          for (Algorithm a : Algorithm.getAlgorithms(AlgorithmType.ASYMMETRIC)) {
            System.out.print("  " + a.toString());
            System.out.print(" (modes: ");
            int i=0;
            for (Mode m: Mode.getModes(a)) {
              if( i>0) {
                System.out.print(", ");
              }
              i++;
              System.out.print(m.toString());
            }
            System.out.print("; paddings: ");
            i=0;
            for (Padding p: Padding.getAlgorithms(a.getAlgorithmType())) {
              if( i>0) {
                System.out.print(", ");
              }
              i++;
              System.out.print(p.toString());
            }
            System.out.println(")");
          }
          break;
        case SYM:
          System.out.println("Symmetric cpiher types:");
          for (Algorithm a : Algorithm.getAlgorithms(AlgorithmType.SYMMETRIC)) {
            System.out.print("  " + a.toString());
            System.out.print(" (modes: ");
            int i=0;
            for (Mode m: Mode.getModes(a)) {
              if( i>0) {
                System.out.print(", ");
              }
              i++;
              System.out.print(m.toString());
            }
            System.out.print("; paddings: ");
            i=0;
            for (Padding p: Padding.getAlgorithms(a.getAlgorithmType())) {
              if( i>0) {
                System.out.print(", ");
              }
              i++;
              System.out.print(p.toString());
            }
            System.out.println(")");
          }
          break;
        case MODE:
          System.out.println("Cipher mode:");
          for (Mode a : Mode.values()) {
            System.out.println("  " + a.name());
          }
          break;
        case PAD:
          System.out.println("Padding types:");
          for (Padding a : Padding.values()) {
            System.out.println("  " + a.name());
          }
          break;
        default:
          System.err.println("ERROR: Unknown type specified");
          throw new IOException("Unknown type specified");
      }
      System.out.println("");
      System.out.flush();
    }
    return 0;
  }
}
