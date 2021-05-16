package net.messagevortex.commandline;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AlgorithmParameter;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.asn1.SymmetricKey;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.AlgorithmType;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.asn1.encryption.Mode;
import net.messagevortex.asn1.encryption.Padding;
import net.messagevortex.asn1.encryption.Parameter;
import picocli.CommandLine;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/**
 * <p>Commandline handler for symmetric encryption.</p>
 */
@CommandLine.Command(
        description = "symmetrically encrypt a file",
        name = "encrypt",
        aliases = {"enc"},
        mixinStandardHelpOptions = true
)
public class CommandLineHandlerCipherEncrypt implements Callable<Integer> {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  @CommandLine.Option(names = {"--infile", "-i"}, required = true,
          description = "filename of the file to handle")
  String inFile;

  @CommandLine.Option(names = {"--outfile", "-o"}, required = true,
          description = "output filename")
  String outFile;

  @CommandLine.Option(names = {"--keyfile", "-k"},
          description = "keyfile")
  String keyfile;

  @CommandLine.Option(names = {"--ciphertype", "-c"},
          description = "the cipher type")
  String cipherType;

  @CommandLine.Option(names = {"--ciphermode", "-m"},
          description = "the cipher mode")
  Mode cipherMode;

  @CommandLine.Option(names = {"--cipherpadding", "-p"},
          description = "the padding")
  Padding cipherPadding;

  @CommandLine.Option(names = {"--keysize", "-s"},
          description = "the key size")
  int keySize;

  @CommandLine.Option(names = {"--outkey"},
          description = "output filename for the generated or loaded key")
  String outKey;

  /**
   * <p>Commandline handler or encryption.</p>
   *
   * <p>Use java -jar MessageVortex.jar crypt encrypt --help to get all supported parameters.</p>
   *
   * @return the return code
   * @throws Exception if anything goes wrong
   */
  @Override
  public Integer call() throws Exception {
    byte[] inBuffer = Files.readAllBytes(Paths.get(inFile));
    byte[] key = null;
    if (keyfile != null) {
      key = Files.readAllBytes(Paths.get(inFile));
    }

    Algorithm ct = Algorithm.getByString(cipherType);
    if (ct == null) {
      throw new IOException("Unknown cipher type");
    }

    if (ct.getAlgorithmType() == AlgorithmType.ASYMMETRIC) {
      AsymmetricKey k;
      if (key != null) {
        k = new AsymmetricKey(key);
      } else {
        AlgorithmParameter algParam = new AlgorithmParameter();
        algParam.put(Parameter.KEYSIZE, "" + keySize);
        algParam.put(Parameter.ALGORITHM, ct.toString());
        algParam.put(Parameter.MODE, cipherMode.toString());
        algParam.put(Parameter.PADDING, cipherPadding.toString());
        k = new AsymmetricKey(algParam);
      }

      try(OutputStream fos = Files.newOutputStream(Paths.get(outFile))) {
        fos.write(k.encrypt(inBuffer));
      }

      if (outKey != null) {
        try(OutputStream fosk = Files.newOutputStream(Paths.get(outKey))) {
          fosk.write(k.toBytes(DumpType.ALL_UNENCRYPTED));
        }
      }

    } else if (ct.getAlgorithmType() == AlgorithmType.SYMMETRIC) {
      SymmetricKey k;

      if (key != null) {
        k = new SymmetricKey(key);
      } else {
        k = new SymmetricKey(ct, cipherPadding, cipherMode);
      }

      System.out.println("writing encrypted file " + outFile);
      try(OutputStream fos = Files.newOutputStream(Paths.get(outFile))) {
        fos.write(k.encrypt(inBuffer));
      }

      if (outKey != null) {
        System.out.println("writing key file " + outKey);
        try(OutputStream fosk = Files.newOutputStream(Paths.get(outKey))) {
          fosk.write(k.toBytes(DumpType.ALL_UNENCRYPTED));
        }
      }

    } else {
      throw new IOException("Unhandleable cipher type");
    }
    return 0;
  }
}
