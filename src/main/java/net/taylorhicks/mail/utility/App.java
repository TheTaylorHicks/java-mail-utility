package net.taylorhicks.mail.utility;

import java.io.Console;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class App {

  private static final Logger LOG = LoggerFactory.getLogger(App.class);

  private static Options buildOptions(String... args) {
// create the Options
    Options options = new Options();

    // Version
    options.addOption(
        Option.builder()
            .longOpt("version")
            .desc("Java properties")
            .build()
    );

    // Java properties
    options.addOption(
        Option.builder("D")
            .numberOfArgs(2)
            .argName("property=value")
            .valueSeparator()
            .desc("Java properties")
            .build()
    );

    // Headers
    options.addOption(
        Option.builder("H")
            .longOpt("header")
            .numberOfArgs(2)
            .argName("header=value")
            .valueSeparator()
            .desc("header to include with email")
            .build()
    );

    // From
    options.addOption(
        Option.builder("f")
            .longOpt("from")
            .hasArg()
            .argName("address")
            .desc("the From address")
            .type(String.class)
            .build()
    );

    // To
    options.addOption(
        Option.builder("t")
            .longOpt("to")
            .hasArg()
            .argName("address")
            .desc("the To address")
            .type(String.class)
            .build()
    );

    // CC
    options.addOption(
        Option.builder()
            .longOpt("cc")
            .hasArg()
            .argName("address")
            .desc("the CC address")
            .type(String.class)
            .build()
    );

    // BCC
    options.addOption(
        Option.builder()
            .longOpt("bcc")
            .hasArg()
            .argName("address")
            .desc("the BCC address")
            .type(String.class)
            .build()
    );

    // Sender
    options.addOption(
        Option.builder("s")
            .longOpt("sender")
            .hasArg()
            .argName("address")
            .desc("the Sender address")
            .type(String.class)
            .build()
    );

    // Reply To
    options.addOption(
        Option.builder("r")
            .longOpt("reply-to")
            .hasArg()
            .argName("address")
            .desc("the Reply To address")
            .type(String.class)
            .build()
    );

    // Subject
    options.addOption(
        Option.builder("S")
            .longOpt("subject")
            .hasArg()
            .argName("line")
            .desc("the email subject line")
            .type(String.class)
            .build()
    );

    // Body
    options.addOption(
        Option.builder("B")
            .longOpt("body")
            .hasArg()
            .argName("text")
            .desc("the email body")
            .type(String.class)
            .build()
    );

    // Character Set
    options.addOption(
        Option.builder("C")
            .longOpt("charset")
            .hasArg()
            .argName("charset")
            .desc("the character set used to encode the email subject and body")
            .type(String.class)
            .build()
    );

    // Authentication
    options.addOption(
        Option.builder("a")
            .longOpt("authenticate")
            .desc("Prompt for username and password")
            .type(Boolean.class)
            .build()
    );

    // Help
    options.addOption(
        Option.builder("h")
            .longOpt("help")
            .desc("print this help message")
            .build()
    );

    return options;
  }

  private static void send(Address sender, Address[] replyTo, Address[] to, Address[] cc, Address[] bcc, Address[] from, Map<Object, Object> headers, String subject, String body, String charset, boolean authenticate) throws MessagingException {
    final Session session;

    session = Session.getDefaultInstance(System.getProperties());

    MimeMessage msg = new MimeMessage(session);

    // Set sending address
    msg.setSender(sender);

    // Add reply-to addresses
    msg.setReplyTo(replyTo);

    // Add to addresses
    msg.setRecipients(Message.RecipientType.TO, to);

    // Add CC addresses
    msg.setRecipients(Message.RecipientType.CC, cc);

    // Add BCC addresses
    msg.setRecipients(Message.RecipientType.BCC, bcc);

    // Add from addresses
    msg.addFrom(from);

    // Add headers
    for (Map.Entry header : headers.entrySet()) {
      msg.setHeader(header.getKey().toString(), header.getValue().toString());
    }

    // Set sent date
    msg.setSentDate(new Date());

    // Set subject
    msg.setSubject(subject, charset);

    // Set subject
    msg.setText(body, charset);

    if (authenticate) {
      Console console = System.console();
      Transport.send(
          msg,
          console.readLine("Username: "),
          new String(console.readPassword("Password: "))
      );
    } else {
      Transport.send(msg);
    }
  }

  private static Address[] parseAddresses(String addresses) {
    try {
      return InternetAddress.parse(addresses != null ? addresses : "");
    } catch (AddressException e) {
      throw new RuntimeException("Failed to parse addresses from string: \"" + addresses + "\"", e);
    }
  }

  public static void main(String... args) {
    // create the command line parser
    CommandLineParser parser = new DefaultParser();

    Options options = App.buildOptions(args);

    try {
      // parse the command line arguments
      CommandLine line = parser.parse(options, args);

      if (args.length == 0 || line.hasOption("help")) {
        new HelpFormatter().printHelp("java-mail-utility", options);
        System.exit(0);
      }

      if (line.hasOption("version")) {
        System.out.println(App.class.getPackage().getImplementationTitle() +
            " - " + App.class.getPackage().getImplementationVersion());
        System.exit(0);
      }

      System.getProperties().putAll(line.getOptionProperties("D"));

      App.send(
          App.parseAddresses(line.getOptionValue("sender"))[0],
          App.parseAddresses(line.getOptionValue("reply-to")),
          App.parseAddresses(line.getOptionValue("to")),
          App.parseAddresses(line.getOptionValue("cc")),
          App.parseAddresses(line.getOptionValue("bcc")),
          App.parseAddresses(line.getOptionValue("from")),
          line.getOptionProperties("header"),
          line.getOptionValue("subject"),
          line.getOptionValue("body"),
          line.hasOption("charset") ? line.getOptionValue("charset") : Charset.defaultCharset().name(),
          line.hasOption("authenticate")
      );

    } catch (ParseException e) {
      System.err.println("java-mail-utility: " + e.getMessage());
      new HelpFormatter().printHelp("java-mail-utility", options);
      System.exit(1);
    } catch (Exception e) {
      LOG.error("java-mail-utility: " + e.getMessage(), e);
      System.exit(1);
    }
  }
}
