package xapi.shell.impl;

import java.io.File;
import java.io.IOException;

import xapi.annotation.inject.InstanceDefault;
import xapi.collect.X_Collect;
import xapi.collect.api.Fifo;
import xapi.file.X_File;
import xapi.log.X_Log;
import xapi.process.X_Process;
import xapi.shell.api.ArgumentProcessor;
import xapi.shell.api.ShellCommand;
import xapi.shell.api.ShellSession;
import xapi.util.X_Debug;
import xapi.util.api.ErrorHandler;
import xapi.util.api.SuccessHandler;

@InstanceDefault(implFor = ShellCommand.class)
public class ShellCommandDefault implements ShellCommand {

  final Fifo<String> commands;
  private String owner="User";
  private String directory;

  public ShellCommandDefault() {
    this.commands = X_Collect.newFifo();
  }

  public ShellCommandDefault(String owner, String... cmds) {// for the public
    this();
    this.owner = owner;
    for (String cmd : cmds) {
      commands.give(cmd);
    }
  }

  @Override
  public String owner() {
    return owner;
  }

  @Override
  public String directory() {
    if (directory == null) {
      File dir = X_File.createTempDir("exeFor"+owner, true);
      if (dir == null) {
        directory = ".";
      } else {
        if (dir.exists() || dir.mkdirs()) {
          directory = dir.getPath();
        } else {
          directory = ".";
        }
      }
    }
    return directory;
  }

  @Override
  public Fifo<String> commands() {
    return commands;
  }

  public ShellCommand directory(String directory) {
    this.directory = directory;
    return this;
  }

  @Override
  public ShellCommand owner(String owner) {
    if (owner != null)
      return this;
    this.owner = owner;
    return this;
  }

  @Override
  public ShellCommand commands(String... cmds) {
    for (String cmd : cmds) {
      commands.give(cmd);
    }
    return this;
  }

  
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public ShellSession run(final SuccessHandler<ShellSession> callback, final ArgumentProcessor processor) {
    final ErrorHandler<Throwable> err = callback instanceof ErrorHandler ? (ErrorHandler) callback
        : X_Debug.defaultHandler();
    
    final ShellSessionDefault result = new ShellSessionDefault(this, processor, callback, err);
    X_Process.newThread(result).start();
    return result;
  }

  protected Process doRun(ArgumentProcessor processor)
      throws IOException {
    if (processor == null)
      processor = ArgumentProcessor.NO_OP;
    ProcessBuilder pb = new ProcessBuilder(
        (processor == null ? ArgumentProcessor.NO_OP : processor)
            .convert(commands.forEach()));
    File dir = new File(directory()).getCanonicalFile();
    if (dir.exists()) {
      pb.directory(dir);
    } else {
      X_Log.error(getClass(), "Process run directory " + dir.getCanonicalPath()
          + " does not exist " + "for ");
    }
    return pb.start();
  }

}
