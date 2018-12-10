package com.rbkmoney.midgard.clearing.commands;

import com.rbkmoney.midgard.clearing.data.ClearingInstruction;
import com.rbkmoney.midgard.clearing.data.enums.ServiceCommand;

public interface Command {

    void execute(ClearingInstruction instruction);

    boolean isInstance(ServiceCommand command);

}
