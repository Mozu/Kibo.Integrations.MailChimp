package com.mozu.mailchimp.controllers;

import java.util.Comparator;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.mozu.mailchimp.model.BatchModel;

public class JobHistoryComparator implements Comparator<BatchModel> {
    private static final String FORMAT_PATTERN = "dd-MMM-yyyy' 'HH:mm:ss";

    @Override
    public int compare(BatchModel o1, BatchModel o2) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(FORMAT_PATTERN);
        DateTime o1_date = formatter.parseDateTime(o1.getCreatedDate());
        DateTime o2_date = formatter.parseDateTime(o2.getCreatedDate());
        // return in descending order
        return o2_date.compareTo(o1_date);
    }

}
