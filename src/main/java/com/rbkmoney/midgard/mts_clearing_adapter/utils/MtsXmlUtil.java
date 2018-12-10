package com.rbkmoney.midgard.mts_clearing_adapter.utils;

import com.rbkmoney.midgard.clearing.data.ClearingData;
import com.rbkmoney.midgard.mts_clearing_adapter.data.XmlHeader;
import com.rbkmoney.midgard.mts_clearing_adapter.data.CardData;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.generic.DateTool;
import org.jooq.generated.midgard.tables.pojos.ClearingTransaction;
import org.jooq.generated.midgard.tables.pojos.Merchant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public final class MtsXmlUtil {

    private static final String VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    /**
     * Создание итогового клирингового XML
     *
     * @param clearingData клиринговые данные
     * @return строку со сформированным документом
     * TODO: это предварительная версия. В конечном счете файл будет формироваться по частям
     */
    public static String createXml(ClearingData clearingData) {
        String fileOriginator = "fileOriginator";
        int fileNumber = 1;
        XmlHeader header = new XmlHeader(fileOriginator, fileNumber);
        VelocityContext headerContext = new VelocityContext();
        headerContext.put("header", header);
        String clearingXmlHeader = VelocityUtil.create("vm/header.vm", headerContext);


        DateTool dateTool = new DateTool();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String procDate = dateFormat.format(new Date());
        String msgNr = UUID.randomUUID().toString();
        List<ClearingTransaction> transactions = clearingData.getTransactions();
        List<Merchant> merchants = clearingData.getMerchants();

        List<VelocityContext> transactionsContext = new ArrayList<>();
        for (ClearingTransaction transaction : transactions) {
            VelocityContext context = new VelocityContext();
            CardData cardData = new CardData();
            context.put("dateTool", dateTool);
            context.put("procDate", procDate);
            context.put("msgNr", msgNr);
            context.put("cardData", cardData);
            context.put("transaction", transaction);
            Merchant merchant = merchants.stream()
                    .filter(mrch -> mrch.getMerchantId().equals(transaction.getMerchantId()))
                    .findFirst().orElse(new Merchant());
            context.put("merchant", merchant);
            transactionsContext.add(context);
        }
        List<String> trxXmlBlocks = VelocityUtil.create("vm/transaction.vm", transactionsContext);

        return MtsXmlUtil.createXML(clearingXmlHeader, trxXmlBlocks);


    }

    //TODO: зашлушка. В рамках финального адаптера будет сделано иначе
    public static String createXML(String header, List<String> transactions) {
        StringBuilder builder = new StringBuilder();
        builder.append(VERSION);
        builder.append("<File>\n");
        builder.append(header).append("\n");

        builder.append("<Transactions>\n");
        builder.append(transactions.stream().collect(Collectors.joining("\n")));
        builder.append("</Transactions>\n");
        builder.append("</File>\n");
        return builder.toString();
    }

    private MtsXmlUtil() {}

}
