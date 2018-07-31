package com.github.ompc.greys.core.exception;

/**
 * 命令不存在
 *
 * @author
 */
public class CommandNotFoundException extends CommandException {

    public CommandNotFoundException(String command) {
        super(command);
    }

}
