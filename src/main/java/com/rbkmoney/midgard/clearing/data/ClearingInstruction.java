package com.rbkmoney.midgard.clearing.data;

import com.rbkmoney.midgard.clearing.data.enums.Bank;
import com.rbkmoney.midgard.clearing.data.enums.ServiceCommand;

import java.util.Date;

/** Класс, содержащий информацию по команде */
public class ClearingInstruction {

    /** Банк, для которого необходимо выполнить некоторую команду */
    private Bank bank;
    /** Команда, которую необходимо выполнить */
    private ServiceCommand command;
    /** Дата, с которой необходимо получить данные */
    private Date dateFrom;
    /** Дата, до которой необходимо получить данные */
    private Date dateTo;

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date dateTo) {
        this.dateTo = dateTo;
    }

    public ServiceCommand getCommand() {
        return command;
    }

    public void setCommand(ServiceCommand command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "ClearingInstruction{" +
                "bank=" + bank +
                ", dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", command='" + command + '\'' +
                '}';
    }
}
