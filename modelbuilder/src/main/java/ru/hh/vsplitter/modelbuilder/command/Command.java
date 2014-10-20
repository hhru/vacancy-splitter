package ru.hh.vsplitter.modelbuilder.command;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public abstract class Command {

  protected final CmdLineParser parser;

  public Command(String[] args) throws CmdLineException {
    parser = new CmdLineParser(this);
    parser.parseArgument(args);
  }

  public CmdLineParser getParser() {
    return parser;
  }

  public abstract void run() throws Exception;

}
