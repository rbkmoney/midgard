package com.rbkmoney.midgard.adapter.mts.data;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Класс, содержащий информацию для создания заголовка клирингового файла для МТС банка */
public class XmlHeader {

    /** Версия формата XML файла */
    private static final String FORMAT_VERSION = "2";
    /** Идентификатор компании, которая создала файл */
    private String fileOriginator;
    /** Номер файла в пределах текущего дня */
    private int fileNumber;

    public XmlHeader(String fileOriginator, int fileNumber) {
        this.fileOriginator = fileOriginator;
        this.fileNumber = fileNumber;
    }

    public static String getFormatVersion() {
        return FORMAT_VERSION;
    }

    public String getFileOriginator() {
        return fileOriginator;
    }

    public String getFileId() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return "rbcm" + dateFormat.format(new Date()) + "_" + fileNumber;
    }

    public String getFileDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(new Date());
    }

}
