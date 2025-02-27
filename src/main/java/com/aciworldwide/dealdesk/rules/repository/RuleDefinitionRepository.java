package com.aciworldwide.dealdesk.rules.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aciworldwide.dealdesk.rules.model.RuleDefinition;

@Repository
public interface RuleDefinitionRepository extends MongoRepository<RuleDefinition, String> {

    Optional<RuleDefinition> findByRuleKey(String ruleKey);

    @Query("{ 'enabled': true, 'category': ?0, $or: [{'validTo': null}, {'validTo': {$gt: ?1}}], 'validFrom': {$lte: ?1} }")
    List<RuleDefinition> findActiveRulesByCategory(String category, LocalDateTime currentTime);

    @Query("{ 'enabled': true, $or: [{'validTo': null}, {'validTo': {$gt: ?0}}], 'validFrom': {$lte: ?0} }")
    List<RuleDefinition> findActiveRules(LocalDateTime currentTime);

    List<RuleDefinition> findByCategory(String category);

    @Query("SELECT DISTINCT r.category FROM RuleDefinition r WHERE r.enabled = true")
    List<String> findAllActiveCategories();

    boolean existsByRuleKey(String ruleKey);

    @Query("SELECT COUNT(r) > 0 FROM RuleDefinition r WHERE r.ruleKey = :ruleKey " +
           "AND r.version > :version")
    boolean hasNewerVersion(@Param("ruleKey") String ruleKey, @Param("version") Long version);

    @Query("SELECT r FROM RuleDefinition r WHERE r.lastModifiedAt > :since " +
           "ORDER BY r.lastModifiedAt DESC")
    List<RuleDefinition> findRecentlyModified(@Param("since") LocalDateTime since);

    @Query("SELECT r FROM RuleDefinition r WHERE r.category = :category " +
           "AND r.enabled = true " +
           "AND (r.validTo IS NULL OR r.validTo > :currentTime) " +
           "AND r.validFrom <= :currentTime " +
           "AND r.priority >= :minPriority " +
           "ORDER BY r.priority DESC")
    List<RuleDefinition> findHighPriorityRules(
            @Param("category") String category,
            @Param("currentTime") LocalDateTime currentTime,
            @Param("minPriority") Integer minPriority);
}