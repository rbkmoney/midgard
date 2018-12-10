package com.rbkmoney.midgard.clearing.commands;

import com.rbkmoney.midgard.clearing.data.ClearingInstruction;
import com.rbkmoney.midgard.clearing.data.enums.ServiceCommand;
import com.rbkmoney.midgard.clearing.helpers.ClearingInfoHelper;
import org.jooq.generated.midgard.tables.pojos.ClearingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Команда для получения статуса клиринга для банка */
public class GetClearingStatusCommand implements Command {

    /** Логгер */
    private static final Logger log = LoggerFactory.getLogger(GetClearingStatusCommand.class);
    /** Тип команды */
    private static final ServiceCommand COMMAND = ServiceCommand.GET_CLEARING_STATUS;
    /** Класс для взаимодействия с информацией о клиринновом эвенте */
    private final ClearingInfoHelper clearingInfoHelper;

    public GetClearingStatusCommand(ClearingInfoHelper clearingInfoHelper) {
        this.clearingInfoHelper = clearingInfoHelper;
    }

    @Override
    public void execute(ClearingInstruction instruction) {
        log.debug("Получение статуса последнего клиринга для банка {}", instruction.getBank());
        ClearingEvent lastClearingEvent = clearingInfoHelper.getLastClearingEvent(instruction.getBank());
        sendStatus(lastClearingEvent);
        log.debug("Cтатус последнего клиринга для банка {}: {}", instruction.getBank(), lastClearingEvent);
    }

    @Override
    public boolean isInstance(ServiceCommand serviceCommand) {
        return COMMAND.equals(serviceCommand);
    }

    void sendStatus(ClearingEvent lastClearingEvent) {
        //TODO: отправить статус запросившему
    }

}
