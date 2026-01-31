package com.aciworldwide.dealdesk.mapper;

import com.aciworldwide.dealdesk.dto.DealRequestDTO;
import com.aciworldwide.dealdesk.dto.DealResponseDTO;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import org.mapstruct.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = {ZonedDateTime.class, ZoneId.class},
        uses = {DealMapperHelper.class, MapStructDateTimeConverter.class})
public interface DealMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "createdAt", expression = "java(ZonedDateTime.now(ZoneId.systemDefault()))")
    @Mapping(target = "value", source = ".")
    @Mapping(target = "products", source = "products")
    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "accountName", source = "accountName")
    @Mapping(target = "salesRepId", source = "salesRepId")
    @Mapping(target = "salesRepName", source = "salesRepName")
    @Mapping(target = "notes", source = "notes")
    @Mapping(target = "components", ignore = true)
    @Mapping(target = "pricingModel", ignore = true)
    @Mapping(target = "adjustments", ignore = true)
    @Mapping(target = "contingentRevenue", ignore = true)
    @Mapping(target = "tcvCalculation", ignore = true)
    @Mapping(target = "statusChangedAt", expression = "java(ZonedDateTime.now(ZoneId.systemDefault()))")
    @Mapping(target = "updatedAt", expression = "java(ZonedDateTime.now(ZoneId.systemDefault()))")
    Deal toDeal(DealRequestDTO requestDTO);

    @Mapping(target = "daysInCurrentStatus", source = ".")
    @Mapping(target = "nextAction", source = "status")
    @Mapping(target = "synced", ignore = true)
    @Mapping(target = "lastSyncAt", ignore = true)
    @Mapping(target = "syncError", ignore = true)
    @Mapping(target = "syncedWithSalesforce", ignore = true)
    @Mapping(target = "lastSyncedAt", ignore = true)
    @Mapping(target = "lastSyncStatus", ignore = true)
    @Mapping(target = "lastSyncError", ignore = true)
    @Mapping(target = "requiresApproval", ignore = true)
    @Mapping(target = "availableActions", ignore = true)
    DealResponseDTO toResponseDTO(Deal deal);

    List<DealResponseDTO> toResponseDTOList(List<Deal> deals);

    default DealStatus mapSalesforceStatus(String salesforceStatus) {
        if (salesforceStatus == null) {
            return DealStatus.DRAFT;
        }
        switch (salesforceStatus) {
            case "NEW":
                return DealStatus.DRAFT;
            case "IN_PROGRESS":
                return DealStatus.SUBMITTED;
            case "CLOSED_WON":
                return DealStatus.APPROVED;
            default:
                return DealStatus.DRAFT;
        }
    }
}
