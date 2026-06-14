package com.takeout.module.statistics.vo;

import lombok.Data;

@Data
public class RiderStatsVO {

    private Long riderId;
    private String riderName;
    private String status;
    private Long totalDeliveries;
    private Long todayDeliveries;
}
