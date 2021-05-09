package net.messagevortex.commandline;

import picocli.CommandLine;

/**
 * <p>Commandline handler for using the encryot operations on files.</p>
 *
 * <p>This handler was used in the course of detecting remanences of messages. It serves as rump
 * for all subcommands and is empty.</p>
 */
@CommandLine.Command(
        description = "Helper for symmetrical encryption operations",
        name = "cipher",
        aliases = { "cip","cypher","cyp" },
        mixinStandardHelpOptions = true,
        subcommands = {
                CommandLineHandlerCipherList.class,
                CommandLineHandlerCipherEncrypt.class
        }
)
public class CommandLineHandlerCipher {}
