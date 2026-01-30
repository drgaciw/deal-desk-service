package com.aciworldwide.dealdesk.mapper;

import com.aciworldwide.dealdesk.dto.DealRequestDTO;
import com.aciworldwide.dealdesk.dto.DealResponseDTO;
import com.aciworldwide.dealdesk.model.Deal;
import com.aciworldwide.dealdesk.model.DealStatus;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-30T09:04:38+0000",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class DealMapperImpl implements DealMapper {

    @Override
    public Deal toDeal(DealRequestDTO requestDTO) {
        if ( requestDTO == null ) {
            return null;
        }

        Deal.DealBuilder deal = Deal.builder();

        Set<String> set = requestDTO.getProducts();
        if ( set != null ) {
            deal.products( new ArrayList<String>( set ) );
        }
        if ( requestDTO.getAccountId() != null ) {
            deal.accountId( requestDTO.getAccountId() );
        }
        if ( requestDTO.getAccountName() != null ) {
            deal.accountName( requestDTO.getAccountName() );
        }
        if ( requestDTO.getSalesRepId() != null ) {
            deal.salesRepId( requestDTO.getSalesRepId() );
        }
        if ( requestDTO.getSalesRepName() != null ) {
            deal.salesRepName( requestDTO.getSalesRepName() );
        }
        if ( requestDTO.getNotes() != null ) {
            deal.notes( requestDTO.getNotes() );
        }
        if ( requestDTO.getName() != null ) {
            deal.name( requestDTO.getName() );
        }
        if ( requestDTO.getDescription() != null ) {
            deal.description( requestDTO.getDescription() );
        }
        if ( requestDTO.getSalesforceOpportunityId() != null ) {
            deal.salesforceOpportunityId( requestDTO.getSalesforceOpportunityId() );
        }

        deal.status( DealStatus.DRAFT );
        deal.createdAt( ZonedDateTime.now(ZoneId.systemDefault()) );
        deal.value( DealMapperHelper.convertCurrency(requestDTO.getValue(), requestDTO.getCurrency()) );
        deal.statusChangedAt( ZonedDateTime.now(ZoneId.systemDefault()) );
        deal.updatedAt( ZonedDateTime.now(ZoneId.systemDefault()) );

        return deal.build();
    }

    @Override
    public DealResponseDTO toResponseDTO(Deal deal) {
        if ( deal == null ) {
            return null;
        }

        DealResponseDTO dealResponseDTO = new DealResponseDTO();

        if ( deal.getId() != null ) {
            dealResponseDTO.setId( deal.getId() );
        }
        if ( deal.getName() != null ) {
            dealResponseDTO.setName( deal.getName() );
        }
        if ( deal.getDescription() != null ) {
            dealResponseDTO.setDescription( deal.getDescription() );
        }
        if ( deal.getSalesforceOpportunityId() != null ) {
            dealResponseDTO.setSalesforceOpportunityId( deal.getSalesforceOpportunityId() );
        }
        if ( deal.getStatus() != null ) {
            dealResponseDTO.setStatus( deal.getStatus() );
        }
        if ( deal.getValue() != null ) {
            dealResponseDTO.setValue( deal.getValue() );
        }
        List<String> list = deal.getProducts();
        if ( list != null ) {
            dealResponseDTO.setProducts( new LinkedHashSet<String>( list ) );
        }
        if ( deal.getAccountId() != null ) {
            dealResponseDTO.setAccountId( deal.getAccountId() );
        }
        if ( deal.getAccountName() != null ) {
            dealResponseDTO.setAccountName( deal.getAccountName() );
        }
        if ( deal.getSalesRepId() != null ) {
            dealResponseDTO.setSalesRepId( deal.getSalesRepId() );
        }
        if ( deal.getSalesRepName() != null ) {
            dealResponseDTO.setSalesRepName( deal.getSalesRepName() );
        }
        if ( deal.getCreatedAt() != null ) {
            dealResponseDTO.setCreatedAt( deal.getCreatedAt() );
        }
        if ( deal.getUpdatedAt() != null ) {
            dealResponseDTO.setUpdatedAt( deal.getUpdatedAt() );
        }
        if ( deal.getApprovedAt() != null ) {
            dealResponseDTO.setApprovedAt( deal.getApprovedAt() );
        }
        if ( deal.getApprovedBy() != null ) {
            dealResponseDTO.setApprovedBy( deal.getApprovedBy() );
        }
        if ( deal.getNotes() != null ) {
            dealResponseDTO.setNotes( deal.getNotes() );
        }

        dealResponseDTO.setDaysInCurrentStatus( DealMapperHelper.calculateDaysInStatus(deal) );
        dealResponseDTO.setNextAction( DealMapperHelper.determineNextAction(deal.getStatus()) );

        return dealResponseDTO;
    }

    @Override
    public List<DealResponseDTO> toResponseDTOList(List<Deal> deals) {
        if ( deals == null ) {
            return null;
        }

        List<DealResponseDTO> list = new ArrayList<DealResponseDTO>( deals.size() );
        for ( Deal deal : deals ) {
            list.add( toResponseDTO( deal ) );
        }

        return list;
    }
}
