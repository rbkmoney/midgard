package com.rbkmoney.midgard.clearing.services;

import com.rbkmoney.midgard.clearing.commands.Command;
import com.rbkmoney.midgard.clearing.data.ClearingInstruction;
import com.rbkmoney.midgard.clearing.data.enums.Bank;
import com.rbkmoney.midgard.clearing.data.enums.ServiceCommand;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для работы с клиринговыми эвентами
 *
 * Принцип работы.
 * От внешней системы поступает сообытие ClearingInstruction с командой, банком и временным интервалом.
 * Сервис обрабатывает запрос и запускает определенные комманды на выоплнение. По завершении работы
 * источнику должно быть отправлено сообщение о статусе задачи
 *
 * Примечание: понятие "должен отправить запрос" спорное, так как запускать задачу в идеальном случае будет
 *             CRON сервис, а ему в принципе все равно как завершилась задача - главное отправить. В общем случае
 *             команды получение статуса последнего клиринга/операции для банка должно быть достаточно
 */
@Service
public class ClearingService implements GenericService {

    private final List<Command> commands;

    public ClearingService(List<Command> commands) {
        this.commands = commands;
    }

    @Override
    public void process() {
        //TODO: <some code>
        //TODO: надо продумать что и в каком формате пришло.

        ClearingInstruction instruction = new ClearingInstruction();
        instruction.setBank(Bank.MTS);
        instruction.setCommand(ServiceCommand.START_CLEARING);

        for (Command command : commands) {
            if (command.isInstance(instruction.getCommand())) {
                command.execute(instruction);
            }
        }

    }

}
